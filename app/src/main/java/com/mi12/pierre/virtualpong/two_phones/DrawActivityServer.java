package com.mi12.pierre.virtualpong.two_phones;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;

import com.mi12.R;
import com.mi12.pierre.virtualpong.CST;
import com.mi12.pierre.virtualpong.Player;

public class DrawActivityServer extends AppCompatActivity implements SensorEventListener {

    private GameView gameView;
    private SensorManager sensorManager;
    private Sensor gravity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.findViewById(android.R.id.content).setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);


        setContentView(R.layout.activity_bouncing_ball);

        //get screen size
        Display screenSize = getWindowManager().getDefaultDisplay();
        int screenWidth = screenSize.getWidth();
        int screenHeight = screenSize.getHeight();

        //creation of players
        Player player = new Player(screenWidth * CST.PLAYER_PERCENT_X,
                screenHeight * CST.PLAYER_PERCENT_Y_BTM, (int) (screenWidth * CST.PLAYER_PERCENT_W),
        (int) (screenHeight * CST.PLAYER_PERCENT_H), Color.BLUE);
        Player opp = new Player(screenWidth * CST.PLAYER_PERCENT_X,
                screenHeight * CST.PLAYER_PERCENT_Y_TOP, (int) (screenWidth * CST.PLAYER_PERCENT_W),
        (int) (screenHeight * CST.PLAYER_PERCENT_H), Color.RED);

        gameView = new GameView(this, player, opp, screenWidth, screenHeight);
        setContentView(gameView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        //start server communication which will start a thread to send data
        ServerComAsyncTask comAT = new ServerComAsyncTask(this, gameView);
        comAT.execute();
    }

    protected void onPause() {
        super.onPause();
        gameView.pause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    protected void onResume() {
        super.onResume();
        gameView.resume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, gravity, CST.SENSOR_SAMPLING);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_GRAVITY) {
            float x = event.values[0];
            if (x > CST.THRESHOLD_SENSOR) {
                gameView.movePlayer(CST.MOVE_LEFT_LOCAL);
            } else if (x < -CST.THRESHOLD_SENSOR) {
                gameView.movePlayer(CST.MOVE_RIGHT_LOCAL);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    class GameView extends SurfaceView implements Runnable {

        public Thread thread = null;
        private SurfaceHolder holder;
        private boolean status = false;

        //ball
        private Bitmap ball;
        private float x_ball, y_ball;
        private float dx_ball, dy_ball;

        //players
        private Player player;
        private Player opp;
        private Bitmap playerBTM;
        private Bitmap oppBTM;

        //screen size
        private int screenWidth;
        private int screenHeight;

        //player = 1 | opp = -1 | 0 : other
        private int lastTouch;

        //boolean for the beginning of the activity
        //the game should not start directly
        private Boolean isFirstLaunch;


        public GameView(Context context, Player _player, Player _opp, int _width, int _height) {
            super(context);
            this.player = _player;
            this.opp = _opp;
            this.screenHeight = _height;
            this.screenWidth = _width;
            this.isFirstLaunch = true;
            this.playerBTM = BitmapFactory.decodeResource(getResources(), R.drawable.player);
            this.playerBTM = Bitmap.createScaledBitmap(playerBTM, player.getWidth(), player.getHeight(), false);
            this.oppBTM= BitmapFactory.decodeResource(getResources(), R.drawable.opp);
            this.oppBTM = Bitmap.createScaledBitmap(oppBTM, opp.getWidth(), opp.getHeight(), false);

            holder = getHolder();

            //get ball
            ball = BitmapFactory.decodeResource(getResources(), R.drawable.yellowball);
            ball = Bitmap.createScaledBitmap(ball, CST.BALL_SCALE_X, CST.BALL_SCALE_Y, false);

            //initial position and speed and lastTouch
            initBall(CST.BALL_INIT_POS_X,CST.BALL_INIT_POS_Y,CST.BALL_INIT_DX,CST.BALL_INIT_DY);

        }

        public void setIsFirstLaunch(boolean state){isFirstLaunch = state;}

        public GamePositions getPositions() {
            GamePositions gp = new GamePositions(opp.getX() / screenWidth, player.getX() / screenWidth,
            x_ball / screenWidth, y_ball / screenHeight, player.getScore(), opp.getScore());
            return  gp;
        }


        public void moveOpponent(String str) {
            if (str.equals(CST.MOVE_RIGHT_LOCAL) && opp.getX() + oppBTM.getWidth() < screenWidth) {
                opp.moveRight();
            } else if (str.equals(CST.MOVE_LEFT_LOCAL) && opp.getX() > 0) {
                opp.moveLeft();
            }
        }

        public void movePlayer(String str){
            if(str.equals(CST.MOVE_RIGHT_LOCAL) && player.getX() + playerBTM.getWidth() < screenWidth){
                player.moveRight();
            } else if(str.equals(CST.MOVE_LEFT_LOCAL) && player.getX() > 0){
                player.moveLeft();
            }
        }

        public void initBall(int x, int y, int dx, int dy){
            x_ball = x;
            y_ball = y;
            //dx_ball random [dx-2 ; dx+2]
            //dy_ball random [dy-2 ; dy+2]
            dx_ball = dx-2 + (int)(Math.random() * ((4) + 1));
            dy_ball = dy-2 + (int)(Math.random() * ((4) + 1));
            lastTouch = -1; //server : last_touch == opp at the beginning (opp on top)
        }

        public void run(){
            Canvas c;
            while (status){
                if (!holder.getSurface().isValid()){
                    continue;
                }

                //deplacement balle + rebonds mur/joueur
                x_ball += dx_ball;
                if (x_ball <= 0 || x_ball > screenWidth - ball.getWidth()){
                    dx_ball = 0 - dx_ball;
                }
                y_ball += dy_ball;

                //balle sort en haut => point player
                if (y_ball <= 0 ){
                    initBall(CST.BALL_INIT_POS_X,CST.BALL_INIT_POS_Y,CST.BALL_INIT_DX,CST.BALL_INIT_DY);
                    player.addOnePoint();
                }
                //balle sort en bas => point opp
                if(y_ball > screenHeight - ball.getHeight()){
                    initBall(CST.BALL_INIT_POS_X,CST.BALL_INIT_POS_Y,CST.BALL_INIT_DX,CST.BALL_INIT_DY);
                    opp.addOnePoint();
                }

                //rebond sur opp (joueur en haut)
                if(lastTouch != -1 &&
                        y_ball <= opp.getY() + oppBTM.getHeight() &&
                        x_ball + ball.getWidth() <= opp.getX() + oppBTM.getWidth() &&
                        x_ball >= opp.getX()){
                    dy_ball = 0 - dy_ball;
                    lastTouch = -1;
                }
                //rebond sur player (joueur en bas)
                if(lastTouch != 1 &&
                        y_ball + ball.getHeight() >= player.getY() &&
                        x_ball + ball.getWidth() <= player.getX() + playerBTM.getWidth() &&
                        x_ball >= player.getX()) {
                    dy_ball = 0 - dy_ball;
                    lastTouch = 1;
                }

                //dessin du jeu
                //lock Before painting
                c = holder.lockCanvas();
                c.drawARGB(255, 0, 0, 0);

                player.draw(c, playerBTM);
                opp.draw(c, oppBTM);

                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(CST.SCORE_TXT_SIZE);
                c.drawText(Integer.toString(player.getScore()), CST.SCORE_X,
                        screenHeight * CST.SCORE_PERCENT_BTM_Y, paint);
                paint.setColor(Color.RED);
                c.drawText(Integer.toString(opp.getScore()), CST.SCORE_X,
                        screenHeight * CST.SCORE_PERCENT_TOP_Y, paint);
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(CST.LINE_WIDTH);
                c.drawLine(0, screenHeight / 2f, screenWidth, screenHeight / 2f, paint);

                c.drawBitmap(ball, x_ball, y_ball, null);
                holder.unlockCanvasAndPost(c);
            }
        }

        public void pause(){
            status = false;
            while(true){
                try{
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            thread = null;
        }

        public void resume(){
            status = true;
            thread = new Thread(this);
            //we don't start game if the activity is created
            //the server thread should get message from the other player before
            if(!isFirstLaunch) {
                thread.start();
            }
        }
    }
}


