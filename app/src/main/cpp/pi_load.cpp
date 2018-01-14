
#include "util.h"
#include <unistd.h>
#include <libgen.h>
#include <string>
#include <libuvc/libuvc.h>
#include "pi_cv.h"
#include "pi2_cv.h"
#include "pi2_plot.h"

bool pi_cv::learn = false;
#if 0
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
#endif

static JNINativeMethod methods[] = {
#if 0
        { "passFrameToCVPIPI",					"(JJ)I", (void *) passFrameToCVPIPI },
        { "passRoiRectToCVPIPI",                  "(IIJ)I", (void *) passRoiRectToCVPIPI },
        { "enableLearn",                  "(Z)V", (void *) enableLearn },
#endif
        { "startCV",                  "()Z", (void *) pi2_cv::startCV },
        { "setRectOfMask",                  "(IIJ)I", (void *) pi2_cv::setRectOfMask },
        { "setMode",                  "(I)V", (void *)pi2_cv::setMode },
        { "setPlotOption",                  "(J)V", (void *)pi2_plot::setPlotOption },
        { "setDisablePlot",                  "(Z)V", (void *)pi2_plot::setDisablePlot },
        { "getFrame",                  "(I)J", (void *)pi2_cv::getFrame },
        { "setOptions",                  "(III)V", (void *) pi2_cv::setOptions },

};

#define		NUM_ARRAY_ELEMENTS(p)		((int) sizeof(p) / sizeof(p[0]))

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("register CV resolver:");
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        LOGE("it's not JNI_VERSION_1_6");
        return JNI_ERR;
    }

    const char JAVA_CLASS[] = "sermk/pipi/pilauncher/CVResolver";

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

