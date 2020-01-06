package com.burt.mysocket;

public class CodeList {

    private static byte[] bytes = null;

    public static void setCodeToSend(byte[] bytes){
        CodeList.bytes = bytes;
    }

    public static byte[] getCodeToSend() {
        return bytes;
    }
}
