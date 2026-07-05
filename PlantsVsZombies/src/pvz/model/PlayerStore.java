package pvz.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simple file-backed user store.
 *
 * Storage format (binary serialized):
 * - data/users.bin    -> serialized HashMap of users and passwords
 * - data/session.bin  -> serialized username string of the signed-in user
 *
 * Notes
 * - This uses binary serialization for compact storage.
 * - Don't store real passwords like this in production. Use hashing + salting and proper
 *   credential storage instead.
 */
public class PlayerStore {
    // Where we keep the binary files for this demo (relative to app working dir)
    private static final Path DATA_DIR = Path.of("data");
    private static final Path USERS_FILE = DATA_DIR.resolve("users.bin");
    private static final Path SESSION_FILE = DATA_DIR.resolve("session.bin");

    /** All registered users for this run (keyed by username). */
    private final Map<String, Player> users = new HashMap<>();
    /** The user who most recently signed in (null if none). */
    private Player current;

    /** Ensure the data directory exists. */
    private static void ensureDataDir() {
        try { Files.createDirectories(DATA_DIR); } catch (IOException ignored) { }
    }

    public synchronized boolean createAccount(String username, String password) {
        if (username == null || username.isBlank()) return false;
        // Load from disk if needed so duplicate checks are accurate
        load();
        if (users.containsKey(username)) return false; // duplicate

        Player p = new Player(username, password);
        users.put(username, p);
        passwords.put(username, password);

        // Save updated users to binary file
        ensureDataDir();
        try {
            saveToBinary();
        } catch (IOException e) {
            // If we can't persist, roll back the in-memory add for consistency
            users.remove(username);
            passwords.remove(username);
            return false;
        }
        return true;
    }

    public synchronized boolean signIn(String username, String password) {
        load();
        Player p = users.get(username);
        if (p != null && p.passwordMatches(password)) {
            current = p;
            writeSession(username);
            return true;
        }
        return false;
    }

    /** @return the currently signed-in player, if any. */
    public synchronized Optional<Player> getCurrentPlayer() { return Optional.ofNullable(current); }

    /**
     * Sign out the current user; clears session.txt if present.
     */
    public synchronized void signOut() {
        current = null;
        try { Files.deleteIfExists(SESSION_FILE); } catch (IOException ignored) { }
    }
    
    public synchronized boolean deleteAccount(String username, String password) {
        load();
        Player p = users.get(username);
        if (p == null || !p.passwordMatches(password)) return false;

        // Remove from memory
        users.remove(username);
        passwords.remove(username);

        // Rewrite users binary file without this user
        ensureDataDir();
        try {
            saveToBinary();
        } catch (IOException e) {
            // If rewrite fails, reload from disk to avoid diverging state
            reloadFromDisk();
            return false;
        }

        // If that user was logged in, sign them out
        if (current != null && username.equals(current.getUsername())) {
            signOut();
        }
        return true;
    }

    // -------- Persistence helpers (tiny and explicit for readability) --------

    /** Load users and session from disk into memory (idempotent and cheap). */
    public synchronized void load() { reloadFromDisk(); }

    /** Save all users back to users.bin and current session (if any). */
    public synchronized void save() {
        ensureDataDir();
        try {
            saveToBinary();
        } catch (IOException ignored) { }
        if (current != null) writeSession(current.getUsername());
    }

    /** Read users.bin and session.bin freshly. */
    private void reloadFromDisk() {
        users.clear();
        passwords.clear();
        // Users
        if (Files.exists(USERS_FILE)) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(USERS_FILE))) {
                @SuppressWarnings("unchecked")
                Map<String, Player> loadedUsers = (Map<String, Player>) ois.readObject();
                @SuppressWarnings("unchecked")
                Map<String, String> loadedPasswords = (Map<String, String>) ois.readObject();
                users.putAll(loadedUsers);
                passwords.putAll(loadedPasswords);
            } catch (IOException | ClassNotFoundException ignored) { }
        }
        // Session
        current = null;
        if (Files.exists(SESSION_FILE)) {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(SESSION_FILE))) {
                String u = (String) ois.readObject();
                Player p = users.get(u);
                if (p != null) current = p; // only set if user still exists
            } catch (IOException | ClassNotFoundException ignored) { }
        }
    }

    /** Write session.bin containing just the username. */
    private void writeSession(String username) {
        ensureDataDir();
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(SESSION_FILE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE))) {
            oos.writeObject(username);
        } catch (IOException ignored) { }
    }

    /** Save users and passwords maps to binary file. */
    private void saveToBinary() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(USERS_FILE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE))) {
            oos.writeObject(users);
            oos.writeObject(passwords);
        }
    }

    /**
     * Demo helper: return the plain-text password we keep alongside the user
     * so we can rewrite users.bin. (Never do this in a real app.)
     */
    private String getPasswordFor(Player u) { return passwords.getOrDefault(u.getUsername(), ""); }

    // Mirror of plain-text passwords keyed by username for persistence only (demo).
    private final Map<String, String> passwords = new HashMap<>();
}