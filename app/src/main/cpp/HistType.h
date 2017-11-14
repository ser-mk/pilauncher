//
// Created by ser on 14.11.17.
//

#ifndef PIPI_STADALONE_HISTTYPE_H
#define PIPI_STADALONE_HISTTYPE_H
#include <opencv2/opencv.hpp>
struct HistType{
    static const size_t MAX_WIGTH_HIST = 640;
    uint64 hist[MAX_WIGTH_HIST] = {0};
    size_t currSize = 0;

    void clearHist(uint64 val = 0) {
        memset(hist, val, MAX_WIGTH_HIST * sizeof(uint64));
    }

    void newVerHistArrayYUYV(const uint8_t *array, const cv::Rect &maskRect);
};


struct LearnHType: HistType {
    void setMinValue(const HistType & compHist){
        for(size_t i = 0; i < compHist.currSize; i++){
            uint64 val = compHist.hist[i];
            if(this->hist[i] > val){
                this->hist[i] = val;
            }
        }
        this->currSize = compHist.currSize;
    }
    void mullArray(const uint64 factor, const uint64 denominator){
        for(size_t i=0; i<this->currSize; i++){
            hist[i] -= (hist[i]*factor)/denominator;
        }
    }
};

struct PowerHType: HistType {
    void calcPower(const LearnHType & learn, const HistType & signal){
        this->clearHist();
        for(size_t i=1; i < signal.currSize; i++){
            int dif = static_cast<int>(learn.hist[i] - signal.hist[i]);
            if(dif > 0) {
                this->hist[i] = dif;
                this->hist[i] += this->hist[i - 1];
            }
        }
        this->currSize = signal.currSize;
    }

    int calcPosition(){
        uint64 mv = 0;
        int position = -1;
        for(int i=0; i < this->currSize; i++){
            uint64 val = hist[i];
            if(val > mv){
                mv = val;
                position = i;
            }
        }
        return position;
    }
};
#endif //PIPI_STADALONE_HISTTYPE_H
