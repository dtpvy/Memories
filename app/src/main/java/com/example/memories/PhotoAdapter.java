package com.example.memories;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.LayoutInflater;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyView> {
    private ArrayList<Media> list;
    private Boolean isHold = false;
    private Callback callback;
    private ArrayList<Media> selected;
    private Context context;
    private String albumId;

    public class MyView extends RecyclerView.ViewHolder {
        ImageView imageView;
        CardView checkBox;
        ImageView checkImg;
        ConstraintLayout video;

        public MyView(View view)
        {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.photoImage);
            checkBox = view.findViewById(R.id.checkBox);
            checkImg = view.findViewById(R.id.checkImage);
            video = view.findViewById(R.id.video);
        }
    }

    public PhotoAdapter(ArrayList<Media> list, Context context, String albumId) {
        this.list = list;
        this.selected = new ArrayList<>();
        this.context = context;
        this.albumId = albumId;
    }

    public PhotoAdapter(ArrayList<Media> list, ArrayList<Media> selected, Context context, String albumId) {
        this.list = list;
        this.selected = selected;
        this.context = context;
        this.albumId = albumId;
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo, parent, false);
        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder, final int position) {
        Media media = list.get(position);
        String imageUrl = media.getImgUrl();
        Glide.with(holder.imageView).load(imageUrl).into(holder.imageView);
        Boolean isSelect = isSelect(position) >= 0;

        holder.checkImg.setVisibility(isSelect ? View.VISIBLE : View.INVISIBLE);
        holder.checkBox.setVisibility(isHold ? View.VISIBLE : View.INVISIBLE);
        holder.video.setVisibility(media.getType().contains("video") ? View.VISIBLE : View.INVISIBLE);

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(holder.getAdapterPosition());
                if (callback != null) callback.onClick(list.get(holder.getAdapterPosition()), !isSelect);
            }
        });
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setIsHold(true);
                if (callback != null) callback.onLongClick();
                return true;
            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Media _media = list.get(holder.getAdapterPosition());
                if (_media.isVideo()) {
                    Intent intent = new Intent(context, VideoActivity.class);
                    intent.putExtra("media_id", _media.getId());
                    intent.putExtra("album_id", albumId);
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, PhotoDetailActivity.class);
                    intent.putExtra("media_id", _media.getId());
                    intent.putExtra("album_id", albumId);
                    context.startActivity(intent);
                }
            }
        });
    }

    public int isSelect(int position) {
        for (int i = 0; i < selected.size(); i++) {
            if (list.get(position).getId().compareTo(selected.get(i).getId()) == 0) {
                return i;
            }
        }
        return -1;
    }

    public void setSelected(ArrayList<Media> media) {
        this.selected = media;
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setIsHold(Boolean isHold) {
        this.isHold = isHold;
        notifyDataSetChanged();
    }

    public interface Callback {
        void onLongClick();
        void onClick(Media media, Boolean checked);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}