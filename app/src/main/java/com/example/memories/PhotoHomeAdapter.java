package com.example.memories;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PhotoHomeAdapter extends RecyclerView.Adapter<PhotoHomeAdapter.ViewHolder> {
    ArrayList<String> images;
    Context context;

    // Constructor for initialization
    public PhotoHomeAdapter(Context context, ArrayList<String> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = images.get(position);
        Glide.with(holder.image.getContext()).load(imageUrl).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.photoHome);
        }
    }
}