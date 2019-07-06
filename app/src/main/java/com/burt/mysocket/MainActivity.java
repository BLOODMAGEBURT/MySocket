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
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

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
    private AppCompatButton toTest;

    private final MyHandler handler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();
        initView();
    }

    private void initView() {
        AppCompatButton send = findViewById(R.id.btn_send);

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

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("abc", "start");

                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {

                        easyWay();

//                        complicateWay();
                    }
                });

            }
        });

        toTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, TestActivity.class));
            }
        });

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

    private void easyWay() {

        try {

            socket = new Socket(HOST, PORT);
            output = socket.getOutputStream();
            output.write(("are you ok ?" + "\n").getBytes("utf-8"));// 把msg信息写入输出流中
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
            bundle.putString("receivedMsg", sb.toString());  //往Bundle中存放数据
            msg.setData(bundle);//mes利用Bundle传递数据
            handler.sendMessage(msg);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    static class MyHandler extends Handler {

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
                    Toast.makeText(activity, "连接成功", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(activity, "连接断开", Toast.LENGTH_SHORT).show();
                    break;
                case 2:

                    String receivedMsg = msg.getData().getString("receivedMsg");//接受msg传递过来的参数

                    int[] formatedMsg = formatTheMsg(receivedMsg.replace("{", "").replace("}", ""));

                    newVal = (float) (formatedMsg[2] - formatedMsg[3]) / (float) (formatedMsg[4] - formatedMsg[3]) * 100;
                    activity.waveView.setProgressNum(oldVal, newVal, 5000);
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
}
