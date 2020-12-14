package com.example.fish_camera;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class recipe extends AppCompatActivity {

    WebView recipe_web;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        recipe_web = findViewById(R.id.recipe_web);
        recipe_web.getSettings().setJavaScriptEnabled(true);
        recipe_web.setWebViewClient(new WebViewClient()); //不調用系統瀏覽器
        Intent intent = getIntent();
        String url = "https://cookpad.com/tw/%E6%90%9C%E5%B0%8B/"+intent.getStringExtra("Identify_result");
        Log.v("test","URL: "+url);
        recipe_web.loadUrl(url);

    }
    //按返回是回網頁上一頁不是關閉webview
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && recipe_web.canGoBack()) {
            recipe_web.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
