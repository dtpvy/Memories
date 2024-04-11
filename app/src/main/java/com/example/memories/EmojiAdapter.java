package com.example.memories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.github.rockerhieu.emojicon.EmojiconTextView;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {
    ArrayList<String> list;
    Context context;
    EmojiAdapterListener listener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        EmojiconTextView emojiconTextView;

        public ViewHolder(View view)
        {
            super(view);
            emojiconTextView = (EmojiconTextView) view.findViewById(R.id.emoji);
            emojiconTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onEmojiSelectedItem(list.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface EmojiAdapterListener {
        void onEmojiSelectedItem(String emoji);
    }

    public void setListener(EmojiAdapterListener listener) {
        this.listener = listener;
    }

    public EmojiAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public EmojiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_item, parent, false);
        return new EmojiAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EmojiAdapter.ViewHolder holder, final int position) {
        holder.emojiconTextView.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
