

package com.example.memories;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import ja.burhanrashid52.photoeditor.PhotoEditor;

public class PhotoEmojiFragment extends Fragment implements FragmentCallbacks {
    PhotoEditorActivity main;
    Context context = null;
    TextView cancelBtn, saveBtn;
    RecyclerView recyclerView;
    public String name = "PhotoEmojiFragment";

    public static PhotoEmojiFragment newInstance(String strArg1) {
        PhotoEmojiFragment fragment = new PhotoEmojiFragment();
        Bundle bundle = new Bundle();
        bundle.putString("strArg1", strArg1);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            context = getActivity();
            main = (PhotoEditorActivity) getActivity();
        }
        catch (IllegalStateException e) {
            throw new IllegalStateException("Activity must implement callbacks");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_photo_emoji, container, false);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        saveBtn = view.findViewById(R.id.saveBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.onMsgFromFragToMain(name, "cancel");
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.onMsgFromFragToMain(name, "save");
            }
        });

        recyclerView = view.findViewById(R.id.emojiList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false));
        EmojiAdapter emojiAdapter = new EmojiAdapter(getActivity(), PhotoEditor.getEmojis(getActivity()));
        emojiAdapter.setListener(new EmojiAdapter.EmojiAdapterListener() {
            @Override
            public void onEmojiSelectedItem(String emoji) {
                main.onMsgFromFragToMain(name, emoji);
            }
        });
        recyclerView.setAdapter(emojiAdapter);
        return view;
    }

    @Override
    public void onMsgFromMainToFragment(String action) {
//        switch (action) {
//            case "control": {
//                container.setVisibility(View.INVISIBLE);
//            }
//            default: {
//                container.setVisibility(View.INVISIBLE);
//                break;
//            }
//        }
    }
}