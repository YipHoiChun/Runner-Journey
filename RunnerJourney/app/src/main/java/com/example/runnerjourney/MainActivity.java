package com.example.runnerjourney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickRecord(View v) {
        // go to the record journey activity
        Intent intent = new Intent(MainActivity.this, RecordActivity.class);
        startActivity(intent);
    }

    public void onClickView(View v) {
        // go to the activity for displaying journeys
        Intent intent = new Intent(MainActivity.this, JourneysActivity.class);
        startActivity(intent);
    }

    public void onClickCamera(View v) {
        // go to the activity for Camera
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }


}