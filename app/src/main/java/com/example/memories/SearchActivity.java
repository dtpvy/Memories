package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    EditText search;
    CollectionReference dbObject, dbMedia;
    RecyclerView discoverView, photosView;
    DiscoverHomeAdapter discoverHomeAdapter;
    PhotoHomeAdapter photoHomeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Database database = new Database();
        dbObject = database.getDbObject();
        dbMedia = database.getDbMedia();

        discoverView = (RecyclerView) findViewById(R.id.discoverView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        discoverView.setLayoutManager(layoutManager);
        discoverHomeAdapter = new DiscoverHomeAdapter(SearchActivity.this);
        discoverView.setAdapter(discoverHomeAdapter);

        photosView = (RecyclerView) findViewById(R.id.photosView);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        gridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        photosView.setLayoutManager(gridLayoutManager);
        photoHomeAdapter = new PhotoHomeAdapter(SearchActivity.this);
        photosView.setAdapter(photoHomeAdapter);

        search = findViewById(R.id.search);
        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loadData();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    public void loadData() {
        dbObject.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Object> objects = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot: task.getResult()) {
                        Object object = documentSnapshot.toObject(Object.class);
                        if (object.getName().contains(search.getText()) || object.getLabel().contains(search.getText())) {
                            objects.add(object);
                        }
                    }
                    discoverHomeAdapter.setList(objects);
                }
            }
        });

        dbMedia.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Media> mediaList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot: task.getResult()) {
                        Media media = documentSnapshot.toObject(Media.class);
                        if (media.getId().contains(search.getText())) {
                            mediaList.add(media);
                        } else if (media.getLabels() != null) {
                            for (int i = 0; i < media.getLabels().size(); i++) {
                                String label = media.getLabels().get(i);
                                if (label.contains(search.getText())) {
                                    mediaList.add(media);
                                    break;
                                }
                            }
                        }
                    }
                    photoHomeAdapter.setData(mediaList);
                }
            }
        });
    }
}