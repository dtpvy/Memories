

package com.example.memories;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class PhotoLightControlFragment extends Fragment implements FragmentCallbacks {
    PhotoEditorActivity main;
    Context context = null;
    TextView cancelBtn, saveBtn;
    SeekBar seekLightingMul, seekLightingAdd;
    int multiplerBB = 0, additionBB = 0;
    public String name = "PhotoLightControlFragment";

    public PhotoLightControlFragment() {
        this.multiplerBB = spread(255);
        this.additionBB = spread(0);
    }

    public static  PhotoLightControlFragment newInstance(String strArg1) {
        PhotoLightControlFragment fragment = new PhotoLightControlFragment();
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

    private int spread(int progress) {
        return (progress * 0x10000 + progress * 0x100 + progress * 0x1);
    }

    private int calcProcess(int x) {
        return x / 0x10101;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_light_control, container, false);
        seekLightingMul = view.findViewById(R.id.seek_lighting_mul);
        seekLightingAdd = view.findViewById(R.id.seek_lighting_add);
        cancelBtn = view.findViewById(R.id.cancelBtn);
        saveBtn = view.findViewById(R.id.saveBtn);

        seekLightingMul.setProgress(calcProcess(this.multiplerBB));
        seekLightingAdd.setProgress(calcProcess(this.additionBB));

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

        seekLightingMul.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                multiplerBB = spread(progress);
                main.onMsgFromFragToMain(name, "change");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekLightingAdd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                additionBB = spread(progress);
                main.onMsgFromFragToMain(name, "change");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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