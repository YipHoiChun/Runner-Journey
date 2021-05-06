package com.example.runnerjourney;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;

public class EditActivity extends AppCompatActivity {
    private final int RESULT_LOAD_IMG = 1;

    private ImageView journeyImge;
    private EditText titleEditText;
    private EditText commentEditText;
    private EditText ratingEditText;
    private long journeyID;

    private Uri selectedJourneyImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Bundle bundle = getIntent().getExtras();

        journeyImge = findViewById(R.id.journeyImg);
        titleEditText = findViewById(R.id.titleEditText);
        commentEditText = findViewById(R.id.commentEditText);
        ratingEditText = findViewById(R.id.ratingEditText);
        journeyID = bundle.getLong("journeyID");

        selectedJourneyImg = null;

        populateEditText();
    }

    public void onClickSave(View v) {
        int rating = checkRating(ratingEditText);
        if (rating == -1) {
            return;
        }

        Uri rowQueryUri = Uri.withAppendedPath(JourneyProviderContract.J_URI, "" + journeyID);

        ContentValues cv = new ContentValues();
        cv.put(JourneyProviderContract.J_RATING, rating);
        cv.put(JourneyProviderContract.J_COMMENT, commentEditText.getText().toString());
        cv.put(JourneyProviderContract.J_NAME, titleEditText.getText().toString());

        if (selectedJourneyImg != null) {
            cv.put(JourneyProviderContract.J_IMAGE, selectedJourneyImg.toString());
        }

        getContentResolver().update(rowQueryUri, cv, null, null);
        finish();
    }

    public void onClickChangeImage(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case RESULT_LOAD_IMG: {
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = data.getData();
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        journeyImge.setImageBitmap(selectedImage);
                        selectedJourneyImg = imageUri;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(EditActivity.this, "You didn't pick an Image", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void populateEditText() {
        Cursor c = getContentResolver().query(Uri.withAppendedPath(JourneyProviderContract.J_URI,
                journeyID + ""), null, null, null, null);

        if (c.moveToFirst()) {
            titleEditText.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_NAME)));
            commentEditText.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_COMMENT)));
            ratingEditText.setText(c.getString(c.getColumnIndex(JourneyProviderContract.J_RATING)));

            String strUri = c.getString(c.getColumnIndex(JourneyProviderContract.J_IMAGE));
            if (strUri != null) {
                try {
                    final Uri imageUri = Uri.parse(strUri);
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    journeyImge.setImageBitmap(selectedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int checkRating(EditText newRating) {
        int rating;
        try {
            rating = Integer.parseInt(newRating.getText().toString());
        } catch (Exception e) {
            Log.d("M", "The following is not a number: " + newRating.getText().toString());
            return -1;
        }

        if (rating < 0 || rating > 5) {
            Log.d("M", "Rating must be between 0-5");
            return -1;
        }
        return rating;
    }
}