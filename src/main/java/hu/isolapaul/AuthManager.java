package hu.isolapaul;

import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * SZERVER oldali osztály.
 * Felelős a felhasználók regisztrációjáért, bejelentkeztetéséért
 * és az adatok fájlba mentéséért (szerializáció).
 */
public class AuthManager {

    // Gyűjtemény: Tárolja a felhasználóneveket és a hozzájuk tartozó User objektumokat.
    // ConcurrentHashMap-et használunk, mert a szerver több szálon fogja ezt elérni.
    private Map<String, User> userDatabase;
    private static final String userDbFile = "users.dat";

    // Jelszó erősség ellenőrző (legalább 6 karakter, 1 nagybetű, 1 szám)
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*\\d)(?=.*[A-Z]).{6,}$");

    public AuthManager() {
        userDatabase = new ConcurrentHashMap<>();
        loadUsersFromFile();
    }

    /**
     * Regisztrációs kísérlet.
     * Validálja az adatokat és létrehozza a felhasználót.
     */
    public synchronized String registerUser(String username, String email, String password, String passwordConfirm) {
        // 1. Validáció
        if (username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null) {
            return "Hiba: Minden mező kitöltése kötelező!";
        }
        if (userDatabase.containsKey(username)) {
            return "Hiba: Ez a felhasználónév már foglalt!";
        }
        if (!password.equals(passwordConfirm)) {
            return "Hiba: A két jelszó nem egyezik!";
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return "Hiba: A jelszónak legalább 6 karakter hosszúnak kell lennie, " +
                   "tartalmaznia kell legalább egy nagybetűt és egy számot.";
        }

        // 2. Jelszó hashelése
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // 3. Felhasználó létrehozása és mentése
        User newUser = new User(username, email, hashedPassword);
        userDatabase.put(username, newUser);
        
        saveUsersToFile(); // Mentés fájlba
        
        return "SIKER"; // Sikeres regisztráció
    }

    /**
     * Bejelentkezési kísérlet.
     * Visszaadja a User objektumot siker esetén, null-t hiba esetén.
     */
    public synchronized User loginUser(String username, String password) {
        User user = userDatabase.get(username);
        if (user == null) {
            return null; // Nincs ilyen felhasználó
        }

        // A jBCrypt ellenőrzi, hogy a kapott jelszó megegyezik-e a tárolt hash-sel
        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            return user; // Sikeres bejelentkezés
        }

        return null; // Hibás jelszó
    }

    public synchronized String addFriend(String requesterUsername, String targetUsername) {
        // 1. Ellenőrizzük, hogy léteznek-e a felhasználók
        User requester = userDatabase.get(requesterUsername);
        User target = userDatabase.get(targetUsername);

        if (target == null) {
            return "Hiba: A felhasználó nem található: " + targetUsername;
        }
        if (requester == null) {
            // Ennek sosem szabadna megtörténnie, ha be van jelentkezve
            return "Hiba: Belső hiba (kérelmező nem található).";
        }
        if (requesterUsername.equals(targetUsername)) {
            return "Hiba: Magadat nem adhatod hozzá barátként.";
        }

        // 2. Ellenőrizzük, hogy nem barátok-e már
        if (requester.getFriends().contains(targetUsername)) {
            return "Hiba: Már barátok vagytok.";
        }

        // 3. Hozzáadás mindkét félhez
        requester.addFriend(targetUsername);
        target.addFriend(requesterUsername);

        // 4. Változások mentése
        saveUsersToFile();
        
        System.out.println("[AuthManager] Új barátság: " + requesterUsername + " <-> " + targetUsername);
        return "SIKER";
    }
    
    public User getUser(String username) {
        return userDatabase.get(username);
    }

    /**
     * Elmenti a teljes felhasználói adatbázist fájlba (Szerializáció).
     * Teljesíti a "Fájlba írás" követelményt.
     */
    public synchronized void saveUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userDbFile))) {
            oos.writeObject(userDatabase);
            System.out.println("[AuthManager] Felhasználók sikeresen mentve.");
        } catch (IOException e) {
            System.err.println("Hiba a felhasználók mentése közben: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Betölti a felhasználói adatbázist fájlból (Szerializáció).
     */
    @SuppressWarnings("unchecked")
    private void loadUsersFromFile() {
        File f = new File(userDbFile);
        if (!f.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userDbFile))) {
            userDatabase = (Map<String, User>) ois.readObject();
            System.out.println("[AuthManager] Felhasználók betöltve: " + userDatabase.size() + " db");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Hiba a felhasználók betöltése közben: " + e.getMessage());
            userDatabase = new ConcurrentHashMap<>(); // Hiba esetén üres adatbázissal indulunk
        }
    }
}
