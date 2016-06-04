//
// Created by ABGG on 01/06/2016.
//

#include <jni.h>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <android/log.h>

#include <sstream>
#include <iostream>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>


#define LOG_TAG "Img_Proc"
#define LOGI(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

extern "C"
{

    //for computing brightness  com.abggcv.mycameraapp
    JNIEXPORT void JNICALL Java_com_abggcv_mycameraapp_CameraPreview_ComputeBrightness(JNIEnv* env, jobject thiz,
                                                              jint width, jint height, jbyteArray p_data, jstring jFilePath);


    JNIEXPORT void JNICALL Java_com_abggcv_mycameraapp_TouchActivity_WriteBrightnessToYAML(JNIEnv* env, jobject thiz,
                                                                                            jstring jFilePath);

    //vector to store brightness values for different images or frames
    vector<double> brightnessValues;

    //function to convert byte array to opencv mat and then compute brightness as mean pixel value for green channel
    // width and height correspond to number of columns and rows of the frame so that byte array can be converted to opencv mat of
    // corresponding size. jFilePath could be skipped if the OpenCV mat is not saved to file. It contains the file path where OpenCV
    // mat can be saved on the mobile device
    JNIEXPORT void JNICALL Java_com_abggcv_mycameraapp_CameraPreview_ComputeBrightness(JNIEnv* env, jobject thiz,
                                                                  jint width, jint height, jbyteArray p_data, jstring jFilePath)
    {



        jbyte* _p_data= env->GetByteArrayElements(p_data, 0);

        jsize lengthOfArray = env->GetArrayLength(p_data);

        LOGI("length of array: %d", lengthOfArray);

        if(lengthOfArray < 1)
            LOGI("empty bytearray");
        //convert to opencv mat
        Mat mdata(height, width, CV_8UC4, (unsigned char *)_p_data);

        LOGI("image size: %d", width*height);
        LOGI("image width: %d", width);
        LOGI("image height: %d", height);

        Mat tmp_mat= imdecode(mdata,1); //possibly in RGB format

        //release bytes
        env->ReleaseByteArrayElements(p_data, _p_data, 0);

        /*
        //filename to save to test images are correctly converted to opencv mat -- not required for final code but keep for debugging
        //in case the byte array is different
        const char *fnameptr = env->GetStringUTFChars(jFilePath, NULL);

        string stdFileName(fnameptr);
        imwrite(stdFileName, tmp_mat);
        //LOGD("Image saved ");
        //env->ReleaseStringUTFChars(jFilePath, fnameptr);
        */

        //find mean pixel values for all channels separately
        Scalar meanPxl = mean(tmp_mat);

        //store mean pixel value of green color into vector
        brightnessValues.push_back(meanPxl.val[1]);

        //print brightness value --- might not be relavant but can be used to check brightness values printed to yaml file
        LOGI("Image brightness: %f", meanPxl.val[1]);

    }


    //function to write brightness values to yaml file --- input required jFilePath is String containing path to yaml file including
    // name of file and extension .yaml where yaml file can be written
    JNIEXPORT void JNICALL Java_com_abggcv_mycameraapp_TouchActivity_WriteBrightnessToYAML(JNIEnv* env, jobject thiz,
                                                                                                jstring jFilePath)
    {
        //filename to save to
        const char *fnameptr = env->GetStringUTFChars(jFilePath, NULL);

        string yamlFileName(fnameptr);

        if(!brightnessValues.empty())
        {
            FileStorage fs(yamlFileName, FileStorage::WRITE);

            fs << "brightnessValues" << brightnessValues;
            fs.release();

            LOGD("Brightness values written to yaml file: %s", fnameptr);
            //LOGD(yamlFileName);
        }
        else
            LOGD("No brightness values found to write to yaml file");

        //release char pointer
        env->ReleaseStringUTFChars(jFilePath, fnameptr);
    }

}