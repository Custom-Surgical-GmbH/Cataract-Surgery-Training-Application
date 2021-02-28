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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ToricIOLPositioningStageActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "ToricIOLPositioning";
    public static final double LENS_AXIS_ANGLE_DEFAULT = 90.0;
    public static final double LENS_AXIS_SHIFT_P = 0.05;

    private CameraBridgeViewBase mOpenCvCameraView;
    private LimbusDetectionHough limbusDetectionHough;
//    private BionikoDetectionCorrelation bionikoDetectionCorrelation;
    private ColorMarkersDetectionHuMoments colorMarkersDetectionHuMoments;
    private Mat mRgba;
    private Mat mRgb;
    private Mat mHsv;
    private Mat mGray;
    private Mat mValue;

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

        setContentView(R.layout.activity_video_processing);

        lensAxisAngle = getIntent().getDoubleExtra("lensAxisAngle",
                LENS_AXIS_ANGLE_DEFAULT);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.OpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCameraPermissionGranted();
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
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
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
//        bionikoDetectionCorrelation = new BionikoDetectionCorrelation(this, width, height);
        colorMarkersDetectionHuMoments = new ColorMarkersDetectionHuMoments(width, height);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgb = new Mat(height, width, CvType.CV_8UC3);
        mHsv = new Mat(height, width, CvType.CV_8UC3);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mValue = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mRgb.release();
        mHsv.release();
        mGray.release();
        mValue.release();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        Imgproc.cvtColor(mRgba, mRgb, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(mRgb, mHsv, Imgproc.COLOR_RGB2HSV);
        Core.extractChannel(mHsv, mValue, 2);

        double[] limbusCircle = limbusDetectionHough.process(mGray);
        if (limbusCircle != null) {
//            double bionikoAngle = bionikoDetectionCorrelation.process(mGray, mValue, limbusCircle);
//            Log.i(TAG, "bionikoAngle: " + bionikoAngle);
            double zerothAngle = colorMarkersDetectionHuMoments.process(mHsv, limbusCircle);
            Log.i(TAG, "zerothAngle (red strip): " + zerothAngle);

            Point limbusCenter =  new Point(limbusCircle[0], limbusCircle[1]);
            double limbusRadius = limbusCircle[2];

            // stage-specific overlays
            Overlays.drawAxis(mRgba,
                    limbusCenter,
                    zerothAngle + lensAxisAngle,
                    limbusRadius*2,
                    new Scalar(0,255,0,255));
            Overlays.drawAxis(mRgba,
                    limbusCenter,
                    zerothAngle + lensAxisAngle,
                    limbusRadius*2,
                    new Scalar(0,255,0,255),
                    limbusRadius*LENS_AXIS_SHIFT_P);
            Overlays.drawAxis(mRgba,
                    limbusCenter,
                    zerothAngle + lensAxisAngle,
                    limbusRadius*2,
                    new Scalar(0,255,0,255),
                    -limbusRadius*LENS_AXIS_SHIFT_P);

            // helper overlays
            Overlays.drawAxis(mRgba,
                    limbusCenter,
                    zerothAngle,
                    limbusRadius*2,
                    new Scalar(0,0,255,255));

            // TODO: debug; remove
//            Overlays.drawCircle(mRgba, limbusCenter, limbusRadius, new Scalar(0,255,0,255));
//            Overlays.drawAxis(mRgba, limbusCenter, bionikoAngle, limbusRadius*2,
//                    new Scalar(0,255,0,255));
//            Mat bionikoVis = bionikoDetectionCorrelation.visualize();
//            Overlays.drawVisualization(mRgba, bionikoVis, 0.5);
            Mat colorMarkersVis = colorMarkersDetectionHuMoments.visualize();
            Overlays.drawVisualization(mRgba, colorMarkersVis, 0.5);
        }

        return mRgba;
    }
}