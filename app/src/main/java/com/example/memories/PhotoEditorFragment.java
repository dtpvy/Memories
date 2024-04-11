package com.example.memories;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PhotoEditorFragment extends Fragment implements FragmentCallbacks {
    PhotoEditorActivity main;
    Context context = null;
    TextView cancelBtn, saveBtn;
    LinearLayout cutBtn, lightBtn, emojiBtn, textBtn, effectBtn;
    public String name = "PhotoEditorFragment";

    public static PhotoEditorFragment newInstance(String strArg1) {
        PhotoEditorFragment fragment = new PhotoEditorFragment();
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
        View view = inflater.inflate(R.layout.fragment_photo_editor, container, false);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        saveBtn = view.findViewById(R.id.saveBtn);
        cutBtn = view.findViewById(R.id.cutBtn);
        lightBtn = view.findViewById(R.id.lightBtn);
        emojiBtn = view.findViewById(R.id.emojiBtn);
        textBtn = view.findViewById(R.id.textBtn);
        effectBtn = view.findViewById(R.id.effectBtn);

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

        cutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.onMsgFromFragToMain(name, "crop");
            }
        });

        effectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.onMsgFromFragToMain(name, "effect");
            }
        });

        lightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.onMsgFromFragToMain(name, "light");
            }
        });

        emojiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.onMsgFromFragToMain(name, "emoji");
            }
        });

        textBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.onMsgFromFragToMain(name, "text");
            }
        });

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