package com.burt.mysocket;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.burt.mysocket.consts.Const;

import java.io.IOException;

public class Main2Activity extends AppCompatActivity {
    private String Tag = "Main2Activity";
    public static Handler myhandler;
    //socket线程是否running的标志位
    public static boolean running = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //实例化Handler，用于进程间的通信//////////////////
        myhandler = new MyHandler();

        //启动Socket线程
        new Thread(new StartSocketThread()).start();

        Button sendMsg = findViewById(R.id.send_msg);
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = "@RUN_Now!";
                CodeList.setCodeToSend(code.getBytes());
                SocketSendThread.writing = true;
            }
        });
    }


    class MyHandler extends Handler {//在主线程处理Handler传回来的message

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    String str = (String) msg.obj;
                    Log.i("received:", str);

                    break;
                case Const.ConnectSuccess:
                    Log.i("handleMessage_Socket", "连接成功");
                    Toast.makeText(getApplicationContext(),
                            "连接成功！", Toast.LENGTH_SHORT).show();

                    break;
                case Const.ServiceDisconnect:
                    Log.i("handleMessage_Socket", "服务端连接断开");
                    Toast.makeText(getApplicationContext(),
                            "与服务器断开连接！", Toast.LENGTH_SHORT).show();

                    break;
                case Const.ConnectFail:
                    Log.i("handleMessage_Socket", "连接失败");
                    Toast.makeText(getApplicationContext(),
                            "连接失败", Toast.LENGTH_LONG).show();

                    break;

                default:
                    break;
            }
        }
    }


    /**
     * 销毁activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (StartSocketThread.socket == null) {
            return;
        }
        try {
            //关闭Socket线程
            Main2Activity.running = false;
            StartSocketThread.socket.close();
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

}
