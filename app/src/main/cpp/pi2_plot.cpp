//
// Created by ser on 13.11.17.
//

#include "pi2_plot.h"


using namespace cv;

Mat pi2_plot::chart;
const Rect pi2_plot::rect = Rect(0,0, pi2_plot::widthPreview, pi2_plot::heightPreview);
Mat pi2_plot::grayCapture = Mat(pi2_plot::heightPreview,pi2_plot::widthPreview,CV_8UC1);
bool pi2_plot::disablePlot = false;

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
    Mat subMatPreview = Mat(pi2_plot::chart(rectFrame));
    cvtColor(frameMat, subMatPreview, COLOR_YUV2RGB_YUYV);
}


static uint64 getMaxValueOfHist(const HistType &vh){
    uint64 maxValue = 0;
    for(int i= 0; i < vh.currSize; i++){
        const uint64 val = vh.hist[i];
        if( maxValue < val){
            maxValue = val;
        }
    }
    return maxValue;
}

void normalizeHist(HistType &vh,const size_t maxHeight, const uint64 maxValue){
    for(size_t i=0; i < vh.currSize; i++){
        vh.hist[i] = (maxHeight * vh.hist[i])/maxValue;
    }
}

#define LINE_OPTIONS 2,8,0

void pi2_plot::plotHist(HistType &vh, const HistType &lh, HistType &ph) {
    size_t heightChart = chart.rows - pi2_plot::heightPreview;
    heightChart -= heightChart/20;
    const uint64 maxVH = getMaxValueOfHist(vh);
    if(maxVH == 0){
        return;
    }
    normalizeHist(vh, heightChart, maxVH);
    const Scalar redPoint(255,0,0);
    for( int i = 1; i < vh.currSize; i++ ){
        cv::line(chart,
                Point(i-1, vh.hist[i-1] + pi2_plot::heightPreview),
                Point(i, vh.hist[i] + pi2_plot::heightPreview),
                redPoint,
                 LINE_OPTIONS
        );
    }

    HistType copyHist;
    for(size_t i=0; i< lh.currSize; i++){
        copyHist.hist[i] = lh.hist[i];
    }
    copyHist.currSize = lh.currSize;
    normalizeHist(copyHist, heightChart, maxVH);
    const Scalar greenPoints(0,255,0);
    for( int i = 1; i < copyHist.currSize; i++ ){
        cv::line(chart,
                Point(i-1, copyHist.hist[i-1] + pi2_plot::heightPreview),
                Point(i, copyHist.hist[i] + pi2_plot::heightPreview),
                greenPoints,
                 LINE_OPTIONS
        );
    }

    const uint64 maxPH = getMaxValueOfHist(ph);
    if(maxPH == 0){
        return;
    }
    normalizeHist(ph, heightChart, maxPH);
    const Scalar bluePoints(0,0,255);
    for( int i = 1; i < ph.currSize; i++ ){
        cv::line(chart,
                 Point(i-1, ph.hist[i-1] + pi2_plot::heightPreview),
                 Point(i, ph.hist[i] + pi2_plot::heightPreview),
                 bluePoints,
                 LINE_OPTIONS
        );
    }

    int levelPower = 0;
    Scalar levelColor;
    if(maxPH > maxVH){
        levelPower = (maxVH * heightChart) / maxPH;
        levelColor = Scalar(255,0,255);
    } else {
        levelPower = (maxPH * heightChart) / maxVH;
        levelColor = Scalar(0,255,255);
    }

    cv::line(chart,
             Point(0, levelPower + pi2_plot::heightPreview),
             Point(ph.currSize-1, levelPower + pi2_plot::heightPreview),
             levelColor,
             LINE_OPTIONS
    );
}
