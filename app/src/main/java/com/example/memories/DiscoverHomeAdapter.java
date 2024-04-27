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

public class DiscoverHomeAdapter extends RecyclerView.Adapter<DiscoverHomeAdapter.MyView> {
    private ArrayList<Object> list;
    public class MyView extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public MyView(View view)
        {
            super(view);
            textView = (TextView) view.findViewById(R.id.discoverName);
            imageView = (ImageView) view.findViewById(R.id.discoverImage);
        }
    }

    public DiscoverHomeAdapter(ArrayList<Object> horizontalList) {
        this.list = horizontalList;
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.discover_home, parent, false);
        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder, final int position) {
        holder.textView.setText(list.get(position).getName());
        String imageUrl = list.get(position).getImgUrl();
        System.out.println(imageUrl);
        Glide.with(holder.imageView).load(imageUrl).placeholder(R.drawable.stockphoto).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}