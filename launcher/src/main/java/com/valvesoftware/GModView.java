package com.valvesoftware;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL ES surface view for the Source Engine.
 * Handles touch input forwarding to the native engine.
 */
public class GModView extends GLSurfaceView {
    private static final String TAG = "GModView";

    public GModView(Context context) {
        super(context);

        // Request OpenGL ES 3.0
        setEGLContextClientVersion(3);

        // Try best config first, fallback to simpler config
        try {
            setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Preferred EGL config not supported, using default");
            setEGLConfigChooser(true);
        }

        // Set renderer
        setRenderer(new Renderer());

        // Render continuously
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        // Enable touch events
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Touch events are handled by SDL2's event system
        // which intercepts SDL_FINGERDOWN/UP/MOTION via SDL_AddEventWatch
        // in engine/inputsystem/touch_sdl.cpp
        return super.onTouchEvent(event);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private static class Renderer implements GLSurfaceView.Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.i(TAG, "GL Surface created");
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.i(TAG, "GL Surface changed: " + width + "x" + height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            // The actual rendering is done by the native engine
            // through SDL2's GL context. This callback just keeps
            // the GL surface alive.
        }
    }
}
