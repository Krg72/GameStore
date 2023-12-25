package com.krg.gamestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.krg.gamestore.Models.Upload;
import com.krg.gamestore.databinding.ActivityUploadBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

public class UploadActivity extends AppCompatActivity {

    ActivityUploadBinding binding;
    private static final int PICK_APK_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private Uri selectedApkUri;
    private Uri selectedImgUri;

    TextView TitleTextView, DescriptionTextView;

    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Button ChooseApkButton = findViewById(R.id.ChooseApkBtn);
        ChooseApkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseApkFile();
            }
        });

        Button UploadToStoreButton = findViewById(R.id.UploadToStoreBtn);
        UploadToStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TitleTextView = findViewById(R.id.TitleText);
                DescriptionTextView = findViewById(R.id.DescriptionText);

                String Title = TitleTextView.getText().toString();
                String Description = DescriptionTextView.getText().toString();

                if(Title.isEmpty()){
                    TitleTextView.setError("Enter Title For Your Game");
                }else if(Description.isEmpty()){
                    DescriptionTextView.setError("Enter Description For Your Game");
                }else{
                    if(selectedApkUri != null && selectedImgUri != null) {
                        uploadFileToFirebaseStorage(selectedApkUri);
                    }
                }
            }
        });

        Button ChooseLogoButton = findViewById(R.id.ChooseLogoBtn);
        ChooseLogoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseLogoFile();
            }
        });
    }

    void ChooseLogoFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType("application/vnd.android.package-archive");
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Choose an APK file"), PICK_IMAGE_REQUEST);
    }
    private void chooseApkFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType("application/vnd.android.package-archive");
        intent.setType("*/*");

        startActivityForResult(Intent.createChooser(intent, "Choose an APK file"), PICK_APK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_APK_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedApkUri = data.getData();

            TextView FilePathText = findViewById(R.id.fileChosePath);
            Uri file = Uri.fromFile(new File(selectedApkUri.getPath()));
            FilePathText.setText("File: " + file.getLastPathSegment());
        }
        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImgUri = data.getData();

            TextView LogoPathText = findViewById(R.id.logoChosePath);
            Uri file = Uri.fromFile(new File(selectedImgUri.getPath()));
            LogoPathText.setText("File: " + file.getLastPathSegment());
        }
    }

    private void uploadFileToFirebaseStorage(Uri fileUri) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        String fileName = formatter.format(now);

        String ImageFilePath;
        StorageReference logoRef = storage.getReference("Images/"+fileName);
        StorageMetadata ImageMetaData = new StorageMetadata.Builder()
                .setCustomMetadata("GameName", TitleTextView.getText().toString())
                        .build();


        logoRef.putFile(selectedImgUri, ImageMetaData)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String logoPath = taskSnapshot.getMetadata().getPath();
                        saveAppDataWithLogoPath(fileUri, fileName, logoPath);
                        Toast.makeText(UploadActivity.this, "Logo Uploaded", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, "Logo Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void saveAppDataWithLogoPath(Uri fileUri, String fileName, String logoPath) {
        StorageReference storageRef = storage.getReference("Apps/"+fileName);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("title", TitleTextView.getText().toString())
                .setCustomMetadata("description", DescriptionTextView.getText().toString())
                .setCustomMetadata("logoPath", logoPath) // Set the path to the uploaded logo
                .build();

        Toast.makeText(UploadActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();

        storageRef.putFile(fileUri, metadata)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(UploadActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Upload", "Upload failed: " + e.getMessage(), e);
                        Toast.makeText(UploadActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(taskSnapshot -> {
                    // Calculate progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    // Update progress bar
                    ProgressBar progressBar = findViewById(R.id.progressBar);
                    progressBar.setProgress((int) progress);

                    TextView ProgressInt = findViewById(R.id.ProgressInt);
                    String progressText = String.format("%.0f%%", progress);
                    ProgressInt.setText((progressText));
                });
    }

    private void storeFileDetailsInDatabase(String fileName, String downloadUrl) {
        // Now, you can store the file details (e.g., filename and download URL) in Firebase Realtime Database
        // Assume you have a "uploads" node in your database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        // Create a unique key for each upload
        String uploadId = databaseRef.push().getKey();

        // Create an Upload object with file details

        Upload upload = new Upload(fileName, downloadUrl);

        // Store the upload in the database
        databaseRef.child(uploadId).setValue(upload);

        // Optionally, you can inform the user that the upload was successful
        Toast.makeText(UploadActivity.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
    }
}