ifeq ($(findstring ro.wcn=disabled,$(PRODUCT_PROPERTY_OVERRIDES)),)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := BtReverse
LOCAL_CERTIFICATE := platform

LOCAL_JAVA_LIBRARIES := javax.obex telephony-common services
LOCAL_STATIC_JAVA_LIBRARIES := android.bluetooth
LOCAL_STATIC_JAVA_LIBRARIES := com.android.settingslib.bluetooth
LOCAL_STATIC_JAVA_LIBRARIES := com.android.vcard
LOCAL_STATIC_JAVA_LIBRARIES := android.bluetooth.client.pbap

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
endif
