package com.example.runnerjourney;

import android.net.Uri;

public class JourneyProviderContract {
    public static final String AUTHORITY = "com.example.runnerjourney.JourneyProvider";

    public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"");
    public static final Uri J_URI = Uri.parse("content://"+AUTHORITY+"/journey");
    public static final Uri L_URI = Uri.parse("content://"+AUTHORITY+"/location");

    public static final String J_ID = "journeyID";
    public static final String J_DURATION = "duration";
    public static final String J_distance = "distance";
    public static final String J_NAME = "name";
    public static final String J_RATING = "rating";
    public static final String J_COMMENT = "comment";
    public static final String J_DATE = "date";
    public static final String J_IMAGE = "image";

    public static final String L_ID = "locationID";
    public static final String L_JID = "journeyID";
    public static final String L_ALTITUDE = "altitude";
    public static final String L_LONGITUDE = "longitude";
    public static final String L_LATITUDE = "latitude";

}
