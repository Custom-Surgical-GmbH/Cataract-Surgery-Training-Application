package com.example.cataractsurgerytrainingapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TimingLogger;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.core.content.res.ResourcesCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class IncisionsStageActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "Incisions";

    public static final double FIRST_INCISION_LENGTH_DEFAULT = 5.0;
    public static final double FIRST_INCISION_ANGLE_DEFAULT = 90.0;
    public static final double SECOND_INCISION_LENGTH_DEFAULT = 3.0;
    public static final double SECOND_INCISION_ANGLE_DEFAULT = 180.0;

    private CameraBridgeViewBase mOpenCvCameraView;
    private LimbusDetectionHough limbusDetectionHough;
    private BionikoDetectionCorrelation bionikoDetectionCorrelation;
    private Mat mRgba;
    private Mat mGray;
    private Mat mTest;

    private double firstIncisionLength;
    private double firstIncisionAngle;
    private double secondIncisionLength;
    private double secondIncisionAngle;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(IncisionsStageActivity.this);
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

        firstIncisionLength = getIntent().getDoubleExtra("firstIncisionLength",
                FIRST_INCISION_LENGTH_DEFAULT);
        firstIncisionAngle = getIntent().getDoubleExtra("firstIncisionAngle",
                FIRST_INCISION_ANGLE_DEFAULT);
        secondIncisionLength = getIntent().getDoubleExtra("secondIncisionLength",
                SECOND_INCISION_LENGTH_DEFAULT);
        secondIncisionAngle = getIntent().getDoubleExtra("secondIncisionAngle",
                SECOND_INCISION_ANGLE_DEFAULT);

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
//            Log.i(TAG, "bionikoAngle: " + bionikoAngle);

            Point limbusCenter =  new Point(limbusCircle[0], limbusCircle[1]);
            double limbusRadius = limbusCircle[2];

            Overlays.drawIncision(mRgba,
                    limbusCenter,
                    bionikoAngle + firstIncisionAngle,
                    limbusRadius,
                    AnatomyHelpers.mmToPixels(limbusRadius, firstIncisionLength),
                    AnatomyHelpers.mmToPixels(limbusRadius, firstIncisionLength)*0.1,
                    new Scalar(0,255,0,255));
            Overlays.drawIncision(mRgba,
                    limbusCenter,
                    bionikoAngle + secondIncisionAngle,
                    limbusRadius,
                    AnatomyHelpers.mmToPixels(limbusRadius, secondIncisionLength),
                    AnatomyHelpers.mmToPixels(limbusRadius, secondIncisionLength)*0.1,
                    new Scalar(0,255,0,255));

            // TODO: debug; remove
            Overlays.drawCircle(mRgba, limbusCenter, limbusRadius, new Scalar(0,255,0,255));
            Overlays.drawAxis(mRgba, limbusCenter, bionikoAngle, limbusRadius*2,
                    new Scalar(0,255,0,255));
            Mat bionikoVis = bionikoDetectionCorrelation.visualize();
            Overlays.drawVisualization(mRgba, bionikoVis, 0.5);
        }

        return mRgba;
    }
}