package com.example.memories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MoreSheetFragment extends BottomSheetDialogFragment {
    Callback callback;

    public MoreSheetFragment() {
        // Required empty public constructor
    }

    interface Callback {
        void setWrapper();
        void seeInformation();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more_sheet, container, false);
        TextView wrapper = view.findViewById(R.id.wrapper);
        TextView info = view.findViewById(R.id.info);

        wrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) callback.setWrapper();
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null) callback.seeInformation();
            }
        });

        return view;
    }
}