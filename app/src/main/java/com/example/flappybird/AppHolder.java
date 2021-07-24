package com.example.flappybird;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class AppHolder {
    private static AppHolder INSTANCE = null;

    public BitmapControl bitmapControl;
    public GameManager gameManager;
    public User user = new User();
    public Context gameActivityContext;
    public SoundPlayer soundPlay;

    public int SCREEN_WIDTH_X;
    public int SCREEN_HEIGHT_Y;
    public int gravityPull;
    public int JUMP_VELOCITY;
    public int tubeGap;
    public int tube_numbers;
    public int tubeVelocity;
    public int minimumTubeCollection_y;
    public int maximumTubeCollection_y;
    public int tubeDistance;

    public int shieldVelocity;
    public int shieldDistance;

    public int bombVelocity;
    public int bombDistance;
    public int moneyVelocity;
    public int moneyDistance;

    public boolean guestMode;
    public boolean gameOver;

    private AppHolder() {
    }

    public static AppHolder getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppHolder();
        }
        return (INSTANCE);
    }

    public void assign(Context context) {
        mapScreenSize(context);
        bitmapControl = new BitmapControl(context.getResources());
        holdGameVariables();
        gameManager = new GameManager();
        soundPlay = new SoundPlayer(context);
        guestMode = false;
        gameOver = false;
    }

    public SoundPlayer getSoundPlay() {
        return soundPlay;
    }

    public void holdGameVariables() {

        gravityPull = 5;
        JUMP_VELOCITY = -40;

        //****************Tubes variables****************//
        tubeGap = 650;
        tube_numbers = 2;
        tubeVelocity = 19;
        minimumTubeCollection_y = (int) (tubeGap / 2.0);
        maximumTubeCollection_y = SCREEN_HEIGHT_Y - minimumTubeCollection_y - tubeGap;
        tubeDistance = SCREEN_WIDTH_X;
        //***********************************************//

        //****************Shield variables****************//
        shieldVelocity = tubeVelocity;
        shieldDistance = SCREEN_WIDTH_X;
        //************************************************//

        //****************Bomb variables****************//
        bombDistance = SCREEN_WIDTH_X * 6;
        bombVelocity = tubeVelocity * 2;
        //**********************************************//

        //****************Money variables****************//
        moneyDistance = SCREEN_WIDTH_X * 3;
        moneyVelocity = tubeVelocity;
        //***********************************************//
    }

    public BitmapControl getBitmapControl() {
        return bitmapControl;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public void mapScreenSize(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        SCREEN_WIDTH_X = width;
        SCREEN_HEIGHT_Y = height;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User u) {
        user.setUsername(u.getUsername());
        user.setEmail(u.getEmail());
        user.setBestScore(u.getBestScore());
        user.setMoneyCount(u.getMoneyCount());
        user.setAvatarArrayList(u.getAvatarArrayList());
        user.setCurrentAvatar(u.getCurrentAvatar());

    }

}
