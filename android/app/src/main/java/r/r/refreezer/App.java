package r.r.refreezer;

import android.app.Application;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Only the main process (not the :ReFreezerDownloadService one)
        if (!isMainProcess()) return;

        // Start the streaming server right away, without waiting for MainActivity.
        // Android Auto can start the media service on its own, with no activity.
        new Thread(() -> {
            try {
                File settings = new File(getDir("flutter", MODE_PRIVATE), "settings.json");
                if (!settings.exists()) return;

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(settings), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }

                String arl = new JSONObject(sb.toString()).optString("arl", null);
                File offline = getExternalFilesDir("offline");
                if (arl == null || arl.isEmpty() || "null".equals(arl) || offline == null) return;

                StreamServerHolder.ensureStarted(arl, offline.getAbsolutePath());
                Log.i("App", "Stream server started from Application");
            } catch (Exception e) {
                Log.w("App", "Could not start stream server early: " + e.getMessage());
            }
        }, "stream-server-init").start();
    }

    private boolean isMainProcess() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream("/proc/self/cmdline"), StandardCharsets.UTF_8))) {
            String name = reader.readLine();
            if (name == null) return true;
            return name.trim().replace("\0", "").equals(getPackageName());
        } catch (Exception e) {
            return true;
        }
    }
}
