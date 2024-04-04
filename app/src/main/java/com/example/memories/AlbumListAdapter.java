package com.example.memories;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumListAdapter extends ArrayAdapter<Album> {
    private Context context;
    private ArrayList<Album> albums;
    private Callback callback;

    public AlbumListAdapter(Context context, ArrayList<Album> albums) {
        super(context, 0, albums);
        this.context = context;
        this.albums = albums;
    }

    @Override
    public int getCount() {
        if (albums == null) return 0;
        return albums.size();
    }

    @Nullable
    @Override
    public Album getItem(int position) {
        return albums.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.horizontal_album,null);
        Album album = albums.get(position);
        TextView albumName = (TextView) view.findViewById(R.id.albumName);
        ImageView imageView = (ImageView) view.findViewById(R.id.photoImage);
        LinearLayout layout = view.findViewById(R.id.albumView);

        if (album.getImgUrl() == null) {
            ArrayList<Media> media = album.getPhotos();
            if (media.size() > 0) {
                album.setImgUrl(media.get(media.size()-1).getImgUrl());
            } else {
                album.setImgUrl(((Activity) context).getString(R.string.empty_img));
            }
        }

        Glide.with(imageView).load(album.getImgUrl()).into(imageView);
        albumName.setText(albums.get(position).getName());

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) callback.onClick(album);
            }
        });

        return view;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onClick(Album album);
    }
}