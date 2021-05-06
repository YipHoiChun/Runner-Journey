package com.example.runnerjourney;

import androidx.fragment.app.FragmentActivity;

import android.database.Cursor;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap myMap;
    private long journeyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle bundle = getIntent().getExtras();
        journeyID = bundle.getLong("journeyID");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;

        Cursor c = getContentResolver().query(JourneyProviderContract.L_URI,
                null, JourneyProviderContract.L_JID + " = " + journeyID, null, null);

        PolylineOptions line = new PolylineOptions().clickable(false);
        LatLng firstLocation = null;
        LatLng lastLocation = null;
        try {
            while (c.moveToNext()) {
                LatLng loc = new LatLng(c.getDouble(c.getColumnIndex(JourneyProviderContract.L_LATITUDE)),
                        c.getDouble(c.getColumnIndex(JourneyProviderContract.L_LONGITUDE)));
                if (c.isFirst()) {
                    firstLocation = loc;
                }
                if (c.isLast()) {
                    lastLocation = loc;
                }
                line.add(loc);
            }
        } finally {
            c.close();
        }

        float zoom = 15.0f;
        if (lastLocation != null && firstLocation != null) {
            myMap.addMarker(new MarkerOptions().position(firstLocation).title("Start"));
            myMap.addMarker(new MarkerOptions().position(lastLocation).title("End"));
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, zoom));
        }
        myMap.addPolyline(line);
    }


}