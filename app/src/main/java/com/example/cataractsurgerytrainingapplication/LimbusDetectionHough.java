package com.example.cataractsurgerytrainingapplication;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class LimbusDetectionHough {
    private Mat gray;
    private Mat circles;
    private final double scale;
    private final int scaledHeight;
    private final int scaledWidth;
    private Mat grayScaled;
    private final Mat whiteScaled;


    public LimbusDetectionHough(int width, int height) {
        gray = new Mat(height, width, CvType.CV_8UC1);
        circles = new Mat();

        scale = 640.0 / (double) Math.max(width, height);
        scaledHeight = (int) Math.round(height * scale);
        scaledWidth = (int) Math.round(width * scale);
        grayScaled = new Mat(scaledHeight, scaledWidth, CvType.CV_8UC1);
        whiteScaled = new Mat(scaledHeight, scaledWidth, CvType.CV_8UC1, new Scalar(255));
    }

    public double[] process(Mat newGray) {
        gray = newGray;
        Imgproc.resize(gray, grayScaled, new Size(scaledWidth, scaledHeight));
        Core.subtract(whiteScaled, grayScaled, grayScaled);
        Imgproc.GaussianBlur(grayScaled, grayScaled, new Size(0,0), 2);
        Imgproc.HoughCircles(grayScaled, circles, Imgproc.HOUGH_GRADIENT,
                1.0, 10.0, 120.0, 40.0);

        if (circles.cols() == 0) {
            return null;
        }

        // TODO: add validation; don't pick the first circle naively
        double[] bestCircle = circles.get(0, 0);
        for (int i = 0; i < 3; i++) {
            bestCircle[i] = bestCircle[i] / scale;
        }

        return bestCircle;
    }
}
