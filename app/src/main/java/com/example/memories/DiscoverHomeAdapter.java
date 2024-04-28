package com.example.memories;

import android.content.Context;
import android.content.Intent;
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
    private Context context;
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

    public DiscoverHomeAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
    }

    public void setList(ArrayList<Object> list) {
        this.list = list;
        notifyDataSetChanged();
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
        Glide.with(holder.imageView).load(imageUrl).placeholder(R.drawable.stockphoto).into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DiscoveryActivity.class);
                intent.putExtra("objectId", list.get(holder.getAdapterPosition()).getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}