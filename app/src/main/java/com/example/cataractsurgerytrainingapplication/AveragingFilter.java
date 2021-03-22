package com.example.cataractsurgerytrainingapplication;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class AveragingFilter {
    public static final String TAG = "AveragingFilter";
    public static final int DEFAULT_WINDOW_SIZE = 10;

    private int windowSize;
    private int parametersIndex;
    private boolean windowFilled;
    private Mat parametersWindow;

    public AveragingFilter(int parametersCount) {
        this(parametersCount, DEFAULT_WINDOW_SIZE);
    }

    public AveragingFilter(int parametersCount, int windowSize) {
        this.windowSize = windowSize;
        this.parametersIndex = 0;
        this.windowFilled = false;

        this.parametersWindow = new Mat(windowSize, parametersCount, CvType.CV_32F);
    }

    public void process(Mat parametersCurrent, Mat parametersAveraged) {
        parametersCurrent.copyTo(parametersWindow.row(parametersIndex));
        if ((parametersIndex + 1) == windowSize) {
            windowFilled = true;
        }

        if (windowFilled) {
            Core.reduce(parametersWindow, parametersAveraged, 0, Core.REDUCE_AVG);
        } else {
            parametersCurrent.copyTo(parametersAveraged);
        }

        parametersIndex = (parametersIndex + 1) % windowSize;
    }
}
