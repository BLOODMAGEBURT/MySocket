package com.burt.mysocket;

import android.os.Message;
import android.util.Log;

import com.burt.mysocket.consts.Const;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketReceiveThread implements Runnable {

    private InputStream is;
    private String str_receive;
    private String Tag = "SocketReceiveThread";

    //建立构造函数来获取socket对象的输入流
    public SocketReceiveThread(Socket socket) throws IOException {
        is = socket.getInputStream();
    }

    @Override
    public void run() {
        while (Main2Activity.running) {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            //用Handler把读取到的信息发到主线程
            Message msg0 = Main2Activity.myhandler.obtainMessage();

            try {
                //读服务器端发来的数据，阻塞直到收到结束符\n或\r
//                str_receive = br.readLine();

                //非阻塞接收数据，但限定接收数据字节
                byte buffer[] = new byte[1024 * 30];
                int count = is.read(buffer);
                if (count != -1) {
                    str_receive = new String(buffer, 0, count);
                    Log.i("received:", str_receive);
                } else {
                    Main2Activity.running = false;
                    StartSocketThread.socket.close();
                    return;
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e(Tag, e.getMessage());
                Main2Activity.running = false;//防止服务器端关闭导致客户端读到空指针而导致程序崩溃
                msg0.what = Const.ServiceDisconnect;
                Main2Activity.myhandler.sendMessage(msg0);//发送信息通知用户客户端已关闭
                break;

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(Tag, e.getMessage());
                Main2Activity.running = false;//防止服务器端关闭导致客户端读到空指针而导致程序崩溃
                msg0.what = Const.ServiceDisconnect;
                Main2Activity.myhandler.sendMessage(msg0);//发送信息通知用户客户端已关闭
                break;
            }
        }

    }
}
