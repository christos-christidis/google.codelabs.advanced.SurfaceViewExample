package com.advanced.surfaceviewexample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

// SOS: Other views get drawn in the main/UI thread. A SurfaceView can be drawn in a background thread.
// Unfortunately, it does not use hardware-acceleration unless I use lockHardwareCanvas. But that's
// an API 23 call!
class GameView extends SurfaceView implements Runnable {

    private final Context mContext;
    private final SurfaceHolder mSurfaceHolder;

    private final Paint mPaint = new Paint();
    private final Path mPath = new Path();

    private int mBitmapX;
    private int mBitmapY;
    private int mViewWidth;
    private int mViewHeight;
    private Bitmap mBitmap;
    private RectF mWinnerRect;

    private boolean mRunning;
    private Thread mGameThread;

    private FlashlightCone mFlashlightCone;

    GameView(Context context) {
        super(context);

        mContext = context;
        mSurfaceHolder = getHolder();
        mPaint.setColor(Color.DKGRAY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
        mFlashlightCone = new FlashlightCone(mViewWidth, mViewHeight);
        mPaint.setTextSize(mViewHeight / 5f);
        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.android);
        setUpBitmap();
    }

    @Override
    public void run() {
        while (mRunning) {
            if (mSurfaceHolder.getSurface().isValid()) {
                Canvas canvas = mSurfaceHolder.lockCanvas();
                canvas.save();
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(mBitmap, mBitmapX, mBitmapY, mPaint);

                int x = mFlashlightCone.getX();
                int y = mFlashlightCone.getY();
                int radius = mFlashlightCone.getRadius();
                mPath.addCircle(x, y, radius, Path.Direction.CCW);

                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    canvas.clipPath(mPath, Region.Op.DIFFERENCE);
                } else {
                    canvas.clipOutPath(mPath);
                }

                canvas.drawColor(Color.BLACK);

                if (x > mWinnerRect.left && x < mWinnerRect.right &&
                        y > mWinnerRect.top && y < mWinnerRect.bottom) {
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(mBitmap, mBitmapX, mBitmapY, mPaint);
                    canvas.drawText("WIN!", mViewWidth / 3f, mViewHeight / 2f, mPaint);
                }

                mPath.rewind();
                canvas.restore();
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    void pause() {
        mRunning = false;
        try {
            mGameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void resume() {
        mRunning = true;
        mGameThread = new Thread(this);
        mGameThread.start();
    }

    private void setUpBitmap() {
        mBitmapX = (int) Math.floor(Math.random() * (mViewWidth - mBitmap.getWidth()));
        mBitmapY = (int) Math.floor(Math.random() * (mViewHeight - mBitmap.getHeight()));
        mWinnerRect = new RectF(mBitmapX, mBitmapY,
                mBitmapX + mBitmap.getWidth(),
                mBitmapY + mBitmap.getHeight());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setUpBitmap();
                updateFrame((int) x, (int) y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                updateFrame((int) x, (int) y);
                invalidate();
                break;
            default:
                // Do nothing.
        }
        return true;
    }

    private void updateFrame(int newX, int newY) {
        mFlashlightCone.update(newX, newY);
    }
}
