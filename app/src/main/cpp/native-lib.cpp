#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring

JNICALL
Java_sermk_pipi_game_Standalone_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
