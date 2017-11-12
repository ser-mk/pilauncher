//
// Created by echormonov on 12.11.17.
//

#ifndef GAME_PI2_CV_H
#define GAME_PI2_CV_H
#include <libuvc/libuvc.h>
#include <opencv2/opencv.hpp>


class pi2_cv {

    static void test(JNIEnv *env, uvc_frame_t *frame);
    static void callByteBufferFrame(JNIEnv *env, uvc_frame_t *frame);
    static jobject objectCV;
    static jmethodID midCV;

public:

    static void startCV(JNIEnv *env, jobject thiz, jboolean plotiing);

};


struct pi2_plot{

    static void setPlotOption(JNIEnv *env, jobject thiz, jint width, jint height){
        chart = cv:: Mat(height, width,CV_8UC3);
    }

    static cv::Mat chart;
};


#endif //GAME_PI2_CV_H
