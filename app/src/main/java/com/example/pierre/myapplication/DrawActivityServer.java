package com.example.pierre.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class DrawActivityServer extends AppCompatActivity implements SensorEventListener {

    private Paint paint = new Paint();
    private ServerComAsyncTask comAT;

    private int posX;
    private int posY;
    private GameView gameView;


    private int playerX;
    private int playerY;
    private SensorManager sensorManager;
    private Sensor gravity;
    private BallAsyncTask ballAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        posX = 500;
        posY = 1400;

        playerX = 500;
        playerY = 300;
        gameView = new GameView(this, posX, posY, playerX, playerY);
        setContentView(gameView);
        gameView.setWillNotDraw(false);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        comAT = new ServerComAsyncTask(this, gameView);
        comAT.execute();

        ballAT = new BallAsyncTask(gameView);
        ballAT.execute();
    }

    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_GRAVITY) {
            float x = event.values[0];
            if (x > 1) {
                gameView.movePlayer("g");
//                client.setDirection("g");
                comAT.setDirection(gameView.getPositions());
            } else if (x < -1) {
                gameView.movePlayer("d");
//                client.setDirection("d");
                comAT.setDirection(gameView.getPositions());

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    class GameView extends View {
        private int x;
        private int y;
        private int px;
        private int py;

        private int ball_x;
        private int ball_y;

        private int dir;

        public GameView(Context context, int _x, int _y, int _px, int _py) {
            super(context);
            x = _x;
            y = _y;
            px = _px;
            py = _py;
            ball_x = 500;
            ball_y = 500;
            dir = 10;
        }

        public String getPositions() {
            return Integer.toString(x) + " " + Integer.toString(px);
        }


        public void move(String str) {
            if (str.equals("d")) {
                x += 10;
            } else if (str.equals("g")) {
                x -= 10;
            }
            invalidate();

        }

        public void movePlayer(String str) {
            if (str.equals("d")) {
                px += 10;
            } else if (str.equals("g")) {
                px -= 10;
            }
            invalidate();
        }

        public void moveBall() throws InterruptedException {
            ball_y += dir;
            if (ball_y < py && ball_x <= px + 100 && ball_x >= px - 100) {
                dir *= -1;
                Log.w("Change pos", "BAS");
            } else if (ball_y > y && ball_x <= x + 100 && ball_x >= x - 100) {
                Log.w("Change pos", "HAUT");
                dir *= -1;
            }
            invalidate();

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int radius = 40;
            Paint p = new Paint();
            p.setColor(Color.RED);
//            canvas.drawCircle(x, y, radius, p);
            canvas.drawRect(x - 100, y - 52, x + 100, y + 25, p);
            p.setColor(Color.BLUE);
            canvas.drawRect(px - 100, py - 52, px + 100, py + 25, p);
            p.setColor(Color.YELLOW);
            canvas.drawCircle(ball_x, ball_y, 20, p);

        }

    }

}
