package com.example.flappybird;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class GameManager extends AppCompatActivity {
    BgImage bgImage;
    FlyingBird bird;
    ArrayList<TubeCollection> tubeCollections;
    BitmapElement bomb, money, shield, circleShield;
    Random rand;
    int scoreCount, scoreWhenBirdTouchTheShield, winningTube, scoreWhenTheBirdIsTouchingTheMoney, moneyCounter, isTheFirstTouch;
    Paint designPaint;
    boolean isObstacleOn, isShieldOn, isOverBecauseOfBomb;
    static int pauseVelocity, gameState, bestScore;
    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    AppHolder appHolder = AppHolder.getInstance();

    public GameManager() {
        bgImage = new BgImage();
        bird = new FlyingBird();
        tubeCollections = new ArrayList<>();
        bomb = new BitmapElement();
        money = new BitmapElement();
        shield = new BitmapElement();
        circleShield = new BitmapElement();
        rand = new Random();
        generateTubeObject();
        initScoreVariables();
    }

    public void initScoreVariables() {
        scoreCount = 0;
        winningTube = 0;
        gameState = 0;
        isObstacleOn = true;
        isShieldOn = false;
        isTheFirstTouch = 0;
        isOverBecauseOfBomb = false;
        pauseVelocity = 1;
        designPaint = new Paint();
        designPaint.setColor(Color.YELLOW);
        designPaint.setTextSize(200);
        designPaint.setStyle(Paint.Style.FILL);
        designPaint.setFakeBoldText(true);
        designPaint.setShadowLayer(5.0f, 20.0f, 20.0f, Color.BLACK);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.keepSynced(true);
            userID = user.getUid();
            getDataFromDB();
        }
    }

    public void generateTubeObject() {
        for (int j = 0; j < appHolder.tube_numbers; j++) {
            int tubeX = appHolder.SCREEN_WIDTH_X + j * appHolder.tubeDistance;
            int upTubeCollectionY = appHolder.minimumTubeCollection_y;
            rand.nextInt(appHolder.maximumTubeCollection_y - appHolder.minimumTubeCollection_y + 1);
            TubeCollection tubeCollection = new TubeCollection(tubeX, upTubeCollectionY);
            tubeCollections.add(tubeCollection);
        }
    }

    public void scrollingTube(Canvas canvas) {
        if (gameState == 1) {
            tubeObstacleOn(isObstacleOn);
            if (tubeCollections.get(winningTube).getXtube() < bird.getX() - appHolder.bitmapControl.getTubeWidth()) {
                scoreCount++;
                winningTube++;
                appHolder.getSoundPlay().playScore();
                if (winningTube > appHolder.tube_numbers - 1) {
                    winningTube = 0;
                }
            }
            for (int j = 0; j < appHolder.tube_numbers; j++) {
                if (tubeCollections.get(j).getXtube() < -appHolder.getBitmapControl().getTubeWidth()) {
                    tubeCollections.get(j).setXtube(tubeCollections.get(j).getXtube() + appHolder.tube_numbers * appHolder.tubeDistance);
                    int upTubeCollectionY = appHolder.minimumTubeCollection_y + rand.nextInt(appHolder.maximumTubeCollection_y - appHolder.minimumTubeCollection_y + 1);
                    tubeCollections.get(j).setUpTubeCollection_Y(upTubeCollectionY);
                }
                tubeCollections.get(j).setXtube(tubeCollections.get(j).getXtube() - appHolder.tubeVelocity * pauseVelocity);
                canvas.drawBitmap(appHolder.getBitmapControl().getUpTube(), tubeCollections.get(j).getXtube(), tubeCollections.get(j).getUpTube_Y(), null);
                canvas.drawBitmap(appHolder.getBitmapControl().getDownTube(), tubeCollections.get(j).getXtube(), tubeCollections.get(j).getDownTube_Y(), null);
            }
            canvas.drawText("" + scoreCount, (int) (appHolder.SCREEN_WIDTH_X / 2.4), appHolder.SCREEN_HEIGHT_Y / 8, designPaint);
        }
    }

    public void scrollingBomb(Canvas canvas) {

        int y = rand.nextInt(1 + appHolder.SCREEN_HEIGHT_Y);
        int x = (appHolder.SCREEN_WIDTH_X / 2 + tubeCollections.get(1).getXtube() + appHolder.bombDistance*(1+rand.nextInt(3)));
        if (gameState == 1) {
            if (bomb.getX() < -appHolder.getBitmapControl().getBombWidth()) {
                bomb.setY(y);
                bomb.setX(x);
            }
            if (isTheBirdTouchedTheCoin(bomb) && !isShieldOn) {
                isOverBecauseOfBomb = true;
                gameState = 2;
                gameOver();
            } else {
                bomb.setX(bomb.getX() - appHolder.bombVelocity * pauseVelocity);
                canvas.drawBitmap(appHolder.getBitmapControl().getBomb(), bomb.getX(), bomb.getY(), null);
            }
        }
    }

    public void scrollingMoney(Canvas canvas) {
        if (scoreCount >= 3) {
            if (gameState == 1) {
                int y = (tubeCollections.get(1).getDownTube_Y() - appHolder.tubeGap / 2);
                int x = (tubeCollections.get(1).getXtube() + appHolder.getBitmapControl().getTubeWidth() / 2 - appHolder.getBitmapControl().getMoneyWidth() / 2);
                money.setY(y);
                money.setX(x);
                if (isTheBirdTouchedTheCoin(money)) {
                    scoreWhenTheBirdIsTouchingTheMoney = scoreCount;
                    isTheFirstTouch++;
                    if (isTheFirstTouch == 1)
                        moneyCounter++;
                }
                if (scoreWhenTheBirdIsTouchingTheMoney + 1 < scoreCount) {
                    canvas.drawBitmap(appHolder.getBitmapControl().getMoney(), money.getX(), money.getY(), null);
                    isTheFirstTouch = 0;
                }
                money.setX(money.getX() - appHolder.moneyVelocity * pauseVelocity);
            }
        }
    }

    public void scrollingShield(Canvas canvas) {
        int y = rand.nextInt(1 + appHolder.SCREEN_HEIGHT_Y);
        int x = (appHolder.SCREEN_WIDTH_X * 2 + appHolder.SCREEN_WIDTH_X / 2 + tubeCollections.get(0).getXtube() + appHolder.shieldDistance * (1 + rand.nextInt(3)));
        if (gameState == 1) {
            if (shield.getX() < -appHolder.getBitmapControl().getShieldWidth()) {
                shield.setY(y);
                shield.setX(x);
            }
            if (isTheBirdTouchedTheCoin(shield)) {
                scoreWhenBirdTouchTheShield = scoreCount;
                isObstacleOn = false;
            }
            if (scoreWhenBirdTouchTheShield + 3 > scoreCount && scoreCount > 3) {
                circleShield.setX(bird.getX() + appHolder.getBitmapControl().getBirdWidth() / 2 - appHolder.getBitmapControl().getCircleShieldWidth() / 2);
                circleShield.setY(bird.getY() + appHolder.getBitmapControl().getBirdHeight() / 2 - appHolder.getBitmapControl().getCircleShieldHeight() / 2);
                canvas.drawBitmap(appHolder.getBitmapControl().getCircleShield(), circleShield.getX(), circleShield.getY(), null);
                isShieldOn = true;
            } else if (scoreCount > 2) {
                isObstacleOn = true;
                isShieldOn = false;
                canvas.drawBitmap(appHolder.getBitmapControl().getShield(), shield.getX(), shield.getY(), null);
            }
            shield.setX(shield.getX() - appHolder.shieldVelocity * pauseVelocity);
        }
    }

    public void birdAnimation(Canvas canvas) {

        if (gameState == 1) {
            if (bird.getY() < (appHolder.SCREEN_HEIGHT_Y - appHolder.getBitmapControl().getBirdHeight()) || bird.getVelocity() < 0) {
                bird.setVelocity(bird.getVelocity() * pauseVelocity + appHolder.gravityPull * pauseVelocity);
                bird.setY(bird.getY() + bird.getVelocity() * pauseVelocity);
            }
        }
        int currentFrame = bird.getCurrentFrame();
        canvas.drawBitmap(appHolder.getBitmapControl().getBird(currentFrame), bird.getX(), bird.getY(), null);
        currentFrame++;
        if (currentFrame > FlyingBird.maximumFrame) {
            currentFrame = 0;
        }
        bird.setCurrentFrame(currentFrame);
    }

    public void backgroundAnimation(Canvas canvas) {
        bgImage.setX(bgImage.getX() - bgImage.getVelocity() * pauseVelocity);
        if (bgImage.getX() < -appHolder.getBitmapControl().getBackgroundWidth())// if the image finish than set x to the beginning
        {
            bgImage.setX(0);
        }
        canvas.drawBitmap(appHolder.getBitmapControl().getBackground(), bgImage.getX(), bgImage.getY(), null);
        if (bgImage.getX() < -(appHolder.getBitmapControl().getBackgroundWidth() - appHolder.SCREEN_WIDTH_X)) {
            canvas.drawBitmap(appHolder.getBitmapControl().getBackground(), bgImage.getX() +
                    appHolder.getBitmapControl().getBackgroundWidth(), bgImage.getY(), null);
        }
    }

    public void tubeObstacleOn(boolean bool) {
        if (bool) {
            if ((tubeCollections.get(winningTube).getXtube() < bird.getX() + appHolder.getBitmapControl().getTubeWidth())
                    && (tubeCollections.get(winningTube).getUpTubeCollection_Y() > bird.getY()
                    || tubeCollections.get(winningTube).getDownTube_Y() < (bird.getY() + appHolder.getBitmapControl().getBirdHeight()))
                    || (bird.getY() <= 0)) {
                gameState = 2;
                gameOver();
            }
        }
    }

    public boolean isTheBirdTouchedTheCoin(BitmapElement coin) {
        return ((bird.getY() + appHolder.getBitmapControl().getBirdHeight() >= coin.getY() && bird.getY() <= coin.getY()) ||
                (coin.getY() + appHolder.getBitmapControl().getShieldHeight() >= bird.getY() && bird.getY() >= coin.getY())) &&
                (((bird.getX() + appHolder.getBitmapControl().getBirdWidth() >= coin.getX()) && bird.getX() < coin.getX()) ||
                        (coin.getX() + appHolder.getBitmapControl().getShieldWidth() >= bird.getX() && bird.getX() > coin.getX()));
    }

    public void gameOver() {
        appHolder.gameOver = true;
        appHolder.getSoundPlay().playCrash();
        Context mContext = appHolder.gameActivityContext;
        Intent mIntent = new Intent(mContext, GameOverActivity.class);
        mIntent.putExtra("score", scoreCount);
        addDataToDB();
        if (isOverBecauseOfBomb) {
            mIntent.putExtra("isOverBecauseOfBomb", isOverBecauseOfBomb);
        }
        mContext.startActivity(mIntent);
    }

    public void addDataToDB() {
        if (!appHolder.guestMode) {
            reference.child(userID).child("moneyCount").setValue(moneyCounter);
            appHolder.getUser().setMoneyCount(moneyCounter);
            if (scoreCount > bestScore) {
                bestScore = scoreCount;
                reference.child(userID).child("bestScore").setValue(bestScore);
                appHolder.getUser().setBestScore(scoreCount);
            }
        }
    }

    public void getDataFromDB() {
        if (!appHolder.guestMode) {
            reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User userProfile = snapshot.getValue(User.class);
                    bestScore = userProfile.getBestScore();
                    moneyCounter = userProfile.getMoneyCount();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

}


