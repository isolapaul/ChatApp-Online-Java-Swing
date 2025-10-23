package hu.isolapaul;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Adatmodell, ami egy regisztrált felhasználót reprezentál.
 * Serializable, hogy fájlba menthessük.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 3L;

    private String username;
    private String email;
    private String passwordHash; // SOHA nem tárolunk sima jelszót
    private List<String> friends; // Barátok listája (felhasználónevek)

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.friends = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void addFriend(String username) {
        if (!friends.contains(username)) {
            friends.add(username);
        }
    }

    // A hashCode és equals fontos a gyűjtemények helyes működéséhez
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
