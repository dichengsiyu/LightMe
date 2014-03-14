 LOCAL_PATH := $(call my-dir)
 LOCAL_CPP_EXTENSION := .cpp
        include $(CLEAR_VARS)
        LOCAL_MODULE    := hellodev
        LOCAL_SRC_FILES := hellodev.cpp
        include $(BUILD_SHARED_LIBRARY)