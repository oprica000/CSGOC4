package com.victor.oprica.csgoc4;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView img_c4;
    Handler mHandler = new Handler();
    MediaPlayer audio1, audio2;
    int bomb_time;
    TextView tv_bomb_time, tv_code;
    boolean is_armed, is_pressed;
    ProgressBar pb_progress;
    Random rng;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img_c4 = findViewById(R.id.img_c4);
        bomb_time = 45;
        tv_bomb_time = findViewById(R.id.tv_bomb_time);
        tv_code = findViewById(R.id.tv_code);
        is_armed = is_pressed = false;
        pb_progress = findViewById(R.id.pb_progress);
        rng = new Random();

        img_c4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent event) {

                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        is_pressed = true;
                        if(is_armed){

                            pb_progress.setMax(5000);
                            pb_progress.setProgress(0);
                            new CountDownTimer(5000, 5) {
                                public void onTick(long millisUntilFinished) {
                                    if(is_pressed && is_armed) {
                                        pb_progress.setProgress((int) (5000 - millisUntilFinished));
                                        tv_bomb_time.setText(String.valueOf((millisUntilFinished) / 1000) + "." + String.valueOf((millisUntilFinished) % 1000));
                                    }
                                    else{
                                        pb_progress.setProgress(0);
                                        tv_bomb_time.setText("");
                                        cancel();
                                    }
                                }
                                public void onFinish() {
                                    pb_progress.setProgress(0);
                                    tv_bomb_time.setText("");
                                }
                            }.start();
                            disarm_start();
                            mHandler.postDelayed(mDisarmRunnable, 5000);


                        }
                        else{
                            if(!mHandler.hasMessages(0)){

                                pb_progress.setMax(3200);
                                pb_progress.setProgress(0);
                                new CountDownTimer(3200, 5) {

                                    public void onTick(long millisUntilFinished) {
                                        if (is_pressed) {
                                            pb_progress.setProgress((int) (3200 - millisUntilFinished));
                                            tv_bomb_time.setText(String.valueOf((millisUntilFinished) / 1000) + "." + String.valueOf((millisUntilFinished) % 1000));
                                            if(millisUntilFinished % 225 < 20 && tv_code.getText().length() < 8 && millisUntilFinished < 2550){
                                                tv_code.setText(tv_code.getText().toString() + rng.nextInt(10));
                                                c4_button_press();
                                            }
                                        }
                                        else {
                                            pb_progress.setProgress(0);
                                            tv_bomb_time.setText("");
                                            cancel();
                                        }
                                    }
                                    public void onFinish() {
                                        pb_progress.setProgress(0);
                                        tv_bomb_time.setText("");
                                    }
                                }.start();
                                initiate_c4();
                                mHandler.postDelayed(mBoomRunnable, 3200);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(!is_armed) {
                            mHandler.removeCallbacks(mBoomRunnable);
                            stopPlayer1();
                            tv_code.setText("");
                        }
                        else{
                            mHandler.removeCallbacks(mDisarmRunnable);
                            stopPlayer2();
                        }
                        is_pressed = false;
                        return true;
                }
                return false;
            }
        });

    }

    private void stopPlayer1(){
        if(audio1 != null){
            audio1.release();
            audio1 = null;
            //Toast.makeText(MainActivity.this, "Released 1", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlayer2(){
        if(audio2 != null){
            audio2.release();
            audio2 = null;
            //Toast.makeText(MainActivity.this, "Released 2", Toast.LENGTH_SHORT).show();
        }
    }

    Runnable mBoomRunnable = new Runnable() {
        @Override
        public void run() {
            //Toast.makeText(MainActivity.this, "Boom", Toast.LENGTH_SHORT).show();
            is_armed = true;
            beep_c4();
            mHandler.postDelayed(mExplodeRunnable, 41500);
        }
    };

    Runnable mExplodeRunnable = new Runnable() {
        @Override
        public void run() {
            is_armed = false;
            explode_c4();
        }
    };

    Runnable mDisarmRunnable = new Runnable() {
        @Override
        public void run() {
            if(is_armed) {
                disarm_finish();
                tv_code.setText("");
                mHandler.removeMessages(0);
                bomb_time = 45;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeMessages(0);
        stopPlayer1();
        stopPlayer2();
    }

    private void explode_c4(){
        bomb_time = 45;
        tv_code.setText("");
        if(audio2 == null){
            audio2 = MediaPlayer.create(getApplicationContext(), R.raw.c4_explode);
            audio2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer2();
                    t_win();
                }
            });
        }

        audio2.start();
    }

    private void t_win(){
        if(audio2 == null){
            audio2 = MediaPlayer.create(getApplicationContext(), R.raw.t_win);
            audio2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer2();
                }
            });
        }

        audio2.start();
    }

    void beep_c4(){
        if(audio1 == null){
            audio1 = MediaPlayer.create(getApplicationContext(), R.raw.bomb_beep_full);
            audio1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer1();
                }
            });
        }

        audio1.start();
    }

    void initiate_c4(){
        if(audio1 == null){
            audio1 = MediaPlayer.create(getApplicationContext(), R.raw.c4_initiate);
            audio1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer1();
                }
            });
        }

        audio1.start();
    }

    void c4_button_press(){
        if(audio2 == null){
            audio2 = MediaPlayer.create(getApplicationContext(), R.raw.c4_key_press);
            audio2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer2();
                }
            });
        }

        audio2.start();
    }

    void disarm_start(){
        if(audio2 == null){
            audio2 = MediaPlayer.create(getApplicationContext(), R.raw.c4_disarmstart);
            audio2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer2();
                }
            });
        }

        audio2.start();
    }

    void disarm_finish(){
        mHandler.removeMessages(0);
        is_armed = false;
        bomb_time = 45;
        stopPlayer1();
        if(audio2 == null){
            audio2 = MediaPlayer.create(getApplicationContext(), R.raw.c4_disarmfinish);
            audio2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer2();
                    announcer_bomb_defused();
                }
            });
        }

        audio2.start();
    }

    void announcer_bomb_defused(){
        if(audio2 == null){
            audio2 = MediaPlayer.create(getApplicationContext(), R.raw.announcer_bomb_defused);
            audio2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer2();
                    ct_win();
                }
            });
        }

        audio2.start();
    }

    void ct_win(){
        if(audio2 == null){
            audio2 = MediaPlayer.create(getApplicationContext(), R.raw.ct_win);
            audio2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlayer2();
                }
            });
        }

        audio2.start();
    }
}