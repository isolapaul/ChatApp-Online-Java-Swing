package hu.isolapaul;

import java.io.Serializable;

/**
 * KLIENS -> SZERVER objektum.
 * Regisztrációs kérést csomagol.
 */
public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 101L;
    private String username;
    private String email;
    private String password;
    private String password2;

    public RegisterRequest(String username, String email, String password, String password2) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.password2 = password2;
    }

    // Itt vannak a metódusok, amiket a ClientHandlerThread hív
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword1() { return password; }
    public String getPassword2() { return password2; }
}

