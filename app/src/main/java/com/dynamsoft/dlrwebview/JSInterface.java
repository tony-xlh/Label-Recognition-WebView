package com.dynamsoft.dlrwebview;

import android.webkit.JavascriptInterface;

public class JSInterface {
    private ScanHandler mHandler;
    JSInterface(ScanHandler handler){
        mHandler = handler;
    }

    @JavascriptInterface
    public void returnResult(String result) {
        mHandler.onScanned(result);
    }

    @JavascriptInterface
    public void onInitialized() {
        mHandler.onInitialized();
    }
}
