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
    private MyLocationListener myLocationListener;
    private final IBinder binder = new LocationServiceBinder();

    private final String CHANNEL_ID = "123";
    private final int NOTIFICATION_ID = 001;
    private long startTime = 0;
    private long stopTime = 0;

    final int TI = 3;//Time Interval
    final int DI = 3;//Dist Interval

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("M", "Location service has been created");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        myLocationListener = new MyLocationListener();
        myLocationListener.recordLocations = false;


        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, TI, DI, myLocationListener);
        } catch (SecurityException e) {
            Log.d("M", "No GPS permission");
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.getBoolean("battery")) {
                changeGPSRequestFrequency(TI * 3, DI * 3);
            }
        }

        return START_NOT_STICKY;
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


    private void addNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Track the journey";
            String description = "Keep Running!";
            int i = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name,
                    i);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder myBuilder = new NotificationCompat.Builder(this,
                CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Track the journey")
                .setContentText("Keep running!")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(NOTIFICATION_ID, myBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(myLocationListener);
        myLocationListener = null;
        locationManager = null;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        Log.d("M", "Location service is broken");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected float getDistance() {
        return myLocationListener.getDistanceOfJourney();
    }

    protected void playJourney() {
        addNotification();
        myLocationListener.newJourney();
        myLocationListener.recordLocations = true;
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

        for (Location location : myLocationListener.getLocations()) {
            ContentValues lData = new ContentValues();
            lData.put(JourneyProviderContract.L_JID, journeyID);
            lData.put(JourneyProviderContract.L_ALTITUDE, location.getAltitude());
            lData.put(JourneyProviderContract.L_LATITUDE, location.getLatitude());
            lData.put(JourneyProviderContract.L_LONGITUDE, location.getLongitude());

            getContentResolver().insert(JourneyProviderContract.L_URI, lData);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        myLocationListener.recordLocations = false;
        stopTime = SystemClock.elapsedRealtime();
        startTime = 0;
        myLocationListener.newJourney();

        Log.d("M", "Journey saved with id = " + journeyID);
    }

    protected void changeGPSRequestFrequency(int time, int dist) {
        try {
            locationManager.removeUpdates(myLocationListener);
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, time, dist, myLocationListener);
            Log.d("M", "New minimum time = " + time + ", shortest distance = " + dist);
        } catch (SecurityException e) {
            Log.d("M", "No GPS permission");
        }
    }


    protected void notifyGPSEnabled() {
        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 3, 3, myLocationListener);
        } catch (SecurityException e) {
            Log.d("M", "No GPS permission");
        }
    }

    private String getDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return formatter.format(date);
    }

}