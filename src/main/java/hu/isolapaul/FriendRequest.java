package hu.isolapaul;

import java.io.Serializable;

/**
 * KLIENS -> SZERVER objektum.
 * Egy barát felvételi kérést csomagol.
 */
public class FriendRequest implements Serializable {
    private static final long serialVersionUID = 103L;
    
    // Annak a felhasználónak a neve, akit barátnak akarunk jelölni
    private String targetUsername;

    public FriendRequest(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getTargetUsername() {
        return targetUsername;
    }
}

