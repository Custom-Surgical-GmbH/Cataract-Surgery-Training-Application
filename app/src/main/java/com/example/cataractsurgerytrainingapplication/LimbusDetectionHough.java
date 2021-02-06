package com.example.cataractsurgerytrainingapplication;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.Collections;

public class LimbusDetectionHough {
    public static final int MAX_CIRCLES_PROCESSED = 10;
    public static final double DESIRED_WIDTH_SIZE = 640.0;

    private Mat gray;
    private Mat circles;
    private final double scale;
    private final int scaledHeight;
    private final int scaledWidth;
    private Mat grayScaled;
    private Mat laplacianScaled;
    private Mat laplacianMask;
    private Mat laplacianMasked;
    private final Mat whiteScaled;


    public LimbusDetectionHough(int width, int height) {
        gray = new Mat(height, width, CvType.CV_8UC1);
        circles = new Mat();

        scale = DESIRED_WIDTH_SIZE / (double) Math.max(width, height);
        scaledHeight = (int) Math.round(height * scale);
        scaledWidth = (int) Math.round(width * scale);
        grayScaled = new Mat(scaledHeight, scaledWidth, CvType.CV_8UC1);
        laplacianScaled = new Mat(scaledHeight, scaledWidth, CvType.CV_16SC1);
        laplacianMasked = new Mat(scaledHeight, scaledWidth, CvType.CV_16SC1);
        laplacianMask = new Mat(scaledHeight, scaledWidth, CvType.CV_8UC1);
        whiteScaled = new Mat(scaledHeight, scaledWidth, CvType.CV_8UC1, new Scalar(255));
    }

    public double[] process(Mat newGray) {
        gray = newGray;
        Imgproc.resize(gray, grayScaled, new Size(scaledWidth, scaledHeight));
        Core.subtract(whiteScaled, grayScaled, grayScaled);
        Imgproc.GaussianBlur(grayScaled, grayScaled, new Size(0,0), 2);
        Imgproc.HoughCircles(grayScaled,
                circles,
                Imgproc.HOUGH_GRADIENT,
                1.0,
                10.0,
                100.0,
                40.0,
                (int) Math.round(Math.min(scaledWidth, scaledHeight) / 8.0),
                (int) Math.round(Math.min(scaledWidth, scaledHeight) / 1.5)
        );

        if (circles.cols() == 0) {
            return null;
        }

        // TODO: add validation; don't pick the first circle naively
        // naive
//        double[] bestCircle = circles.get(0, 0);
//        for (int i = 0; i < 3; i++) {
//            bestCircle[i] = bestCircle[i] / scale;
//        }

        // robust
        Imgproc.Laplacian(grayScaled, laplacianScaled, CvType.CV_16SC1);
        int processedCircles = Math.min(MAX_CIRCLES_PROCESSED, circles.cols());
        double[] currentCircle;
        double[] bestCircle = new double[3];
        double avgLaplacian;
        double maxAvgLaplacian = -1;
        for (int i = 0; i < processedCircles; i++) {
            currentCircle = circles.get(0, i);
            avgLaplacian = compute_avg_laplacian(laplacianScaled, currentCircle);

            if (avgLaplacian > maxAvgLaplacian) {
                maxAvgLaplacian = avgLaplacian;
                bestCircle = currentCircle;
            }
        }

        for (int i = 0; i < 3; i++) {
            bestCircle[i] = bestCircle[i] / scale;
        }

        // clean up
//        circles.release();

        return bestCircle;
    }

    private double compute_avg_laplacian(Mat laplacian, double[] circle) {
        laplacianMask.setTo(new Scalar(0));
        laplacianMasked.setTo(new Scalar(0));
        Imgproc.circle(laplacianMask,
                new Point(circle[0], circle[1]),
                (int) Math.round(circle[2]),
                new Scalar(255),
                2);
        laplacian.copyTo(laplacianMasked, laplacianMask);
        Core.absdiff(laplacianMasked, new Scalar(0), laplacianMasked);

        return (double) Core.sumElems(laplacianMasked).val[0] / (double) Core.countNonZero(laplacianMask);
    }
}
