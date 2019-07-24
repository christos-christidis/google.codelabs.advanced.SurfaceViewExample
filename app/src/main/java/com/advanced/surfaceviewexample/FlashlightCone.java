package com.advanced.surfaceviewexample;

class FlashlightCone {

    private int mX;
    private int mY;
    private final int mRadius;

    FlashlightCone(int viewWidth, int viewHeight) {
        mX = viewWidth / 2;
        mY = viewHeight / 2;
        mRadius = Math.min(viewWidth, viewHeight) / 3;
    }

    void update(int newX, int newY) {
        mX = newX;
        mY = newY;
    }

    int getX() {
        return mX;
    }

    int getY() {
        return mY;
    }

    int getRadius() {
        return mRadius;
    }
}
