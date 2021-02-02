package com.example.cataractsurgerytrainingapplication;

import android.graphics.Color;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Overlays {
    public static void drawCircle(Mat img, double[] circle, Scalar color) {
        Point center = new Point(Math.round(circle[0]), Math.round(circle[1]));
        int radius = (int) Math.round(circle[2]);
        int markerSize = (int) Math.round(circle[2]*0.1);

        Imgproc.drawMarker(img, center, color,
                Imgproc.MARKER_CROSS, markerSize, 3, Imgproc.LINE_AA);
        Imgproc.circle(img, center, radius, color,
                3, Imgproc.LINE_AA);
    }

    // angle is measured in degrees; counterclockwise; starting at three hours
    public static void drawAxis(Mat img, Point center, double angle, double length, Scalar color) {
        double angleRad = 2*Math.PI*angle/360;

        Point direction = new Point(Math.cos(angleRad), -Math.sin(angleRad));
        Point lineStart = new Point(center.x + length*direction.x/2,
                center.y + length*direction.y/2);
        Point lineEnd = new Point(center.x - length*direction.x/2,
                center.y - length*direction.y/2);

        Imgproc.line(img, lineStart, lineEnd, color, 3, Imgproc.LINE_AA);
    }
}
