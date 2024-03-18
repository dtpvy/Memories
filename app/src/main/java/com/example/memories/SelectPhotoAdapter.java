package com.example.memories;


import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import javax.security.auth.callback.Callback;

public class SelectPhotoAdapter extends RecyclerView.Adapter<SelectPhotoAdapter.ViewHolder> {
    ArrayList<Photo> photos;
    ArrayList<Photo> selectedPhotos;
    Context context;
    private Callback callback;

    // Constructor for initialization
    public SelectPhotoAdapter(Context context, ArrayList<Photo> photos) {
        this.context = context;
        this.photos = photos;
        this.selectedPhotos = new ArrayList<>();
    }

    public SelectPhotoAdapter(Context context, ArrayList<Photo> photos, ArrayList<Photo> selectedPhotos) {
        this.context = context;
        this.photos = photos;
        this.selectedPhotos = filterPhotos(selectedPhotos);
    }

    public ArrayList<Photo> filterPhotos(ArrayList<Photo> selected) {
        ArrayList<Photo> arr = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            for (int j = 0; j < photos.size(); j++) {
                if (photos.get(j).getId().compareTo(selected.get(i).getId()) == 0) {
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
        String imageUrl = photos.get(position).getImgUrl();
        Glide.with(holder.image.getContext()).load(imageUrl).into(holder.image);
        holder.checkBox.setVisibility(isSelect(position) >= 0 ? View.VISIBLE : View.INVISIBLE);

        holder.rowItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                Boolean selected = holder.checkBox.getVisibility() == View.VISIBLE;
                if (selected) {
                    selectedPhotos.remove(photos.get(pos));
                } else {
                    selectedPhotos.add(photos.get(pos));
                }
                holder.checkBox.setVisibility(!selected ? View.VISIBLE : View.INVISIBLE);
                if (callback != null) callback.onCheckedChanged(photos.get(pos), !selected);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onCheckedChanged(Photo photos, Boolean checked);
        void onCheckAll(ArrayList<Photo> photos);
    }

    public void checkAll() {
        if (selectedPhotos.size() < photos.size()) {
            selectedPhotos = new ArrayList<>(photos);
        } else {
            selectedPhotos = new ArrayList<>();
        }
        notifyDataSetChanged();
        if (callback != null) callback.onCheckAll(selectedPhotos);
    }

    public int isSelect(int position) {
        for (int i = 0; i < selectedPhotos.size(); i++) {
            if (photos.get(position).getId().compareTo(selectedPhotos.get(i).getId()) == 0) {
                return i;
            }
        }
        return -1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView checkBox;
        LinearLayout rowItem;

        public ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.photoHome);
            checkBox = (ImageView) view.findViewById(R.id.checkBox);
            rowItem = (LinearLayout) view.findViewById(R.id.rowItem);
        }
    }
}