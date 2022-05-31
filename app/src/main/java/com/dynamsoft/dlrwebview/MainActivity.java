package com.dynamsoft.dlrwebview;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebViewAssetLoader;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private WebView webView;
    private TextView textView;
    private Context ctx;
    private Boolean initialized = false;
    final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;
        webView = findViewById(R.id.webView);
        textView = findViewById(R.id.resultTextView);

        if (hasCameraPermission() == false) {
            requestPermission();
        }

        loadWebViewSettings();
        webView.loadUrl("https://appassets.androidplatform.net/assets/scanner.html");
        //webView.loadUrl("file:android_asset/scanner.html");

        Button recognizeTextButton = findViewById(R.id.recognizeTextButton);
        recognizeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    private void loadWebViewSettings(){
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        // Enable remote debugging via chrome://inspect
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                           SslError error) {
                handler.proceed();
            }
            @Override
            @RequiresApi(21)
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            @SuppressWarnings("deprecation") // for API < 21
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return assetLoader.shouldInterceptRequest(Uri.parse(url));
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }
        });
        webView.addJavascriptInterface(new JSInterface(new ScanHandler (){
            @Override
            public void onScanned(String result){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.setVisibility(View.INVISIBLE);
                        textView.setText(result);
                    }
                });
            }
            @Override
            public void onInitialized(){
                initialized = true;
            }
        }), "AndroidFunction");
    }

    private void startScan(){
        if (initialized == false) {
            Toast.makeText(ctx,"The scanner has not been initialized.",Toast.LENGTH_SHORT).show();
        }else {
            webView.evaluateJavascript("javascript:startScan()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                }
            });
            webView.setVisibility(View.VISIBLE);
        }
    }

    private void pauseScan(){
        webView.evaluateJavascript("javascript:pauseScan()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
            }
        });
    }
}