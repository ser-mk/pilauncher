//
// Created by echormonov on 12.11.17.
//

#include "pi2_cv.h"
#include <UVCCamera.h>
#include "pi2_plot.h"

using namespace cv;

jobject pi2_cv::objectCV = NULL;
jmethodID pi2_cv::midCV = NULL;
uint8_t pi2_cv::arrayOfMask[pi2_plot::sizePreview] = {0};
Rect pi2_cv::mask;


#define TAG "###>"

void pi2_cv::calcUVC_FrameOfMask(uvc_frame_t *frame, const cv::Rect & mask) {
    const size_t xEnd = mask.x + mask.width;
    const size_t yEnd = mask.y + mask.height;
    const size_t widhtFrameYU = frame->width * 2;
    const uint8_t * pFrame = reinterpret_cast<const uint8_t *>(frame->data);
    LOGV(TAG"pFrame %p", pFrame);
    size_t i = 0;
    for(size_t y= mask.y; y < yEnd; y++){
        for(size_t x=mask.x; x < xEnd; x++){
            arrayOfMask[i] = pFrame[y*widhtFrameYU + 2*x];
            i++;
        }
    }

}

void pi2_cv::cvProccessing(JNIEnv *env, uvc_frame_t *frame) {
    LOGV(TAG"test %p", frame);
    if (!LIKELY(frame)) {
        LOGW(TAG"bad frame !!!");
        return;
    }


    Rect testRect = Rect(0,0,400,100);
    calcUVC_FrameOfMask(frame,testRect);

    pi2_plot::plotPreviewFrame(frame);
    testRect.y += pi2_plot::heightPreview;
    pi2_plot::plotSubGrayArray(arrayOfMask,testRect);
/*
    env->CallVoidMethod(objectCV, midCV,&pi2_plot::chart);
*/
}

void pi2_cv::startCV(JNIEnv *env, jobject thiz, jboolean plotiing) {
    LOGI(TAG"startCV");
    UVCPreview::setPass2cv(pi2_cv::cvProccessing);
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
