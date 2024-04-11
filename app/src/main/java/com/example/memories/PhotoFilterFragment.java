

package com.example.memories;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Arrays;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;

public class PhotoFilterFragment extends Fragment implements FragmentCallbacks {
    PhotoEditorActivity main;
    Context context = null;
    TextView cancelBtn, saveBtn;
    RecyclerView recyclerView;
    public String name = "PhotoFilterFragment";
    static ArrayList<PhotoFilter> list = new ArrayList<PhotoFilter>(Arrays.asList(
            PhotoFilter.NONE,
            PhotoFilter.NEGATIVE,
            PhotoFilter.POSTERIZE,
            PhotoFilter.SATURATE,
            PhotoFilter.TEMPERATURE,
            PhotoFilter.VIGNETTE,
            PhotoFilter.BLACK_WHITE,
            PhotoFilter.DUE_TONE,
            PhotoFilter.FISH_EYE,
            PhotoFilter.GRAY_SCALE,
            PhotoFilter.AUTO_FIX,
            PhotoFilter.BRIGHTNESS,
            PhotoFilter.CONTRAST,
            PhotoFilter.CROSS_PROCESS,
            PhotoFilter.DOCUMENTARY,
            PhotoFilter.FILL_LIGHT,
            PhotoFilter.FLIP_HORIZONTAL,
            PhotoFilter.GRAIN,
            PhotoFilter.LOMISH,
            PhotoFilter.SEPIA,
            PhotoFilter.SHARPEN,
            PhotoFilter.TINT
    ));
    ArrayList<Filter> filters;

    public static PhotoFilterFragment newInstance(String strArg1) {
        PhotoFilterFragment fragment = new PhotoFilterFragment();
        Bundle bundle = new Bundle();
        bundle.putString("strArg1", strArg1);
        fragment.setArguments(bundle);
        return fragment;
    }

    public PhotoFilterFragment() {
        if (filters != null) return;;
        filters = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            filters.add(new Filter(list.get(i).name(), list.get(i)));
        }
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
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
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

        recyclerView = view.findViewById(R .id.filterList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        PhotoFilterAdapter filterAdapter = new PhotoFilterAdapter(getActivity(), filters);
        filterAdapter.setListener(new PhotoFilterAdapter.Listener() {
            @Override
            public void onSelectedItem(PhotoFilter filter) {
                main.onMsgFromFragToMain(name, filter.name());
            }
        });
        recyclerView.setAdapter(filterAdapter);

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