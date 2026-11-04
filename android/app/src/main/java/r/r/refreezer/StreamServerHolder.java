package r.r.refreezer;

/**
 * Process-wide owner of the local streaming server (127.0.0.1:36958).
 *
 * The server must not depend on MainActivity: when Android Auto wakes the
 * media service, the process starts without any activity and the player
 * would otherwise fail with "Failed to connect to localhost:36958".
 */
public final class StreamServerHolder {
    private static StreamServer instance;

    private StreamServerHolder() {}

    public static synchronized StreamServer get() {
        return instance;
    }

    /** Starts the server if it isn't running yet. Returns the running instance. */
    public static synchronized StreamServer ensureStarted(String arl, String offlinePath) {
        if (instance == null) {
            instance = new StreamServer(arl, offlinePath);
            instance.start();
        }
        return instance;
    }

    public static synchronized void stop() {
        if (instance != null) {
            instance.stop();
            instance = null;
        }
    }
}
