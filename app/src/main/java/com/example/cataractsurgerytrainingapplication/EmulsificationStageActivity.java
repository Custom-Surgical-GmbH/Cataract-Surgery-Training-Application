package com.example.cataractsurgerytrainingapplication;

import androidx.appcompat.app.AppCompatActivity;

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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class EmulsificationStageActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "Emulsification";
    public static final double SAFE_ZONE_DIAMETER_DEFAULT = 5.0;

    private CameraBridgeViewBase mOpenCvCameraView;
    private LimbusDetectionHough limbusDetectionHough;
    private Mat mRgba;
    private Mat mGray;
    private Mat mTest;

    private double safeZoneDiameter;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(EmulsificationStageActivity.this);
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

        setContentView(R.layout.activity_incisions_stage);

        safeZoneDiameter = getIntent().getDoubleExtra("safeZoneDiameter",
                SAFE_ZONE_DIAMETER_DEFAULT);

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
//        Log.i(TAG, "Scaled height/width: " + mScaledHeight + "/" + mScaledWidth);

        limbusDetectionHough = new LimbusDetectionHough(width, height);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mTest = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mTest.release();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        double[] limbusCircle = limbusDetectionHough.process(mGray);
        if (limbusCircle != null) {
                        Point limbusCenter =  new Point(limbusCircle[0], limbusCircle[1]);
            double limbusRadius = limbusCircle[2];

            // stage-specific overlays
            Overlays.drawCircle(mRgba,
                    limbusCenter,
                    AnatomyHelpers.mmToPixels(limbusRadius, safeZoneDiameter)/2,
                    new Scalar(0,255,0,255));

            // helper overlays
            Overlays.drawCircle(mRgba,
                    limbusCenter,
                    limbusRadius,
                    new Scalar(0,0,255,255));

            // TODO: debug; remove
//            Overlays.drawCircle(mRgba, limbusCenter, limbusRadius, new Scalar(0,255,0,255));
//            Overlays.drawAxis(mRgba, limbusCenter, bionikoAngle, limbusRadius*2,
//                    new Scalar(0,255,0,255));
//            Mat bionikoVis = bionikoDetectionCorrelation.visualize();
//            Overlays.drawVisualization(mRgba, bionikoVis, 0.5);
        }

        return mRgba;
    }
}