//
// Created by echormonov on 12.11.17.
//

#ifndef GAME_PI2_CV_H
#define GAME_PI2_CV_H
#include <libuvc/libuvc.h>
#include <opencv2/opencv.hpp>
#include "pi2_plot.h"

class pi2_cv {

    enum MODE {
        CAPTURE = 0,
        LEARN,
    };

    enum FRAME_CAPTURE_COMMAND {
      START_TO = 0,
      RETURN_RESULT,
    };

    static int mode;

    static void cvProccessing(JNIEnv *env, uvc_frame_t *frame);
    static jobject objectCV;
    static jmethodID midCV;
    static cv::Rect maskRect;
    static bool enableSingleCaptureFrame;
    static cv::Mat captureFrame;
    static uint8_t arrayFromMask[pi2_plot::sizePreview];
    static uint8_t arrayMask[pi2_plot::sizePreview];
    static void calcUVC_FrameOfMask(uvc_frame_t *frame, const cv::Rect & maskRect);
    static int normilizePosition(const int position);

    static size_t MAX_PULSE_WIDTH;
    static size_t MIN_PULSE_WIDTH;
    static size_t GAP_DECREASE_MASK;

public:

    static bool startCV(JNIEnv *env, jobject thiz);
    static void setRectOfMask(JNIEnv *env, jobject thiz,
                              jint xsRoi, jint ysRoi, ID_TYPE refMat);

    static void setMode(JNIEnv *env, jobject thiz, jint mode);
    static jlong getFrame(JNIEnv *env, jobject thiz, jint mode);
    static void setOptions(JNIEnv *env, jobject thiz, jint MAX_PULSE_WIDTH,
                           jint MIN_PULSE_WIDTH, jint GAP_DECREASE_MASK);
};

#endif //GAME_PI2_CV_H
