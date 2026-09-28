package com.tabnova.aidashboard.Knox.license;

import com.google.gson.annotations.SerializedName;

/**
 * Created by  on 04/09/16.
 */
public class LicensesProfile {
    @SerializedName("id")
    public int id;
    @SerializedName("profile_id")
    public String profile_id;
    @SerializedName("licence_name")
    public String licence_name;
    @SerializedName("used_quantity")
    public int used_quantity;
    @SerializedName("maximum_quantity")
    public int maximum_quantity;
    @SerializedName("licence_code")
    public String licence_code;
    @SerializedName("created_at")
    public String created_at;
    @SerializedName("updated_at")
    public String updated_at;

}
/*
[{"id":2,"licence_name":"HelloKitty","licence_code":"1234567889",
"profile_id":1,"maximum_quantity":109,"used_quantity":0,"licence_status":1,"created_by":1,"created_at":"2016-10-21 01:59:34","updated_at":"2016-10-21 01:59:34"}]
 */