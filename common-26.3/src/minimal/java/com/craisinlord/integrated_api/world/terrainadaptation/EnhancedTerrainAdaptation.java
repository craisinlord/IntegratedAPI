package com.craisinlord.integrated_api.world.terrainadaptation;

import net.minecraft.util.Mth;

public abstract class EnhancedTerrainAdaptation {
    public static final EnhancedTerrainAdaptation NONE = new NoneAdaptation();

    private final boolean doCarving;
    private final boolean doBearding;
    private final TerrainAdaptationDirection direction;
    private final int kernelSize;
    private final int kernelDistance;
    private final float[] kernel;

    public abstract EnhancedTerrainAdaptationType<?> type();

    EnhancedTerrainAdaptation(int kernelSize, int kernelDistance, boolean doCarving, boolean doBearding) {
        this(kernelSize, kernelDistance, doCarving, doBearding, TerrainAdaptationDirection.DOWN);
    }

    EnhancedTerrainAdaptation(int kernelSize, int kernelDistance, boolean doCarving, boolean doBearding, TerrainAdaptationDirection direction) {
        this.kernelSize = kernelSize;
        this.kernelDistance = kernelDistance;
        this.doCarving = doCarving;
        this.doBearding = doBearding;
        this.direction = direction;
        int kernelRadius = this.getKernelRadius();
        this.kernel = new float[kernelSize * kernelSize * kernelSize];
        for (int x = 0; x < kernelSize; ++x) {
            for (int y = 0; y < kernelSize; ++y) {
                for (int z = 0; z < kernelSize; ++z) {
                    int i = index(x, y, z);
                    double kernelX = x - kernelRadius;
                    double kernelY = y - kernelRadius + 0.5;
                    double kernelZ = z - kernelRadius;
                    kernel[i] = computeKernelValue(kernelX, kernelY, kernelZ);
                }
            }
        }
    }

    private float computeKernelValue(double xDistance, double yDistance, double zDistance) {
        double squaredDistance = Mth.lengthSquared(xDistance, yDistance, zDistance);
        return (float) Math.pow(Math.E, -squaredDistance / this.kernelDistance);
    }

    public boolean carves() {
        return this.doCarving;
    }

    public boolean beards() {
        return this.doBearding;
    }

    public TerrainAdaptationDirection direction() {
        return this.direction;
    }

    public int getKernelSize() {
        return this.kernelSize;
    }

    public int getKernelRadius() {
        return this.kernelSize / 2;
    }

    public int getKernelDistance() {
        return this.kernelDistance;
    }

    public float[] getKernel() {
        return this.kernel;
    }

    public double computeDensityFactor(int xDistance, int yDistance, int zDistance, int yDistanceToBeardBase) {
        int kernelRadius = this.getKernelRadius();
        int kernelX = xDistance + kernelRadius;
        int kernelY = yDistance + kernelRadius;
        int kernelZ = zDistance + kernelRadius;
        if (isInKernelRange(kernelX) && isInKernelRange(kernelY) && isInKernelRange(kernelZ)) {
            int i = index(kernelX, kernelY, kernelZ);
            float kernelValue = this.getKernel()[i];
            double actualYDistanceToBeardBase = (double) yDistanceToBeardBase + 0.5;
            double squaredDistance = Mth.lengthSquared(xDistance, actualYDistanceToBeardBase, zDistance);
            double multiplier = -actualYDistanceToBeardBase * Mth.invSqrt(squaredDistance / 2.0) / 2.0;
            if (multiplier > 0 && !this.beards()) {
                return 0;
            }
            if (multiplier < 0 && !this.carves()) {
                return 0;
            }
            return multiplier * kernelValue;
        } else {
            return 0;
        }
    }

    private boolean isInKernelRange(int i) {
        return i >= 0 && i < this.kernelSize;
    }

    private int index(int x, int y, int z) {
        return z * this.kernelSize * this.kernelSize + x * this.kernelSize + y;
    }
}
