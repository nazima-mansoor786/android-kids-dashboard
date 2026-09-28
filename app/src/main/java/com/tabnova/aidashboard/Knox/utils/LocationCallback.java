package com.tabnova.aidashboard.Knox.utils;

import android.content.Context;
import android.location.Location;

import com.tabnova.aidashboard.CustomDashboardApplication;


/**
 * Created by  on 07/11/2016.
 */

public class LocationCallback implements Locator.Listener{


    @Override
    public void onLocationFound(Location location, Context context) {

      String lo =  ""+ Location.convert(location.getLongitude(), Location.FORMAT_DEGREES)
              + ", " +  Location.convert(location.getLatitude(), Location.FORMAT_DEGREES);

        CustomDashboardApplication.setString(context, "Location", ""+ Location.convert(location.getLatitude(), Location.FORMAT_DEGREES)
                + ", " +  Location.convert(location.getLongitude(), Location.FORMAT_DEGREES));
    }

    @Override
    public void onLocationNotFound() {

    }

}
