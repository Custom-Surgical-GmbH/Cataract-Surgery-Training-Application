package com.example.cataractsurgerytrainingapplication;

import android.graphics.Color;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Overlays {
    public static void drawCircle(Mat img, Point center, double radius, Scalar color) {
        int radiusInt = (int) Math.round(radius);
        int markerSize = (int) Math.round(radius*0.1);

        Imgproc.drawMarker(img, center, color,
                Imgproc.MARKER_CROSS, markerSize, 3, Imgproc.LINE_AA);
        Imgproc.circle(img, center, radiusInt, color,
                3, Imgproc.LINE_AA);
    }

    // angle is measured in degrees; counterclockwise; starting at three hours
    public static void drawAxis(Mat img, Point center, double angle, double length, Scalar color) {
        Point lineStart = rotate2d(center, angle, length/2);
        Point lineEnd = rotate2d(center, angle+180.0, length/2);

        Imgproc.line(img, lineStart, lineEnd, color, 3, Imgproc.LINE_AA);
    }

    public static void drawIncision(Mat img, Point center, double angle, double radius,  double width,
                                    double height, Scalar color) {
        Point rectCenter = rotate2d(center, angle, radius);
        RotatedRect rect = new RotatedRect(rectCenter, new Size(width, height),
                (360 - angle) + 90.0);
        Point[] vertices = new Point[4];
        rect.points(vertices);
        for (int i = 0; i < 4; i++) {
            Imgproc.line(img, vertices[i], vertices[(i+1)%4], color, 3, Imgproc.LINE_AA);
        }
    }

    public static void drawVisualization(Mat img, Mat vis, double fx) {
        double newWidth = img.width()*fx;
        double newHeight = newWidth / ((double) vis.width() / (double) vis.height());
        Imgproc.resize(vis, vis, new Size(Math.round(newWidth), Math.round(newHeight)));
        vis.copyTo(img.rowRange(0, vis.rows()).colRange(0, vis.cols()));
    }

    public static Point rotate2d(Point center, double angle, double radius) {
        double angleRad = 2*Math.PI*angle/360.0;
        Point direction = new Point(Math.cos(angleRad), -Math.sin(angleRad));

        return new Point(center.x + radius*direction.x, center.y + radius*direction.y);
    }
}
