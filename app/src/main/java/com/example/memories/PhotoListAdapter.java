package com.example.memories;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PhotoListAdapter extends ArrayAdapter<PhotoList> {
    private final Context context;
    private final ArrayList<PhotoList> photoLists;

    public PhotoListAdapter(Context context, ArrayList<PhotoList> photoLists) {
        super(context, 0, photoLists);
        this.context = context;
        this.photoLists = photoLists;
    }

    @Override
    public int getCount() {
        return photoLists.size();
    }

    @Nullable
    @Override
    public PhotoList getItem(int position) {
        return photoLists.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.photo_list,null);
        TextView dateText = (TextView) view.findViewById(R.id.date);

        RecyclerView photosView = (RecyclerView) view.findViewById(R.id.listPhoto);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 4);
        photosView.addItemDecoration(new SpacesItemDecoration(16));
        photosView.setLayoutManager(gridLayoutManager);

        Locale locale = new Locale("vi", "VN");
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        String date = dateFormat.format(photoLists.get(position).getDate());
        dateText.setText(date);

        PhotoAdapter photoAdapter = new PhotoAdapter(photoLists.get(position).getPhotos());
        photosView.setAdapter(photoAdapter);

        return view;
    }
}