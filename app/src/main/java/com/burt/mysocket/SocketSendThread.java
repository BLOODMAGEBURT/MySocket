package com.burt.mysocket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SocketSendThread implements Runnable {

    private OutputStream out = null;
    public static boolean writing = false;

    //建立构造函数来获取socket对象的输入流
    public SocketSendThread(Socket socket) throws IOException {
        out = socket.getOutputStream();//得到socket的输出流
    }

    @Override
    public void run() {
        // 发送请求数据
        while (Main2Activity.running) {
            try {

                while (writing) {
                    writing = false;
                    byte[] codeToSend = CodeList.getCodeToSend();
                    out.write(codeToSend);
//                    String str = new String(codeToSend);
                }
                //数据最后加上换行符才可以让服务器端的readline()停止阻塞
//                out.write((str).getBytes("utf-8"));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
