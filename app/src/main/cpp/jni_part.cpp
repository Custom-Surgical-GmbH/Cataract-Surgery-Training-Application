#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT void JNICALL
Java_com_example_cataractsurgerytrainingapplication_NativeVideoProcessingDemoActivity_NativeCanny(
        JNIEnv *, jobject, jlong addrGray, jlong addrRgba);

JNIEXPORT void JNICALL
Java_com_example_cataractsurgerytrainingapplication_NativeVideoProcessingDemoActivity_NativeCanny(
        JNIEnv *, jobject, jlong addrGray, jlong addrRgba) {
    Mat &matGray = *(Mat *) addrGray;
    Mat &matRgb = *(Mat *) addrRgba;

    // Perform Canny edge detection
    Mat matGrayCanny;
    Canny(matGray, matGrayCanny, 10, 100);

    // Copy edge image into output
    cvtColor(matGrayCanny, matRgb, COLOR_GRAY2RGB); // BGR
}
}
