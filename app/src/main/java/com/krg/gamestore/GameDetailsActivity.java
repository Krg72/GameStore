package com.krg.gamestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.krg.gamestore.Models.ScreenshotPagerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameDetailsActivity extends AppCompatActivity {

    private ProgressBar installProgressBar;
    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);

        Intent intent = getIntent();
        String title = intent.getStringExtra("gamename");
        String logoPath = intent.getStringExtra("logoUrl");
        String description = intent.getStringExtra("gameDescription");
        String gameId = intent.getStringExtra("gameId");
        String gameRating = intent.getStringExtra("gameRating");
        String GameDeveloper = intent.getStringExtra("DeveloperName");
        String GameDownloads = intent.getStringExtra("gameDownloads");
        String gameRelease = intent.getStringExtra("gameRelease");
        String videoUrl = intent.getStringExtra("VideoUrl");
//        String GameSS = intent.getStringExtra("GameSS");
        ArrayList<String> screenshotUrls = intent.getStringArrayListExtra("screenshotUrls");


        TextView titleTextView = findViewById(R.id.TitleOfGame);
        titleTextView.setText(title);

        TextView descriptionTextView = findViewById(R.id.DescriptionOfGame);
        descriptionTextView.setText(description);

        TextView ratingText = findViewById(R.id.RatingOfGame);
        ratingText.setText("Rating: " + gameRating +"⭐");

        TextView DeveloperName = findViewById(R.id.DeveloperName);
        DeveloperName.setText(GameDeveloper);

        TextView DownloadsText = findViewById(R.id.DownloadOfGame);
        DownloadsText.setText(GameDownloads);

        TextView ReleasedText = findViewById(R.id.ReleasedText);
        ReleasedText.setText(gameRelease);

        VideoView videoView = findViewById(R.id.videoView);
        if(videoUrl!=null) {
            View view = findViewById(R.id.videoView);
            view.setVisibility(View.VISIBLE);

            View Desview = findViewById(R.id.textView15);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) Desview.getLayoutParams();
            layoutParams.topToBottom = R.id.videoView;
            Desview.setLayoutParams(layoutParams);
            Desview.requestLayout();

            Uri videoUri = Uri.parse(videoUrl);
            //videoView.setVideoURI(videoUri);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(mediaController);
            videoView.setVideoPath(videoUrl);
            videoView.setMediaController(mediaController);

            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the video when the user clicks on the VideoView
                    if (!videoView.isPlaying()) {

                        videoView.start();
                    }
                }
            });
        }
        else{
            View Desview = findViewById(R.id.textView15);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) Desview.getLayoutParams();
            layoutParams.topToBottom = R.id.viewPagerScreenshots;
            Desview.setLayoutParams(layoutParams);
            Desview.requestLayout();
            View view = findViewById(R.id.videoView);
            view.setVisibility(View.INVISIBLE);
        }
        // Load the image using Glide or another image loading library
        ImageView logoImageView = findViewById(R.id.LogoOfGame);
        // Example using Glide:
        Glide.with(this).load(logoPath).into(logoImageView);

        ViewPager viewPager = findViewById(R.id.viewPagerScreenshots);

        ScreenshotPagerAdapter adapter = new ScreenshotPagerAdapter(this, screenshotUrls);
        viewPager.setAdapter(adapter);

        DeveloperName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://play.google.com/store/apps/developer?id="+DeveloperName+"&hl=en_IN&gl=US");
                //Uri uri = Uri.parse("https://play.google.com/store/apps/developer?id=" + DeveloperName + "&hl=en_IN&gl=US");
                //Uri uri = Uri.parse("market://details?id=" + gameId);
                // Create an Intent to open the URL
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                // Check if there is an app that can handle the Intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    // Start the activity if there is an app available
                    startActivity(intent);
                } else {
                    // Handle the case where there is no app available
                    // For example, open the link in a web browser
                    Uri webUri = Uri.parse("https://play.google.com/store/apps/developer?id=" + DeveloperName + "&hl=en_IN&gl=US");
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                    startActivity(webIntent);
                }
            }
        });

        installProgressBar = findViewById(R.id.Installbar); // Replace with your ProgressBar ID
        progressText = findViewById(R.id.progressText);

        Button InstallApkButton = findViewById(R.id.InstallBtn);
        InstallApkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create a Uri with the Google Play Store URL
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + gameId + "&hl=en&gl=US");
                //Uri uri = Uri.parse("market://details?id=" + gameId);
                // Create an Intent to open the URL
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                // Check if there is an app that can handle the Intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    // Start the activity if there is an app available
                    startActivity(intent);
                } else {
                    // Handle the case where there is no app available
                    // For example, open the link in a web browser
                    Uri webUri = Uri.parse("https://play.google.com/store/apps/details?id=" + gameId + "&hl=en&gl=US");
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                    startActivity(webIntent);
                }
                /*installProgressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                InstallApkButton.setVisibility(View.INVISIBLE);
                File[] localFile = {null};*/
                //downloadApk(gameFileName, localFile);
            }
        });

        Button ShareApkButton = findViewById(R.id.ShareBtn);
        ShareApkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Game for you: "+ title);
                String shareMessage= "\nLet me recommend you this game\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + gameId +"\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            }
        });
    }

    private void downloadApk(String fileName, File[] localFile) {
        // Get a reference to the root of your storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Get a reference to the APK file
        StorageReference apkRef = storageRef.child("Apps/" + fileName);

        // Create a temporary file to store the downloaded APK
        localFile[0] = null; // Initialize the array element
        try {
            localFile[0] = File.createTempFile("temp", "apk");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Download the file to a local temporary file
        if (localFile[0] != null) {
            apkRef.getFile(localFile[0]).addOnSuccessListener(taskSnapshot -> {
                // Install the downloaded APK
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int progress = 0;
                        while (progress <= 100) {
                            // Simulate download progress
                            try {
                                Thread.sleep(100); // Simulate a delay
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // Update progress
                            final int finalProgress = progress;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    installProgressBar.setProgress(finalProgress);
                                    progressText.setText(finalProgress + "%");
                                }
                            });

                            progress += 5; // Simulate progress increment
                        }

                        // Hide ProgressBar and TextView when the process is complete
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                installProgressBar.setVisibility(View.GONE);
                                progressText.setVisibility(View.GONE);
                            }
                        });

                        // For simplicity, immediately launch the installation
                        installApk(localFile[0]);
                    }
                }).start();
                //installApk(localFile[0]);
            }).addOnFailureListener(e -> {
                // Handle download failure
            });
        }

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Hide ProgressBar and TextView when the process is complete
                installProgressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);

                // For simplicity, immediately launch the installation
                installApk(localFile[0]);
            }
        }, 5000); // Simulating a 5-second download and installation process
*/


    }

    private void installApk(File apkFile) {
        // Prompt the user to install the app
        if(apkFile.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                getApplicationContext().startActivity(intent);
                Toast.makeText(this, "Installing", Toast.LENGTH_SHORT).show();
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "failed Install", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error in opening the file!");
            }
        }else {
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(installIntent);
        }
    }


}