package hu.isolapaul;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.SocketException;

/**
 * A SZERVER alkalmazás belépési pontja.
 * Ennek kell futnia a te gépeden ("szerver gép").
 * GUI nélküli alkalmazás.
 */
public class ChatServer {

    private static final int PORT = 12345;
    private AuthManager authManager;
    private MessageStorage messageStorage;

    // Gyűjtemény az aktív, BEJELENTKEZETT kliensek kezelőiről
    // A kulcs a felhasználónév, az érték a hozzá tartozó Thread
    private Set<ClientHandlerThread> activeClients;

    private volatile boolean running = true; // Módosíthatóvá tesszük
    private ServerSocket serverSocket; // Osztályszintű mező

    public ChatServer() {
        this.authManager = new AuthManager();
        // ConcurrentHashMap-ből készítünk Set-et, mert szálbiztos
        this.activeClients = ConcurrentHashMap.newKeySet();
        this.messageStorage = new MessageStorage();
    }

    public void start() {
        System.out.println("Chat Szerver indul a " + PORT + " porton...");
        System.out.println("Várakozás a kliensek csatlakozására...");
        System.out.println("A szerver leállításához írd be: 'exit' és nyomj Enter-t.");

        try {
            serverSocket = new ServerSocket(PORT);
            // 2. Végtelen ciklus: folyamatosan vár új csatlakozókra
            while (running) {
                // 3. serverSocket.accept() hívás (ez blokkol, amíg valaki nem csatlakozik)
                Socket clientSocket = serverSocket.accept();
                
                // 4. Ha van csatlakozás, átadjuk egy új ClientHandlerThread-nek
                //    Ez a szál még NINCS bejelentkezve, csak csatlakozva van.
                ClientHandlerThread clientHandler = new ClientHandlerThread(clientSocket, this);
                clientHandler.start(); // Elindítjuk a szálat
            }

        } catch (Exception e) {
            System.err.println("Szerver hiba: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hozzáad egy klienst a BEJELENTKEZETT kliensek listájához.
     * Ezt a ClientHandlerThread hívja meg sikeres login után.
     */
    public synchronized void addClient(ClientHandlerThread client) {
        activeClients.add(client);
        System.out.println("[Szerver] Aktív kliensek száma: " + activeClients.size());
    }

    public void shutdown() {
        System.out.println("[Szerver] Leállítás... A kliensek értesítése...");
        running = false; // Leállítja a 'while(running)' ciklust

        try {
            // 1. Mindenkit lecsatakoztatunk
            // Fontos: a 'forEach' ConcurrentModificationException-t dobhat
            for (ClientHandlerThread client : activeClients) {
                client.forceDisconnect(); // Ezt a metódust mindjárt létrehozzuk
            }
            activeClients.clear();

            // 2. Mentjük az adatokat
            authManager.saveUsersToFile();
            
            // 3. Bezárjuk a fő socket-et (ez feloldja az accept() blokkolását)
            if (serverSocket != null) {
                serverSocket.close();
            }
            System.out.println("[Szerver] Leállás befejezve.");
        } catch (Exception e) {
            System.err.println("Hiba a leállítás során: " + e.getMessage());
        }
    }


    /**
     * Eltávolít egy klienst az aktív listáról (pl. lecsatlakozott).
     */
    public synchronized void removeClient(ClientHandlerThread client) {
        activeClients.remove(client);
        System.out.println("[Szerver] Aktív kliensek száma: " + activeClients.size());
    }
    
    /**
     * Továbbít egy üzenetet a megfelelő, bejelentkezett felhasználónak.
     */
    public synchronized void sendMessageToUser(ChatMessage msg) {
        String receiverUsername = msg.getReceiver();
        
        // Végigmegyünk az összes aktív (bejelentkezett) kliensen
        for (ClientHandlerThread client : activeClients) {
            // Ha a kliens be van jelentkezve és a neve megegyezik a címzettel
            if (receiverUsername.equals(client.getUsername())) {
                client.sendMessage(msg); // Átküldjük neki az üzenetet
                return; // Megvan, kész
            }
        }
        
        System.out.println("[Szerver] Hiba: A címzett nincs bejelentkezve: " + receiverUsername);
        // TODO: Opcionálisan elmenthetnénk "offline" üzenetként
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();

        // 1. Indítunk egy szálat a konzol figyelésére
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    if (reader.readLine().equalsIgnoreCase("exit")) {
                        server.shutdown();
                        break; // A figyelő szál leáll
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 2. A fő szálon elindítjuk a szervert
        server.start(); 
    }



    public MessageStorage getMessageStorage() {
        return messageStorage;
    }
}
