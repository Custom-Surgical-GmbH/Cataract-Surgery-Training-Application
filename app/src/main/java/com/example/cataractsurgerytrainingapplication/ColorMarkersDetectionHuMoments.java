package com.example.cataractsurgerytrainingapplication;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ColorMarkersDetectionHuMoments {
    public static final String TAG = "ColorMarkerDetection";
//    public static final Scalar RED_LOWER_THRESHOLD_1 = new Scalar(165, 100, 50);
//    public static final Scalar RED_UPPER_THRESHOLD_1 = new Scalar(179, 255, 255);
//    public static final Scalar RED_LOWER_THRESHOLD_2 = new Scalar(0, 100, 50);
//    public static final Scalar RED_UPPER_THRESHOLD_2 = new Scalar(15, 255, 255);
//    public static final Scalar GREEN_LOWER_THRESHOLD = new Scalar(35, 50, 50);
//    public static final Scalar GREEN_UPPER_THRESHOLD = new Scalar(70, 255, 255);
//    public static final Scalar BLUE_LOWER_THRESHOLD = new Scalar(100, 75, 50);
//    public static final Scalar BLUE_UPPER_THRESHOLD = new Scalar(150, 255, 255);
//    public static final Scalar BLACK_LOWER_THRESHOLD = new Scalar(0, 0, 0);
//    public static final Scalar BLACK_UPPER_THRESHOLD = new Scalar(179, 40, 170);
    public static final Scalar RED_LOWER_THRESHOLD_1 = new Scalar(150, 90, 60);
    public static final Scalar RED_UPPER_THRESHOLD_1 = new Scalar(179, 240, 230);
    public static final Scalar RED_LOWER_THRESHOLD_2 = new Scalar(0, 90, 60);
    public static final Scalar RED_UPPER_THRESHOLD_2 = new Scalar(15, 240, 230);
    public static final Scalar GREEN_LOWER_THRESHOLD = new Scalar(40, 40, 75);
    public static final Scalar GREEN_UPPER_THRESHOLD = new Scalar(100, 240, 230);
    public static final Scalar BLUE_LOWER_THRESHOLD = new Scalar(110, 25, 75);
    public static final Scalar BLUE_UPPER_THRESHOLD = new Scalar(160, 165, 190);
    public static final Scalar BLACK_LOWER_THRESHOLD = new Scalar(0, 0, 0); // TODO
    public static final Scalar BLACK_UPPER_THRESHOLD = new Scalar(179, 40, 170); // TODO

//    public static final double SCLERA_TO_LIMBUS_RATIO = 1.7;
//    public static final int POLAR_SCLERA_RESOLUTION = 47;
//    public static final double PLANAR_SCLERA_TO_LIMBUS_RATIO = 1.25;
//    public static final int POLAR_PLANAR_SCLERA_RESOLUTION = 23;
    public static final double PLANAR_SCLERA_TO_LIMBUS_RATIO = 1.7; // TODO: provisional
    public static final int POLAR_PLANAR_SCLERA_RESOLUTION = 43; // TODO: provisional

    public static final String HU_MOMENT_DECIDER = "mu02";

    public static final int POLAR_ANGLE_RESOLUTION = 720;
    public static final int POLAR_ANGLE_SPLIT = 180;
    public static final int POLAR_RADIUS_RESOLUTION = 115;
    public static final int POLAR_HU_MOMENT_NEIGHBORS = 20;

    private Mat hsv;
    private Mat hsvPolarRotated;
    private Mat hsvPolar;
    private Mat hsvPolarPlanarSclera;
    private Mat valuePolarPlanarSclera;
    private Mat maskPolarPlanarSclera;
    private Mat greenPolar;
//    private Mat blackPolar;
    private Mat redPolar;
    private Mat bluePolar;
    private Mat colorPolarHelper1;
    private Mat colorPolarHelper2;
    private Mat stackedPolar;
    private Mat huMomentWindow;
    private double[] huMoments;

    public ColorMarkersDetectionHuMoments(int width, int height) {
        hsv = new Mat(height, width, CvType.CV_8UC3);
        hsvPolarRotated = new Mat(POLAR_ANGLE_RESOLUTION, POLAR_RADIUS_RESOLUTION, CvType.CV_8UC3);
        hsvPolar = new Mat(POLAR_RADIUS_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC3);
        hsvPolarPlanarSclera = new Mat(POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC3);
        valuePolarPlanarSclera = new Mat(POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        maskPolarPlanarSclera = new Mat(POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);

        greenPolar = new Mat(POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
//        blackPolar = new Mat(POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        redPolar = new Mat(POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        bluePolar = new Mat(POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        colorPolarHelper1 = new Mat(POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        colorPolarHelper2 = new Mat(POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);

//        stackedPolar = new Mat(4*POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);
        stackedPolar = new Mat(3*POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_ANGLE_RESOLUTION, CvType.CV_8UC1);

//        huMomentWindow = new Mat(4*POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_HU_MOMENT_ANGLE_WINDOW, CvType.CV_8UC1);
        huMomentWindow = new Mat(3*POLAR_PLANAR_SCLERA_RESOLUTION,
                POLAR_HU_MOMENT_NEIGHBORS + 1,
                CvType.CV_8UC1);
        huMoments = new double[360];
    }

    public double process(Mat newHsv, double[] limbusCircle) {
        hsv = newHsv;
        Imgproc.warpPolar(
                hsv,
                hsvPolarRotated,
                new Size(POLAR_RADIUS_RESOLUTION, POLAR_ANGLE_RESOLUTION),
                new Point(limbusCircle[0], limbusCircle[1]),
                PLANAR_SCLERA_TO_LIMBUS_RATIO*limbusCircle[2],
                Imgproc.WARP_POLAR_LINEAR + Imgproc.WARP_FILL_OUTLIERS
        );
        Core.rotate(hsvPolarRotated, hsvPolar, Core.ROTATE_90_CLOCKWISE);
        hsvPolar.rowRange(
                POLAR_RADIUS_RESOLUTION - POLAR_PLANAR_SCLERA_RESOLUTION, POLAR_RADIUS_RESOLUTION
        ).copyTo(hsvPolarPlanarSclera);
//        Core.extractChannel(hsvPolarPlanarSclera, valuePolarPlanarSclera, 2);
//        Imgproc.threshold(valuePolarPlanarSclera,
//                maskPolarPlanarSclera,
//                0,
//                255,
//                Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY_INV);
//        Imgproc.threshold(valuePolarPlanarSclera,
//                valuePolarPlanarSclera,
//                0,
//                255,
//                Imgproc.THRESH_BINARY_INV);
//        maskPolarPlanarSclera.setTo(new Scalar(0), valuePolarPlanarSclera); // setting warped pixels to zero when outside of domain

        segmentColor(hsvPolarPlanarSclera, redPolar, "red");
        segmentColor(hsvPolarPlanarSclera, greenPolar, "green");
        segmentColor(hsvPolarPlanarSclera, bluePolar, "blue");
//        segmentColor(hsvPolar, blackPolar, "black");

//        Core.bitwise_and(redPolar, maskPolarPlanarSclera, redPolar);
//        Core.bitwise_and(greenPolar, maskPolarPlanarSclera, greenPolar);
//        Core.bitwise_and(bluePolar, maskPolarPlanarSclera, bluePolar);

        stackedPolar.setTo(new Scalar(0));
        redPolar.copyTo(
                stackedPolar
                        .rowRange(0, POLAR_PLANAR_SCLERA_RESOLUTION));
        bluePolar.colRange(0, 1*POLAR_ANGLE_SPLIT).copyTo(
                stackedPolar
                        .rowRange(1*POLAR_PLANAR_SCLERA_RESOLUTION, 2*POLAR_PLANAR_SCLERA_RESOLUTION)
                        .colRange(POLAR_ANGLE_RESOLUTION-1*POLAR_ANGLE_SPLIT, POLAR_ANGLE_RESOLUTION));
        bluePolar.colRange(1*POLAR_ANGLE_SPLIT, POLAR_ANGLE_RESOLUTION).copyTo(
                stackedPolar
                        .rowRange(1*POLAR_PLANAR_SCLERA_RESOLUTION, 2*POLAR_PLANAR_SCLERA_RESOLUTION)
                        .colRange(0, POLAR_ANGLE_RESOLUTION-1*POLAR_ANGLE_SPLIT));
        greenPolar.colRange(0, 2*POLAR_ANGLE_SPLIT).copyTo(
                stackedPolar
                        .rowRange(2*POLAR_PLANAR_SCLERA_RESOLUTION, 3*POLAR_PLANAR_SCLERA_RESOLUTION)
                        .colRange(POLAR_ANGLE_RESOLUTION-2*POLAR_ANGLE_SPLIT, POLAR_ANGLE_RESOLUTION));
        greenPolar.colRange(2*POLAR_ANGLE_SPLIT, POLAR_ANGLE_RESOLUTION).copyTo(
                stackedPolar
                        .rowRange(2*POLAR_PLANAR_SCLERA_RESOLUTION, 3*POLAR_PLANAR_SCLERA_RESOLUTION)
                        .colRange(0, POLAR_ANGLE_RESOLUTION-2*POLAR_ANGLE_SPLIT));

        int angleResolutionScale = POLAR_ANGLE_RESOLUTION / 360;
        for (int angle = 0; angle < 360; angle++) {
            huMomentWindow.setTo(new Scalar(0));
            int colRangeStart = Math.max(0,
                    angle*angleResolutionScale - POLAR_HU_MOMENT_NEIGHBORS/2);
            int colRangeEnd = Math.min(POLAR_ANGLE_RESOLUTION,
                    angle*angleResolutionScale + POLAR_HU_MOMENT_NEIGHBORS/2 + 1);
            stackedPolar
                    .colRange(colRangeStart, colRangeEnd)
                    .copyTo(huMomentWindow.colRange(0, colRangeEnd - colRangeStart));

            if (HU_MOMENT_DECIDER.equals("mu02")) {
                huMoments[angle] = Imgproc.moments(huMomentWindow, true).mu02;
            } else {
                throw new IllegalArgumentException("Unsupported Hu moment: " + HU_MOMENT_DECIDER);
            }
        }

        double maxMoment = -1.0;
        double argmaxAngle = -1.0;
        for (int angle = 0; angle < 360; angle++) {
            if (huMoments[angle] > maxMoment) {
                maxMoment = huMoments[angle];
                argmaxAngle = angle;
            }
        }

        return argmaxAngle;
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
        } else if (color.equals("black")) {
            Core.inRange(srcHsv, BLACK_LOWER_THRESHOLD, BLACK_UPPER_THRESHOLD, dest);
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
