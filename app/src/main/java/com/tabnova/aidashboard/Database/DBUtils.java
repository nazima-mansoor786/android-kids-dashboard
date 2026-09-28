package com.tabnova.aidashboard.Database;

public class DBUtils {
    static final String DB_NAME = "kids_dashboard_db.db";

    //TABLES
    static final String LOCATION_TABLE = "location_tbl";

    //LOCATION TABLE FIELDS
    static final String ID = "id";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String IS_UPLOAD = "is_upload";

    //current database version
    static final int DB_VERSION = 1;//MAKE CHANGES AS NECESSARY WHEN APP IS IN PRODUCTION

    //create tables
    static final String CREATE_LOCATION_TABLE =
            "CREATE TABLE " + LOCATION_TABLE + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LATITUDE + " TEXT, " +
                    LONGITUDE + " TEXT, " +
                    IS_UPLOAD + " INTEGER ) ";
}
