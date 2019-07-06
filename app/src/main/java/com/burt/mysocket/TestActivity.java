package com.burt.mysocket;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.burt.mysocket.view.WaveProgressView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;
    WaveProgressView waveView;
    TextView textProgress;
    private AppCompatButton btn_test;
    private AppCompatButton btn_con;
    private AppCompatButton btn_send;
    private Handler handler;
    String receivedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), "连接断开", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
//                        Toast.makeText(getApplicationContext(), receivedMessage, Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "收到消息", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();
        initView();
        initListener();
    }

    private void initView() {
        btn_test = findViewById(R.id.btn_test);
        btn_con = findViewById(R.id.btn_con);
        btn_send = findViewById(R.id.btn_send);

        waveView = findViewById(R.id.wave_progress);
        textProgress = findViewById(R.id.text_progress);
        waveView.setTextView(textProgress);

        waveView.setProgressNum(0, 90, 1000);
    }

    private void initListener() {

        waveView.setOnAnimationListener(new WaveProgressView.OnAnimationListener() {
            @Override
            public String howToChangeText(float interpolatedTime, float updateNum, float maxNum) {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String s = decimalFormat.format(interpolatedTime * updateNum / maxNum * 100) + "%";
                return s;
            }

            @Override
            public String changeTextWithDiff(float interpolatedTime, float oldVal, float diff, float updateNum, float maxNum) {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String s = decimalFormat.format((interpolatedTime * diff + oldVal) / maxNum * 100) + "%";
                return s;
            }

            @Override
            public float howToChangeWaveHeight(float percent, float waveHeight) {
                return (1 - percent) * waveHeight;
            }

        });

        btn_test.setOnClickListener(this);
        btn_con.setOnClickListener(this);
        btn_send.setOnClickListener(this);

        TaskCenter.getInstance().setOnServerConnectedListener(new TaskCenter.OnServerConnectedListener() {
            @Override
            public void onConnected() {
                Message msg = Message.obtain();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        });

        TaskCenter.getInstance().setOnServerDisconnectedListener(new TaskCenter.OnServerDisconnectedListener() {
            @Override
            public void onDisconnected(IOException e) {
                Message msg = Message.obtain();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        });

        TaskCenter.getInstance().setOnReceivedListener(new TaskCenter.OnReceivedListener() {
            @Override
            public void onReceive(String receivedMessage) {
//                receivedMessage = receivedmsg;

//                Message msg = Message.obtain();
//                msg.what = 2;
//                handler.sendMessage(msg);
                Log.d("abc", "onReceive: "+receivedMessage);

            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_con:
                connect();
                break;
            case R.id.btn_send:
                send();
                break;
            case R.id.btn_test:
                test();
                break;
        }

    }

    private void test() {
        waveView.setProgressNum(90, 80, 1000);
    }

    private void send() {
        try {
            TaskCenter.getInstance().send("it will be good".getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        TaskCenter.getInstance().connect("192.168.1.145", 9999);
    }



}
