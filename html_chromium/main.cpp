#include "interface.h"
#include <cstdio>
#include <cstring>
#include <dlfcn.h>
#include <jni.h>
#include <android/log.h>
#include <pthread.h>
#include <stdlib.h>

#define LOG_TAG "GModBrowser"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

typedef void (*ConMsg_t)(const char* format, ...);
static ConMsg_t pConMsg = nullptr;

// ---- JNI helpers ----
static JavaVM* g_jvm = nullptr;
static jobject g_webView = nullptr;  // global ref to android.webkit.WebView
static jobject g_activity = nullptr; // global ref to Activity

// Получаем JNIEnv для текущего потока
static JNIEnv* GetEnv() {
    if (!g_jvm) return nullptr;
    JNIEnv* env = nullptr;
    if (g_jvm->GetEnv((void**)&env, JNI_VERSION_1_6) == JNI_EDETACHED) {
        g_jvm->AttachCurrentThread(&env, nullptr);
    }
    return env;
}

// Вызывается из Java/Kotlin лаунчера при старте
extern "C" JNIEXPORT void JNICALL
Java_com_gmote_browser_GModBrowserLib_init(JNIEnv* env, jclass clazz, jobject activity) {
    env->GetJavaVM(&g_jvm);
    g_activity = env->NewGlobalRef(activity);
    LOGI("JNI init: activity attached");
}

// Передаём WebView объект из Java
extern "C" JNIEXPORT void JNICALL
Java_com_gmote_browser_GModBrowserLib_setWebView(JNIEnv* env, jclass clazz, jobject webView) {
    if (g_webView) {
        env->DeleteGlobalRef(g_webView);
    }
    g_webView = env->NewGlobalRef(webView);
    LOGI("WebView reference set");
}

// ---- WebView helpers ----
static void WebView_loadUrl(const char* url) {
    JNIEnv* env = GetEnv();
    if (!env || !g_webView) return;

    jclass clazz = env->GetObjectClass(g_webView);
    jmethodID mid = env->GetMethodID(clazz, "loadUrl", "(Ljava/lang/String;)V");
    if (!mid) { LOGE("loadUrl method not found"); return; }

    jstring jurl = env->NewStringUTF(url);
    env->CallVoidMethod(g_webView, mid, jurl);
    env->DeleteLocalRef(jurl);
    env->DeleteLocalRef(clazz);
}

static void WebView_evaluateJS(const char* script) {
    JNIEnv* env = GetEnv();
    if (!env || !g_webView) return;

    jclass clazz = env->GetObjectClass(g_webView);
    // evaluateJavascript доступен с API 19+
    jmethodID mid = env->GetMethodID(clazz, "evaluateJavascript",
        "(Ljava/lang/String;Landroid/webkit/ValueCallback;)V");
    if (!mid) { LOGE("evaluateJavascript not found"); return; }

    jstring jscript = env->NewStringUTF(script);
    env->CallVoidMethod(g_webView, mid, jscript, nullptr);
    env->DeleteLocalRef(jscript);
    env->DeleteLocalRef(clazz);
}

static void WebView_goBack() {
    JNIEnv* env = GetEnv();
    if (!env || !g_webView) return;
    jclass clazz = env->GetObjectClass(g_webView);
    jmethodID mid = env->GetMethodID(clazz, "goBack", "()V");
    if (mid) env->CallVoidMethod(g_webView, mid);
    env->DeleteLocalRef(clazz);
}

static void WebView_goForward() {
    JNIEnv* env = GetEnv();
    if (!env || !g_webView) return;
    jclass clazz = env->GetObjectClass(g_webView);
    jmethodID mid = env->GetMethodID(clazz, "goForward", "()V");
    if (mid) env->CallVoidMethod(g_webView, mid);
    env->DeleteLocalRef(clazz);
}

static bool WebView_isLoading() {
    JNIEnv* env = GetEnv();
    if (!env || !g_webView) return false;
    jclass clazz = env->GetObjectClass(g_webView);
    jmethodID mid = env->GetMethodID(clazz, "getProgress", "()I");
    if (!mid) return false;
    jint progress = env->CallIntMethod(g_webView, mid);
    env->DeleteLocalRef(clazz);
    return progress < 100;
}

