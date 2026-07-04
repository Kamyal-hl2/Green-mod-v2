package com.valvesoftware;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Android Activity for Green Engine (Garry's Mod port).
 * Bridges Java lifecycle to native engine via JNI.
 *
 * JNI functions (implemented in engine/launcher/android/main.cpp):
 *   - setenv(String key, String value)
 *   - setArgs(String args)
 *   - nativeOnActivityResult(int requestCode, int resultCode, Intent data)
 */
public class ValveActivity2 extends Activity {
    private static final String TAG = "ValveActivity2";

    static {
        try {
            System.loadLibrary("SDL2");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load libSDL2.so: " + e.getMessage());
        }
        try {
            System.loadLibrary("launcher");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load liblauncher.so: " + e.getMessage());
        }
    }

    // JNI methods from engine/launcher/android/main.cpp
    public static native void setenv(String key, String value);
    public static native void setArgs(String args);
    public static native void nativeOnActivityResult(int requestCode, int resultCode, Intent data);

    private GModView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set environment variables for the engine
        try {
            String filesDir = getFilesDir().getAbsolutePath();
            String cacheDir = getCacheDir().getAbsolutePath();
            String externalFiles = getExternalFilesDir(null) != null
                ? getExternalFilesDir(null).getAbsolutePath() : filesDir;

            setenv("HOME", filesDir);
            setenv("ANDROID_DATA", filesDir);
            setenv("ANDROID_CACHE", cacheDir);
            setenv("GARRYSMOD_PATH", externalFiles + "/garrysmod");

            // Set command line arguments
            String args = "-basedir " + externalFiles
                + " -game " + externalFiles + "/garrysmod"
                + " -console -novid -nojoy -noipx";
            setArgs(args);

            Log.i(TAG, "Engine path: " + externalFiles);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set env: " + e.getMessage());
        }

        // Create SDL/GL view
        view = new GModView(this);
        setContentView(view);

        // Hide system UI
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            nativeOnActivityResult(requestCode, resultCode, data);
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
