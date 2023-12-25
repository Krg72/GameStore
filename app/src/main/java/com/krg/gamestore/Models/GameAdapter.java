package com.krg.gamestore.Models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.krg.gamestore.GameDetailsActivity;
import com.krg.gamestore.R;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {
    private List<Game> gameList;
    private List<Game> filteredList;
    private Context context;

    public GameAdapter(List<Game> gameList, Context context) {
        this.gameList = gameList;
        this.context = context;
    }

    public void setFilteredList(List<Game> filteredList) {
        this.gameList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Game game = (filteredList != null) ? filteredList.get(position) : gameList.get(position);

        Game game = gameList.get(position);

        // Bind data to ViewHolder components
        holder.textViewGameName.setText(game.getGameName());
        holder.GameCategory.setText(game.getCategory());
        holder.textViewDownloadText.setText(game.getDownloads());
        holder.ReleaseYearText.setText(game.getReleased());
        //holder.textViewGameDescription.setText(game.getGameDescription());

        // Load image using Glide
        Glide.with(context)
                .load(game.getLogoUrl())
                .placeholder(R.drawable.mail)  // You can set a placeholder image
                .into(holder.imageViewLogo);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            // Handle item click, for example, open a new activity with detailed information
            // You can pass the game data to the next activity if needed
            Intent intent = new Intent(context, GameDetailsActivity.class);
            intent.putExtra("gamename", game.getGameName());
            intent.putExtra("gameDescription", game.getGameDescription());
            intent.putExtra("gameId", game.getGameId());
            intent.putExtra("logoUrl", game.getLogoUrl());
            intent.putExtra("gameRating", game.getRating());
            intent.putExtra("gameRelease", game.getReleased());
            intent.putExtra("gameDownloads",game.getDownloads());
            intent.putExtra("gameCategory",game.getCategory());
            intent.putExtra("DeveloperName",game.getDeveloperId());
            intent.putExtra("VideoUrl",game.getVideoUrl());
//            intent.putExtra("GameSS",game.getSSUrl());
            intent.putStringArrayListExtra("screenshotUrls", new ArrayList<>(game.getScreenshotUrls()));

            context.startActivity(intent);
            //Toast.makeText(context, "Clicked on: " + game.getGameName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGameName;
        TextView textViewDownloadText;
        TextView GameCategory;
        TextView ReleaseYearText;
        ImageView imageViewLogo;

        ViewHolder(View itemView) {
            super(itemView);
            textViewGameName = itemView.findViewById(R.id.textViewGameName);
            GameCategory = itemView.findViewById(R.id.GameCategory);
            textViewDownloadText = itemView.findViewById(R.id.DownloadsText);
            ReleaseYearText = itemView.findViewById(R.id.ReleasedYearText);
            imageViewLogo = itemView.findViewById(R.id.imageViewLogo);
        }
    }
}

