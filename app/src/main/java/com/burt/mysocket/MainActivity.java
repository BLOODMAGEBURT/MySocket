package com.burt.mysocket;

import android.content.Intent;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String HOST = "192.168.1.81";//主机地址
    private static final int PORT = 9999;//端口号
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;

    Socket socket = null;
    OutputStream output = null;
    InputStream input = null;
    StringBuffer sb = null;
    WaveProgressView waveView;
    TextView textProgress;
    int num = 90;
    int oldVal = 0;
    int newVal = 0;

    private int status = 0;
    private String Now = "@RUN_Now!";
    private String Ready = "@Set_Mod3!";
    private String Start = "@Set_Mod5!";

    private Boolean isRuning = true; // 是否在注射中

    private AppCompatButton toTest;

    private final MyHandler handler = new MyHandler(this);
    private static AppCompatButton connect;
    private AppCompatButton ready;
    private AppCompatButton start;
    private Timer timer;
    private TimerTask alarmTask;
    private TextView tv_con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();
        initView();
    }

    private void initView() {
        connect = findViewById(R.id.btn_con);
        ready = findViewById(R.id.btn_ready);
        start = findViewById(R.id.btn_start);
        tv_con = findViewById(R.id.tv_con); // 测试连接

        toTest = findViewById(R.id.btn_test);
        waveView = findViewById(R.id.wave_progress);
        textProgress = findViewById(R.id.text_progress);
        waveView.setTextView(textProgress);
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


        waveView.setProgressNum(oldVal, num, 1000);
        oldVal = num;

        connect.setOnClickListener(this);
        ready.setOnClickListener(this);
        start.setOnClickListener(this);
        toTest.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_con:
                tv_con.setText("连接中…");
                connect();

                break;
            case R.id.btn_ready:
                Toast.makeText(getApplicationContext(), "准备", Toast.LENGTH_SHORT).show();
                toReady();
                break;
            case R.id.btn_start:
                Toast.makeText(getApplicationContext(), "运行", Toast.LENGTH_SHORT).show();
                toStart();
                break;
            case R.id.btn_test:
                startActivity(new Intent(MainActivity.this, TestActivity.class));
                break;
        }
    }


    private void connect() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket();
                    SocketAddress socAddress = new InetSocketAddress(HOST, PORT);
                    socket.connect(socAddress, 3300);    //连接超时时间3秒3
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    Bundle bundle = new Bundle();
                    bundle.putString("connectMsg", "连接成功");  //往Bundle中存放数据
                    msg.setData(bundle);//mes利用Bundle传递数据
                    handler.sendMessage(msg);

                    socket.close();
                } catch (IOException e) {
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    Bundle bundle = new Bundle();
                    bundle.putString("connectMsg", "连接失败，请连接正确的WIFI");  //往Bundle中存放数据
                    msg.setData(bundle);//mes利用Bundle传递数据
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        });
    }

    private void toReady() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    socket = new Socket(HOST, PORT);
                    output = socket.getOutputStream();
//            output.write((Now + "\n").getBytes("utf-8"));// 把msg信息写入输出流中
                    output.write((Ready).getBytes("utf-8"));// 把msg信息写入输出流中
                    output.flush();

                    //--------接收服务端的返回信息-------------
                    socket.shutdownOutput(); // 一定要加上这句，否则收不到来自服务器端的消息返回 ，意思就是结束msg信息的写入
                    input = socket.getInputStream();
                    byte[] b = new byte[1024];
                    int len;
                    sb = new StringBuffer();
                    while ((len = input.read(b)) != -1) {
                        sb.append(new String(b, 0, len, Charset.forName("UTF-8")));// 得到返回信息
                    }
                    Message msg = handler.obtainMessage();
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("readyMsg", sb.toString());  //往Bundle中存放数据
                    msg.setData(bundle);//mes利用Bundle传递数据
                    handler.sendMessage(msg);

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void toStart() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    socket = new Socket(HOST, PORT);
                    output = socket.getOutputStream();
