
#include "util.h"
#include <unistd.h>
#include <libgen.h>
#include <string>
#include <libuvc/libuvc.h>
#include "pi_cv.h"
#include "pi2_cv.h"


bool pi_cv::learn = false;

static jint passFrameToCVPIPI(JNIEnv *env, jobject thiz,
    ID_TYPE refMatPreview, ID_TYPE refMatChart) {

    jint result = 0;
    //ENTER();
    result = pi_cv::calcPipiChart(refMatPreview, refMatChart);
    //RETURN(result, jint);
    return result;
}

static jint passRoiRectToCVPIPI(JNIEnv *env, jobject thiz,
    jint xsRoi, jint ysRoi, ID_TYPE refMat) {

    jint result = 0;
    //ENTER();
    result = pi_cv::setRectMask(xsRoi, ysRoi, refMat);
    //RETURN(result, jint);
    return result;
}

static void enableLearn(JNIEnv *env, jobject thiz,
                        jboolean enable) {

    pi_cv::setLearnEnable(static_cast<bool>(enable));
    //result = pi_cv::setRectMask(xsRoi, ysRoi, refMat);

}


static JNINativeMethod methods[] = {
        { "passFrameToCVPIPI",					"(JJ)I", (void *) passFrameToCVPIPI },
        { "passRoiRectToCVPIPI",                  "(IIJ)I", (void *) passRoiRectToCVPIPI },
        { "enableLearn",                  "(Z)V", (void *) enableLearn },
        { "startCV",                  "(Z)V", (void *) pi2_cv::startCV },
        { "setPlotOption",                  "(II)V", (void *)pi2_plot::setPlotOption },
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

