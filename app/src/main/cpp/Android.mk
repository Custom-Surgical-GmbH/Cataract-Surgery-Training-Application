LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include /Users/mirek/Documents/Custom_Surgical/Cataract-Surgery-Training-Application/openCVLibrary453/native/jni/OpenCV.mk

LOCAL_MODULE    := native-lib
LOCAL_SRC_FILES := jni_part.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)