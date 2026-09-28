package com.tabnova.aidashboard.Extra;


import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface RetrofitService {

    @POST(Consts.SERVICE_DEVICE_EXISTS)
    Call<JsonObject> checkDeviceExists(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_USER_AUTHENTICATION)
    Call<JsonObject> checkUserAuthentication(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_PROFILE_DEVIEC_AUTHENTICATION)
    Call<JsonObject> profileDeviceAuthentication(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_REGISTER)
    Call<JsonObject> profileDeviceRegister(@Body JsonObject jsonObject);

    @GET
    Call<JsonObject> getCounter(@Url String url);

    @GET
    Call<JsonObject> getAuthenticate(@Url String url);

  /*  @GET(Consts.SERVICE_SET_DEVICE_APPLICATION + "{device_details_json}/{token}"  )
    Call<String> uploadDeviceApplication(@Path("device_details_json") String url, @Path("token") String token);*/

    @POST(Consts.SERVICE_SET_DEVICE_APPLICATION)
    Call<JsonObject> uploadDeviceApplication(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_UPDATE_PROFILE_PIN)
    Call<JsonObject> updatePin(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_UPDATE_PROFILE_SETTING)
    Call<JsonObject> updateProfileSettings(@Body JsonObject jsonObject);

    @GET
    Call<JsonObject> getDailyTimeLimit(@Url String url);

    @POST(Consts.SERVICE_UPDATE_PROFILE_SCHEDULE)
    Call<JsonObject> updateProfileSchedule(@Body JsonObject jsonObject);

    @GET
    Call<JsonObject> getProfile(@Url String url, @Query("profile_id") String profileId);

    @Multipart
    @POST(Consts.SERVICE_UPDATE_PROFILE_WALLPAPER)
    Call<JsonObject> updateProfile(@Part MultipartBody.Part token, @Part MultipartBody.Part profileId, @Part MultipartBody.Part file);

    @GET(Consts.SERVICE_GET_APPLICATION_LIST)
    Call<JsonObject> getApplicationList(@Query(Consts.token) String s, @Query(Consts.added_to_dashboard) int addedToDashboard, @Query(Consts.device_application_type) int installedType);

    @GET(Consts.SERVICE_GET_APPLICATION_LIST)
    Call<JsonObject> getAllApplicationList(@Query(Consts.token) String s);

    @POST(Consts.SERVICE_ADD_SCHEDULE_EVENT)
    Call<JsonObject> addScheduleEvent(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_GET_EVENT_LIST)
    Call<JsonObject> getScheduleEventList(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_DELETE_SCHEDULE_EVENT)
    Call<JsonObject> deleteScheduleEvent(@Body JsonObject jsonObject);

    @GET(Consts.SERVICE_GET_APPS_ANALYTICS_DATA)
    Call<JsonObject> getAppsAnalyticsData(@Query(Consts.token) String s);

    @POST(Consts.SERVICE_APPS_ANALYTICS_DATA)
    Call<JsonObject> uploadAppsAnalyticsData(@Body JsonObject jsonObject);

    @GET
    Call<JsonObject> logoutUser(@Url String s);

    @POST(Consts.SERVICE_FORGOT_PASSWORD)
    Call<JsonObject> forgotPassword(@Body JsonObject jsonObject);

    @GET
    Call<JsonObject> updateDataOnServer(@Url String url);

    @POST(Consts.SERVICE_GOOGLE_LOGIN)
    Call<JsonObject> loginWithGoogle(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_DELETE_WALLPAPER)
    Call<JsonObject> deleteWallpaper(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_ADD_WEB_HISTORY)
    Call<JsonObject> addWebHistory(@Body JsonObject jsonObject);

    @GET(Consts.SERVICE_GET_WEB_HISTORY)
    Call<JsonObject> getWebHistory(@Query(Consts.token) String url);

    @POST(Consts.SERVICE_SAVE_WEB_SHORTCUTS)
    Call<JsonObject> saveWebShortcuts(@Body JsonObject jsonObject);

    @GET(Consts.SERVICE_GET_WEB_SHORTCUTS)
    Call<JsonObject> getWebShortcuts(@Query(Consts.token) String url);

    @GET(Consts.SERVICE_DELETE_WEB_SHORTCUT)
    Call<JsonObject> deleteWebShortcut(@Query(Consts.token) String url, @Query(Consts.id) String id);

    @POST(Consts.SERVICE_SET_DEVICE_LOCATION)
    Call<JsonObject> setDeviceLocation(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_SAVE_PHONE_HISTORY)
    Call<JsonObject> savePhoneHistory(@Body JsonObject jsonObject);

    @GET(Consts.SERVICE_GET_PAYMENT_STATUS)
    Call<JsonObject> checkPaymentStatus(@Query(Consts.token) String token, @Query(Consts.profile_id_str) String profileId);

    @POST(Consts.SERVICE_UPDATE_KIDS_PROFILE_SETTINGS)
    Call<JsonObject> updateAdvanceSettings(@Body JsonObject jsonObject);

    @POST(Consts.SERVICE_UPDATE_KIDS_PROFILE_WIPE_DETAILS)
    Call<JsonObject> updateWipeDetails(@Body JsonObject jsonObject);
}
