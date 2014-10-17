#include <string.h>
#include <jni.h>

static jboolean hasLisense = false;
extern "C" {

	void Java_com_hellodev_lightme_FlashHelper_changeFlashLight(JNIEnv* env,
			jobject thiz, jboolean isIncreasing) {
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
