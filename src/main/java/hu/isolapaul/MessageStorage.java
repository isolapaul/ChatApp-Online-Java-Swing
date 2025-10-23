package hu.isolapaul;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SZERVER oldali osztály.
 * Felelős a ChatMessage objektumok mentéséért és betöltéséért.
 * Minden beszélgetést külön fájlba ment.
 */
public class MessageStorage {

    private final String storageFolder = "chat_history";
    

    public MessageStorage() {
        // Létrehozza a mappát, ha nem létezik
        new File(storageFolder).mkdir();
    }

    private String getFilenameForChat(String chatId) {
        return storageFolder + File.separator + chatId + "_chat.dat";
    }

    /**
     * Elment egyetlen üzenetet.
     * Beolvassa a meglévő listát, hozzáadja az újat, visszaírja.
     */
    @SuppressWarnings("unchecked")
    public synchronized void saveMessage(ChatMessage msg) {
        String filename = getFilenameForChat(msg.getChatId());
        List<ChatMessage> history = new ArrayList<>();
        
        File f = new File(filename);
        if (f.exists()) {
            // Ha van már fájl, beolvassuk a tartalmát
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                history = (List<ChatMessage>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Hiba az előzmény beolvasásánál: " + e.getMessage());
            }
        }
        
        // Hozzáadjuk az új üzenetet
        history.add(msg);

        // Visszaírjuk a teljes listát
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(history);
        } catch (IOException e) {
            System.err.println("Hiba az üzenet mentésekor: " + e.getMessage());
        }
    }

    /**
     * Betölti egy adott beszélgetés teljes előzményét.
     */
    @SuppressWarnings("unchecked")
    public synchronized List<ChatMessage> loadHistory(String chatId) {
        String filename = getFilenameForChat(chatId);
        File f = new File(filename);

        if (!f.exists()) {
            return new ArrayList<>(); // Üres lista, ha nincs előzmény
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<ChatMessage>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Hiba az előzmény betöltésénél: " + e.getMessage());
            return new ArrayList<>(); // Hiba esetén is üres lista
        }
    }
}
