LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include /home/miroslav/Android/OpenCV/opencv-4.5.1-android-sdk/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := native-lib
LOCAL_SRC_FILES := jni_part.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)