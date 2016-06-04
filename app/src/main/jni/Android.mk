LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include C:/Users/abgg/Downloads/OpenCV3-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := img_pro
LOCAL_SRC_FILES := image_processing.cpp
LOCAL_LDLIBS +=  -llog -ldl
LOCAL_C_INCLUDES += $(LOCAL_PATH)

include $(BUILD_SHARED_LIBRARY)
