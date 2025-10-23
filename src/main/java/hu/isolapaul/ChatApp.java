package hu.isolapaul; // Csomagnév PONTtal

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * A KLIENS alkalmazás belépési pontja.
 * Ez a Swing GUI, amit a barátaid futtatnak (.jar).
 */
public class ChatApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JPanel loginPanel;
    private JPanel registerPanel;
    private JPanel chatPanel;

    private NetworkClient networkClient; 

    private User currentUser;
    
    private JTextArea chatArea;
    private JComboBox<String> friendSelector;

    /**
     * @param serverHost A szerver címe (pl. "0.tcp.ngrok.io")
     * @param serverPort A szerver portja (pl. 19876)
     */
    public ChatApp(String serverHost, int serverPort) {
        super("SzuperChat");

        
        this.networkClient = new NetworkClient(this, serverHost, serverPort);
        new Thread(networkClient).start(); // Elindítjuk a háttérszálat

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Panelek létrehozása
        createLoginPanel();
        createRegisterPanel();
        createChatPanel();

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(registerPanel, "REGISTER");
        mainPanel.add(chatPanel, "CHAT");

        cardLayout.show(mainPanel, "LOGIN"); // Alapértelmezetten a login panelt mutatjuk

        add(mainPanel);

        // ===== ABLAK =====
        setSize(500, 450); 
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Nem léphet ki simán
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                
                if (networkClient != null) {
                    networkClient.disconnect();
                }
                System.exit(0);
            }
        });
        setVisible(true);
    }

    /**
     * Létrehozza a Login panelt (bejelentkezési képernyő)
     */
    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Felhasználónév:"), gbc);
        gbc.gridx = 1;
        JTextField userField = new JTextField(15);
        loginPanel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("Jelszó:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(15);
        loginPanel.add(passField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton loginButton = new JButton("Bejelentkezés");
        loginPanel.add(loginButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        JButton showRegisterButton = new JButton("Regisztráció");
        loginPanel.add(showRegisterButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton exitButton = new JButton("Kilépés");
        loginPanel.add(exitButton, gbc);

        // ===== ESEMÉNYKEZELŐK =====
        
        // Listener a gombnak és az Enternek
        ActionListener loginAction = e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            if (user.isEmpty() || pass.isEmpty()) {
                showLoginError("A felhasználónév és jelszó nem lehet üres!");
                return;
            }
            // Kérés küldése a hálózati kliensnek
            networkClient.sendLoginRequest(user, pass);
        };
        
        loginButton.addActionListener(loginAction);
        passField.addActionListener(loginAction); // Enter-re bejelentkezés
        
        showRegisterButton.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));
        
        exitButton.addActionListener(e -> {
            System.exit(0); 
        });
    }

    /**
     * Létrehozza a Regisztrációs panelt
     */
    private void createRegisterPanel() {
        registerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        registerPanel.add(new JLabel("Felhasználónév:"), gbc);
        gbc.gridx = 1;
        JTextField userField = new JTextField(15);
        registerPanel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        registerPanel.add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(15);
        registerPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        registerPanel.add(new JLabel("Jelszó:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField1 = new JPasswordField(15);
        registerPanel.add(passField1, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        registerPanel.add(new JLabel("Jelszó újra:"), gbc);
        gbc.gridx = 1;
        JPasswordField passField2 = new JPasswordField(15);
        registerPanel.add(passField2, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JButton registerButton = new JButton("Regisztráció végrehajtása");
        registerPanel.add(registerButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        JButton showLoginButton = new JButton("Vissza a bejelentkezéshez");
        registerPanel.add(showLoginButton, gbc);
        
        // ===== ESEMÉNYKEZELŐK =====
        registerButton.addActionListener(e -> {
            String user = userField.getText();
            String email = emailField.getText();
            String pass1 = new String(passField1.getPassword());
            String pass2 = new String(passField2.getPassword());
            // Kérés küldése a hálózati kliensnek
            networkClient.sendRegisterRequest(user, email, pass1, pass2);
        });

        showLoginButton.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
    }

    /**
     * Létrehozza a fő Chat panelt
     */
    private void createChatPanel() {
        chatPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea("Válassz egy barátot a csevegéshez...\n");
        chatArea.setEditable(false);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        friendSelector = new JComboBox<>(new String[]{"Barátok..."});
        chatPanel.add(friendSelector, BorderLayout.NORTH);

        // Üzenetküldő panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Küldés");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Barátfelvételi panel 
        JPanel friendPanel = new JPanel(new BorderLayout());
        JTextField friendField = new JTextField("Felhasználónév");
        JButton addFriendButton = new JButton("Barát felvétele");
        friendPanel.add(friendField, BorderLayout.CENTER);
        friendPanel.add(addFriendButton, BorderLayout.EAST);

        // Kilépés panel 
        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Kijelentkezés és Kilépés");
        exitPanel.add(logoutButton);
        
        // Alsó panel (három panellel) 
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1));
        bottomPanel.add(inputPanel);
        bottomPanel.add(friendPanel);
        bottomPanel.add(exitPanel);
        
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ESEMÉNYKEZELŐK 

        // Küldés gomb
        ActionListener sendAction = e -> {
            String text = messageField.getText().trim();
            if (text.isEmpty()) return;
            
            String receiver = (String) friendSelector.getSelectedItem();
            if (receiver == null || receiver.equals("Barátok...") || receiver.startsWith("Nincsenek")) {
                JOptionPane.showMessageDialog(this, "Válassz egy címzettet!");
                return;
            }
            
            ChatMessage msg = new ChatMessage(currentUser.getUsername(), receiver, text);
            networkClient.sendMessage(msg);
            
            chatArea.append("[Én]: " + text + "\n");
            messageField.setText("");
        };
        sendButton.addActionListener(sendAction);
        messageField.addActionListener(sendAction); // Enter-re küldés
        
        // Barát felvétele gomb
        addFriendButton.addActionListener(e -> {
            String targetUser = friendField.getText().trim();
            if (targetUser.isEmpty() || targetUser.equals("Felhasználónév")) {
                JOptionPane.showMessageDialog(this, "Írd be a felhasználónevet!");
                return;
            }
            networkClient.sendFriendRequest(targetUser);
        });
        
        // Kilépés gomb
        logoutButton.addActionListener(e -> {
            if (networkClient != null) {
                networkClient.disconnect();
            }
            System.exit(0);
        });
        
        // Barátválasztó (lenyíló lista)
        friendSelector.addActionListener(e -> {
            String selected = (String) friendSelector.getSelectedItem();
            // Csak akkor töltünk, ha valódi nevet választottunk
            if (selected != null && !selected.startsWith("Barátok...") && !selected.startsWith("Nincsenek")) {
                chatArea.setText("Előzmények betöltése... (" + selected + ")\n");
                networkClient.sendHistoryRequest(selected);
            } else {
                chatArea.setText("Válassz egy barátot a csevegéshez.\n");
            }
        });
    }

    // Metódusok, amiket a NetworkClient hívogat 

    public void onLoginSuccess(User user) {
        this.currentUser = user;
        setTitle("SzuperChat - Bejelentkezve: " + user.getUsername());
        updateFriendList(user);
        cardLayout.show(mainPanel, "CHAT"); // Átváltás a chat panelre
    }
    
    public void showLoginError(String message) {
        JOptionPane.showMessageDialog(this, message, "Hiba", JOptionPane.ERROR_MESSAGE);
    }
    
    public void onRegisterSuccess() {
        JOptionPane.showMessageDialog(this, "Sikeres regisztráció! Most már bejelentkezhetsz.", "Siker", JOptionPane.INFORMATION_MESSAGE);
        cardLayout.show(mainPanel, "LOGIN"); // Visszaváltás a login panelre
    }

    public void onHistoryReceived(List<ChatMessage> history) {
        chatArea.setText(""); // Töröljük az "Előzmények betöltése..." szöveget
        if (history.isEmpty()) {
            chatArea.append("Nincs még beszgetés.\n");
        } else {
            for (ChatMessage msg : history) {
                chatArea.append(msg.toString() + "\n");
            }
        }
    }

    public void onMessageReceived(ChatMessage msg) {
        String currentChatPartner = (String) friendSelector.getSelectedItem();
        
        // Csak akkor írjuk ki, ha a megfelelő chat van nyitva
        if (msg.getSender().equals(currentChatPartner)) {
             chatArea.append(msg.toString() + "\n");
        } else {
            // Ha nem, értesítjük
            JOptionPane.showMessageDialog(this, "Új üzeneted érkezett: " + msg.getSender(), "Új üzenet", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void onFriendAdded(User updatedUser) {
        this.currentUser = updatedUser; // Frissítjük a felhasználónkat
        updateFriendList(updatedUser); // Frissítjük a lenyíló listát
        JOptionPane.showMessageDialog(this, "Sikeresen hozzáadtad a barátot!", "Siker", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Segédmetódus a barátlista frissítésére
     */
    private void updateFriendList(User user) {
        friendSelector.removeAllItems();
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            friendSelector.addItem("Nincsenek barátaid. Adj hozzá egyet!");
        } else {
            friendSelector.addItem("Barátok...");
            for (String friend : user.getFriends()) {
                friendSelector.addItem(friend);
            }
        }
    }

    
    public static void main(String[] args) {
        // 1. Bekérjük a HOSZT-ot
        String serverHost = JOptionPane.showInputDialog(
            null, 
            "Add meg a szerver HOSZTNEVÉT (pl. 0.tcp.ngrok.io):", 
            "Szerver Csatlakozás (1/2)", 
            JOptionPane.PLAIN_MESSAGE);

        if (serverHost == null || serverHost.trim().isEmpty()) {
            System.exit(0); // Kilépés, ha "Cancel"-t nyom
        }
        
        // 2. Bekérjük a PORT-ot
        String serverPortStr = JOptionPane.showInputDialog(
            null, 
            "Add meg a szerver PORTJÁT (pl. 19876):", 
            "Szerver Csatlakozás (2/2)", 
            JOptionPane.PLAIN_MESSAGE);

        if (serverPortStr == null || serverPortStr.trim().isEmpty()) {
            System.exit(0); // Kilépés, ha "Cancel"-t nyom
        }

        // 3. Átalakítjuk a portot számmá
        int serverPort;
        try {
            serverPort = Integer.parseInt(serverPortStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Hibás port szám! A port csak szám lehet.", "Hiba", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        // 4. Átadjuk az adatokat a kliensnek és indítjuk a GUI-t
        final String finalHost = serverHost.trim();
        final int finalPort = serverPort;
        SwingUtilities.invokeLater(() -> {
            new ChatApp(finalHost, finalPort); // Itt hívjuk a módosított konstruktort
        });
    }
}

