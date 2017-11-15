//
// Created by ser on 13.11.17.
//

#ifndef PIPI_STADALONE_PI2_PLOT_H
#define PIPI_STADALONE_PI2_PLOT_H
#include <opencv2/opencv.hpp>
#include <libuvc/libuvc.h>
#include "HistType.h"

struct pi2_plot{

    static void setPlotOption(JNIEnv *env, jobject thiz, ID_TYPE refMat){
        chart = *reinterpret_cast<cv::Mat*>(refMat);
    }

    static cv::Mat chart;
    static cv::Mat grayCapture;
    static const size_t widthPreview = 640;
    static const size_t heightPreview = 480;
    static const size_t sizePreview = widthPreview * heightPreview;
    static size_t getSizePreview() {
        return sizePreview;
    }
    static const cv::Rect rect;

    static cv::Mat getSubPreviewMat(){
        return cv::Mat(pi2_plot::chart( pi2_plot::rect));
    }
    static cv::Mat getSubPlotMatAfterPreview(cv::Rect rectPlot){
        rectPlot.y += heightPreview;
        return cv::Mat(pi2_plot::chart(rectPlot));
    }

    static void clearAll(){
        chart.setTo(0);
    }

    static void plotSubGrayArray(uint8_t * grayArray, cv::Rect rectPlot);
    static void plotPreviewFrameFast(uvc_frame_t *frame);
    static void plotPreviewFrame(uvc_frame_t *frame);

    static void plotHist(HistType &vh, const HistType &lh, HistType &ph);

    static bool disablePlot;

    static bool isDisablePlot() {
        return disablePlot;
    }

    static void setDisablePlot(const jboolean disable){
        disablePlot = disable;
    }
};


#endif //PIPI_STADALONE_PI2_PLOT_H
