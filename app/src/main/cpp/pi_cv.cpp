//
// Created by echormonov on 05.11.17.
//

#include "pi_cv.h"
#include <opencv2/opencv.hpp>

using namespace cv;

static struct {
    Mat Mask;// = new Mat(0, 0, CV_8UC1);
    Rect rect;
} piMask;



int pi_cv::calcPipiChart(ID_TYPE refMatPreview, ID_TYPE refMatChart) {
    const Mat & preview = *reinterpret_cast<Mat*>(refMatPreview);
    const Mat chart = *reinterpret_cast<Mat*>(refMatChart);

    //return 0;
    Mat roiPreview = Mat(preview, piMask.rect);

    Mat rgbRoiPreview = Mat(roiPreview.rows, roiPreview.cols, CV_8UC3);
    cvtColor(roiPreview, rgbRoiPreview, COLOR_YUV2RGB_YUYV);
    rgbRoiPreview.copyTo(chart);
    return 0;
}


int pi_cv::setRectMask(jint xsRoi, jint ysRoi, ID_TYPE refMat) {
    const Mat & mask = *reinterpret_cast<Mat*>(refMat);
    piMask.Mask = mask.clone();
    int rows = mask.rows;
    int cols = mask.cols;
    piMask.rect = Rect(xsRoi, ysRoi, cols, rows);
    return 0;
}