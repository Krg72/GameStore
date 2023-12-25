package com.krg.gamestore.Models;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krg.gamestore.R;

public class AppItemViewHolder extends RecyclerView.ViewHolder {
    ImageView logoImageView;
    TextView titleTextView;
    public AppItemViewHolder(@NonNull View itemView) {
        super(itemView);
        logoImageView = itemView.findViewById(R.id.GameThumbnail);
        titleTextView = itemView.findViewById(R.id.GameTitle);
    }
}
