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
import org.opencv.imgproc.Imgproc;

public class CapsulotomyProjectorPrototypeStageActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "CapsulotomyProjectorPrototype";
    public static final double CAPSULOTOMY_DIAMETER_DEFAULT = 5.0;
    public static final double PROJECTOR_VIEW_DIAMETER_DEFAULT = 25.0;
    public static final int TRACKING_PARAMETERS_COUNT = 3;

    private CameraBridgeViewBase mOpenCvCameraView;
    private LimbusDetectionHough limbusDetectionHough;
    private AveragingFilter averagingFilter;
    private Mat trackingParameters;
    private Mat mRgba;
    private Mat mGray;
    private Mat mHsv;
    private Mat mValue;

    private double capsulotomyDiameter;
    private double projectorViewDiameter;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(CapsulotomyProjectorPrototypeStageActivity.this);
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

        capsulotomyDiameter = getIntent().getDoubleExtra("capsulotomyDiameter",
                CAPSULOTOMY_DIAMETER_DEFAULT);
        projectorViewDiameter = getIntent().getDoubleExtra("projectorViewDiameter",
                PROJECTOR_VIEW_DIAMETER_DEFAULT);

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
        averagingFilter = new AveragingFilter(TRACKING_PARAMETERS_COUNT);
        trackingParameters = new Mat(1, TRACKING_PARAMETERS_COUNT, CvType.CV_32F);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mValue = new Mat(height, width, CvType.CV_8UC1);
        mHsv = new Mat(height, width, CvType.CV_8UC3);
    }

    @Override
    public void onCameraViewStopped() {
        trackingParameters.release();
        mRgba.release();
        mGray.release();
        mValue.release();
        mHsv.release();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        Imgproc.cvtColor(mRgba, mHsv, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(mHsv, mHsv, Imgproc.COLOR_RGB2HSV);
        Core.extractChannel(mHsv, mValue, 2);

        double[] limbusCircle = limbusDetectionHough.process(mValue);
        if (limbusCircle != null) {
            trackingParameters.put(0, 0, limbusCircle[0]);
            trackingParameters.put(0, 1, limbusCircle[1]);
            trackingParameters.put(0, 2, limbusCircle[2]);

            averagingFilter.process(trackingParameters, trackingParameters);

            Point limbusCenter =  new Point(trackingParameters.get(0, 0)[0],
                    trackingParameters.get(0, 1)[0]);
            double limbusRadius = trackingParameters.get(0, 2)[0];

            // projector-specific changes
            mRgba.setTo(new Scalar(0,0,0,255));

            // stage-specific overlays
            Overlays.drawCircle(mRgba,
                    limbusCenter,
                    AnatomyHelpers.mmToPixels(limbusRadius, capsulotomyDiameter)/2,
                    new Scalar(0,255,0,255));

            // helper overlays
            Overlays.drawCircle(mRgba,
                    limbusCenter,
                    limbusRadius,
                    new Scalar(0,0,255,255),
                    false);

            // TODO: debug; remove
//            Overlays.drawCircle(mRgba,
//                    limbusCenter,
//                    limbusRadius,
//                    new Scalar(0,255,0,255));
//            Overlays.drawAxis(mRgba, limbusCenter, bionikoAngle, limbusRadius*2,
//                    new Scalar(0,255,0,255));
//            Mat bionikoVis = bionikoDetectionCorrelation.visualize();
//            Overlays.drawVisualization(mRgba, bionikoVis, 0.5);

            // clean up
        }

        return mRgba;
    }
}