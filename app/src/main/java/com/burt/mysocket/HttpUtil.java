package com.burt.mysocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import static com.burt.mysocket.ByteUtil.addBytes;
import static com.burt.mysocket.ByteUtil.hexStringToByteArray;
import static com.burt.mysocket.ByteUtil.intToBytes;

public class HttpUtil {

    private static final String HOST = "192.168.0.100";//主机地址
    private static final int PORT = 2222;//端口号

    //TCP向服务端发送数据
    public static void requestServer(String json, CallListener callListener) {

        byte[] bytes1 = hexStringToByteArray("8EEEEEEE");//将自定义的16进制魔数转变为二进制的流
        byte[] bytes2 = intToBytes(json.length(), ByteOrder.LITTLE_ENDIAN);//将协议头的LENGHT转变为二进制的byte，第二个参数是大端
        byte[] byte3 = json.getBytes();  //将json数据转换为二进制的流
        byte[] bys = addBytes(bytes1, bytes2);//合并byte数组
        final byte[] bytes = addBytes(bys, byte3);//将3个byte数组合并为一个

        Socket socket = null;
        OutputStream output = null;
        InputStream input;
        StringBuffer sb = null;
        try {
            socket = new Socket(HOST, PORT);
            //--------向服务端的写入信息-------------
            output = socket.getOutputStream();
//            output.write((json).getBytes("utf-8"));// 把msg信息写入输出流中
            output.write(bytes);// 把msg信息写入输出流中
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
        } catch (Exception e) {
            callListener.onError();//请求失败的回调
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    output.flush();
                    String jsonResult = sb.substring(8);//截取服务器返回的数据
                    callListener.onResult(jsonResult);//请求成功的回调
                    socket.close();// 释放资源，关闭这个Socket
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
