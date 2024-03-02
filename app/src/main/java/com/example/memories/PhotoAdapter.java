package com.example.memories;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyView> {
    private ArrayList<Photo> list;
    public class MyView extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MyView(View view)
        {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.photoImage);
        }
    }

    public PhotoAdapter(ArrayList<Photo> list) {
        this.list = list;
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo, parent, false);
        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder, final int position) {
        String imageUrl = list.get(position).getImgUrl();
        Glide.with(holder.imageView).load(imageUrl).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}