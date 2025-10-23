package hu.isolapaul; // JAVÍTVA (pontra)


import java.util.List;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.io.EOFException;

/**
 * SZERVER oldali szál, amely egyetlen klienst kezel.
 */
public class ClientHandlerThread extends Thread {

    private Socket socket;
    private ChatServer server;
    private AuthManager authManager;
    private MessageStorage messageStorage;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean running = true;

    private User currentUser;   // A bejelentkezett User objektum
    private String username;    // A bejelentkezett felhasználó neve

    public ClientHandlerThread(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.authManager = server.getAuthManager();
        this.currentUser = null; // Alapból nincs bejelentkezve
        this.username = null;
        this.messageStorage = server.getMessageStorage();
    }

    /**
     * Visszaadja a bejelentkezett felhasználó nevét.
     */
    public String getUsername() {
        return this.username;
    }

    @Override
    public void run() {
        try {
            // Streamek inicializálása
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Végtelen ciklus: Várakozás a kliens üzeneteire
            while (running) {
                Object inputObject = ois.readObject();

                // Bejövő objektumok kezelése
                if (inputObject instanceof RegisterRequest) {
                    handleRegisterRequest((RegisterRequest) inputObject);
                } else if (inputObject instanceof LoginRequest) {
                    handleLoginRequest((LoginRequest) inputObject);
                } else if (inputObject instanceof ChatMessage) {
                    handleChatMessage((ChatMessage) inputObject);
                } else if (inputObject instanceof FriendRequest) {
                    handleFriendRequest((FriendRequest) inputObject);
                } else if (inputObject instanceof HistoryRequest) {
                    handleHistoryRequest((HistoryRequest) inputObject);
                } else if (inputObject instanceof DisconnectRequest) {
                    handleDisconnect();
                }
            }
        } catch (EOFException | SocketException e) {
            if (running) { // Csak akkor írja ki, ha váratlan volt
                System.out.println("[Szerver] Kliens váratlanul lecsatlakozott: " + (username != null ? username : socket.getInetAddress()));
            }
            running = false;
        } catch (Exception e) {
            // Egyéb hiba
            System.err.println("[Szerver] Hiba a kliens kezelése közben: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Bármi is történt, a klienst eltávolítjuk és lezárjuk a kapcsolatot
            server.removeClient(this);
            try {
                if (socket != null) socket.close();
            } catch (Exception e) {
                // Hiba bezáráskor, nem nagy ügy
            }
        }
    }

    /**
     * Feldolgozza a regisztrációs kérést.
     */
    private void handleRegisterRequest(RegisterRequest req) {
        try {
            // Most már létezni fognak ezek a metódusok
            String result = authManager.registerUser(
                req.getUsername(), 
                req.getEmail(), 
                req.getPassword1(), 
                req.getPassword2()
            );

            if (result.equals("SIKER")) {
                oos.writeObject(ServerResponse.success("REGISTER_SUCCESS"));
            } else {
                oos.writeObject(ServerResponse.error(result)); // A hibaüzenet maga a string
            }
            oos.flush();
        } catch (Exception e) {
            System.err.println("[Szerver] Hiba a regisztráció során: " + e.getMessage());
        }
    }
    /**
     * Feldolgozza a barát hozzáadás kérést.
     */
    private void handleFriendRequest(FriendRequest req) {
        // Csak bejelentkezett felhasználó adhat hozzá barátot
        if (this.currentUser == null) return;
        
        try {
            String result = authManager.addFriend(
                this.currentUser.getUsername(), 
                req.getTargetUsername()
            );

            if (result.equals("SIKER")) {
                // Frissítjük a memóriában lévő User objektumot is
                this.currentUser = authManager.getUser(this.currentUser.getUsername());
                // Visszaküldjük a frissített User objektumot a kliensnek
                oos.writeObject(ServerResponse.success("FRIEND_ADDED", this.currentUser));
            } else {
                oos.writeObject(ServerResponse.error(result)); // A hibaüzenet maga a string
            }
            oos.flush();
        } catch (Exception e) {
            System.err.println("[Szerver] Hiba a barátfelvétel során: " + e.getMessage());
        }
    }

    /**
     * Feldolgozza a bejelentkezési kérést.
     */
    private void handleLoginRequest(LoginRequest req) {
        try {
            User user = authManager.loginUser(req.getUsername(), req.getPassword());

            if (user != null) {
                // Siker!
                this.currentUser = user;
                this.username = user.getUsername(); 
                
                // Visszaküldjük a sikeres választ a User objektummal
                oos.writeObject(ServerResponse.success("LOGIN_SUCCESS", user));
                oos.flush();
                
                server.addClient(this); // Hozzáadjuk a BEJELENTKEZETT kliensek listájához
                System.out.println("[Szerver] Felhasználó bejelentkezett: " + user.getUsername());
            } else {
                // Hiba!
                oos.writeObject(ServerResponse.error("Hibás felhasználónév vagy jelszó."));
                oos.flush();
            }
        } catch (Exception e) {
            System.err.println("[Szerver] Hiba a bejelentkezés során: " + e.getMessage());
        }
    }

    /**
     * Feldolgozza a bejövő chat üzenetet és továbbítja.
     */
    private void handleChatMessage(ChatMessage msg) {
        // Ellenőrizzük, hogy a kliens be van-e jelentkezve
        if (this.currentUser == null) {
            System.out.println("[Szerver] Nem bejelentkezett kliens próbált üzenetet küldeni.");
            return;
        }
        
        System.out.println("[Szerver] Üzenet: " + msg.getSender() + " -> " + msg.getReceiver());
        
        
        
        // Továbbítjuk az üzenetet a címzettnek (ha online)
        server.sendMessageToUser(msg);
        messageStorage.saveMessage(msg);
    }

    /**
     * Üzenet küldése ennek a kliensnek (ezt a ChatServer hívja).
     */
    public void sendMessage(ChatMessage msg) {
        try {
            oos.writeObject(msg);
            oos.flush();
        } catch (Exception e) {
            System.err.println("[Szerver] Hiba az üzenet küldésekor (" + username + "): " + e.getMessage());
        }
    }

    private void handleDisconnect() {
        System.out.println("[Szerver] Kliens lecsatlakozik: " + username);
        this.running = false; // Leállítja a while ciklust
        
    }

private void handleHistoryRequest(HistoryRequest req) {
        if (currentUser == null) return; // Csak bejelentkezett felhasználó
        
        try {
            // Létrehozzuk a ChatID-t a kért partner és a bejelentkezett user alapján
            ChatMessage temp = new ChatMessage(currentUser.getUsername(), req.getChatPartner(), "");
            String chatId = temp.getChatId();
            
            // Betöltjük az előzményeket
            List<ChatMessage> history = messageStorage.loadHistory(chatId);
            
            // Visszaküldjük a listát a kliensnek
            oos.writeObject(history);
            oos.flush();
            
        } catch (Exception e) {
            System.err.println("[Szerver] Hiba az előzmény küldésekor: " + e.getMessage());
        }
    }

    public void forceDisconnect() {
        try {
            running = false; // Leállítja a 'while' ciklust
            // Értesítjük a klienst, hogy a szerver áll le
            oos.writeObject(ServerResponse.error("A szerver leáll. A kapcsolat bontva."));
            oos.flush();
            socket.close(); // Bezárja a kapcsolatot
        } catch (Exception e) {
            // Itt már mindegy, ha hiba van, a szál leáll
        }
    }
}

