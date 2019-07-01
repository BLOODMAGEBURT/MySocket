package com.burt.mysocket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String HOST = "192.168.1.81";//主机地址
    private static final int PORT = 9999;//端口号
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;

    Socket socket = null;
    OutputStream output = null;
    InputStream input = null;
    StringBuffer sb = null;

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

        TextView hello = findViewById(R.id.hello);

        String html = "<div class=\\\"text\\\" style=\\\" text-align:center;\\\"><font size=\\\"4\\\" color=\\\"#666666\\\"><b>您有&nbsp;<font \\r\\ncolor=\\\"#FC5F49\\\">1</font>&nbsp;条额度出让申请</b></font><br><br><font size=\\\"5\\\" color=\\\"#999999\\\">赶快帮助好友提额吧</font></div>";


        hello.setText(Html.fromHtml(html));


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
            Log.d("abc", "ready to connect");


            Log.d("easyWay", "easyWay: connected successful");

            output = socket.getOutputStream();
            output.write(("are you ok ?" + "\n").getBytes("utf-8"));// 把msg信息写入输出流中
            output.flush();

            //--------接收服务端的返回信息-------------
            socket.shutdownOutput(); // 一定要加上这句，否则收不到来自服务器端的消息返回 ，意思就是结束msg信息的写入
            Log.d("easyWay", "i am 1 ");
            input = socket.getInputStream();
            byte[] b = new byte[1024];
            int len;
            sb = new StringBuffer();
            while ((len = input.read(b)) != -1) {
                sb.append(new String(b, 0, len, Charset.forName("UTF-8")));// 得到返回信息
            }
            Log.d("easyWay", "得到返回信息: " + sb.toString());

        } catch (IOException e) {
            Log.d("wrong", "there is something wrong1");
            e.printStackTrace();
            Log.d("wrong", "there is something wrong2");
        }

    }

}
