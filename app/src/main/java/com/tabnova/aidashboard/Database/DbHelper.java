package com.tabnova.aidashboard.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tabnova.aidashboard.Extra.SessionManager;
import com.tabnova.aidashboard.Model.LocationModel;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private static DbHelper sInstance;
    private static SessionManager sessionManager;

    public static synchronized DbHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new DbHelper(context);
        }
        if (sessionManager == null) {
            sessionManager = SessionManager.getInstance(context);
        }
        return sInstance;
    }

    public DbHelper(Context context) {
        super(context, DBUtils.DB_NAME, null, DBUtils.DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DBUtils.CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion == DBUtils.DB_VERSION) {//HANDLES MODIFICATIONS TO DATABASE
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + DBUtils.LOCATION_TABLE + "'");
            onCreate(sqLiteDatabase);
        }
    }

    //METHOD TO GET ALL TEACHERS DETAILS
    public List<LocationModel> getAllLocations() {
        List<LocationModel> locations = new ArrayList<>();
        LocationModel location;
        SQLiteDatabase db = null;
        try {
            db = this.getReadableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Cursor cursor = db.query(DBUtils.COURSE_TABLE, null, null, null, null, null, null, null);

        if (db != null) {
            try {
                Cursor cursor = db.query(DBUtils.LOCATION_TABLE,
                        new String[]{DBUtils.ID, DBUtils.LATITUDE, DBUtils.LONGITUDE, DBUtils.IS_UPLOAD},
                        null, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    location = new LocationModel();
                    location.setId(cursor.getInt(cursor.getColumnIndex(DBUtils.ID)));
                    location.setLatitude(cursor.getString(cursor.getColumnIndex(DBUtils.LATITUDE)));
                    location.setLongitude(cursor.getString(cursor.getColumnIndex(DBUtils.LONGITUDE)));
                    if (cursor.getInt(cursor.getColumnIndex(DBUtils.IS_UPLOAD)) == 1) {
                        location.setUpload(true);
                    } else {
                        location.setUpload(false);
                    }

                    locations.add(location);
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return locations;
    }

    //METHOD TO INSERT LOCATION DETAIL
    public boolean insertLocationDetails(LocationModel locationModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBUtils.LATITUDE, locationModel.getLatitude());
            contentValues.put(DBUtils.LONGITUDE, locationModel.getLongitude());
            if (locationModel.isUpload()) {
                contentValues.put(DBUtils.IS_UPLOAD, 1);
            } else {
                contentValues.put(DBUtils.IS_UPLOAD, 0);
            }
            result = db.insert(DBUtils.LOCATION_TABLE, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return result != -1;
    }

    //METHOD TO UPDATE LOCATION DETAIL
    public boolean updateLocationData(LocationModel locationModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBUtils.LATITUDE, locationModel.getLatitude());
            contentValues.put(DBUtils.LONGITUDE, locationModel.getLongitude());
            if (locationModel.isUpload()) {
                contentValues.put(DBUtils.IS_UPLOAD, 1);
            } else {
                contentValues.put(DBUtils.IS_UPLOAD, 0);
            }
            result = db.update(DBUtils.LOCATION_TABLE, contentValues, DBUtils.ID + "=" + locationModel.getId(), null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return result != -1;
    }

    // METHOD TO DELETE LOCATION DATA
    public int deleteLocationData(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DBUtils.LOCATION_TABLE, DBUtils.ID + "=" + id, null);
    }

    //METHOD TO DELETE ALL LOCATION DATA
    public int deleteTeacherData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DBUtils.LOCATION_TABLE, null, null);
    }
}