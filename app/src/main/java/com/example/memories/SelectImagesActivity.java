package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SelectImagesActivity extends AppCompatActivity {
    Spinner spinner;
    User user;
    FirebaseFirestore db;
    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<Photo> selectPhotos;
    RecyclerView photosView;
    TextView albumName;
    SelectPhotoAdapter photoHomeAdapter;
    Boolean selectAll = false;
    Album currentAlbum;
    ImageButton closeBtn, selectAllBtn;
    Button doneBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_images);

        Intent intent = getIntent();
        String message = intent.getStringExtra("album_name");
        selectPhotos = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        user = new User().getUser(this);
        spinner = findViewById(R.id.albumList);

        AlertDialog dialog = onCreateDialog();
        albumName = findViewById(R.id.albumName);
        albumName.setText(message);

        photosView = (RecyclerView) findViewById(R.id.allPhotoView);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        gridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        photosView.setLayoutManager(gridLayoutManager);
        photosView.setHasFixedSize(true);

        loadAlbums();
        loadPhotos(user.getDefaultAlbum().getId());

        doneBtn = findViewById(R.id.doneBtn);
        closeBtn = findViewById(R.id.cancelButton);
        selectAllBtn = findViewById(R.id.selectAllButton);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        selectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoHomeAdapter.checkAll();
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
    }

    public void loadAlbums() {
        db.collection("albums").whereEqualTo("userId", user.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Album album = document.toObject(Album.class);
                        albums.add(album);
                    }
                    ArrayAdapter ad = new ArrayAdapter(SelectImagesActivity.this, R.layout.spinner_list, albums);
                    ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(ad);
                    for (int i = 0; i < albums.size(); i++) {
                        if (albums.get(i).getId().compareTo(user.getDefaultAlbum().getId()) == 0) {
                            spinner.setSelection(i, true);
                        }
                    }

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            loadPhotos(albums.get(i).getId());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }
            }
        });
    }

    public void loadPhotos(String albumId) {
        System.out.println(selectPhotos);

        db.collection("albums").document(albumId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Album album = documentSnapshot.toObject(Album.class);
                currentAlbum = album;
                photoHomeAdapter = new SelectPhotoAdapter(SelectImagesActivity.this, album.getPhotos(), selectPhotos);
                photoHomeAdapter.setCallback(new SelectPhotoAdapter.Callback() {
                    @Override
                    public void onCheckedChanged(Photo photo, Boolean checked) {
                        if (checked) {
                            selectPhotos.add(photo);
                        } else {
                            selectPhotos.remove(photo);
                        }
                        System.out.println(selectPhotos);
                    }

                    @Override
                    public void onCheckAll(ArrayList<Photo> photos) {
                        selectAll(photos.size() > 0);
                    }
                });

                photosView.setAdapter(photoHomeAdapter);
            }
        });
    }

    public void selectAll(Boolean isSelect) {
        ArrayList<Photo> photos = currentAlbum.getPhotos();
        for (int i = 0; i < photos.size(); i++) {
            int pos = -1;
            for (int j = 0; j < selectPhotos.size(); j++) {
                if (selectPhotos.get(j).getId().compareTo(photos.get(i).getId()) == 0) {
                    pos = j;
                    break;
                }
            }
            if (!isSelect && pos >= 0) {
                selectPhotos.remove(pos);
            }
            if (isSelect && pos == -1) {
                selectPhotos.add(photos.get(i));
            }
        }
    }

    public AlertDialog onCreateDialog() {
        String[] items = {"Di chuyển", "Sao chép"};
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectImagesActivity.this);
        builder.setTitle("Đang thêm ảnh")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int pos) {
                        if (pos == 0) {
                            for (Album album : albums) {
                                ArrayList<Photo> photos = album.getPhotos();
                                ArrayList<Photo> newPhotos = new ArrayList<>();
                                for (Photo photo : photos) {
                                    Boolean include = false;
                                    for (int k = 0; k < selectPhotos.size(); k++) {
                                        if (selectPhotos.get(k).getId().compareTo(photo.getId()) == 0) {
                                            include = true;
                                            break;
                                        }
                                    }
                                    if (!include) {
                                        newPhotos.add(photo);
                                    }
                                }
                                db.collection("albums").document(album.getId()).update("photos", newPhotos);
                            }

                            Album album = new Album(user.getId(), selectPhotos.get(0).getImgUrl(), albumName.getText().toString(), selectPhotos);
                            db.collection("albums").document(album.getId()).set(album)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Intent intent = new Intent(SelectImagesActivity.this, PhotoActivity.class);
                                            intent.putExtra("album_id", album.getId());
                                            SelectImagesActivity.this.startActivity(intent);
                                            finish();
                                        }
                                    });
                        } else {
                            Date created = new Date();
                            for (int i = 0; i < selectPhotos.size(); i++) {
                                Photo p = selectPhotos.get(i);
                                p.setId(UUID.randomUUID().toString());
                                p.setCreatedAt(created);
                                db.collection("photos").document(p.getId()).set(p);
                            }
                            Album album = new Album(user.getId(), selectPhotos.get(0).getImgUrl(), albumName.getText().toString(), selectPhotos);
                            db.collection("albums").document(album.getId()).set(album)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Intent intent = new Intent(SelectImagesActivity.this, PhotoActivity.class);
                                            intent.putExtra("album_id", album.getId());
                                            SelectImagesActivity.this.startActivity(intent);
                                            finish();
                                        }
                                    });
                        }

                    }
                });
        return builder.create();
    }
}