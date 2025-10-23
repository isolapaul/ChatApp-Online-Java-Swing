package hu.isolapaul;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Adatmodell egyetlen üzenethez.
 * Tartalmazza a küldőt és a címzettet is.
 * Serializable, hogy hálózaton küldhető és menthető legyen.
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L; // Változtattam a verziót

    private String sender;
    private String receiver;
    private String message;
    private LocalDateTime timestamp;

    public ChatMessage(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }
    
    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Létrehoz egy "chat azonosítót" két felhasználó között.
     * Fontos, hogy "Bence_Fruzsi" és "Fruzsi_Bence" ugyanazt eredményezze.
     */
    public String getChatId() {
        if (sender.compareTo(receiver) > 0) {
            return receiver + "_" + sender;
        } else {
            return sender + "_" + receiver;
        }
    }

    public String getFormattedTime() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        return timestamp.format(fmt);
    }

    

    @Override
    public String toString() {
        // A kliens oldalon már csak a küldőt kell mutatnunk
        return "[" + getFormattedTime() + "] " + sender + ": " + message;
    }
}
