package com.valvesoftware;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Android Activity for Green Engine (Garry's Mod port).
 * Bridges Java lifecycle to native engine via JNI.
 *
 * Supports optional Chromium runtime download for in-game browser.
 */
public class ValveActivity2 extends Activity {
    private static final String TAG = "ValveActivity2";

    static {
        boolean sdlLoaded = false;
        try {
            System.loadLibrary("SDL2");
            sdlLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load libSDL2.so: " + e.getMessage());
        }
        if (sdlLoaded) {
            try {
                System.loadLibrary("launcher");
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Failed to load liblauncher.so: " + e.getMessage());
            }
        }
    }

    public static native void setenv(String key, String value);
    public static native void setArgs(String args);
    public static native void nativeOnActivityResult(int requestCode, int resultCode, android.content.Intent data);

    private GModView view;
    private ChromiumManager chromiumManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        chromiumManager = new ChromiumManager(this);

        setupEngineEnvironment();

        if (chromiumManager.isChromiumInstalled()) {
            Log.i(TAG, "Chromium already installed");
            startEngine();
        } else {
            promptChromiumDownload();
        }

        hideSystemUI();
    }

    private void setupEngineEnvironment() {
        try {
            String filesDir = getFilesDir().getAbsolutePath();
            String cacheDir = getCacheDir().getAbsolutePath();
            String externalFiles = getExternalFilesDir(null) != null
                ? getExternalFilesDir(null).getAbsolutePath() : filesDir;

            setenv("HOME", filesDir);
            setenv("ANDROID_DATA", filesDir);
            setenv("ANDROID_CACHE", cacheDir);
            setenv("GARRYSMOD_PATH", externalFiles + "/garrysmod");
            setenv("CHROMIUM_PATH", chromiumManager.getChromiumDir().getAbsolutePath());

            String args = "-basedir " + externalFiles
                + " -game " + externalFiles + "/garrysmod"
                + " -console -novid -nojoy -noipx";
            setArgs(args);

            Log.i(TAG, "Engine path: " + externalFiles);
            Log.i(TAG, "Chromium path: " + chromiumManager.getChromiumDir());
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "JNI call failed (native libs not loaded): " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Failed to set env: " + e.getMessage());
        }
    }

    private void promptChromiumDownload() {
        new AlertDialog.Builder(this)
            .setTitle("Chromium Browser")
            .setMessage("Download Chromium for in-game browser support?\n\n" +
                "This is optional. The game works without it, but some server features may be limited.\n\n" +
                "Size: ~500 MB")
            .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    downloadChromiumAndStart();
                }
            })
            .setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startEngine();
                }
            })
            .setCancelable(false)
            .show();
    }

    private void downloadChromiumAndStart() {
        final View progressView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        final TextView statusText = new TextView(this);
        statusText.setText("Downloading Chromium...");
        statusText.setPadding(48, 32, 48, 32);
        setContentView(statusText);

        chromiumManager.downloadChromium(new ChromiumManager.Callback() {
            @Override
            public void onProgress(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText(message);
                    }
                });
            }

            @Override
            public void onComplete(boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        if (success) {
                            Log.i(TAG, "Chromium download complete");
                        } else {
                            Log.w(TAG, "Chromium download incomplete - running without it");
                        }
                        startEngine();
                    }
                });
            }
        });
    }

    private void startEngine() {
        view = new GModView(this);
        setContentView(view);
        hideSystemUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (view != null) {
            view.onResume();
        }
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        if (view != null) {
            view.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (view != null) {
            view.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            nativeOnActivityResult(requestCode, resultCode, data);
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "nativeOnActivityResult failed (native libs not loaded): " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "nativeOnActivityResult failed: " + e.getMessage());
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }
}
