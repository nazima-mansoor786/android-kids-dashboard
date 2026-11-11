package com.tabnova.aidashboard.Knox.license;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Knox B2B endpoints, relative to {@link KnoxApiClient}'s base URL.
 * All path values are sent as-is (encoded = true); callers apply the same manual
 * escaping the former Volley requests used so URLs stay byte-for-byte identical.
 */
public interface KnoxApi {

    @GET("authenticate/{serial}/{pwd}")
    Call<Tocken> authenticate(@Path(value = "serial", encoded = true) String serial,
                              @Path(value = "pwd", encoded = true) String pwd);

    @GET("deviceprofilelicences/{token}")
    Call<String> deviceProfileLicences(@Path(value = "token", encoded = true) String token);

    // The trailing 179 must match Const.PROFILE_ID.
    @GET("register/{serial}/{pwd}/{code}/179")
    Call<String> register(@Path(value = "serial", encoded = true) String serial,
                          @Path(value = "pwd", encoded = true) String pwd,
                          @Path(value = "code", encoded = true) String code);

    @GET("setdevicedetails/{battery}/{installDate}/{appVersion}/{os}/{model}/{mac}/{ip}/{location}/{ip2}/{profileCounter}/{token}")
    Call<String> setDeviceDetails(@Path(value = "battery", encoded = true) String battery,
                                  @Path(value = "installDate", encoded = true) String installDate,
                                  @Path(value = "appVersion", encoded = true) String appVersion,
                                  @Path(value = "os", encoded = true) String os,
                                  @Path(value = "model", encoded = true) String model,
                                  @Path(value = "mac", encoded = true) String mac,
                                  @Path(value = "ip", encoded = true) String ip,
                                  @Path(value = "location", encoded = true) String location,
                                  @Path(value = "ip2", encoded = true) String ip2,
                                  @Path(value = "profileCounter", encoded = true) String profileCounter,
                                  @Path(value = "token", encoded = true) String token);

    @GET("setdeviceprovisionstatus/{status}/{token}")
    Call<String> setDeviceProvisionStatus(@Path(value = "status", encoded = true) String status,
                                          @Path(value = "token", encoded = true) String token);

    @GET("setdevicelog/{serial}/{log}/{result}/{token}")
    Call<String> setDeviceLog(@Path(value = "serial", encoded = true) String serial,
                              @Path(value = "log", encoded = true) String log,
                              @Path(value = "result", encoded = true) String result,
                              @Path(value = "token", encoded = true) String token);
}
