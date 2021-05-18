package com.example.runnerjourney.view;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.runnerjourney.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class CameraActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName() + "My";
    private String myPath = "";
    public static final int CAMERA_PERMISSION = 100;
    public static final int REQUEST_CAMERA = 101;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Button button = findViewById(R.id.button);
        if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Check if permission has been obtained
            if (intent.resolveActivity(getPackageManager()) == null) return;
            //Get the URI address of the photo file and set the file name
            File imageFile = getImageFile();
            if (imageFile == null) return;
            //Get the URI address of the photo file
            Uri imageUri = FileProvider.getUriForFile(
                    this,
                    "com.example.runnerjourney.CameraEx",
                    imageFile
            );
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, REQUEST_CAMERA);//Open Camera
        });
    }

    private File getImageFile() {
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            //Giving file names and file formats
            File imageFile = File.createTempFile(fileName, ".jpg", dir);
            // Give the location of the photo file in the global variable for easy access later
            myPath = imageFile.getAbsolutePath();
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Photo Return
        if (requestCode == REQUEST_CAMERA && resultCode == -1) {
            ImageView imageView = findViewById(R.id.imageView);
            new Thread(() -> {
                // Get the photo file in BitmapFactory with the file URI path and process it as AtomicReference<Bitmap> to facilitate the subsequent rotation of the image.
                AtomicReference<Bitmap> getImage = new AtomicReference<>(BitmapFactory.decodeFile(myPath));
                Matrix matrix = new Matrix();
                matrix.setRotate(90f);//Turn 90 degrees
                getImage.set(Bitmap.createBitmap(getImage.get()
                        , 0, 0
                        , getImage.get().getWidth()
                        , getImage.get().getHeight()
                        , matrix, true));
                runOnUiThread(() -> {
                    // Set the picture with Glide (because rotating the picture is a time-consuming process, so it will be LAG for a while, and it must be threaded with Thread)
                    Glide.with(this)
                            .load(getImage.get())
                            .centerCrop()
                            .into(imageView);
                });
            }).start();
        }/***/
        else {
            Toast.makeText(this, "No filming was done", Toast.LENGTH_SHORT).show();
        }
    }
}