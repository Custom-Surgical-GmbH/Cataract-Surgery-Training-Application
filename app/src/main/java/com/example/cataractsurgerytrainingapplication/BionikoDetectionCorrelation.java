package com.example.cataractsurgerytrainingapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class BionikoDetectionCorrelation {
    public static final String TAG = "BionikoDetection";
    public static final double BIONIKO_HEIGHT_P = 0.18;
    public static final double BIONIKO_WIDTH_P = 0.14;
    public static final double BIONIKO_ASPECT_RATIO = 5.7;
    public static final double BIONIKO_FONT_FACTOR = 1.35;
    public static final String BIONIKO_TEXT = "BIONIKO";
    public static final double CCOEFF_NORMED_WIDTH_IGNORE_P = 0.1;
    public static final Size POLAR_SIZE = new Size(172, 1080);

    private final Paint bionikoPaint;
    private double[] limbusCircle;
    private Mat gray;
    private Mat grayPolar;
    private Bitmap bionikoBm;
    Rect bionikoBounds;
    private Mat bionikoGray;
    private Mat bionikoGrayScaled;
    private Mat ccoeffNormed;
    MinMaxLocResult minMaxLocResult;

    public BionikoDetectionCorrelation(Context context, int width, int height) {
        gray = new Mat(height, width, CvType.CV_8UC1);
        grayPolar = new Mat();
        bionikoGray = new Mat();
        bionikoGrayScaled = new Mat();
        ccoeffNormed = new Mat();

        bionikoBounds = new Rect();
        bionikoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bionikoPaint.setColor(Color.BLACK);
        bionikoPaint.setTypeface(ResourcesCompat.getFont(context, R.font.century_gothic_bold));

        writeBioniko(Math.max(width, height));
    }

    // TODO: do multiple matchings to also check for blind angles (move polar coordinates by 180)
    //  and visibility in various color spaces
    public double process(Mat newGray, Mat newValue, double[] limbusCircle) {
        this.limbusCircle = limbusCircle;
        gray = newGray;
        Imgproc.warpPolar(gray,
                grayPolar,
                POLAR_SIZE,
                new Point(limbusCircle[0], limbusCircle[1]),
                limbusCircle[2],
                Imgproc.WARP_POLAR_LINEAR);
        Core.rotate(grayPolar, grayPolar, Core.ROTATE_90_CLOCKWISE);
        grayPolar = grayPolar.submat((int) ((1.0 - (BIONIKO_HEIGHT_P + 0.05))*grayPolar.rows()),
                grayPolar.rows(),
                0,
                grayPolar.cols());

        int bionikoHeight = (int) Math.min(POLAR_SIZE.width*BIONIKO_HEIGHT_P, grayPolar.height());
        int bionikoWidth = (int) ((double) bionikoHeight*BIONIKO_ASPECT_RATIO);
        // TODO: call only in the constructor and then resize (for performance reasons)
        Imgproc.resize(bionikoGray, bionikoGrayScaled, new Size(bionikoWidth, bionikoHeight));

        // gray
        Imgproc.matchTemplate(grayPolar, bionikoGrayScaled, ccoeffNormed, Imgproc.TM_CCOEFF_NORMED);
        MinMaxLocResult minMaxLocResultGray = Core.minMaxLoc(ccoeffNormed);

        // value
        gray = newValue;
        Imgproc.warpPolar(gray,
                grayPolar,
                POLAR_SIZE,
                new Point(limbusCircle[0], limbusCircle[1]),
                limbusCircle[2],
                Imgproc.WARP_POLAR_LINEAR);
        Core.rotate(grayPolar, grayPolar, Core.ROTATE_90_CLOCKWISE);
        grayPolar = grayPolar.submat((int) ((1.0 - (BIONIKO_HEIGHT_P + 0.05))*grayPolar.rows()),
                grayPolar.rows(),
                0,
                grayPolar.cols());
        Imgproc.matchTemplate(grayPolar, bionikoGrayScaled, ccoeffNormed, Imgproc.TM_CCOEFF_NORMED);
        MinMaxLocResult minMaxLocResultValue = Core.minMaxLoc(ccoeffNormed);

        // TODO: add validation
        // angle is measured in degrees; counterclockwise; starting at three hours

        if (minMaxLocResultGray.maxVal > minMaxLocResultValue.maxVal) {
            minMaxLocResult = minMaxLocResultGray;
        } else {
            minMaxLocResult = minMaxLocResultValue;
        }

        double bionikoAngle = 360.0*(minMaxLocResult.maxLoc.x / grayPolar.width());

        // clean up
        gray.release();

        return bionikoAngle;
    }

    // TODO: write 'BIONIKO' so it starts exactly at the left edge of the picture
    private void writeBioniko(double bionikoHeight) {
        Mat bionikoRgba = new Mat();
        bionikoPaint.setTextSize((float) (bionikoHeight*BIONIKO_FONT_FACTOR));
        bionikoPaint.getTextBounds(BIONIKO_TEXT, 0, BIONIKO_TEXT.length(), bionikoBounds);
        bionikoBm = Bitmap.createBitmap(bionikoBounds.width(), bionikoBounds.height(),
                Bitmap.Config.ARGB_8888);
        bionikoBm.eraseColor(Color.WHITE);
        Log.i(TAG, bionikoHeight + " -> " + bionikoBounds.height());

        Canvas canvas = new Canvas(bionikoBm);
        canvas.drawText(BIONIKO_TEXT, 0, bionikoBm.getHeight() - 1, bionikoPaint);

        Utils.bitmapToMat(bionikoBm, bionikoRgba);
        Imgproc.cvtColor(bionikoRgba, bionikoGray, Imgproc.COLOR_BGRA2GRAY);

        // clean up
        bionikoRgba.release();
    }

    public Mat visualize() {
        Mat vis = grayPolar.clone();
        Imgproc.cvtColor(vis, vis, Imgproc.COLOR_GRAY2BGRA);
        Imgproc.line(vis, new Point(minMaxLocResult.maxLoc.x, 0),
                new Point(minMaxLocResult.maxLoc.x, vis.height()), new Scalar(0,255,0,255));

        Mat bionikoRgbaScaled = new Mat();
        Imgproc.cvtColor(bionikoGrayScaled, bionikoRgbaScaled, Imgproc.COLOR_GRAY2BGRA);
        bionikoRgbaScaled.copyTo(
                vis.rowRange(0, bionikoRgbaScaled.rows()).colRange(vis.cols() - bionikoRgbaScaled.cols(),
                        vis.cols()));

        // clean up
        bionikoRgbaScaled.release();

        return vis;
    }
}
