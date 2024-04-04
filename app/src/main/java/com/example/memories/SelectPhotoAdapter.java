package com.example.memories;


import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SelectPhotoAdapter extends RecyclerView.Adapter<SelectPhotoAdapter.ViewHolder> {
    ArrayList<Media> media;
    ArrayList<Media> selectedMedia;
    Context context;
    private Callback callback;
    Utils utils = new Utils();
    Handler handlerInMainThread;

    public SelectPhotoAdapter(Context context) {
        this.context = context;
        this.media = new ArrayList<>();
        this.selectedMedia = new ArrayList<>();
        this.handlerInMainThread = new Handler(context.getMainLooper());
    }
    public SelectPhotoAdapter(Context context, ArrayList<Media> media) {
        this.context = context;
        this.media = media;
        this.selectedMedia = new ArrayList<>();
    }

    public SelectPhotoAdapter(Context context, ArrayList<Media> media, ArrayList<Media> selectedMedia) {
        this.context = context;
        this.media = media;
        this.selectedMedia = filterPhotos(selectedMedia);
    }

    public ArrayList<Media> filterPhotos(ArrayList<Media> selected) {
        ArrayList<Media> arr = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            for (int j = 0; j < media.size(); j++) {
                if (media.get(j).getId().compareTo(selected.get(i).getId()) == 0) {
                    arr.add(selected.get(i));
                    break;
                }
            }
        }
        return arr;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Media m = media.get(position);
        String imageUrl = m.getImgUrl();
        Glide.with(holder.image.getContext()).load(imageUrl).placeholder(R.drawable.stockphoto).into(holder.image);
        holder.checkBox.setVisibility(isSelect(position) >= 0 ? View.VISIBLE : View.INVISIBLE);

        holder.rowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                Boolean selected = holder.checkBox.getVisibility() == View.VISIBLE;
                if (selected) {
                    selectedMedia.remove(media.get(pos));
                } else {
                    selectedMedia.add(media.get(pos));
                }
                holder.checkBox.setVisibility(!selected ? View.VISIBLE : View.INVISIBLE);
                if (callback != null) callback.onCheckedChanged(media.get(pos), !selected);
            }
        });

        if (m.getType().contains("video")) {
            holder.video.setVisibility(View.VISIBLE);
            holder.time.setVisibility(View.VISIBLE);
            Runnable calcDuration = new Runnable() {
                @Override
                public void run() {
                    String durations= utils.convertMillieToHMmSs(utils.getVideoDuration(imageUrl, context));
                    holder.timeText.setText(durations);
                }
            };

            handlerInMainThread.post(calcDuration);
        }
    }

    @Override
    public int getItemCount() {
        return media.size();
    }

    public void setPhotos(ArrayList<Media> media) {
        this.media = media;
    }

    public void setSelectedPhotos(ArrayList<Media> media) {
        this.selectedMedia = filterPhotos(media);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onCheckedChanged(Media photos, Boolean checked);
        void onCheckAll(ArrayList<Media> media);
    }

    public void checkAll() {
        if (selectedMedia.size() < media.size()) {
            selectedMedia = new ArrayList<>(media);
        } else {
            selectedMedia = new ArrayList<>();
        }
        notifyDataSetChanged();
        if (callback != null) callback.onCheckAll(selectedMedia);
    }

    public int isSelect(int position) {
        for (int i = 0; i < selectedMedia.size(); i++) {
            if (media.get(position).getId().compareTo(selectedMedia.get(i).getId()) == 0) {
                return i;
            }
        }
        return -1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView checkBox;
        LinearLayout rowItem;
        ConstraintLayout video, time;
        TextView timeText;

        public ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.photoHome);
            checkBox = (ImageView) view.findViewById(R.id.checkBox);
            rowItem = (LinearLayout) view.findViewById(R.id.rowItem);
            video = (ConstraintLayout) view.findViewById(R.id.video);
            time = (ConstraintLayout) view.findViewById(R.id.time);
            timeText = view.findViewById(R.id.timeText);
        }
    }
}