//            output.write((Now + "\n").getBytes("utf-8"));// 把msg信息写入输出流中
                    output.write((Start).getBytes("utf-8"));// 把msg信息写入输出流中
                    output.flush();

                    //--------接收服务端的返回信息-------------
                    socket.shutdownOutput(); // 一定要加上这句，否则收不到来自服务器端的消息返回 ，意思就是结束msg信息的写入
                    input = socket.getInputStream();
                    byte[] b = new byte[1024];
                    int len;
                    sb = new StringBuffer();
                    while ((len = input.read(b)) != -1) {
                        sb.append(new String(b, 0, len, Charset.forName("UTF-8")));// 得到返回信息
                    }
                    Message msg = handler.obtainMessage();
                    msg.what = 2;
                    Bundle bundle = new Bundle();
                    bundle.putString("startMsg", sb.toString());  //往Bundle中存放数据
                    msg.setData(bundle);//mes利用Bundle传递数据
                    handler.sendMessage(msg);

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getNow() {
//        mThreadPool.execute();
    }

    private void getNowLoop() {

        timer = new Timer();
        alarmTask = new AlarmTask();

        timer.schedule(alarmTask, 0, 2 * 1000);//0毫秒后每2秒执行该任务一次
    }

    private void complicateWay() {

        HttpUtil.requestServer("are you ok?", new CallListener() {
            @Override
            public void onResult(String jsonresult) {

                Log.d("abc", jsonresult);
            }

            @Override
            public void onError() {

            }
        });

    }

    class MyHandler extends Handler {

        float oldVal = 90;
        float newVal = 0;
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            System.out.println(msg);
            if (mActivity.get() == null) {
                return;
            }
            MainActivity activity = mActivity.get();

            switch (msg.what) {
                case 0:
                    String connectedMsg = msg.getData().getString("connectMsg");//接受msg传递过来的参数
                    Toast.makeText(activity, connectedMsg, Toast.LENGTH_LONG).show();
                    tv_con.setText("测试连接");
                    break;
                case 1:
                    String readyMsg = msg.getData().getString("readyMsg");//接受msg传递过来的参数
                    Toast.makeText(activity, readyMsg, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    String startMsg = msg.getData().getString("startMsg");//接受msg传递过来的参数
                    Toast.makeText(activity, startMsg, Toast.LENGTH_SHORT).show();

                    getNowLoop();

                    break;

                case 3:
                    String receivedMsg = msg.getData().getString("nowMsg");//接受msg传递过来的参数
                    Log.d("abc", "receivedMsg:" + receivedMsg);
                    int[] formatedMsg = formatTheMsg(receivedMsg.replace("{", "").replace("}", ""));

                    newVal = (float) (formatedMsg[2] - formatedMsg[3]) / (float) (formatedMsg[4] - formatedMsg[3]) * 100;
                    activity.waveView.setProgressNum(oldVal, newVal, 1000);
                    oldVal = newVal;

                    Toast.makeText(activity, Arrays.toString(formatedMsg), Toast.LENGTH_SHORT).show();
                    break;
            }
        }


        private int[] formatTheMsg(String receivedMsg) {

            //正则表达式，用于匹配非数字串，+号用于匹配出多个非数字串。
            String regex = "[^0-9]+";
            Pattern pattern = Pattern.compile(regex);
            //用定义好的正则表达式拆分字符串，把字符串中的数字留出来
            String[] str = pattern.split(receivedMsg);
            Log.d("abd", "formatTheMsg: " + Arrays.toString(str));
            // 转换为 int 数组
            int[] intTemp = new int[str.length];
            for (int i = 0; i < str.length; i++) {
                intTemp[i] = Integer.parseInt(str[i]);
            }
            return intTemp;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    public class AlarmTask extends TimerTask {
        @Override
        public void run() {
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        socket = new Socket(HOST, PORT);
                        output = socket.getOutputStream();
//            output.write((Now + "\n").getBytes("utf-8"));// 把msg信息写入输出流中
                        output.write((Now).getBytes("utf-8"));// 把msg信息写入输出流中
                        output.flush();

                        //--------接收服务端的返回信息-------------
                        socket.shutdownOutput(); // 一定要加上这句，否则收不到来自服务器端的消息返回 ，意思就是结束msg信息的写入
                        input = socket.getInputStream();
                        byte[] b = new byte[1024];
                        int len;
                        sb = new StringBuffer();
                        while ((len = input.read(b)) != -1) {
                            sb.append(new String(b, 0, len, Charset.forName("UTF-8")));// 得到返回信息
                        }
                        Message msg = handler.obtainMessage();
                        msg.what = 3;
                        Bundle bundle = new Bundle();
                        bundle.putString("nowMsg", sb.toString());  //往Bundle中存放数据
                        msg.setData(bundle);//mes利用Bundle传递数据
                        handler.sendMessage(msg);

                        socket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
