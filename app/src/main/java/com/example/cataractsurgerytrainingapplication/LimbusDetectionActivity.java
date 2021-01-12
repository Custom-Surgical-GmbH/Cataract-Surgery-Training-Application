package com.example.cataractsurgerytrainingapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class LimbusDetectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limbus_detection);

        OpenCVLoader.initDebug();
    }

    public void displayLimbusDetectionResult(View v) {
        Mat imgOrig = new Mat();
        Mat imgRes = new Mat();
        Mat imgHough = new Mat();
        Mat circles = new Mat();
        double scale = 0.25;

        try {
            imgOrig = Utils.loadResource(getApplicationContext(), R.drawable.base1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // simple color conversion
        Imgproc.cvtColor(imgOrig, imgOrig, Imgproc.COLOR_RGB2BGRA);

        // Hough transform preprocessing
        Imgproc.cvtColor(imgOrig, imgHough, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.resize(imgHough, imgHough, new Size(0, 0), scale, scale);
        Mat imgWhite = new Mat(imgHough.rows(), imgHough.cols(), imgHough.type(), new Scalar(255));
        Core.subtract(imgWhite, imgHough, imgHough);
        Imgproc.GaussianBlur(imgHough, imgHough, new Size(0,0), 2);
//        Imgproc.HoughCircles(imgHough, circles, Imgproc.HOUGH_GRADIENT,
//                1.0, 10.0, 120.0, 40.0,
//                imgHough.rows() / 10, imgHough.rows() / 2);
        Imgproc.HoughCircles(imgHough, circles, Imgproc.HOUGH_GRADIENT,
                1.0, 10.0, 120.0, 40.0);

        // drawing markers on the final image
        imgRes = imgOrig.clone();
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);

            Point center = new Point(Math.round(c[0] / scale), Math.round(c[1] / scale));
            // circle center
            Imgproc.circle(imgRes, center, 1, new Scalar(0,255,0,255), 3, 8, 0 );
            // circle outline
            int radius = (int) Math.round(c[2] / scale);
            Imgproc.circle(imgRes, center, radius, new Scalar(0,255,0,255), 3, 8, 0 );

            break;
        }

        // creating bitmap to display
        Bitmap bmRes = Bitmap.createBitmap(imgRes.cols(), imgRes.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(imgRes, bmRes);
        ImageView imageView = findViewById(R.id.resultImageView);
        imageView.setImageBitmap(bmRes);
    }
}