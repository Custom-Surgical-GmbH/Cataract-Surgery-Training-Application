package com.example.cataractsurgerytrainingapplication;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ColorMarkersDetectionEntropy {
    public static final String TAG = "ColorMarkerDetection";

    public static final Scalar RED_LOWER_THRESHOLD_1 = new Scalar(150, 90, 60);
    public static final Scalar RED_UPPER_THRESHOLD_1 = new Scalar(179, 240, 230);
    public static final Scalar RED_LOWER_THRESHOLD_2 = new Scalar(0, 90, 60);
    public static final Scalar RED_UPPER_THRESHOLD_2 = new Scalar(15, 240, 230);
    public static final Scalar GREEN_LOWER_THRESHOLD = new Scalar(40, 40, 75);
    public static final Scalar GREEN_UPPER_THRESHOLD = new Scalar(100, 240, 230);
    public static final Scalar BLUE_LOWER_THRESHOLD = new Scalar(110, 25, 75);
    public static final Scalar BLUE_UPPER_THRESHOLD = new Scalar(160, 165, 190);

    public static final double SCLERA_TO_LIMBUS_RATIO = 1.7;
    public static final int POLAR_SCLERA_RESOLUTION = 43;

    public static final int POLAR_ANGLE_RESOLUTION = 720;
    public static final int POLAR_ANGLE_SPLIT = 180;
    public static final int POLAR_RADIUS_RESOLUTION = 115;
    public static final int POLAR_ENTROPY_NEIGHBORS = 20;

    public static final double ENTROPY_EPS = Math.log(0.8);

    private Mat polarEntropyFilter;
    private Mat polarEntropyMask;
    private Core.MinMaxLocResult minMaxLocResult;

    private Mat hsv;
    private Mat hsvPolarRotated;
    private Mat hsvPolar;
    private Mat hsvPolarPlanarSclera;
    private Mat greenPolar;

    private Mat redPolar;
    private Mat bluePolar;
    private Mat colorPolarHelper1;
    private Mat colorPolarHelper2;
    private Mat stackedPolar;
    private Mat stackedPolarAugFloat1;
    private Mat stackedPolarAugFloat2;
    private Mat stackedPolarAugReducedFloat1;
    private Mat stackedPolarAugReducedFloat2;

    public ColorMarkersDetectionEntropy(int width, int height) {
        hsv = new Mat(height, width, CvType.CV_8UC3);
        hsvPolarRotated = new Mat(POLAR_ANGLE_RESOLUTION, POLAR_RADIUS_RESOLUTION, CvType.CV_8UC3);
        hsvPolar = new Mat(POLAR_RADIUS_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC3);
        hsvPolarPlanarSclera = new Mat(POLAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC3);

        greenPolar = new Mat(POLAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        redPolar = new Mat(POLAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        bluePolar = new Mat(POLAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        colorPolarHelper1 = new Mat(POLAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        colorPolarHelper2 = new Mat(POLAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);

        polarEntropyFilter = new Mat(1,
                POLAR_ENTROPY_NEIGHBORS + 1,
                CvType.CV_16F,
                new Scalar(1.0));
        polarEntropyMask = new Mat(1,
                POLAR_ANGLE_RESOLUTION+POLAR_ENTROPY_NEIGHBORS,
                CvType.CV_8UC1,
                new Scalar(0));
        polarEntropyMask.colRange(POLAR_ENTROPY_NEIGHBORS/2 + 1,
                polarEntropyMask.width() - POLAR_ENTROPY_NEIGHBORS/2).setTo(new Scalar(255));

        stackedPolar = new Mat(3*POLAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        stackedPolarAugFloat1 = new Mat(3*POLAR_SCLERA_RESOLUTION,
                POLAR_ANGLE_RESOLUTION+POLAR_ENTROPY_NEIGHBORS,
                CvType.CV_32F);
        stackedPolarAugFloat2 = new Mat(3*POLAR_SCLERA_RESOLUTION,
                POLAR_ANGLE_RESOLUTION+POLAR_ENTROPY_NEIGHBORS,
                CvType.CV_32F);
        stackedPolarAugReducedFloat1 = new Mat(1,
                POLAR_ANGLE_RESOLUTION+POLAR_ENTROPY_NEIGHBORS,
                CvType.CV_32F);
        stackedPolarAugReducedFloat2 = new Mat(1,
                POLAR_ANGLE_RESOLUTION+POLAR_ENTROPY_NEIGHBORS,
                CvType.CV_32F);
    }

    public double process(Mat newHsv, double[] limbusCircle) {
        hsv = newHsv;
        Imgproc.warpPolar(
                hsv,
                hsvPolarRotated,
                new Size(POLAR_RADIUS_RESOLUTION, POLAR_ANGLE_RESOLUTION),
                new Point(limbusCircle[0], limbusCircle[1]),
                SCLERA_TO_LIMBUS_RATIO*limbusCircle[2],
                Imgproc.WARP_POLAR_LINEAR + Imgproc.WARP_FILL_OUTLIERS
        );
        Core.rotate(hsvPolarRotated, hsvPolar, Core.ROTATE_90_CLOCKWISE);
        hsvPolar.rowRange(
                POLAR_RADIUS_RESOLUTION - POLAR_SCLERA_RESOLUTION, POLAR_RADIUS_RESOLUTION
        ).copyTo(hsvPolarPlanarSclera);

        segmentColor(hsvPolarPlanarSclera, redPolar, "red");
        segmentColor(hsvPolarPlanarSclera, greenPolar, "green");
        segmentColor(hsvPolarPlanarSclera, bluePolar, "blue");

        stackedPolar.setTo(new Scalar(0));
        redPolar.copyTo(
                stackedPolar
                        .rowRange(0, POLAR_SCLERA_RESOLUTION));
        bluePolar.colRange(0, 1*POLAR_ANGLE_SPLIT).copyTo(
                stackedPolar
                        .rowRange(1*POLAR_SCLERA_RESOLUTION, 2*POLAR_SCLERA_RESOLUTION)
                        .colRange(POLAR_ANGLE_RESOLUTION-1*POLAR_ANGLE_SPLIT, POLAR_ANGLE_RESOLUTION));
        bluePolar.colRange(1*POLAR_ANGLE_SPLIT, POLAR_ANGLE_RESOLUTION).copyTo(
                stackedPolar
                        .rowRange(1*POLAR_SCLERA_RESOLUTION, 2*POLAR_SCLERA_RESOLUTION)
                        .colRange(0, POLAR_ANGLE_RESOLUTION-1*POLAR_ANGLE_SPLIT));
        greenPolar.colRange(0, 2*POLAR_ANGLE_SPLIT).copyTo(
                stackedPolar
                        .rowRange(2*POLAR_SCLERA_RESOLUTION, 3*POLAR_SCLERA_RESOLUTION)
                        .colRange(POLAR_ANGLE_RESOLUTION-2*POLAR_ANGLE_SPLIT, POLAR_ANGLE_RESOLUTION));
        greenPolar.colRange(2*POLAR_ANGLE_SPLIT, POLAR_ANGLE_RESOLUTION).copyTo(
                stackedPolar
                        .rowRange(2*POLAR_SCLERA_RESOLUTION, 3*POLAR_SCLERA_RESOLUTION)
                        .colRange(0, POLAR_ANGLE_RESOLUTION-2*POLAR_ANGLE_SPLIT));

        stackedPolar.convertTo(
                stackedPolarAugFloat1
                        .colRange(POLAR_ENTROPY_NEIGHBORS/2,
                                stackedPolarAugFloat1.width()-POLAR_ENTROPY_NEIGHBORS/2), CvType.CV_32F);
        stackedPolar.colRange(POLAR_ANGLE_RESOLUTION-POLAR_ENTROPY_NEIGHBORS/2,
                POLAR_ANGLE_RESOLUTION).convertTo(
                stackedPolarAugFloat1
                            .colRange(0, POLAR_ENTROPY_NEIGHBORS/2), CvType.CV_32F);
        stackedPolar.colRange(0, POLAR_ENTROPY_NEIGHBORS/2).convertTo(
                stackedPolarAugFloat1
                        .colRange(stackedPolarAugFloat1.width()-POLAR_ENTROPY_NEIGHBORS/2,
                                stackedPolarAugFloat1.width()), CvType.CV_32F);

        Imgproc.filter2D(stackedPolarAugFloat1,
                stackedPolarAugFloat1,
                -1,
                polarEntropyFilter);
        stackedPolarAugFloat1.copyTo(stackedPolarAugFloat2);
        Core.add(stackedPolarAugFloat2, new Scalar(0.001), stackedPolarAugFloat2);
        Core.log(stackedPolarAugFloat2, stackedPolarAugFloat2);
        Core.reduce(stackedPolarAugFloat1, stackedPolarAugReducedFloat1, 0, Core.REDUCE_SUM);
        Core.multiply(stackedPolarAugFloat1, stackedPolarAugFloat2, stackedPolarAugFloat1);
        Core.reduce(stackedPolarAugFloat1, stackedPolarAugReducedFloat2, 0, Core.REDUCE_SUM);
        Core.divide(stackedPolarAugReducedFloat2,
                stackedPolarAugReducedFloat1,
                stackedPolarAugReducedFloat2);
        Core.patchNaNs(stackedPolarAugReducedFloat2, 0.0);
        Core.add(stackedPolarAugReducedFloat1, new Scalar(0.001), stackedPolarAugReducedFloat1);
        Core.log(stackedPolarAugReducedFloat1, stackedPolarAugReducedFloat1);
        Core.subtract(stackedPolarAugReducedFloat1,
                stackedPolarAugReducedFloat2,
                stackedPolarAugReducedFloat1);

        minMaxLocResult = Core.minMaxLoc(stackedPolarAugReducedFloat1, polarEntropyMask);
        double minOptimalVal = minMaxLocResult.maxVal + ENTROPY_EPS;
        List<Integer> optimalDegs = new ArrayList<>();
        for (int i = POLAR_ENTROPY_NEIGHBORS/2;
                i < (stackedPolarAugReducedFloat1.width() - POLAR_ENTROPY_NEIGHBORS/2); i++) {
            if (stackedPolarAugReducedFloat1.get(0, i)[0] > minOptimalVal) {
                optimalDegs.add(i);
            }
        }

        double optimalDeg = optimalDegs.get(optimalDegs.size()/2);
        Log.i(TAG, optimalDegs.toString() + " -> " + optimalDeg);

        return (double) (optimalDeg - POLAR_ENTROPY_NEIGHBORS / 2) / 2;
    }

    private void segmentColor(Mat srcHsv, Mat dest, String color) {
        if (color.equals("red")) {
            Core.inRange(srcHsv, RED_LOWER_THRESHOLD_1, RED_UPPER_THRESHOLD_1, colorPolarHelper1);
            Core.inRange(srcHsv, RED_LOWER_THRESHOLD_2, RED_UPPER_THRESHOLD_2, colorPolarHelper2);
            Core.bitwise_or(colorPolarHelper1, colorPolarHelper2, dest);
        } else if (color.equals("green")) {
            Core.inRange(srcHsv, GREEN_LOWER_THRESHOLD, GREEN_UPPER_THRESHOLD, dest);
        } else if (color.equals("blue")) {
            Core.inRange(srcHsv, BLUE_LOWER_THRESHOLD, BLUE_UPPER_THRESHOLD, dest);
        } else {
            throw new IllegalArgumentException("Passed color '" + color + "' is not supported.");
        }
    }

    public Mat visualize() {
        Mat vis = stackedPolar.clone();
        Imgproc.cvtColor(vis, vis, Imgproc.COLOR_GRAY2BGRA);
//        Mat vis = greenPolar.clone();
//        Imgproc.cvtColor(vis, vis, Imgproc.COLOR_GRAY2BGRA);
//        Mat vis = maskPolarPlanarSclera.clone();
////        Imgproc.cvtColor(vis, vis, Imgproc.COLOR_GRAY2BGRA);
//        Mat vis = hsvPolarPlanarSclera.clone();
//        Imgproc.cvtColor(vis, vis, Imgproc.COLOR_HSV2BGR);
//        Imgproc.cvtColor(vis, vis, Imgproc.COLOR_BGR2RGBA);

        return vis;
    }
}
