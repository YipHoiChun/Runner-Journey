package com.example.runnerjourney;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent i) {
        Log.d("M", "Low battery");
        Intent intent = new Intent(context, LocationService.class);
        Bundle b = new Bundle();
        b.putBoolean("battery", true);
        intent.putExtras(b);
        context.startService(intent);
    }
}
