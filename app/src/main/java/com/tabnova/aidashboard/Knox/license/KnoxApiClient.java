package com.tabnova.aidashboard.Knox.license;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Retrofit client for the Knox B2B API (replaces the former Volley RequestQueue).
 * Separate from APIManager's Retrofit instance. Callbacks run on the main thread.
 */
public final class KnoxApiClient {

    /** Success callback, same shape as Volley's Response.Listener. */
    public interface Listener<T> {
        void onResponse(T response);
    }

    /** Error callback for network/parse failures and non-2xx responses (Volley's onErrorResponse). */
    public interface ErrorListener {
        void onError(Throwable error);
    }

    private static final String BASE_URL = Const.getUrlBase() + "/";
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final int LOGIN_TIMEOUT_SECONDS = 20;

    private static volatile KnoxApiClient instance;

    private final KnoxApi api;
    private final KnoxApi loginApi;

    private KnoxApiClient() {
        OkHttpClient defaultClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        // Shares the connection pool with the default client.
        OkHttpClient loginClient = defaultClient.newBuilder()
                .connectTimeout(LOGIN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(LOGIN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(LOGIN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        api = buildRetrofit(defaultClient).create(KnoxApi.class);
        loginApi = buildRetrofit(loginClient).create(KnoxApi.class);
    }

    public static KnoxApiClient getInstance() {
        if (instance == null) {
            synchronized (KnoxApiClient.class) {
                if (instance == null) {
                    instance = new KnoxApiClient();
                }
            }
        }
        return instance;
    }

    public KnoxApi getApi() {
        return api;
    }

    /** Uses the 20 s timeout previously set on the LoginService Volley request. */
    public KnoxApi getLoginApi() {
        return loginApi;
    }

    /** Adapts Volley-style listeners: 2xx goes to onSuccess, non-2xx and failures go to onError. */
    public static <T> Callback<T> callback(final Listener<T> onSuccess, final ErrorListener onError) {
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) {
                    onSuccess.onResponse(response.body());
                } else {
                    onError.onError(new HttpException(response));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                onError.onError(t);
            }
        };
    }

    private static Retrofit buildRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
