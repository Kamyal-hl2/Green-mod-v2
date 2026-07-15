package com.valvesoftware;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages Chromium .so files for the in-game browser.
 * Downloads prebuilt Chromium binaries from GitHub Releases on first launch.
 * Keeps APK small by not bundling Chromium (~500MB+).
 */
public class ChromiumManager {
    private static final String TAG = "ChromiumManager";
    private static final String RELEASE_BASE = "https://github.com/Kamyal-hl2/Green-mod-v2/releases/download/chromium";
    private static final String[] CHROMIUM_FILES = {
        "libchrome.so",
        "libchromiumview.so"
    };

    public interface Callback {
        void onProgress(String message);
        void onComplete(boolean success);
    }

    private final Context context;
    private final File chromiumDir;

    public ChromiumManager(Context context) {
        this.context = context;
        this.chromiumDir = new File(context.getFilesDir(), "chromium");
        chromiumDir.mkdirs();
    }

    public boolean isChromiumInstalled() {
        for (String file : CHROMIUM_FILES) {
            if (!new File(chromiumDir, file).exists()) {
                return false;
            }
        }
        return true;
    }

    public File getChromiumDir() {
        return chromiumDir;
    }

    public void downloadChromium(Callback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            for (String file : CHROMIUM_FILES) {
                File target = new File(chromiumDir, file);
                if (target.exists()) {
                    mainHandler.post(() -> callback.onProgress("Already exists: " + file));
                    continue;
                }
                try {
                    mainHandler.post(() -> callback.onProgress("Downloading " + file + "..."));
                    URL url = new URL(RELEASE_BASE + "/" + file);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(60000);
                    conn.setRequestMethod("GET");

                    try {
                        if (conn.getResponseCode() != 200) {
                            mainHandler.post(() -> callback.onProgress("Failed to download " + file + " (HTTP " + conn.getResponseCode() + ")"));
                            continue;
                        }

                        File tmp = new File(chromiumDir, file + ".tmp");
                        InputStream in = null;
                        FileOutputStream out = null;
                        try {
                            in = conn.getInputStream();
                            out = new FileOutputStream(tmp);
                            byte[] buf = new byte[8192];
                            int read;
                            while ((read = in.read(buf)) != -1) {
                                out.write(buf, 0, read);
                            }
                        } finally {
                            if (out != null) try { out.close(); } catch (Exception ignored) {}
                            if (in != null) try { in.close(); } catch (Exception ignored) {}
                        }

                        if (!tmp.renameTo(target)) {
                            target.delete();
                            tmp.renameTo(target);
                        }

                        final long sizeMB = target.length() / 1024 / 1024;
                        mainHandler.post(() -> callback.onProgress("Downloaded " + file + " (" + sizeMB + " MB)"));
                    } finally {
                        conn.disconnect();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Download failed for " + file + ": " + e.getMessage());
                    mainHandler.post(() -> callback.onProgress("Failed: " + file + " - " + e.getMessage()));
                }
            }
            final boolean result = isChromiumInstalled();
            mainHandler.post(() -> callback.onComplete(result));
        });
    }

    /**
     * Returns the system library path that includes downloaded Chromium .so files.
     * Used to set java.library.path or pass to native code.
     */
    public String getLibraryPath() {
        return chromiumDir.getAbsolutePath();
    }

    /**
     * Load a Chromium native library from the downloaded directory.
     * Falls back to system library path if not found locally.
     */
    public boolean loadLibrary(String name) {
        File libFile = new File(chromiumDir, "lib" + name + ".so");
        if (libFile.exists()) {
            try {
                System.load(libFile.getAbsolutePath());
                Log.i(TAG, "Loaded Chromium library: " + name);
                return true;
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Failed to load " + name + ": " + e.getMessage());
            }
        }
        return false;
    }
}
