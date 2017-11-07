//
// Created by echormonov on 05.11.17.
//

#ifndef GAME_PIPI_CV_H
#define GAME_PIPI_CV_H
#include <libuvc/libuvc.h>


class pi_cv {

    static bool learn;// = false;
    static void resetLearnHist();
    static void setupLearnHist();
public:
	static int setRectMask(jint xsRoi, jint ysRoi, ID_TYPE refMat);
	static int calcPipiChart(ID_TYPE refMatPreview, ID_TYPE refMatChart);
    static void setLearnEnable(bool enable){
        const bool reset = (!learn) && enable;
        if(reset){
            resetLearnHist();
        }
        const bool setup = learn && (!enable);
        if(setup){
            setupLearnHist();
        }
        learn = enable;
    }
    static bool getLearnEnable(){ return learn; }
};


#endif //GAME_PIPI_CV_H
