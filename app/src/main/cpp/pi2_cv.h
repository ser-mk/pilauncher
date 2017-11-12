//
// Created by echormonov on 12.11.17.
//

#ifndef GAME_PI2_CV_H
#define GAME_PI2_CV_H
#include <libuvc/libuvc.h>


class pi2_cv {

    static void test(JNIEnv *env, uvc_frame_t *in);

public:

    static void startCV(JNIEnv *env, jobject thiz, jboolean plotiing);

};


#endif //GAME_PI2_CV_H
