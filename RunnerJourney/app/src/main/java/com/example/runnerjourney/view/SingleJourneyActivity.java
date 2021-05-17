package com.example.runnerjourney.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.runnerjourney.JourneyProviderContract;
import com.example.runnerjourney.R;

import java.io.InputStream;

public class SingleJourneyActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView distanceTextView, avgSTextView, timeTextView, dateTextView, ratingTextView, commentTextView, titleTextView;
    private long journeyID;

    private Handler handler = new Handler();

    protected class MyObserver extends ContentObserver {

        public MyObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

            populateView();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_journey);

        Bundle bundle = getIntent().getExtras();

        imageView = findViewById(R.id.ViewSingleJourney_journeyImg);
        distanceTextView = findViewById(R.id.Statistics_recordDistance);
        avgSTextView = findViewById(R.id.Statistics_distanceToday);
        timeTextView = findViewById(R.id.Statistics_timeToday);
        dateTextView = findViewById(R.id.ViewSingleJourney_dateText);
        ratingTextView = findViewById(R.id.ViewSingleJourney_ratingText);
        commentTextView = findViewById(R.id.ViewSingleJourney_commentText);
        titleTextView = findViewById(R.id.ViewSingleJourney_titleText);
        journeyID = bundle.getLong("journeyID");

        populateView();
        getContentResolver().registerContentObserver(
                JourneyProviderContract.ALL_URI, true, new MyObserver(handler));
    }

    public void onClickEdit(View v) {
        Intent editActivity = new Intent(SingleJourneyActivity.this, EditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong("journeyID", journeyID);
        editActivity.putExtras(bundle);
        startActivity(editActivity);
    }

    public void onClickMap(View v) {
        Intent map = new Intent(SingleJourneyActivity.this, MapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong("journeyID", journeyID);
        map.putExtras(bundle);
        startActivity(map);
    }

    private void populateView() {
        Cursor cursor = getContentResolver().query(Uri.withAppendedPath(JourneyProviderContract.J_URI,
                journeyID + ""), null, null, null, null);

        if (cursor.moveToFirst()) {
            double distance = cursor.getDouble(cursor.getColumnIndex(JourneyProviderContract.J_distance));
            long time = cursor.getLong(cursor.getColumnIndex(JourneyProviderContract.J_DURATION));
            double avgSpeed = 0;

            if (time != 0) {
                avgSpeed = distance / (time / 3600.0);
            }

            long hours = time / 3600;
            long minutes = (time % 3600) / 60;
            long seconds = time % 60;

            distanceTextView.setText(String.format("%.2f KM", distance));
            avgSTextView.setText(String.format("%.2f KM/H", avgSpeed));
            timeTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            // // The date will be stored as year-month-day, converted to day-month-year
            String date = cursor.getString(cursor.getColumnIndex(JourneyProviderContract.J_DATE));
            String[] dateParts = date.split("-");
            date = dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];

            dateTextView.setText(date);
            ratingTextView.setText(cursor.getInt(cursor.getColumnIndex(JourneyProviderContract.J_RATING)) + "");
            commentTextView.setText(cursor.getString(cursor.getColumnIndex(JourneyProviderContract.J_COMMENT)));
            titleTextView.setText(cursor.getString(cursor.getColumnIndex(JourneyProviderContract.J_NAME)));
            //// Display the image if the user has set it, otherwise display the default image
            String strUri = cursor.getString(cursor.getColumnIndex(JourneyProviderContract.J_IMAGE));
            if (strUri != null) {
                try {
                    final Uri imageUri = Uri.parse(strUri);
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imageView.setImageBitmap(selectedImage);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}