//
// Created by echormonov on 12.11.17.
//

#ifndef GAME_PI2_CV_H
#define GAME_PI2_CV_H
#include <libuvc/libuvc.h>
#include <opencv2/opencv.hpp>
#include "pi2_plot.h"

class pi2_cv {

    static void cvProccessing(JNIEnv *env, uvc_frame_t *frame);
    static jobject objectCV;
    static jmethodID midCV;
    static cv::Rect maskRect;
    static uint8_t arrayFromMask[pi2_plot::sizePreview];
    static uint8_t arrayMask[pi2_plot::sizePreview];
    static void calcUVC_FrameOfMask(uvc_frame_t *frame, const cv::Rect & maskRect);

public:

    static void startCV(JNIEnv *env, jobject thiz, jboolean plotiing);
    static void setRectOfMask(JNIEnv *env, jobject thiz,
                              jint xsRoi, jint ysRoi, ID_TYPE refMat);

};

struct HistType{
    static const size_t MAX_WIGTH_HIST = 640;
    uint64 hist[MAX_WIGTH_HIST] = {0};
    size_t currSize = 0;

    void clearHist(uint64 val = 0) {
        memset(hist, val, MAX_WIGTH_HIST * sizeof(uint64));
    }

    void verHistArrayYUYV(uint8_t * data_frame, size_t size_frame){
        const size_t size_data = size_frame/2;
    }
};

#endif //GAME_PI2_CV_H
