package com.example.flappybird;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.SurfaceHolder;

public class MainThread extends Thread implements Runnable {
    final SurfaceHolder mySurfaceHolder;
    long timeSpent;
    long kickOffTime;
    long WAIT = 31;
    boolean Running;
    private static Canvas canvas;

    public MainThread(SurfaceHolder surfaceHolder) {
        this.mySurfaceHolder = surfaceHolder;
        Running = true;
    }

    @Override
    public void run() {
        while (Running) {
            kickOffTime = SystemClock.uptimeMillis();
            canvas = null;
            try {
                synchronized (mySurfaceHolder) {
                    canvas = mySurfaceHolder.lockCanvas();
                    AppHolder.getInstance().getGameManager().backgroundAnimation(canvas);
                    AppHolder.getInstance().getGameManager().scrollingTube(canvas);
                    AppHolder.getInstance().getGameManager().birdAnimation(canvas);
                    AppHolder.getInstance().getGameManager().scrollingBomb(canvas);
                    AppHolder.getInstance().getGameManager().scrollingShield(canvas);
                    if (!AppHolder.getInstance().guestMode) {
                        AppHolder.getInstance().getGameManager().scrollingMoney(canvas);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        mySurfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            timeSpent = SystemClock.uptimeMillis() - kickOffTime;
            if (timeSpent < WAIT) {
                try {
                    Thread.sleep(WAIT - timeSpent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    public void setIsRunning(boolean state) {
        Running = state;
    }

}
