package com.example.runnerjourney;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    public static final int CROP_PHOTO = 2;
    private ImageView showImage;

    private Uri imageUri;

    private String filename;
    private TextView lbl_imgpath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        showImage = (ImageView) findViewById(R.id.imgpic);
        lbl_imgpath = (TextView) findViewById(R.id.lblimgpath);
    }

    public void prc_camera(View view) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        filename = format.format(date);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File outputImage = new File(path, filename + ".jpg");

        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }

            outputImage.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }

        imageUri = Uri.fromFile(outputImage);

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CROP_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(CameraActivity.this, "ActivityResult resultCode error", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (requestCode) {
            case CROP_PHOTO:
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(imageUri));

                    Intent intentBcc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intentBcc.setData(imageUri);
                    this.sendBroadcast(intentBcc);

                    lbl_imgpath.setText(imageUri.toString());
                    showImage.setImageBitmap(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                break;
            default:
                break;
        }
    }
}