package com.tabnova.aidashboard.Extra;

import android.graphics.Bitmap;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class RetrieveWebSiteIcon extends WebChromeClient {

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
    }
}
