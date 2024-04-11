package com.example.memories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.rockerhieu.emojicon.EmojiconTextView;
import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;

public class PhotoFilterAdapter extends RecyclerView.Adapter<PhotoFilterAdapter.ViewHolder> {
    ArrayList<Filter> list;
    Context context;
    Listener listener;
    Handler handlerInMainThread;

    public class ViewHolder extends RecyclerView.ViewHolder {
        PhotoEditorView photo;
        TextView filterName;

        public ViewHolder(View view)
        {
            super(view);
            photo = view.findViewById(R.id.photo);
            filterName = view.findViewById(R.id.filterName);
            photo.getSource().setImageResource(R.drawable.photo_filter);
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onSelectedItem(list.get(getAdapterPosition()).getPhotoFilter());
                }
            });
        }
    }

    public interface Listener {
        void onSelectedItem(PhotoFilter filter);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public PhotoFilterAdapter(Context context, ArrayList<Filter> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public PhotoFilterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_item, parent, false);
        return new PhotoFilterAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PhotoFilterAdapter.ViewHolder holder, int position) {
        Filter filter = list.get(position);
        if (filter.getBitmap() == null) {
            holder.filterName.setText(filter.getName());
            PhotoEditor photoEditor = new PhotoEditor.Builder(context, holder.photo).build();
            photoEditor.setFilterEffect(filter.getPhotoFilter());
            photoEditor.saveAsBitmap(new OnSaveBitmap() {
                @Override
                public void onBitmapReady(Bitmap bitmap) {
                    filter.setBitmap(bitmap);
                    holder.photo.getSource().setImageBitmap(bitmap);
                }

                @Override
                public void onFailure(Exception e) {}
            });
        } else {
            holder.filterName.setText(filter.getName());
            holder.photo.getSource().setImageBitmap(filter.getBitmap());
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
