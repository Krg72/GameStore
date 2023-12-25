package com.krg.gamestore.Models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.krg.gamestore.R;
import java.util.ArrayList;
import java.util.List;

public class AppItemAdapter extends RecyclerView.Adapter<AppItemViewHolder>  {
    Context context;
    private List<AppModel> appList;
    private OnClickListener onClickListener;
    public AppItemAdapter(List<AppModel> appList) {
        this.appList = appList;
    }

    @NonNull
    @Override
    public AppItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appitem_layout, parent, false);
        return new AppItemViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AppItemViewHolder holder, int position) {
        AppModel app = appList.get(position);

        Glide.with(holder.itemView.getContext()).asBitmap().load(app.getLogoUrl()).into(holder.logoImageView);

        //holder.logoImageView.setImageMatrix();
        holder.titleTextView.setText(app.getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(position, app);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
    public interface OnClickListener {
        void onClick(int position, AppModel app);
    }

}





