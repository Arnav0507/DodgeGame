package com.example.dodgegame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GestureDetectorCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    GameSurface gameSurface;
    MediaPlayer bgMusic;
    SoundPool sp;
    int collisionSound;
    int score = 0;
    final int numEnemies = 2;
    int timeLeft;
    boolean collision = false;
    boolean ballCollision = false;
    boolean play = true;
    boolean ballPlay = true;
    boolean offScreen = false;
    boolean isTapped = false;
    boolean isFlung = false;
    boolean fbCreated = false;
    private GestureDetectorCompat gestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bgMusic = MediaPlayer.create(this, R.raw.backgroundmusic);
        bgMusic.setLooping(true);
        bgMusic.start();
        sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        collisionSound = sp.load(MainActivity.this, R.raw.collisionsound, 1);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
        gestureDetector = new GestureDetectorCompat(this, new GestureListener());
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeft = (int)(millisUntilFinished/1000);
            }
            public void onFinish() {
                setContentView(R.layout.activity_finished);
                TextView scoreTextView = findViewById(R.id.scoreText);
                scoreTextView.setText("You Scored: " + score);
                bgMusic.stop();
            }

        }.start();
    }
    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }
    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }
    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener{
         Thread gameThread;
         SurfaceHolder holder;
         volatile boolean running = false;
         Bitmap rbImage, rb, lbImage, lb, injImage, inj, fbImage, fb;
         ArrayList<Bitmap> enemyList = new ArrayList<>();
         ArrayList<Integer> enemyX = new ArrayList<>();
         ArrayList<Integer> enemyY = new ArrayList<>();

         int rbX = 0;
         int fbYnew = 0;
         int fbX, fbY;
         int fbSpeed = 5;
         int enemySpeed = 5;
         int flip = 1;
         Paint paintProperty;
         int screenWidth;
         int screenHeight;

         private float gravity[];
        // Magnetic rotational data
        private float magnetic[]; //for magnetic rotational data
        private float accels[] = new float[3];
        private float mags[] = new float[3];
        private float[] values = new float[3];

        // azimuth, pitch and roll
        private float azimuth;
        private float pitch;
        private float roll;

         public GameSurface(Context context) {
             super(context);
             holder = getHolder();
             Display screenDisplay = getWindowManager().getDefaultDisplay();
             Point sizeOfScreen = new Point();
             screenDisplay.getSize(sizeOfScreen);
             screenWidth = sizeOfScreen.x;
             screenHeight = sizeOfScreen.y;

             rbImage = BitmapFactory.decodeResource(getResources(), R.drawable.runningback);
             rb = Bitmap.createScaledBitmap(rbImage, 200, 250, false);
             lbImage = BitmapFactory.decodeResource(getResources(), R.drawable.linebacker);
             lb = Bitmap.createScaledBitmap(lbImage, 200, 250, false);
             injImage = BitmapFactory.decodeResource(getResources(), R.drawable.injured);
             inj = Bitmap.createScaledBitmap(injImage, 200, 250, false);
             fbImage = BitmapFactory.decodeResource(getResources(), R.drawable.football);
             fb = Bitmap.createScaledBitmap(fbImage, 50, 50, false);


             for(int i = 0; i < numEnemies; i++){
                 enemyList.add(Bitmap.createScaledBitmap(lbImage, 200, 250, false));
                 int currWidth = enemyList.get(i).getWidth();
                 enemyX.add((int)(Math.random()*(screenWidth-currWidth)));
                 enemyY.add(-1*enemyList.get(i).getHeight()/2);
             }

             SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
             Sensor accelerometer = sensorManager.getDefaultSensor((Sensor.TYPE_ACCELEROMETER));
             Sensor magnetometer = sensorManager.getDefaultSensor((Sensor.TYPE_MAGNETIC_FIELD));

             sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_GAME);
             sensorManager.registerListener(this, magnetometer, sensorManager.SENSOR_DELAY_GAME);
             paintProperty = new Paint();
         }
         @Override
        public void onSensorChanged(SensorEvent event) {


             switch (event.sensor.getType()) {
                 case Sensor.TYPE_MAGNETIC_FIELD:
                     mags = event.values.clone();
                     break;
                 case Sensor.TYPE_ACCELEROMETER:
                     accels = event.values.clone();
                     break;
             }

             if (mags != null && accels != null) {
                 gravity = new float[9];
                 magnetic = new float[9];
                 SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
                 float[] outGravity = new float[9];
                 SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X,SensorManager.AXIS_Z, outGravity);
                 SensorManager.getOrientation(outGravity, values);

                 azimuth = values[0] * 57.2957795f;
                 pitch =values[1] * 57.2957795f;
                 roll = values[2] * 57.2957795f;
                 mags = null;
                 accels = null;

                 if(azimuth > 0){
                     flip = -1;
                     if(azimuth > 15){
                         flip = -3;
                     }
                 }
                 else if(azimuth < 0){
                     flip = 1;
                     if(azimuth < -15){
                         flip = 3;
                     }
                 }
                 else{
                     flip = 0;
                 }

             }
         }
        @Override
        public void onAccuracyChanged(Sensor sensor, int i){

        }

        @Override
        public void run(){
             while(running){
                 if(!holder.getSurface().isValid()){
                     continue;
                 }
                 Canvas canvas = holder.lockCanvas();
                 Paint paint = new Paint();
                 paint.setColor(Color.BLUE);
                 paint.setStyle(Paint.Style.FILL);
                 canvas.drawPaint(paint);
                 paint.setColor(Color.YELLOW);
                 paint.setTextSize(40);
                 canvas.drawText("Score: " + score + " Time Left: " + timeLeft, 10, 40, paint);
                 if(collision){
                     canvas.drawBitmap(inj, (screenWidth/2)-rb.getWidth()/2+rbX, screenHeight/2+rb.getHeight(), null);
                 }
                 else{
                     canvas.drawBitmap(rb, (screenWidth/2)-rb.getWidth()/2+rbX, screenHeight/2+rb.getHeight(), null);
                 }
                 if(isTapped){
                     enemySpeed = 15;
                 }
                 else{
                     enemySpeed = 5;
                 }
                 if(isFlung && !fbCreated){
                     fbX = (screenWidth/2)-rb.getWidth()/2+rbX;
                     fbY = screenHeight/2+rb.getHeight();
                     fbCreated = true;
                 }
                 for(int i = 0; i < enemyList.size(); i++){
                     canvas.drawBitmap(enemyList.get(i), enemyX.get(i), enemyY.get(i), null);
                     if(isFlung){
                         canvas.drawBitmap(fb, fbX, fbY, null);
                         fbY-=fbSpeed;
                         if(fbY<0){
                             isFlung = false;
                             fbCreated = false;
                             fbY = screenHeight/2+rb.getHeight();
                         }
                     }
                     enemyY.set(i, enemyY.get(i)+enemySpeed);
                     int rbXmin = (screenWidth/2)-rb.getWidth()/2+rbX;
                     int rbXmax = rbXmin+rb.getWidth();
                     int rbYmin = screenHeight/2+rb.getHeight();
                     int rbYmax = rbYmin + rb.getHeight();
                     int lbXmin = enemyX.get(i);
                     int lbXmax = lbXmin + lb.getWidth();
                     int lbYmin = enemyY.get(i);
                     int lbYmax = lbYmin + lb.getHeight();
                     if((lbXmin >= rbXmin && lbXmin <= rbXmax && lbYmin >= rbYmin && lbYmin <= rbYmax) ||
                             (lbXmax >= rbXmin && lbXmax <= rbXmax && lbYmin >= rbYmin && lbYmin <= rbYmax)||
                     (lbXmin >= rbXmin && lbXmin <= rbXmax && lbYmax >= rbYmin && lbYmax <= rbYmax)||
                     (lbXmax >= rbXmin && lbXmax <= rbXmax && lbYmax >= rbYmin && lbYmax <= rbYmax)){
                         if(!ballCollision) {
                             collision = true;
                         }
                         if(collision && play){
                             sp.play(collisionSound, 1.0f, 1.0f, 0, 0, 1.0f);
                             play = false;
                         }
                     }
                     int fbXmin = fbX;
                     int fbXmax = fbXmin+fb.getWidth();
                     int fbYmin = fbY;
                     int fbYmax = fbYmin+fb.getHeight();
                     if((fbXmin >= lbXmin && fbXmin <= lbXmax && fbYmin >= lbYmin && fbYmin <= lbYmax) ||
                             (fbXmax >= lbXmin && fbXmax <= lbXmax && fbYmin >= lbYmin && fbYmin <= lbYmax)||
                             (fbXmin >= lbXmin && fbXmin <= lbXmax && fbYmax >= lbYmin && fbYmax <= lbYmax)||
                             (fbXmax >= lbXmin && fbXmax <= lbXmax && fbYmax >= lbYmin && fbYmax <= lbYmax)){
                         ballCollision = true;
                         if(ballCollision && ballPlay){
                             sp.play(collisionSound, 1.0f, 1.0f, 0, 0, 1.0f);
                             ballPlay = false;
                         }
                     }
                     if (enemyY.get(i) > screenHeight+enemyList.get(i).getHeight()/2){
                         int currWidth = enemyList.get(i).getWidth();
                         enemyX.set(i, (int)(Math.random()*(screenWidth-currWidth)));
                         enemyY.set(i, -1*enemyList.get(i).getHeight()/2);
                         offScreen = true;
                     }
                 }

                 if (offScreen){
                     offScreen = false;
                     if (collision) {
                         score -= 1;
                     } else {
                         score += 1;
                     }
                     collision = false;
                     ballCollision = false;
                     play = true;
                     ballPlay = true;
                 }
                 if (flip > 0) {
                     if(rbX + flip < screenWidth/2 - rb.getWidth()/2) {
                         rbX += flip;
                     }
                 } else if(flip < 0) {
                     if (rbX + flip > -1*screenWidth/2 + rb.getWidth()/2){
                         rbX += flip;
                     }
                 }
                 holder.unlockCanvasAndPost(canvas);

             }

        }
        public void resume(){
             running = true;
             gameThread = new Thread(this);
             gameThread.start();
        }

        public void pause(){
             running = false;
             while(true){
                 try{
                     gameThread.join();
                 }
                 catch(InterruptedException e){

                 }
             }
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
            isFlung = true;
            return super.onFling(e1, e2, velocityX, velocityY);
        }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e){
            isTapped = !isTapped;
            return super.onSingleTapConfirmed(e);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}