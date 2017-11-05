//
// Created by echormonov on 05.11.17.
//

#ifndef GAME_PIPI_CV_H
#define GAME_PIPI_CV_H
#include <libuvc/libuvc.h>


class pi_cv {

public:
	static int setRectMask(jint xsRoi, jint ysRoi, ID_TYPE refMat);
	static int calcPipiChart(ID_TYPE refMatPreview, ID_TYPE refMatChart);

};


#endif //GAME_PIPI_CV_H
