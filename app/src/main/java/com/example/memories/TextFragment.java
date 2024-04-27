

package com.example.memories;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.naz013.colorslider.ColorSlider;
import ja.burhanrashid52.photoeditor.PhotoEditor;

public class TextFragment extends Fragment implements FragmentCallbacks {
    PhotoEditorActivity main;
    Context context = null;
    TextView cancelBtn, saveBtn;
    ColorSlider colorFilter;
    PhotoEditor photoEditor;
    String text;
    View viewText;
    int currentColor = Color.BLACK;
    public String name = "TextFrame";

    public static TextFragment newInstance(String strArg1) {
        TextFragment fragment = new TextFragment();
        Bundle bundle = new Bundle();
        bundle.putString("strArg1", strArg1);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setPhotoEditor(PhotoEditor photoEditor) {
        this.photoEditor = photoEditor;
    }

    public void setAtt(String text, int color) {
        this.text = text;
        this.currentColor = color;
        if (colorFilter != null) {
            colorFilter.setSelectorColor(color);
        }
    }

    public void show(View view, String text, int color) {
        this.viewText = view;
        this.text = text;
        if (text != "") this.currentColor = color;
        createDialog();
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

    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.create_album, null);
        AlertDialog dialog = builder.setView(view).create();

        EditText nameInput = (EditText) view.findViewById(R.id.albumName);
        nameInput.setText(text);
        Button addBtn = (Button) view.findViewById(R.id.addBtn);
        addBtn.setText("Cập nhật");
        nameInput.setHint("Nhập văn bản");
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = nameInput.getText().toString();
                photoEditor.editText(viewText, value, currentColor);
                dialog.dismiss();
            }
        });

        Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        colorFilter = view.findViewById(R.id.colorSlider);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        saveBtn = view.findViewById(R.id.saveBtn);

        colorFilter.setGradient(new int[]{Color.WHITE, Color.GRAY, Color.BLACK, Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED, Color.MAGENTA}, 200);
        colorFilter.setSelectorColor(currentColor);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.onMsgFromFragToMain(name, "cancel");
            }
        });

        colorFilter.setListener(new ColorSlider.OnColorSelectedListener() {
            @Override
            public void onColorChanged(int position, int i) {
                currentColor = i;
                if (viewText != null) {
                    photoEditor.editText(viewText, text, currentColor);
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.onMsgFromFragToMain(name, "save");
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