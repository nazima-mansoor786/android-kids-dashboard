package com.rsb.vpnpoc.application;

import android.util.Pair;

import java.util.List;

public class HTTP implements IApplication {
    private List<Pair<String, String>> httpHeaders;
    private byte[] body;

    HTTP(List<Pair<String, String>> httpHeaders, byte[] body) {
        this.httpHeaders = httpHeaders;
        this.body = body;
    }

    public List<Pair<String, String>> getHttpHeaders() {
        return httpHeaders;
    }

    public byte[] getBody() {
        return body;
    }
}
