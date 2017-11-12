//
// Created by echormonov on 12.11.17.
//

#include "pi2_cv.h"
#include <UVCCamera.h>


using namespace cv;

jobject pi2_cv::objectCV = NULL;
jmethodID pi2_cv::midCV = NULL;

cv::Mat pi2_plot::chart;

#define TAG "###>"

void pi2_cv::callByteBufferFrame(JNIEnv *env, uvc_frame_t *frame) {
    jobject buf = env->NewDirectByteBuffer(frame->data, frame->data_bytes);
    LOGV(TAG"buf %p", buf);
    if(buf != NULL) {
        env->CallVoidMethod(objectCV, midCV, buf);
    }
    env->ExceptionClear();
    env->DeleteLocalRef(buf);
}

void pi2_cv::test(JNIEnv * env, uvc_frame_t * frame) {
    LOGV(TAG"test %p", frame);
    if (!LIKELY(frame)) {
        LOGW(TAG"bad frame !!!");
        return;
    }
    env->CallVoidMethod(objectCV, midCV,&pi2_plot::chart);
}

void pi2_cv::startCV(JNIEnv *env, jobject thiz, jboolean plotiing) {
    LOGI(TAG"startCV");
    UVCPreview::setPass2cv(pi2_cv::test);
    jclass clazz = env->GetObjectClass(thiz);
    if (env->IsSameObject(objectCV, thiz)){
        LOGV(TAG"Same CV jobject");
        return;
    }
    env->DeleteGlobalRef(objectCV);
    objectCV = env->NewGlobalRef(thiz);
    if (LIKELY(clazz)) {
        //midCV = env->GetMethodID(clazz, "plottCV","(Ljava/nio/ByteBuffer;)V");
        midCV = env->GetMethodID(clazz, "plottCV","(J)V");
    } else {
        LOGW(TAG"failed to get object class");
    }
    env->ExceptionClear();
    if (!midCV) {
        LOGE(TAG"Can't find plottCV : %p %p", midCV, clazz);
    }
    env->CallVoidMethod(objectCV, midCV, NULL);
    LOGI(TAG"succes set startCV! midCV point %p ",midCV);
}
