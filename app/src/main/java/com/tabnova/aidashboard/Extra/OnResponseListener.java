package com.tabnova.aidashboard.Extra;

import com.google.gson.JsonObject;

import retrofit2.Response;

public interface OnResponseListener {

    void onSuccess(Response<JsonObject> response);

    void onFailed(Throwable t);
}
