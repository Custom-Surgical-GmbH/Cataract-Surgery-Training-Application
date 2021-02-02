package com.example.cataractsurgerytrainingapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
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
    private static final String TAG = "IncisionsStage";
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
            Log.i(TAG, "bionikoAngle: " + bionikoAngle);

            Overlays.drawCircle(mRgba, limbusCircle, new Scalar(0,255,0,255));
            Overlays.drawAxis(mRgba, new Point(limbusCircle[0], limbusCircle[1]), bionikoAngle,
                    limbusCircle[2]*2, new Scalar(0,255,0,255));

            // TODO: debug; remove
            Mat bionikoVis = bionikoDetectionCorrelation.visualize();
            Overlays.drawVisualization(mRgba, bionikoVis, 0.5);
        }

//        Imgproc.putText(mRgba, Double.toString(firstIncisionLength), new Point(100, 100), Core.FONT_HERSHEY_COMPLEX, 2, new Scalar(0,255,0,255));
//        Imgproc.putText(mRgba, Double.toString(firstIncisionAngle), new Point(100, 150), Core.FONT_HERSHEY_COMPLEX, 2, new Scalar(0,255,0,255));
//        Imgproc.putText(mRgba, Double.toString(secondIncisionLength), new Point(100, 200), Core.FONT_HERSHEY_COMPLEX, 2, new Scalar(0,255,0,255));
//        Imgproc.putText(mRgba, Double.toString(secondIncisionAngle), new Point(100, 250), Core.FONT_HERSHEY_COMPLEX, 2, new Scalar(0,255,0,255));

//        Bitmap b = Bitmap.createBitmap(500, 100, Bitmap.Config.ARGB_8888);
//        b.eraseColor(Color.WHITE);
//        Canvas c = new Canvas(b);
//        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
//        p.setColor(Color.BLACK);
//        p.setTextSize((float) (100*1.35));
//        p.setTypeface(ResourcesCompat.getFont(this, R.font.century_gothic_bold));
//        p.setTextAlign(Paint.Align.LEFT);
//        Rect bounds = new Rect();
//        String text = "BIONIKO";
//        p.getTextBounds(text,0, text.length(), bounds);
////        Log.i(TAG, bounds.toString());
//        Log.i(TAG, String.valueOf(bounds.width()) + "x" + String.valueOf(bounds.height()));
//        c.drawText(text, 0, 100, p);
//
//        Mat m = new Mat();
//        Utils.bitmapToMat(b, m);
//
//        m.copyTo(mRgba.rowRange(0, m.rows()).colRange(0, m.cols()));



        return mRgba;
    }
}