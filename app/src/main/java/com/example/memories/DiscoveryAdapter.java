package com.example.memories;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class DiscoveryAdapter extends RecyclerView.Adapter<DiscoveryAdapter.ViewHolder> {
    ArrayList<Media> media, selected;
    Context context;
    Handler handlerInMainThread;
    Callback callback;
    Boolean editView = false;


    public DiscoveryAdapter(Context context) {
        this.context = context;
        this.media = new ArrayList<>();
        this.selected = new ArrayList<>();
        this.handlerInMainThread = new Handler(context.getMainLooper());
    }

    public DiscoveryAdapter(Context context, ArrayList<Media> media) {
        this.context = context;
        this.media = media;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_select, parent, false);
        return new ViewHolder(view);
    }

    public void setData(ArrayList<Media> media) {
        this.media = media;
        notifyDataSetChanged();
    }

    public void setSelected(ArrayList<Media> selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }

    interface Callback {
        void onSelect(ArrayList<Media> media);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setEditView(Boolean editView) {
        this.editView = editView;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Media m = media.get(position);
        String imageUrl = m.getImgUrl();

        Glide.with(holder.image.getContext()).load(imageUrl).placeholder(R.drawable.stockphoto).into(holder.image);

        holder.box.setVisibility(editView ? View.VISIBLE : View.INVISIBLE);

        if (isSelect(position) < 0) {
            holder.checkBox.setVisibility(View.INVISIBLE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
        }

        holder.box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int idx = isSelect(holder.getAdapterPosition());
                if (idx < 0) {
                    holder.checkBox.setVisibility(View.VISIBLE);
                    selected.add(m);
                } else {
                    holder.checkBox.setVisibility(View.INVISIBLE);
                    selected.remove(idx);
                }
                callback.onSelect(selected);
            }
        });

        holder.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setEditView(true);
                return false;
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Media _media = media.get(holder.getAdapterPosition());
                Intent intent = new Intent(context, PhotoDetailActivity.class);
                intent.putExtra("media_id", _media.getId());
                context.startActivity(intent);
            }
        });
    }

    public int isSelect(int position) {
        for (int i = 0; i < selected.size(); i++) {
            if (media.get(position).getId().compareTo(selected.get(i).getId()) == 0) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return media.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView checkBox;
        LinearLayout rowItem;
        ConstraintLayout video, time, box;
        TextView timeText;

        public ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.photoHome);
            checkBox = (ImageView) view.findViewById(R.id.checkBox);
            rowItem = (LinearLayout) view.findViewById(R.id.rowItem);
            video = (ConstraintLayout) view.findViewById(R.id.video);
            time = (ConstraintLayout) view.findViewById(R.id.time);
            timeText = view.findViewById(R.id.timeText);
            box = view.findViewById(R.id.box);

            video.setVisibility(View.INVISIBLE);
            time.setVisibility(View.INVISIBLE);
            box.setVisibility(View.INVISIBLE);
            box.setZ(10);
        }
    }
}