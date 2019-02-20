package com.burt.mysocket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String HOST = "192.168.0.100";//主机地址
    private static final int PORT = 2222;//端口号
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;

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
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("abc","start");

                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
//                        Socket socket = null;
//                        OutputStream output = null;
//                        InputStream input = null;
//                        StringBuffer sb = null;
//                        try {
//                            socket = new Socket(HOST, PORT);
//                            output = socket.getOutputStream();
//                            output.write(("are"+"\n").getBytes("utf-8"));// 把msg信息写入输出流中
//                            output.flush();
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        HttpUtil.requestServer("are you ok?", new CallListener() {
                            @Override
                            public void onResult(String jsonresult) {
                                Log.d("abc",jsonresult);
                            }

                            @Override
                            public void onError() {

                            }
                        });


                    }
                });



            }
        });
    }


}
