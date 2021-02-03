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

public class ToricIOLPositioningStageActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "ToricIOLPositioning";
    public static final double LENS_AXIS_ANGLE_DEFAULT = 90.0;

    private CameraBridgeViewBase mOpenCvCameraView;
    private LimbusDetectionHough limbusDetectionHough;
    private BionikoDetectionCorrelation bionikoDetectionCorrelation;
    private Mat mRgba;
    private Mat mGray;
    private Mat mTest;

    private double lensAxisAngle;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ToricIOLPositioningStageActivity.this);
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

        lensAxisAngle = getIntent().getDoubleExtra("lensAxisAngle",
                LENS_AXIS_ANGLE_DEFAULT);

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
        bionikoDetectionCorrelation = new BionikoDetectionCorrelation(this, width, height);
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
            double bionikoAngle = bionikoDetectionCorrelation.process(mGray, limbusCircle);
            Log.i(TAG, "bionikoAngle: " + bionikoAngle);

            Point limbusCenter =  new Point(limbusCircle[0], limbusCircle[1]);
            double limbusRadius = limbusCircle[2];

            Overlays.drawAxis(mRgba,
                    limbusCenter,
                    bionikoAngle + lensAxisAngle,
                    limbusRadius*2,
                    new Scalar(0,255,0,255));

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