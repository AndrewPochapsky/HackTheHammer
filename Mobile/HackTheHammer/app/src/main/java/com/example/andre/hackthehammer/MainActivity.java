package com.example.andre.hackthehammer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {
    int x = 100;
    private static final int CAMERA_REQUEST_CODE = 1;
    private StorageReference mStorageRef;
    private DatabaseReference mDataBase;
    Uri photoURI = null;
    private ProgressDialog mProgress;

    private Button takePictureButton;
    private ImageView imageView;
    private TextView name, calorieValue, fatValue, sugarValue, proteinValue, sodiumValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDataBase = FirebaseDatabase.getInstance().getReference();

        mProgress = new ProgressDialog(this);

        takePictureButton = (Button) findViewById(R.id.button_image);
        imageView = (ImageView) findViewById(R.id.imageview);

        imageView.setRotation((float)90);

        name = (TextView)findViewById(R.id.name);
        calorieValue = (TextView)findViewById(R.id.caloriesValue);
        fatValue = (TextView)findViewById(R.id.fatValue);
        sugarValue = (TextView)findViewById(R.id.sugarValue);
        proteinValue = (TextView)findViewById(R.id.proteinValue);
        sodiumValue = (TextView)findViewById(R.id.sodiumValue);


        name.setText("None Selected");
        calorieValue.setAlpha(0);
        fatValue.setAlpha(0);
        sugarValue.setAlpha(0);
        proteinValue.setAlpha(0);
        sodiumValue.setAlpha(0);

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        mDataBase.child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                if(imageView.getDrawable() != null){
                    name.setText(data);
                }else{
                    name.setText("None Selected");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDataBase.child("calories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                if(imageView.getDrawable() != null){
                    calorieValue.setText(data);
                    calorieValue.setAlpha(1);
                }else{
                    calorieValue.setAlpha(0);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDataBase.child("fat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                if(imageView.getDrawable() != null){
                    fatValue.setText(data);
                    fatValue.setAlpha(1);
                }else{
                    fatValue.setAlpha(0);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDataBase.child("sugar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                if(imageView.getDrawable() != null){
                    sugarValue.setText(data);
                    sugarValue.setAlpha(1);
                }else{
                    sugarValue.setAlpha(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDataBase.child("protein").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                if(imageView.getDrawable() != null){
                    proteinValue.setText(data);
                    proteinValue.setAlpha(1);
                }else{
                    proteinValue.setAlpha(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDataBase.child("sodium").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                if(imageView.getDrawable() != null){
                    sodiumValue.setText(data);
                    sodiumValue.setAlpha(1);
                }else{
                    sodiumValue.setAlpha(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            //Bitmap bitmap = Bitmap.createBitmap(photoURI, 0, 0, 0, 0);
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                mProgress.setMessage("Uploading...");
                mProgress.show();
                int dimension = getSquareCropDimensionForBitmap(bitmap);
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
                StorageReference filepath = mStorageRef.child(photoURI.getLastPathSegment());
                Uri newUri = getImageUri(this, bitmap);
                filepath.putFile(newUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();

                        Picasso.with(MainActivity.this).load(photoURI).fit().centerCrop().into(imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }catch(IOException e){

            }


        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private int getSquareCropDimensionForBitmap(Bitmap bitmap)
    {
        //use the smallest dimension of the image to crop to
        return Math.min(bitmap.getWidth(), bitmap.getHeight());
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        //= new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = x + "JPEG";
        x++;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                 photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}
