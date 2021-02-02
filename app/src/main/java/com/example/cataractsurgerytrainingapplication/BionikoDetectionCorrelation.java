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

    private final Paint bionikoPaint;
    private double[] limbusCircle;
    private Mat gray;
    private Mat grayPolar;
    private Bitmap bionikoBm;
    Rect bionikoBounds;
    MinMaxLocResult minMaxLocResult;
    private Mat bionikoRgba;
    private Mat bionikoGray;
    private Mat ccoeffNormed;

    public BionikoDetectionCorrelation(Context context, int width, int height) {
        gray = new Mat(height, width, CvType.CV_8UC1);
        grayPolar = new Mat();
        bionikoRgba = new Mat();
        bionikoGray = new Mat();
        ccoeffNormed = new Mat();

        bionikoBounds = new Rect();
        bionikoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bionikoPaint.setColor(Color.BLACK);
        bionikoPaint.setTypeface(ResourcesCompat.getFont(context, R.font.century_gothic_bold));
    }

    // TODO: do multiple matchings to also check for blind angles (move polar coordinates by 180)
    //  and visibility in various color spaces
    public double process(Mat newGray, double[] limbusCircle) {
        this.limbusCircle = limbusCircle;
        gray = newGray;
        Imgproc.warpPolar(gray, grayPolar, new Size(0,0),
                new Point(limbusCircle[0], limbusCircle[1]), limbusCircle[2],
                Imgproc.WARP_POLAR_LINEAR);
        Core.rotate(grayPolar, grayPolar, Core.ROTATE_90_CLOCKWISE);
        Imgproc.resize(grayPolar, grayPolar, new Size(0,0), 2.0, 1.0);

        double bionikoHeight = Math.min(limbusCircle[2]*BIONIKO_HEIGHT_P, grayPolar.rows());
        // TODO: call only in the constructor and then resize (for performance reasons)
        writeBioniko(bionikoHeight);

        Imgproc.matchTemplate(grayPolar, bionikoGray, ccoeffNormed, Imgproc.TM_CCOEFF_NORMED);

        // TODO: add validation
        minMaxLocResult = Core.minMaxLoc(ccoeffNormed);

        // angle is measured in degrees; counterclockwise; starting at three hours
        double bionikoAngle = 360.0*(minMaxLocResult.maxLoc.x / grayPolar.width());

        return bionikoAngle;
    }

    public Mat visualize() {
        Mat vis = grayPolar.clone();
        Imgproc.cvtColor(vis, vis, Imgproc.COLOR_GRAY2BGRA);
        Imgproc.line(vis, new Point(minMaxLocResult.maxLoc.x, 0),
                new Point(minMaxLocResult.maxLoc.x, vis.height()), new Scalar(0,255,0,255));

        return vis;
    }

    private void writeBioniko(double bionikoHeight) {
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
    }
}