static void WebView_clearHistory() {
    JNIEnv* env = GetEnv();
    if (!env || !g_webView) return;
    jclass clazz = env->GetObjectClass(g_webView);
    jmethodID mid = env->GetMethodID(clazz, "clearHistory", "()V");
    if (mid) env->CallVoidMethod(g_webView, mid);
    env->DeleteLocalRef(clazz);
}

static const char* WebView_getUrl() {
    JNIEnv* env = GetEnv();
    if (!env || !g_webView) return "about:blank";
    jclass clazz = env->GetObjectClass(g_webView);
    jmethodID mid = env->GetMethodID(clazz, "getUrl", "()Ljava/lang/String;");
    if (!mid) return "about:blank";
    jstring jurl = (jstring)env->CallObjectMethod(g_webView, mid);
    if (!jurl) return "about:blank";
    static char urlBuf[512];
    const char* tmp = env->GetStringUTFChars(jurl, nullptr);
    strncpy(urlBuf, tmp, sizeof(urlBuf) - 1);
    env->ReleaseStringUTFChars(jurl, tmp);
    env->DeleteLocalRef(jurl);
    env->DeleteLocalRef(clazz);
    return urlBuf;
}

// ---- GMod интерфейс ----
class IGModWebBrowser : public IBaseInterface {
public:
    virtual void Init() = 0;
    virtual void Shutdown() = 0;
    virtual void SetSize(int w, int h) = 0;
    virtual void LoadURL(const char* url) = 0;
    virtual void OnMouseMove(int x, int y) = 0;
    virtual void OnMouseClick(int button, bool down) = 0;
    virtual void Update() = 0;
    virtual void OnKeyType(int key, bool down) = 0;
    virtual void RunJavaScript(const char* script) = 0;
    virtual void ClearHistory() = 0;
    virtual void GoBack() = 0;
    virtual void GoForward() = 0;
    virtual bool IsLoading() = 0;
    virtual const char* GetCurrentURL() = 0;
    virtual void SetPaintTarget(void* pTarget) = 0;
};

class CGModWebBrowser : public IGModWebBrowser {
public:
    void Init() override {
        LOGI("Init: Android WebView browser ready");
        if (pConMsg) pConMsg("[GModBrowser] Init: Android WebView ready.\n");
    }

    void Shutdown() override {
        LOGI("Shutdown");
        if (g_webView) {
            JNIEnv* env = GetEnv();
            if (env) {
                env->DeleteGlobalRef(g_webView);
                g_webView = nullptr;
            }
        }
    }

    void SetSize(int w, int h) override {
        // WebView размер управляется из Java через LayoutParams
        LOGI("SetSize: %dx%d", w, h);
    }

    void LoadURL(const char* url) override {
        if (!url) return;
        LOGI("LoadURL: %s", url);
        WebView_loadUrl(url);
    }

    void OnMouseMove(int x, int y) override {
        // WebView обрабатывает тач-события сам через Java
    }

    void OnMouseClick(int button, bool down) override {}

    void Update() override {}

    void OnKeyType(int key, bool down) override {}

    void RunJavaScript(const char* script) override {
        if (!script) return;
        WebView_evaluateJS(script);
    }

    void ClearHistory() override {
        WebView_clearHistory();
    }

    void GoBack() override {
        WebView_goBack();
    }

    void GoForward() override {
        WebView_goForward();
    }

    bool IsLoading() override {
        return WebView_isLoading();
    }

    const char* GetCurrentURL() override {
        return WebView_getUrl();
    }

    void SetPaintTarget(void* pTarget) override {
        // На Android рендер идёт через SurfaceView/TextureView в Java
        LOGI("SetPaintTarget called (managed by Java layer)");
    }
};

// ---- CreateInterface ----
EXPORT_FUNCTION void* CreateInterface(const char* pName, int* pReturnCode) {
    if (!pConMsg) {
        void* handle = dlopen("libengine.so", RTLD_NOLOAD | RTLD_LAZY);
        if (!handle) handle = dlopen("libtier0.so", RTLD_NOLOAD | RTLD_LAZY);
        if (handle) pConMsg = (ConMsg_t)dlsym(handle, "_Z6ConMsgPKcz");
    }

    if (pName && strstr(pName, "IGModWebBrowser")) {
        if (pReturnCode) *pReturnCode = 0;
        static CGModWebBrowser s_Browser;
        return static_cast<void*>(&s_Browser);
    }

    if (pReturnCode) *pReturnCode = 1;
    return nullptr;
}
