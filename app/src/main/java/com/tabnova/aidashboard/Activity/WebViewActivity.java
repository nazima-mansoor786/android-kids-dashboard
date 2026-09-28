package com.tabnova.aidashboard.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tabnova.aidashboard.Extra.Consts;
import com.tabnova.aidashboard.R;

public class WebViewActivity extends BaseActivity {

    WebView webView;
    ImageView ivClose;
    TextView tvTitle;
    ProgressBar progressBar;
    String url = "https://www.kidsdashboard.co.uk";
    String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = findViewById(R.id.webView);
        ivClose = findViewById(R.id.iv_close);
        tvTitle = findViewById(R.id.tv_title);
        progressBar = findViewById(R.id.progress_bar);

        title = getString(R.string.app_name);

        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().getString(Consts.web_url) != null) {
                url = getIntent().getExtras().getString(Consts.web_url);
            }

            if (getIntent().getExtras().getString(Consts.name) != null) {
                title = getIntent().getExtras().getString(Consts.name);
            }
        }

        tvTitle.setText(title);

        ivClose.setOnClickListener(v -> finish());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportZoom(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                if (view.getTitle() != null)
                    tvTitle.setText(view.getTitle());
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else
            super.onBackPressed();
    }
}