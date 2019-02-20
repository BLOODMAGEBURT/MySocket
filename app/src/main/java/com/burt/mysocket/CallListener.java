package com.burt.mysocket;

public interface CallListener {
    void onResult(String jsonresult);
    void onError();
}
