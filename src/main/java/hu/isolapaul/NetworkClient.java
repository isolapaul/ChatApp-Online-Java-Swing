package hu.isolapaul; // Csomagnév PONTtal

import javax.swing.SwingUtilities;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * KLIENS oldali osztály.
 * Kezeli a hálózati kapcsolatot és kommunikációt a szerverrel.
 * Runnable, hogy külön szálon futhasson (ne fagyassza le a GUI-t).
 */
public class NetworkClient implements Runnable {

    private ChatApp gui; // Referencia a GUI-ra, hogy frissíthessük

    
    private String serverHost;
    private int serverPort;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    
    private volatile boolean running = true;

    /**
     * MÓDOSÍTOTT KONSTRUKTOR
     * @param gui A fő GUI ablak
     * @param serverHost A szerver címe (pl. "0.tcp.ngrok.io")
     * @param serverPort A szerver portja (pl. 19876)
     */
    public NetworkClient(ChatApp gui, String serverHost, int serverPort) {
        this.gui = gui;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            // 1. Csatlakozás a szerverhez
            socket = new Socket(serverHost, serverPort);
            System.out.println("[Kliens] Sikeresen csatlakozva a szerverhez: " + serverHost + ":" + serverPort);

            // 2. Streamek inicializálása
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // 3. Végtelen ciklus: Várakozás a szerver üzeneteire
            while (running) {
                Object inputObject = ois.readObject();

                if (inputObject instanceof ServerResponse) {
                    ServerResponse response = (ServerResponse) inputObject;
                    
                    if (response.getType().equals("REGISTER_SUCCESS")) {
                        SwingUtilities.invokeLater(() -> {
                            gui.onRegisterSuccess();
                        });
                    } else if (response.getType().equals("LOGIN_SUCCESS")) {
                        User user = (User) response.getPayload();
                        SwingUtilities.invokeLater(() -> {
                            gui.onLoginSuccess(user);
                        });
                    } else if (response.getType().equals("FRIEND_ADDED")) {
                        User updatedUser = (User) response.getPayload();
                        SwingUtilities.invokeLater(() -> {
                            gui.onFriendAdded(updatedUser);
                        });
                    } else { // Általános hiba
                        SwingUtilities.invokeLater(() -> {
                            gui.showLoginError(response.getMessage());
                        });
                    }
                
                } else if (inputObject instanceof List) {
                    // Előzmények fogadása
                    @SuppressWarnings("unchecked")
                    List<ChatMessage> history = (List<ChatMessage>) inputObject;
                    SwingUtilities.invokeLater(() -> {
                        gui.onHistoryReceived(history);
                    });

                } else if (inputObject instanceof ChatMessage) {
                    // Élő üzenet fogadása
                    ChatMessage msg = (ChatMessage) inputObject;
                    SwingUtilities.invokeLater(() -> {
                        gui.onMessageReceived(msg);
                    });
                }
            }

        } catch (Exception e) {
            running = false;
            e.printStackTrace();
            if (gui != null) {
                SwingUtilities.invokeLater(() -> {
                    gui.showLoginError("Kapcsolati hiba: " + e.getMessage());
                });
            }
        }
    }

    // Metódusok, amiket a GUI hív

    public void sendLoginRequest(String user, String pass) {
        try {
            oos.writeObject(new LoginRequest(user, pass));
            oos.flush();
        } catch (Exception e) {
            System.err.println("[Kliens] Hiba a login kérés küldésekor: " + e.getMessage());
        }
    }

    public void sendRegisterRequest(String user, String email, String pass1, String pass2) {
        try {
            oos.writeObject(new RegisterRequest(user, email, pass1, pass2));
            oos.flush();
        } catch (Exception e) {
            System.err.println("[Kliens] Hiba a regisztrációs kérés küldésekor: " + e.getMessage());
        }
    }

    public void sendMessage(ChatMessage msg) {
        try {
            oos.writeObject(msg);
            oos.flush();
        } catch (Exception e) {
            System.err.println("[Kliens] Hiba az üzenet küldésekor: " + e.getMessage());
        }
    }

    public void sendFriendRequest(String targetUsername) {
        try {
            oos.writeObject(new FriendRequest(targetUsername));
            oos.flush();
        } catch (Exception e) {
            System.err.println("[Kliens] Hiba a barátkérés küldésekor: " + e.getMessage());
        }
    }

    public void sendHistoryRequest(String chatPartner) {
        try {
            oos.writeObject(new HistoryRequest(chatPartner));
            oos.flush();
        } catch (Exception e) {
            System.err.println("[Kliens] Hiba az előzmény kérés küldésekor: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        running = false;
        try {
            if (oos != null) {
                oos.writeObject(new DisconnectRequest());
                oos.flush();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            System.err.println("[Kliens] Hiba lecsatlakozáskor: " + e.getMessage());
        }
    }
}

