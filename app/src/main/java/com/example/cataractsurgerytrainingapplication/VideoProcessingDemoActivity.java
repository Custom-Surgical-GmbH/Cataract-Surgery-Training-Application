package com.example.cataractsurgerytrainingapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class VideoProcessingDemoActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "VideoProcessingDemo";

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mGray;
    private Mat mRgba;
    private Mat mCircles;
    private double mScale;
    private int mScaledHeight;
    private int mScaledWidth;
    private Mat mGrayScaled;
    private Mat mWhiteScaled;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(VideoProcessingDemoActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_video_processing_demo);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.OpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // TODO: let the user select
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG, Integer.toString(width) + "x" + Integer.toString(height));
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mCircles = new Mat();

        mScale = 640.0 / (double) Math.max(width, height);
        mScaledHeight = (int) Math.round(height * mScale);
        mScaledWidth = (int) Math.round(width * mScale);
        Log.i(TAG, "Scaled height/width: " + mScaledHeight + "/" + mScaledWidth);
        mGrayScaled = new Mat(mScaledHeight, mScaledWidth, CvType.CV_8UC1);
        mWhiteScaled = new Mat(mScaledHeight, mScaledWidth, CvType.CV_8UC1, new Scalar(255));
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mCircles.release();
        mGrayScaled.release();
        mWhiteScaled.release();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        // copied over from LimbusDetectionActivity
        Imgproc.resize(mGray, mGrayScaled, new Size(mScaledWidth, mScaledHeight));
        Core.subtract(mWhiteScaled, mGrayScaled, mGrayScaled);
        Imgproc.GaussianBlur(mGrayScaled, mGrayScaled, new Size(0,0), 2);
//        Imgproc.HoughCircles(imgHough, circles, Imgproc.HOUGH_GRADIENT,
//                1.0, 10.0, 120.0, 40.0,
//                imgHough.rows() / 10, imgHough.rows() / 2);
        Imgproc.HoughCircles(mGrayScaled, mCircles, Imgproc.HOUGH_GRADIENT,
                1.0, 10.0, 120.0, 40.0);

        // drawing markers on the final image
//        Log.i(TAG, "circles detected:" + mCircles.cols());
        for (int x = 0; x < mCircles.cols(); x++) {
            double[] c = mCircles.get(0, x);

            Point center = new Point(Math.round(c[0] / mScale), Math.round(c[1] / mScale));
            // circle center
            Imgproc.circle(mRgba, center, 1, new Scalar(0,255,0,255), 3, 8, 0 );
            // circle outline
            int radius = (int) Math.round(c[2] / mScale);
            Imgproc.circle(mRgba, center, radius, new Scalar(0,255,0,255), 3, 8, 0 );

            break;
        }

        return mRgba;
    }
}