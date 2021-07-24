package com.example.flappybird;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

public class GamePlay extends SurfaceView implements SurfaceHolder.Callback {
    MainThread mainThread;

    public GamePlay(Context context) {
        super(context);
        SurfaceHolder myHolder = getHolder();
        myHolder.addCallback(this);
        mainThread = new MainThread(myHolder);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

        SurfaceHolder myHolder = getHolder();
        if (mainThread.getState() == Thread.State.TERMINATED) {
            mainThread = new MainThread(myHolder);
        }
        mainThread.setIsRunning(true);
        mainThread.start();

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                mainThread.setIsRunning(false);
                mainThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (GameManager.gameState == 0) {
            GameManager.gameState = 1;
            AppHolder.getInstance().getSoundPlay().playMove();
        } else {
            AppHolder.getInstance().getSoundPlay().platJump();
        }
        AppHolder.getInstance().getGameManager().bird.setVelocity(AppHolder.getInstance().JUMP_VELOCITY);
        return true;
    }

}
