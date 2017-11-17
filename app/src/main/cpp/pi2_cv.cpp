//
// Created by echormonov on 12.11.17.
//

#include "pi2_cv.h"
#include <UVCCamera.h>
#include "pi2_plot.h"

using namespace cv;

jobject pi2_cv::objectCV = NULL;
jmethodID pi2_cv::midCV = NULL;
uint8_t pi2_cv::arrayFromMask[pi2_plot::sizePreview] = {0};
uint8_t pi2_cv::arrayMask[pi2_plot::sizePreview] = {0};
Rect pi2_cv::maskRect;
int pi2_cv::mode = pi2_cv::CAPTURE;


#define TAG "###>"

void HistType::newVerHistArrayYUYV(const uint8_t *array, const Rect &maskRect){
    const size_t sizeHist = static_cast<size_t >(maskRect.width);
    const size_t rows = static_cast<size_t >(maskRect.height);
    this->clearHist();
    for(size_t y=0; y<rows; y++){
        for(size_t x=0; x<sizeHist; x++){
            this->hist[x] += array[y*sizeHist + x];
        }
    }
    this->currSize = sizeHist;
}

void pi2_cv::setRectOfMask(JNIEnv *env, jobject thiz, jint x, jint y, ID_TYPE refMat) {
    Mat m = *reinterpret_cast<Mat*>(refMat);
    maskRect = Rect(x, y, m.cols, m.rows);
    uint8_t * pData = m.data;
    memcpy(arrayMask, pData, m.total()*m.elemSize());
}

void pi2_cv::calcUVC_FrameOfMask(uvc_frame_t *frame, const cv::Rect & maskRect) {
    const size_t xEnd = maskRect.x + maskRect.width;
    const size_t yEnd = maskRect.y + maskRect.height;
    const size_t widhtFrameYU = frame->width * 2;
    const uint8_t * pFrame = reinterpret_cast<const uint8_t *>(frame->data);
    size_t i = 0;
    for(size_t y= maskRect.y; y < yEnd; y++){
        for(size_t x=maskRect.x; x < xEnd; x++){
            arrayFromMask[i] = pFrame[y*widhtFrameYU + 2*x] & arrayMask[i];
            i++;
        }
    }

}

struct HistType vertHist;
struct LearnHType learnHist;
struct PowerHType powerHist;

void pi2_cv::cvProccessing(JNIEnv *env, uvc_frame_t *frame) {

    if (!LIKELY(frame)) {
        LOGW(TAG"bad frame !!!");
        return;
    }

    int position = -1;

    if(!maskRect.empty()){
        Rect testRect = Rect(maskRect);
        calcUVC_FrameOfMask(frame,maskRect);
        vertHist.newVerHistArrayYUYV(arrayFromMask,maskRect);
        if( mode == LEARN) {
            learnHist.setMinValue(vertHist);
        } else {
            powerHist.calcPower(learnHist, vertHist);
            position = powerHist.calcPosition();
        }
    }

    if(pi2_plot::isDisablePlot() == true) {
        pi2_plot::clearAll();
        Rect testRect = Rect(maskRect);
        testRect.y = pi2_plot::heightPreview;
        testRect.x = 0;
        pi2_plot::plotSubGrayArray(arrayFromMask, testRect);
        pi2_plot::plotHist(vertHist, learnHist, powerHist);
        pi2_plot::plotPreviewFrame(frame);
    }
/**/
    env->CallVoidMethod(objectCV, midCV,position);

}

void pi2_cv::setMode(JNIEnv *env, jobject thiz, jint modeWork) {
    if( modeWork==CAPTURE ){
        if( mode != CAPTURE ){
            LOGI("provisioning for capture");
            learnHist.mullArray(1,20);
        }
    }

    if( modeWork==LEARN ){
        if( mode != LEARN ){
            LOGI("reset learnHist");
            learnHist.clearHist(UINT64_MAX);
        }
    }
    mode = modeWork;
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
        midCV = env->GetMethodID(clazz, "plottCV","(I)V");
    } else {
        LOGW(TAG"failed to get object class");
    }
    env->ExceptionClear();
    if (!midCV) {
        LOGE(TAG"Can't find plottCV : %p %p", midCV, clazz);
    }
    LOGI(TAG"succes set startCV! midCV point %p ",midCV);
}
