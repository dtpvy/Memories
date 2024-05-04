package com.example.memories;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;

public class PhotoHomeAdapter extends RecyclerView.Adapter<PhotoHomeAdapter.ViewHolder> {
    ArrayList<Media> media;
    Context context;
    ArrayList<String> durationTimes;

    public PhotoHomeAdapter(Context context) {
        this.context = context;
        this.media = new ArrayList<>();
        this.durationTimes = new ArrayList<>();
    }

    public PhotoHomeAdapter(Context context, ArrayList<Media> media) {
        this.context = context;
        this.media = media;
        this.durationTimes = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDurations(ArrayList<String> durations) {
        this.durationTimes = durations;
        notifyDataSetChanged();
    }

    public void setDuration(int position, String time) {
        this.durationTimes.set(position, time);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_home, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(ArrayList<Media> media) {
        this.media = media;
        this.durationTimes = new ArrayList<>(Collections.nCopies(media.size(), "00:00"));
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Media m = media.get(position);
        String imageUrl = m.getImgUrl();

        Glide.with(holder.image.getContext()).load(imageUrl).placeholder(R.drawable.stockphoto).into(holder.image);
        if (m.getType().contains("video")) {
            holder.video.setVisibility(View.VISIBLE);
            holder.time.setVisibility(View.VISIBLE);

            if (!durationTimes.isEmpty()) {
                holder.timeText.setText(durationTimes.get(position));
            }
        } else {
            holder.video.setVisibility(View.INVISIBLE);
            holder.time.setVisibility(View.INVISIBLE);
        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Media _media = media.get(holder.getAdapterPosition());
                if (_media.isVideo()) {
                    Intent intent = new Intent(context, VideoActivity.class);
                    intent.putExtra("media_id", _media.getId());
                    intent.putExtra("album_id", "default");
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, PhotoDetailActivity.class);
                    intent.putExtra("media_id", _media.getId());
                    intent.putExtra("album_id", "default");
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return media.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ConstraintLayout video, time;
        TextView timeText;

        public ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.photoHome);
            video = (ConstraintLayout) view.findViewById(R.id.video);
            time = (ConstraintLayout) view.findViewById(R.id.time);
            timeText = (TextView) view.findViewById(R.id.timeText);
        }
    }
}