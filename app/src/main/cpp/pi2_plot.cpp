//
// Created by ser on 13.11.17.
//

#include "pi2_plot.h"
using namespace cv;

Mat pi2_plot::chart;
const Rect pi2_plot::rect = Rect(0,0, pi2_plot::widthPreview, pi2_plot::heightPreview);
Mat pi2_plot::grayCapture = Mat(pi2_plot::heightPreview,pi2_plot::widthPreview,CV_8UC1);


void pi2_plot::plotSubGrayArray(uint8_t *grayArray, cv::Rect rectPlot) {
    Mat subMat = Mat(pi2_plot::chart(rectPlot));
    Mat matOfArray = Mat(rectPlot.height, rectPlot.width, CV_8UC1, grayArray);
    cvtColor(matOfArray, subMat, COLOR_GRAY2RGB);
}

void pi2_plot::plotPreviewFrameFast(uvc_frame_t *frame) {
    uint8_t * pMat = pi2_plot::chart.data;
    const uint8_t * pFrame = reinterpret_cast<uint8_t *>(frame->data);
    const size_t size_frame = (frame->data_bytes)/2;
    for(size_t i=0; i < size_frame; i++){
        pMat[3*i + 1] = pFrame[2*i];
    }
}

void pi2_plot::plotPreviewFrame(uvc_frame_t *frame) {
    Mat frameMat = Mat(frame->height, frame->width, CV_8UC2, frame->data);
    Rect rectFrame = Rect(0, 0, frame->width, frame->height);
    Mat subSubPreview = Mat(pi2_plot::chart(rectFrame));
    cvtColor(frameMat, subSubPreview, COLOR_YUV2RGB_YUYV);
}
