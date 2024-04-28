package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class DiscoveryActivity extends AppCompatActivity {
    RecyclerView photosView;
    DiscoveryAdapter discoveryAdapter;
    CollectionReference dbObject;
    User user;
    Object object;
    TextView name;
    ArrayList<Media> selected;
    ImageButton deleteBtn, selectAll, closeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        Intent intent = getIntent();
        String objectId = intent.getStringExtra("objectId");

        user = new User().getUser(this);
        dbObject = new Database().getDbObject();

        name = findViewById(R.id.name);

        photosView = (RecyclerView) findViewById(R.id.photosView);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        gridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        photosView.setLayoutManager(gridLayoutManager);
        discoveryAdapter = new DiscoveryAdapter(DiscoveryActivity.this);
        photosView.setAdapter(discoveryAdapter);
        discoveryAdapter.setCallback(new DiscoveryAdapter.Callback() {
            @Override
            public void onSelect(ArrayList<Media> media) {
                selected = media;
                deleteBtn.setVisibility(View.VISIBLE);
                selectAll.setVisibility(View.VISIBLE);
                closeBtn.setVisibility(View.VISIBLE);
            }
        });

        loadData(objectId);

        deleteBtn = findViewById(R.id.deleteBtn);
        selectAll = findViewById(R.id.selectAllBtn);
        closeBtn = findViewById(R.id.closeBtn);
        ImageButton back = findViewById(R.id.backBtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < selected.size(); i++) {
                    object.getPhotos().remove(selected.get(i));
                }
                dbObject.document(objectId).set(object);
                discoveryAdapter.setData(object.getPhotos());
                discoveryAdapter.setSelected(new ArrayList<>());
            }
        });

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected = selected.size() < object.getPhotos().size() ? object.getPhotos() : new ArrayList<>();
                discoveryAdapter.setSelected(selected);
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoveryAdapter.setSelected(new ArrayList<>());
                discoveryAdapter.setEditView(false);
                deleteBtn.setVisibility(View.INVISIBLE);
                selectAll.setVisibility(View.INVISIBLE);
                closeBtn.setVisibility(View.INVISIBLE);
            }
        });

        ImageButton moreBtn = findViewById(R.id.moreBtn);
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCreateDialog();
            }
        });
    }

    void loadData(String objectId) {
        dbObject.document(objectId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                object = documentSnapshot.toObject(Object.class);
                name.setText(object.getName());
                discoveryAdapter.setData(object.getPhotos());
            }
        });
    }

    public void onCreateDialog() {
        String[] items = {"Đổi tên", "Xoá discover"};
        AlertDialog.Builder builder = new AlertDialog.Builder(DiscoveryActivity.this);
        builder.setTitle("More actions")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int pos) {
                        if (pos == 0) {
                            dialog.dismiss();
                            createAlbum();
                        } else {
                            dbObject.document(object.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                        }

                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    public void createAlbum() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.create_album, null);
        android.app.AlertDialog dialog = builder.setView(view).create();

        EditText nameInput = (EditText) view.findViewById(R.id.albumName);
        nameInput.setHint("Discover name");
        nameInput.setText(object.getName());
        TextView error = (TextView) view.findViewById(R.id.errorAlbumName);
        Button addBtn = (Button) view.findViewById(R.id.addBtn);
        addBtn.setText("Cập nhật");
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = nameInput.getText().toString();
                if (value.length() == 0) {
                    error.setText("Bắt buộc");
                } else {
                    error.setText("");
                    object.setName(value);
                    dbObject.document(object.getId()).update("name", value);
                    dialog.dismiss();
                }
            }
        });

        Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameInput.setText("");
                error.setText("");
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}