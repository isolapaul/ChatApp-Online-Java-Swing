package hu.isolapaul; // Figyelj, hogy a csomagnév nálad helyes legyen!

import java.io.Serializable;

/**
 * SZERVER -> KLIENS kommunikációs objektum.
 * A szerver ebben küld választ a kliens kéréseire (pl. Login, Register).
 * Serializable, mert hálózaton küldjük.
 */
public class ServerResponse implements Serializable {
    private static final long serialVersionUID = 100L; // Új, egyedi ID

    private boolean success; // Sikerült-e a művelet?
    private String message;  // Hibaüzenet (ha success == false)
    private String type;     // Válasz típusa (pl. "LOGIN_SUCCESS")
    private Object payload;  // Opcionális adat (pl. sikeres login esetén a User objektum)

    /**
     * Konstruktor egy válaszhoz.
     * @param success Sikerült-e a művelet?
     * @param message Üzenet (pl. hibaüzenet vagy "Sikeres regisztráció")
     * @param type Válasz típusa (pl. "LOGIN_FAILURE", "REGISTER_SUCCESS")
     * @param payload Bármilyen adat, amit a kliensnek küldeni akarunk (pl. User)
     */
    public ServerResponse(boolean success, String message, String type, Object payload) {
        this.success = success;
        this.message = message;
        this.type = type;
        this.payload = payload;
    }

    // Egyszerűbb konstruktorok
    
    // Sikeres válasz, adattal (pl. Login)
    public static ServerResponse success(String type, Object payload) {
        return new ServerResponse(true, "OK", type, payload);
    }
    
    // Sikeres válasz, adat nélkül (pl. Regisztráció)
    public static ServerResponse success(String type) {
        return new ServerResponse(true, "OK", type, null);
    }

    // Hiba válasz
    public static ServerResponse error(String message) {
        return new ServerResponse(false, message, "ERROR", null);
    }


    // --- GETTER metódusok ---
    // Ezek hiányoztak a NetworkClient-ből!

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}
