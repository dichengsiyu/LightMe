#include <string.h>
#include <jni.h>

static jboolean hasLisense = false;
extern "C" {
void Java_com_hellodev_lightme_FlashHelper_initNativeLibrary(JNIEnv* env,
		jobject thiz) {
	env->FindClass("com/hellodev/lightme/service/ControlService");
	if (env->ExceptionCheck()) {
		env->ExceptionClear();
	} else {
		hasLisense = true;
	}
}

void Java_com_hellodev_lightme_FlashHelper_changeFlashLight(JNIEnv* env,
		jobject thiz, jboolean isIncreasing) {
	if (hasLisense) {
		jclass cls = env->FindClass("android/hardware/Camera");
		jmethodID mid = env->GetStaticMethodID(cls, "changingFlashLightVolume",
				"(Z)V");
		if (env->ExceptionCheck()) {
			env->ExceptionClear();
		} else {
			env->CallStaticVoidMethod(cls, mid, isIncreasing);
		}
	}
}
}
