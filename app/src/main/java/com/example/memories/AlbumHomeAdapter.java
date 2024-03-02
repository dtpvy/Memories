
package com.example.memories;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumHomeAdapter extends RecyclerView.Adapter<AlbumHomeAdapter.MyView> {
    private ArrayList<Album> list;
    public class MyView extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public MyView(View view)
        {
            super(view);
            textView = (TextView) view.findViewById(R.id.albumName);
            imageView = (ImageView) view.findViewById(R.id.albumImage);
        }
    }

    public AlbumHomeAdapter(ArrayList<Album> horizontalList) {
        this.list = horizontalList;
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_home, parent, false);
        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder, final int position) {
        holder.textView.setText(list.get(position).getName());
        String imageUrl = list.get(position).getImgUrl();
        Glide.with(holder.imageView).load(imageUrl).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}