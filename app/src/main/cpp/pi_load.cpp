
#include "util.h"
#include <unistd.h>
#include <libgen.h>
#include <string>

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("register_uvccamera:");
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        LOGE("it's not JNI_VERSION_1_6");
        return JNI_ERR;
    }
/*
    const char JAVA_CLASS[] = "com/ford/openxc/webcam/webcam/NativeWebcam";

    jclass cls = env->FindClass(JAVA_CLASS);
    if (cls == NULL)
    {
        LOGE("not find class : ");
        return JNI_ERR;
    }
    LOGI("find class");

    if (env->RegisterNatives( cls,
                              methods, NUM_ARRAY_ELEMENTS(methods)) < 0) {
        LOGE("can not register method!");
        return -1;
    }
    */
    LOGI("register_uvccamera succes");
    return JNI_VERSION_1_6;
}

