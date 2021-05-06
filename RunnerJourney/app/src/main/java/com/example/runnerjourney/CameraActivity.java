package com.example.runnerjourney;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class CameraActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName() + "My";

    private String mPath = "";
    public static final int REQUEST_CAMERA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Button button = findViewById(R.id.button);

        button.setOnClickListener(v -> {
            Intent highIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Check if permission has been obtained
            if (highIntent.resolveActivity(getPackageManager()) == null) return;
            //Get the URI address of the photo file and set the file name
            File imageFile = getImageFile();
            if (imageFile == null) return;
            //Get the URI address of the photo file
            Uri imageUri = FileProvider.getUriForFile(
                    this,
                    "com.example.runnerjourney.CameraEx",
                    imageFile
            );
            highIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(highIntent, REQUEST_CAMERA);//Open Camera
        });
    }

    /**
     * 取得相片檔案的URI位址及設定檔案名稱
     */
    private File getImageFile() {
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            //Giving file names and file formats
            File imageFile = File.createTempFile(fileName, ".jpg", dir);
            // Give the location of the photo file in the global variable for easy access later
            mPath = imageFile.getAbsolutePath();
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 取得照片回傳
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /* you can view here which photo to send back, requestCode is self-defined above, resultCode is -1 is to take pictures, 0 is the user did not take pictures */
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode + ", resultCode " + resultCode);
        /**Photo Return*/
        if (requestCode == REQUEST_CAMERA && resultCode == -1) {
            ImageView imageHigh = findViewById(R.id.imageViewHigh);
            new Thread(() -> {
                // Get the photo file in BitmapFactory with the file URI path and process it as AtomicReference<Bitmap> to facilitate the subsequent rotation of the image.
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                Matrix matrix = new Matrix();
                matrix.setRotate(90f);//Turn 90 degrees
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        , 0, 0
                        , getHighImage.get().getWidth()
                        , getHighImage.get().getHeight()
                        , matrix, true));
                runOnUiThread(() -> {
                    // Set the picture with Glide (because rotating the picture is a time-consuming process, so it will be LAG for a while, and it must be threaded with Thread)
                    Glide.with(this)
                            .load(getHighImage.get())
                            .centerCrop()
                            .into(imageHigh);
                });
            }).start();
        }/***/
        else {
            Toast.makeText(this, "No filming was done", Toast.LENGTH_SHORT).show();
        }
    }
}