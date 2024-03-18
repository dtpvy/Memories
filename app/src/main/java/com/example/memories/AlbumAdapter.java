package com.example.memories;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import javax.security.auth.callback.Callback;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyView> {
    private ArrayList<Album> list;
    private Context context;
    private Boolean showCheck = false;
    private Callback callback;
    private ArrayList<Album> selected;

    public class MyView extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        CardView checkBox;
        ImageView checkImg;

        public MyView(View view)
        {
            super(view);
            textView = (TextView) view.findViewById(R.id.albumName);
            imageView = (ImageView) view.findViewById(R.id.albumImage);
            checkBox = (CardView) view.findViewById(R.id.checkBox);
            checkImg = (ImageView) view.findViewById(R.id.checkImage);
        }
    }

    public AlbumAdapter(Context context, ArrayList<Album> horizontalList) {
        this.list = horizontalList;
        this.context = context;
        this.selected = new ArrayList<>();
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album, parent, false);
        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder, final int position) {
        holder.textView.setText(list.get(position).getName());
        String imageUrl = list.get(position).getImgUrl();
        Glide.with(holder.imageView).load(imageUrl).into(holder.imageView);

        holder.checkImg.setVisibility(isSelect(position) >= 0 ? View.VISIBLE : View.INVISIBLE);
        holder.checkBox.setVisibility(showCheck && list.get(holder.getAdapterPosition()).getMutate() == true ? View.VISIBLE : View.INVISIBLE);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PhotoActivity.class);
                intent.putExtra("album_id", list.get(holder.getAdapterPosition()).getId());
                context.startActivity(intent);
            }
        });
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showCheck = true;
                notifyDataSetChanged();
                if (callback != null) callback.onLongClick();
                return true;
            }
        });

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean select = holder.checkImg.getVisibility() == View.VISIBLE;
                if (select) {
                    selected.remove(list.get(holder.getAdapterPosition()));
                    holder.checkImg.setVisibility(View.INVISIBLE);
                } else {
                    selected.add(list.get(holder.getAdapterPosition()));
                    holder.checkImg.setVisibility(View.VISIBLE);
                }
                if (callback != null) callback.onCheck(selected);
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

    public void setShowCheck(Boolean show) {
        this.showCheck = show;
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void selectAll() {
        if (selected.size() < list.size()) {
            selected = new ArrayList<>(list);
        } else {
            selected = new ArrayList<>();
        }
        notifyDataSetChanged();
        if (callback != null) callback.onCheckAll(selected);
    }

    public interface Callback {
        void onLongClick();
        void onCheck(ArrayList<Album> albums);
        void onCheckAll(ArrayList<Album> albums);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}