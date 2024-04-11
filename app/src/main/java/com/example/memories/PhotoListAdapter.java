package com.example.memories;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PhotoListAdapter extends ArrayAdapter<PhotoList> {
    private final Context context;
    private final ArrayList<PhotoList> photoLists;
    private ArrayList<Media> selected;
    private Boolean isEdit = false;
    private Callback callback;
    private String albumId;

    public PhotoListAdapter(Context context, ArrayList<PhotoList> photoLists, String albumId) {
        super(context, 0, photoLists);
        this.context = context;
        this.photoLists = photoLists;
        this.selected = new ArrayList<>();
        this.albumId = albumId;
    }

    public void setIsEdit(Boolean isEdit) {
        this.isEdit = isEdit;
        this.selected = new ArrayList<>();
        notifyDataSetChanged();
        if (callback != null) callback.onChange(this.selected);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onLongClick();
        void onChange(ArrayList<Media> media);
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
        Boolean select = isSelectedAll(photoLists.get(position));

        View view = inflater.inflate(R.layout.photo_list,null);
        TextView dateText = (TextView) view.findViewById(R.id.date);

        Button selectAll = (Button) view.findViewById(R.id.selectAll);

        selectAll.setText(select ? "Bỏ chọn tất cả" : "Chọn tất cả");
        selectAll.setVisibility(isEdit ? View.VISIBLE : View.INVISIBLE);

        RecyclerView photosView = (RecyclerView) view.findViewById(R.id.listPhoto);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 4);
        photosView.addItemDecoration(new SpacesItemDecoration(16));
        photosView.setLayoutManager(gridLayoutManager);

        Locale locale = new Locale("vi", "VN");
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        String date = dateFormat.format(photoLists.get(position).getDate());
        dateText.setText(date);

        PhotoAdapter photoAdapter = new PhotoAdapter(photoLists.get(position).getPhotos(), selected, context, albumId);
        photoAdapter.setIsHold(isEdit);
        photoAdapter.setCallback(new PhotoAdapter.Callback() {
            @Override
            public void onLongClick() {
                if (callback != null) callback.onLongClick();
            }
            public void onClick(Media media, Boolean checked) {
                int position = -1;
                for (int i = 0; i < selected.size(); i++) {
                    if (selected.get(i).getId().compareTo(media.getId()) == 0) {
                        position = i;
                        break;
                    }
                }
                if (position < 0 && checked) {
                    selected.add(media);
                }
                if (position >= 0 && !checked) {
                    selected.remove(position);
                }
                onChange(selected);
            }
        });

        photosView.setAdapter(photoAdapter);

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean select = isSelectedAll(photoLists.get(position));
                for (Media item: photoLists.get(position).getPhotos()) {
                    int idx = -1;
                    for (int i = 0; i < selected.size(); i++) {
                        if (selected.get(i).getId().compareTo(item.getId()) == 0) {
                            idx = i;
                            break;
                        }
                    }
                    if (select && idx >= 0) {
                        selected.remove(idx);
                    }
                    if (!select && idx < 0) {
                        selected.add(item);
                    }
                }
                selectAll.setText(!select ? "Bỏ chọn tất cả" : "Chọn tất cả");
                onChange(selected);
            }
        });

        return view;
    }

    public Boolean isSelectedAll(PhotoList photoList) {
        for (Media media : photoList.getPhotos()) {
            Boolean include = false;
            for (Media select: selected) {
                if (media.getId().compareTo(select.getId()) == 0) {
                    include = true;
                    break;
                }
            }
            if (!include) return false;
        }
        return true;
    }

    public void onChange(ArrayList<Media> selected) {
        notifyDataSetChanged();
        if (callback != null) callback.onChange(selected);
    }

    public void onSelectAll() {
        ArrayList<Media> medias = new ArrayList<>();
        for (PhotoList photoList: photoLists) {
            for (Media media : photoList.getPhotos()) {
                medias.add(media);
            }
        }
        if (selected.size() < medias.size()) selected = medias;
        else selected.clear();
        onChange(selected);
    }
}