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

static const size_t MAX_WIGTH_HIST = 640;
static const size_t NUM_CHANNEL = 2;

struct HistType{
    uint64 hist[MAX_WIGTH_HIST] = {0};
    int normHist[MAX_WIGTH_HIST] = {0};
    size_t size = 0;
    uint64 maxValue = 0;

    void clearHist(){
        memset(hist,0,MAX_WIGTH_HIST* sizeof(uint64));
        maxValue = 0;
    }

    size_t verHistForMat(const Mat &mat){
        const int matRows = mat.rows;
        const int matCols = mat.cols;
        clearHist();
        const uint8_t * pMat = mat.data;
        for(size_t row= 0; row < matRows; row++ ){
            for(size_t col= 0; col < matCols; col++){
                hist[col] += pMat[row * matCols + col];
            }
        }
        size = static_cast<size_t >(matCols);
        return  size;
    }

    uint64 getMaxValue(){
        maxValue = 0;
        for(int i= 0; i < size; i++){
            const uint64 val = hist[i];
            if( maxValue < val){
                maxValue = val;
            }
        }
        return maxValue;
    }

    void normalize(const int chartRow){
        const uint64 mv = getMaxValue();
        for(size_t i=0; i < size; i++){
            normHist[i] = static_cast<int>((chartRow * hist[i])/mv);
        }
    }

    void plot2MatRGB(const Mat & mat){
        const int matRows = mat.rows;
        const int matCols = mat.cols;
        const Scalar colorPoints(255,0,0);
        for( int i = 1; i < this->size; i++ ){
            cv:line(mat,
                    Point(i-1, normHist[i-1]),
                    Point(i, normHist[i]),
                    colorPoints,
                    2,8,0);
        }
    }


};

struct HistType currHist;


void clear3UMat(const Mat & mat){
    const int matRows = mat.rows;
    const int matCols = mat.cols;
    uint8_t * pMat = mat.data;
    const size_t size = matRows*matCols*3;
    for(size_t i=0; i < size; i++ ){
        pMat[i] = 0;
    }
}


static Mat channels[NUM_CHANNEL];

int pi_cv::calcPipiChart(ID_TYPE refMatPreview, ID_TYPE refMatChart) {
    const Mat & preview = *reinterpret_cast<Mat*>(refMatPreview);
    const Mat & chart = *reinterpret_cast<Mat*>(refMatChart);
    //return 0;
    Mat roiPreview = Mat(preview, piMask.rect);
#if 0
    Mat rgbRoiPreview = Mat(roiPreview.rows, roiPreview.cols, CV_8UC3);
    cvtColor(roiPreview, rgbRoiPreview, COLOR_YUV2RGB_YUYV);
    Rect rect = Rect(0,0, piMask.rect.width, piMask.rect.height);

    //Mat subChart = Mat(chart( piMask.rect));
    Mat subChart = Mat(chart( rect));
    rgbRoiPreview.copyTo(subChart
            ,piMask.Mask
    );
#endif
    clear3UMat(chart);
    split(roiPreview,channels);
    const Mat & samplesMat = channels[0];
#if 1
    Mat rgbRoiPreview = Mat(roiPreview.rows, roiPreview.cols, CV_8UC3);
    cvtColor(samplesMat, rgbRoiPreview, COLOR_GRAY2RGB);
    Rect rect = Rect(0,0, piMask.rect.width, piMask.rect.height);
    Mat subChart = Mat(chart( rect));
    rgbRoiPreview.copyTo(subChart, piMask.Mask);
#endif

    currHist.verHistForMat(samplesMat);
    const int rowChart = chart.rows - chart.rows/10;
    currHist.normalize(rowChart);
    currHist.plot2MatRGB(chart);


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