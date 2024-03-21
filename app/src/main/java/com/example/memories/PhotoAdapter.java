package com.example.memories;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyView> {
    private ArrayList<Photo> list;
    private Boolean isHold = false;
    private Callback callback;
    private ArrayList<Photo> selected;

    public class MyView extends RecyclerView.ViewHolder {
        ImageView imageView;
        CardView checkBox;
        ImageView checkImg;

        public MyView(View view)
        {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.photoImage);
            checkBox = view.findViewById(R.id.checkBox);
            checkImg = view.findViewById(R.id.checkImage);
        }
    }

    public PhotoAdapter(ArrayList<Photo> list) {
        this.list = list;
        this.selected = new ArrayList<>();
    }

    public PhotoAdapter(ArrayList<Photo> list, ArrayList<Photo> selected) {
        this.list = list;
        this.selected = selected;
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
        Boolean isSelect = isSelect(position) >= 0;

        holder.checkImg.setVisibility(isSelect ? View.VISIBLE : View.INVISIBLE);
        holder.checkBox.setVisibility(isHold ? View.VISIBLE : View.INVISIBLE);

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
    }

    public int isSelect(int position) {
        for (int i = 0; i < selected.size(); i++) {
            if (list.get(position).getId().compareTo(selected.get(i).getId()) == 0) {
                return i;
            }
        }
        return -1;
    }

    public void setSelected(ArrayList<Photo> photos) {
        this.selected = photos;
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
        void onClick(Photo photo, Boolean checked);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}