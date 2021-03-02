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

import androidx.appcompat.app.AppCompatActivity;
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

public class IncisionsStageActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "Incisions";

    public static final double FIRST_INCISION_LENGTH_DEFAULT = 5.0;
    public static final double FIRST_INCISION_ANGLE_DEFAULT = 90.0;
    public static final double SECOND_INCISION_LENGTH_DEFAULT = 3.0;
    public static final double SECOND_INCISION_ANGLE_DEFAULT = 180.0;

    private CameraBridgeViewBase mOpenCvCameraView;
    private LimbusDetectionHough limbusDetectionHough;
//    private ColorMarkersDetectionHuMoments colorMarkersDetectionHuMoments;
    private ColorMarkersDetectionEntropy colorMarkersDetectionEntropy;
    private Mat mRgba;
    private Mat mGray;
    private Mat mValue;
    private Mat mHsv;

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
//        colorMarkersDetectionHuMoments = new ColorMarkersDetectionHuMoments(width, height);
        colorMarkersDetectionEntropy = new ColorMarkersDetectionEntropy(width, height);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mValue = new Mat(height, width, CvType.CV_8UC1);
        mHsv = new Mat(height, width, CvType.CV_8UC3);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mValue.release();
        mHsv.release();
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
//            double zerothAngle = colorMarkersDetectionHuMoments.process(mHsv, limbusCircle);
            double zerothAngle = colorMarkersDetectionEntropy.process(mHsv, limbusCircle);
//            Log.i(TAG, "zerothAngle (red strip): " + zerothAngle);

            Point limbusCenter =  new Point(limbusCircle[0], limbusCircle[1]);
            double limbusRadius = limbusCircle[2];

            // stage-specific overlays
            Overlays.drawIncision(mRgba,
                    limbusCenter,
                    zerothAngle + firstIncisionAngle,
                    limbusRadius,
                    AnatomyHelpers.mmToPixels(limbusRadius, firstIncisionLength),
                    AnatomyHelpers.mmToPixels(limbusRadius, firstIncisionLength)*0.1,
                    new Scalar(0,255,0,255));
            Overlays.drawIncision(mRgba,
                    limbusCenter,
                    zerothAngle + secondIncisionAngle,
                    limbusRadius,
                    AnatomyHelpers.mmToPixels(limbusRadius, secondIncisionLength),
                    AnatomyHelpers.mmToPixels(limbusRadius, secondIncisionLength)*0.1,
                    new Scalar(0,255,0,255));

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
//
//            // clean up
//            bionikoVis.release();
        }

        return mRgba;
    }

}