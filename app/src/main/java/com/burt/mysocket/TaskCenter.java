package com.burt.mysocket;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskCenter {

    private static final String TAG = "TaskCenter";
    //    Socket
    private Socket socket;
    //    IP地址
    private String ipAddress;
    //    端口号
    private int port;
    //    线程
    private Thread thread;
    //    Socket输出流
    private OutputStream outputStream;
    //    Socket输入流
    private InputStream inputStream;
    // 使用线程池
    private ExecutorService mThreadPool;

    private OnServerConnectedListener onServerConnectedListener;
    private OnServerDisconnectedListener onServerDisconnectedListener;
    private OnReceivedListener onReceivedListener;


    // 饿汉模式的单例模式
    private static TaskCenter instance = new TaskCenter();

    private TaskCenter() {
        mThreadPool = Executors.newCachedThreadPool();
    }

    public static TaskCenter getInstance() {
        return instance;
    }


    /**
     * 通过IP地址(域名)和端口进行连接
     *
     * @param ipAddress IP地址(域名)
     * @param port      端口
     */
    public void connect(final String ipAddress, final int port) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ipAddress, port);
//                    socket.setSoTimeout ( 2 * 1000 );//设置超时时间
                    if (isConnected()) {
                        TaskCenter.getInstance().ipAddress = ipAddress;
                        TaskCenter.getInstance().port = port;
                        if (onServerConnectedListener != null) {
                            onServerConnectedListener.onConnected();
                        }
                        outputStream = socket.getOutputStream();
                        inputStream = socket.getInputStream();
                        receive();
                        Log.i(TAG, "连接成功");
                    } else {
                        Log.i(TAG, "连接失败");
                        if (onServerDisconnectedListener != null) {
                            onServerDisconnectedListener.onDisconnected(new IOException("连接断开"));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "连接异常");
                    if (onServerDisconnectedListener != null) {
                        onServerDisconnectedListener.onDisconnected(e);
                    }
                }
            }
        });

    }

    /**
     * 接收数据
     */
    private void receive() {
        while (isConnected()) {
            try {
                /**得到的是16进制数，需要进行解析*/
                byte[] bt = new byte[1024];
//                获取接收到的字节和字节数
                int length = inputStream.read(bt);
//                获取正确的字节
                byte[] bs = new byte[length];
                System.arraycopy(bt, 0, bs, 0, length);

                String str = new String(bs, "UTF-8");
                if (str != null) {
                    if (onReceivedListener != null) {
                        onReceivedListener.onReceive(str);
                    }
                }
                Log.i(TAG, "接收成功");
            } catch (IOException e) {
                Log.i(TAG, "接收失败");
            }
        }
    }


    /**
     * 发送数据
     *
     * @param data 数据
     */
    public void send(final byte[] data) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (socket != null) {
                    try {
                        outputStream.write(data);
                        outputStream.flush();
                        Log.i(TAG, "发送成功");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i(TAG, "发送失败");
                    }
                } else {
                    connect();
                }
            }
        });
    }


    /**
     * 判断是否连接
     */
    public boolean isConnected() {
        return socket.isConnected();
    }

    /**
     * 连接
     */
    public void connect() {
        connect(ipAddress, port);
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (isConnected()) {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                socket.close();
                if (socket.isClosed()) {
                    if (onServerDisconnectedListener != null) {
                        onServerDisconnectedListener.onDisconnected(new IOException("断开连接"));
                        //移除回调
                        removeCallback();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 回调声明
     */
    public interface OnServerConnectedListener {
        void onConnected();
    }

    public void setOnServerConnectedListener(OnServerConnectedListener onServerConnectedListener) {
        this.onServerConnectedListener = onServerConnectedListener;
    }

    public interface OnServerDisconnectedListener {
        void onDisconnected(IOException e);
    }

    public void setOnServerDisconnectedListener(OnServerDisconnectedListener onServerDisconnectedListener) {
        this.onServerDisconnectedListener = onServerDisconnectedListener;
    }

    public interface OnReceivedListener {
        void onReceive(String receivedMessage);
    }

    public void setOnReceivedListener(OnReceivedListener onReceivedListener) {
        this.onReceivedListener = onReceivedListener;
    }

    /**
     * 移除回调
     */
    private void removeCallback() {

        if (onServerConnectedListener != null) {
            onServerConnectedListener = null;
        }

        if (onServerDisconnectedListener != null) {
            onServerDisconnectedListener = null;
        }

        if (onReceivedListener != null) {
            onReceivedListener = null;
        }
    }

}
