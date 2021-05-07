package com.example.runnerjourney.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.runnerjourney.LocationService;
import com.example.runnerjourney.MyReceiver;
import com.example.runnerjourney.R;

public class RecordActivity extends AppCompatActivity {

    private LocationService.LocationServiceBinder locationService;

    private TextView distanceText, avgSpeedText, durationText;
    private Button startButton, stopButton;
    private static final int PERMISSION_GPS_CODE = 1;

    private Handler handler = new Handler();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            locationService = (LocationService.LocationServiceBinder) iBinder;
            initButtons();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (locationService != null) {
                        float d = (float) locationService.getDuration();
                        long duration = (long) d;
                        float distance = locationService.getDistance();
                        long hours = duration / 3600;
                        long minutes = (duration % 3600) / 60;
                        long seconds = duration % 60;
                        float avgSpeed = 0;
                        if (d != 0) {
                            avgSpeed = distance / (d / 3600);
                        }

                        final String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        final String dist = String.format("%.2f KM", distance);
                        final String avg_speed = String.format("%.2f KM/H", avgSpeed);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                durationText.setText(time);
                                avgSpeedText.setText(avg_speed);
                                distanceText.setText(dist);
                            }
                        });

                        try {
                            Thread.sleep(500);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            locationService = null;
        }
    };

    private void initButtons() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopButton.setEnabled(false);
            startButton.setEnabled(false);
            return;
        }

        if (locationService != null && locationService.currentlyTracking()) {
            stopButton.setEnabled(true);
            startButton.setEnabled(false);
        } else {
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        distanceText = findViewById(R.id.distanceText);
        durationText = findViewById(R.id.durationText);
        avgSpeedText = findViewById(R.id.avgSpeedText);

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        stopButton.setEnabled(false);
        startButton.setEnabled(false);

        try {
            MyReceiver receiver = new MyReceiver();
            registerReceiver(receiver, new IntentFilter(
                    Intent.ACTION_BATTERY_LOW));
        } catch (IllegalArgumentException illegalArgumentException) {
        }


        handlePermissions();

        startService(new Intent(this, LocationService.class));
        bindService(
                new Intent(this, LocationService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void onClickPlay(View view) {
        locationService.playJourney();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    public void onClickStop(View view) {
        float distance = locationService.getDistance();
        locationService.saveJourney();

        startButton.setEnabled(false);
        stopButton.setEnabled(false);

        DialogFragment modal = FinishedTrackingDialogue.newInstance(String.format("%.2f KM", distance));
        modal.show(getSupportFragmentManager(), "Finished");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            MyReceiver receiver = new MyReceiver();
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException illegalArgumentException) {
        }

        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    public static class FinishedTrackingDialogue extends DialogFragment {
        public static FinishedTrackingDialogue newInstance(String distance) {
            Bundle savedInstanceState = new Bundle();
            savedInstanceState.putString("distance", distance);
            FinishedTrackingDialogue frag = new FinishedTrackingDialogue();
            frag.setArguments(savedInstanceState);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Your Journey has been saved. You ran a total of " + getArguments().getString("distance") + " KM")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getActivity().finish();
                        }
                    });
            return builder.create();
        }
    }


    // PERMISSION THINGS

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] results) {
        switch (reqCode) {
            case PERMISSION_GPS_CODE:
                if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                    initButtons();
                    if (locationService != null) {
                        locationService.notifyGPSEnabled();
                    }
                } else {
                    stopButton.setEnabled(false);
                    startButton.setEnabled(false);
                }
                return;

        }
    }


    public static class NoPermissionDialogue extends DialogFragment {
        public static NoPermissionDialogue newInstance() {
            NoPermissionDialogue frag = new NoPermissionDialogue();
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("GPS is required to track your journey!")
                    .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            return builder.create();
        }
    }

    private void handlePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                DialogFragment modal = NoPermissionDialogue.newInstance();
                modal.show(getSupportFragmentManager(), "Permissions");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_CODE);
            }

        }
    }
}