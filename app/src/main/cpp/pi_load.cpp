
#include "util.h"
#include <unistd.h>
#include <libgen.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <libuvc/libuvc.h>

jint nativeCreate(JNIEnv* env,
                 jobject thiz){
    LOGI("native create");
    return 0;
}


static JNINativeMethod methods[] = {
        { "nativeCreate",					"()I", (void *) nativeCreate },
};

#define		NUM_ARRAY_ELEMENTS(p)		((int) sizeof(p) / sizeof(p[0]))

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("register CV resolver:");
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        LOGE("it's not JNI_VERSION_1_6");
        return JNI_ERR;
    }

    const char JAVA_CLASS[] = "sermk/pipi/game/CVResolver";

    jclass cls = env->FindClass(JAVA_CLASS);
    if (cls == NULL)
    {
        LOGE("not find class : %s",JAVA_CLASS);
        return JNI_ERR;
    }
    LOGI("find class %s succes!", JAVA_CLASS);

    if (env->RegisterNatives( cls,
                              methods, NUM_ARRAY_ELEMENTS(methods)) < 0) {
        LOGE("can not register method<");
        return -1;
    }

    LOGI("register CV resolver succes!");
    return JNI_VERSION_1_6;
}

