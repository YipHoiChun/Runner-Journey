package com.example.runnerjourney;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationService extends Service {
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private final IBinder binder = new LocationServiceBinder();

    private final String CHANNEL_ID = "100";
    private final int NOTIFICATION_ID = 001;
    private long startTime = 0;
    private long stopTime = 0;

    final int TIME_INTERVAL = 3;
    final int DIST_INTERVAL = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("mdp", "Location service has been created");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        locationListener.recordLocations = false;


        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, TIME_INTERVAL, DIST_INTERVAL, locationListener);
        } catch (SecurityException e) {
            Log.d("mdp", "No GPS permission");
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.getBoolean("battery")) {
                changeGPSRequestFrequency(TIME_INTERVAL * 3, DIST_INTERVAL * 3);
            }
        }

        return START_NOT_STICKY;
    }


    private void addNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Tracking Journey";
            String description = "Keep Running!";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,
                    importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,
                CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Tracking Journey")
                .setContentText("Keep Running!")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        locationListener = null;
        locationManager = null;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        Log.d("mdp", "Location service is broken");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected float getDistance() {
        return locationListener.getDistanceOfJourney();
    }

    protected void playJourney() {
        addNotification();
        locationListener.newJourney();
        locationListener.recordLocations = true;
        startTime = SystemClock.elapsedRealtime();
        stopTime = 0;
    }

    protected double getDuration() {
        if (startTime == 0) {
            return 0.0;
        }

        long endTime = SystemClock.elapsedRealtime();

        if (stopTime != 0) {
            endTime = stopTime;
        }

        long elapsedMilliSeconds = endTime - startTime;
        return elapsedMilliSeconds / 1000.0;
    }

    protected boolean currentlyTracking() {
        return startTime != 0;
    }

    protected void saveJourney() {
        ContentValues jData = new ContentValues();
        jData.put(JourneyProviderContract.J_distance, getDistance());
        jData.put(JourneyProviderContract.J_DURATION, (long) getDuration());
        jData.put(JourneyProviderContract.J_DATE, getDateTime());

        long journeyID = Long.parseLong(getContentResolver().insert(JourneyProviderContract.J_URI, jData).getLastPathSegment());

        for (Location location : locationListener.getLocations()) {
            ContentValues lData = new ContentValues();
            lData.put(JourneyProviderContract.L_JID, journeyID);
            lData.put(JourneyProviderContract.L_ALTITUDE, location.getAltitude());
            lData.put(JourneyProviderContract.L_LATITUDE, location.getLatitude());
            lData.put(JourneyProviderContract.L_LONGITUDE, location.getLongitude());

            getContentResolver().insert(JourneyProviderContract.L_URI, lData);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        locationListener.recordLocations = false;
        stopTime = SystemClock.elapsedRealtime();
        startTime = 0;
        locationListener.newJourney();

        Log.d("mdp", "Journey saved with id = " + journeyID);
    }

    protected void changeGPSRequestFrequency(int time, int dist) {
        try {
            locationManager.removeUpdates(locationListener);
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, time, dist, locationListener);
            Log.d("mdp", "New min time = " + time + ", min dist = " + dist);
        } catch (SecurityException e) {
            Log.d("mdp", "No GPS permission");
        }
    }


    protected void notifyGPSEnabled() {
        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 3, 3, locationListener);
        } catch (SecurityException e) {
            Log.d("mdp", "No GPS permission");
        }
    }

    private String getDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return formatter.format(date);
    }

    public class LocationServiceBinder extends Binder {
        public float getDistance() {
            return LocationService.this.getDistance();
        }

        public double getDuration() {
            return LocationService.this.getDuration();
        }

        public boolean currentlyTracking() {
            return LocationService.this.currentlyTracking();
        }

        public void playJourney() {
            LocationService.this.playJourney();
        }

        public void saveJourney() {
            LocationService.this.saveJourney();
        }

        public void notifyGPSEnabled() {
            LocationService.this.notifyGPSEnabled();
        }

        public void changeGPSRequestFrequency(int time, int dist) {
            LocationService.this.changeGPSRequestFrequency(time, dist);
        }
    }
}