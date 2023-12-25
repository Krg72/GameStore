package com.krg.gamestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krg.gamestore.databinding.ActivityProfileBinding;

import io.grpc.android.BuildConfig;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        TextView ProfileUserNameText = findViewById(R.id.ProfileUserName);
        ProfileUserNameText.setText(user.getEmail());

        TextView ShareText = findViewById(R.id.ShareText);
        ShareText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "GameStore");
                String shareMessage= "\nLet me recommend you this cool application which let you explore all the games on play store\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + "com.krg.gamestore" +"\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            }
        });

        TextView AboutText = findViewById(R.id.AboutText);
        AboutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ProfileActivity.this);

                // Set the content view of the dialog to your aboutus layout
                dialog.setContentView(R.layout.aboutus_layout);

                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                dialog.getWindow().setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT
                );

                // Show the dialog
                dialog.show();
            }
        });

        TextView Contact = findViewById(R.id.ContactText);
        Contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(ProfileActivity.this);

                // Set the content view of the dialog to your aboutus layout
                dialog.setContentView(R.layout.contact_layout);

                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                dialog.getWindow().setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT
                );

                // Show the dialog
                dialog.show();
            }
        });

        TextView FollowX = findViewById(R.id.followX);
        FollowX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String twitterUsername = "KRG_8121";

                try {
                    // Check if the Twitter app is installed
                    getPackageManager().getPackageInfo("com.twitter.android", 0);

                    // Open Twitter profile in the Twitter app
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + twitterUsername));
                    startActivity(intent);
                } catch (Exception e) {
                    // Twitter app is not installed, open the Twitter profile in a web browser
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + twitterUsername));
                    startActivity(intent);
                }
            }
        });

        TextView logoutText = findViewById(R.id.LogOutText);
        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();
                // Open the login activity (replace LoginActivity.class with your actual login activity)
                Intent intent = new Intent(ProfileActivity.this, SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            }
        });

        TextView deleteAccountText = findViewById(R.id.DeleteAccountText);
        deleteAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Account deleted successfully
                                        // Redirect to the login page or perform any other desired actions
                                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If the deletion fails, handle the error
                                        Toast.makeText(ProfileActivity.this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        Button PrivacyBtn = findViewById(R.id.Privacybutton);
        PrivacyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://krishnaghate.blogspot.com/2023/12/gamestore-privacy-policy.html";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        Button TnCBtn = findViewById(R.id.TnCButton);
        TnCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://krishnaghate.blogspot.com/2023/12/gamestore-terms.html";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        /*TextView YourStoreText = findViewById(R.id.YourStoreBtn);
        YourStoreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, StoreActivity.class));
            }
        });*/

        /*ImageView ProfileButtonImage = findViewById(R.id.profileButton);
        ProfileButtonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
            }
        });*/

        /*ImageView HomeImageButton = findViewById(R.id.HomeButton);
        HomeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            }
        });*/
    }
}