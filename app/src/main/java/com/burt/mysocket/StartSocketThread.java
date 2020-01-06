package com.burt.mysocket;

import android.os.Message;
import android.util.Log;

import com.burt.mysocket.consts.Const;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class StartSocketThread implements Runnable {
    public static Socket socket;

    Message msg_main = Main2Activity.myhandler.obtainMessage();//////////////

    @Override
    public void run() {
        try {
            String socket_ip = "192.168.1.52";
            int socket_port = 8888;

            socket = new Socket();//连接服务端的IP
            SocketAddress socAddress = new InetSocketAddress(socket_ip, socket_port);
            socket.connect(socAddress, 3300);    //连接超时时间3秒3
            Main2Activity.running = true;//socket线程运行中标志位

            if (socket.isConnected() && !socket.isClosed()) {//成功连接socket对象

                //启动接收数据的线程
                new Thread(new SocketReceiveThread(socket)).start();
                //启动发送数据的线程
                new Thread(new SocketSendThread(socket)).start();
                //发送连接成功消息到主线程
                msg_main.what = Const.ConnectSuccess;
                Main2Activity.myhandler.sendMessage(msg_main);
            }

        } catch (Exception e) {
            e.printStackTrace();
            //利用Handler返回数据（连接失败）到主线程
            msg_main.what = Const.ConnectFail;
            Main2Activity.myhandler.sendMessage(msg_main);
            Main2Activity.running = false;
            Log.e("StartThread:", e.getMessage());
        }

    }
}