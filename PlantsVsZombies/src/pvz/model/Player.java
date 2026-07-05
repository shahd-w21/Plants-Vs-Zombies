package pvz.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Minimal player model.
 * Only stores username and password for this demo (no stars/progress yet).
 * Serializable so it can be saved later if persistence is added.
 */
public class Player implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Immutable username, used as the unique key in the store. */
    private final String username;
    /** Plain-text password for simplicity in this demo. Do not use in production. */
    private String password; // demo only

    /** Construct a player with username and password. */
    public Player(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /** Username getter. */
    public String getUsername() { return username; }

    /** Check if the provided password equals the stored one (demo only). */
    public boolean passwordMatches(String pw) { return Objects.equals(this.password, pw); }

    /** Update the password (no validation/hashing in this demo). */
    public void setPassword(String pw) { this.password = pw; }
}
