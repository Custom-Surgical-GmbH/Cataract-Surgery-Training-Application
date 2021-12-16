LOCAL_PATH := $(call my-dir)
ROOT_PATH := $(realpath ../)

include $(CLEAR_VARS)

include $(ROOT_PATH)/openCVLibrary453/native/jni/OpenCV.mk

LOCAL_MODULE    := native-lib
LOCAL_SRC_FILES := jni_part.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)