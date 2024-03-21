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
    ArrayList<Photo> photos;
    Context context;

    public PhotoHomeAdapter(Context context) {
        this.context = context;
        this.photos = new ArrayList<>();
    }

    public PhotoHomeAdapter(Context context, ArrayList<Photo> photos) {
        this.context = context;
        this.photos = photos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_home, parent, false);
        return new ViewHolder(view);
    }

    public void setData(ArrayList<Photo> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = photos.get(position).getImgUrl();
        System.out.println(imageUrl);
        Glide.with(holder.image.getContext()).load(imageUrl).placeholder(R.drawable.stockphoto).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.photoHome);
        }
    }
}