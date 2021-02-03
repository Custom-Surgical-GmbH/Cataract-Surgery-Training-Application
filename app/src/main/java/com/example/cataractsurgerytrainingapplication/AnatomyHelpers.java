package com.example.cataractsurgerytrainingapplication;

public class AnatomyHelpers {
    public static final double BIONIKO_LIMBUS_RADIUS_MM = 6.245;

    public static final double mmToPixels(double limbusRadius, double mm) {
        return mm * limbusRadius / BIONIKO_LIMBUS_RADIUS_MM;
    }

    public static final double pixelsToMm(double limbusRadius, double pixels) {
        return pixels * BIONIKO_LIMBUS_RADIUS_MM / limbusRadius;
    }
}
