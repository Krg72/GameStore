package com.krg.gamestore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.krg.gamestore.databinding.ActivityStoreBinding;

public class StoreActivity extends AppCompatActivity {

    ActivityStoreBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        Button UploadButton = findViewById(R.id.UploadBtn);
        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoreActivity.this, UploadActivity.class));
            }
        });
        ImageView ProfileButtonImage = findViewById(R.id.profileButton);
        ProfileButtonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoreActivity.this, ProfileActivity.class));
            }
        });

        ImageView HomeImageButton = findViewById(R.id.HomeButton);
        HomeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoreActivity.this, MainActivity.class));
            }
        });
    }
}