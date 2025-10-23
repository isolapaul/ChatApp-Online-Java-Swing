package hu.isolapaul;

import java.io.Serializable;

/**
 * Adatcsomag, amit a kliens küld a szervernek bejelentkezéskor.
 * Serializable, mert hálózaton utazik.
 */
public class LoginRequest implements Serializable {
    private static final long serialVersionUID = 101L;
    
    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
