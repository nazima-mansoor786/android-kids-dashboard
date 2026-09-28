package com.tabnova.aidashboard.Knox.volleyrequests;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class EnhancedStringRequest extends Request<String> {
    private static final String TAG = EnhancedStringRequest.class.getSimpleName();
    private final Listener<String> mListener;
    private JSONObject jsonRequest;

    /**
     * Creates a new request with the given method.
     *
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public EnhancedStringRequest(int method, String url, final JSONObject jsonRequest, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.jsonRequest = jsonRequest;
    }

    /**
     * Creates a new GET request.
     *
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public EnhancedStringRequest(String url, final JSONObject jsonRequest, Listener<String> listener, ErrorListener errorListener) {
        this(Method.GET, url, jsonRequest, listener, errorListener);
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

//    @Override
//    public byte[] getBody() {
//
//
//        try {
//            final JSONObject jObject = new JSONObject("jsonBody");
//            final Iterator<?> keys = jObject.keys();
//            while (keys.hasNext()) {
//                final String key = (String) keys.next();
//                final Object value = jObject.get(key);
//                if (value instanceof Integer) {
//
//                }
//
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }

}