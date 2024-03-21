package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SelectAlbumActivity extends AppCompatActivity {
    FirebaseFirestore db;
    User user;
    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<Photo> photos = new ArrayList<>();
    ListView listView;
    ImageView backBtn;
    LinearLayout addAlbumBtn;
    String albumId;
    Album currentAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_album);

        db = FirebaseFirestore.getInstance();
        user = new User().getUser(this);
        Intent intent = getIntent();
        String intentData = intent.getStringExtra("photos");
        albumId = intent.getStringExtra("album_id");

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Photo>>(){}.getType();
        photos = gson.fromJson(intentData, listType);

        listView = findViewById(R.id.albumList);
        loadAlbum();
        
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addAlbumBtn = findViewById(R.id.addAlbum);
        addAlbumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = createAlbum();
                dialog.show();
            }
        });
    }

    public void loadAlbum() {
        albums = new ArrayList<>();
        db.collection("albums").whereEqualTo("userId", user.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        Album album = queryDocumentSnapshot.toObject(Album.class);
                        if (album.getId().compareTo(albumId) == 0) {
                            currentAlbum = album;
                        } else albums.add(album);
                    }
                    AlbumListAdapter albumListAdapter = new AlbumListAdapter(SelectAlbumActivity.this, albums);
                    albumListAdapter.setCallback(new AlbumListAdapter.Callback() {
                        @Override
                        public void onClick(Album album) {
                            AlertDialog onCreateDialog = onCreateDialog(album);
                            onCreateDialog.show();
                        }
                    });
                    listView.setAdapter(albumListAdapter);
                }
            }
        });
    }

    public Boolean checkAlbumName(String name) {
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getName().compareTo(name) == 0) {
                return false;
            }
        }
        return true;
    }

    public AlertDialog createAlbum() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.create_album, null);
        AlertDialog dialog = builder.setView(view).create();

        EditText nameInput = (EditText) view.findViewById(R.id.albumName);
        TextView error = (TextView) view.findViewById(R.id.errorAlbumName);
        Button addBtn = (Button) view.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = nameInput.getText().toString();
                if (value.length() == 0) {
                    error.setText("Bắt buộc");
                } else if (!checkAlbumName(value)) {
                    error.setText("Tên album đã tồn tại");
                } else {
                    error.setText("");
                    dialog.dismiss();
                    Album album = new Album(user.getId(), photos.get(0).getImgUrl(), nameInput.getText().toString());
                    AlertDialog onCreateDialog = onCreateDialog(album);
                    onCreateDialog.show();
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

        return dialog;
    }

    public AlertDialog onCreateDialog(Album album) {
        String[] items = {"Di chuyển", "Sao chép"};
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectAlbumActivity.this);
        builder.setTitle("Đang thêm ảnh")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int pos) {
                        if (pos == 0) {
                            ArrayList<Photo> aPhotos = currentAlbum.getPhotos();
                            ArrayList<Photo> newPhotos = new ArrayList<>();
                            for (Photo photo : aPhotos) {
                                Boolean include = false;
                                for (int k = 0; k < photos.size(); k++) {
                                    if (photos.get(k).getId().compareTo(photo.getId()) == 0) {
                                        include = true;
                                        break;
                                    }
                                }
                                if (!include) {
                                    newPhotos.add(photo);
                                }
                            }
                            db.collection("albums").document(currentAlbum.getId()).update("photos", newPhotos);

                            ArrayList<Photo> albumPhoto = album.getPhotos();
                            for (Photo photo: photos) {
                                albumPhoto.add(photo);
                            }
                            db.collection("albums").document(album.getId()).set(album)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(SelectAlbumActivity.this, "Upload successfully!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                        } else {
                            Date created = new Date();
                            ArrayList<Photo> albumPhoto = album.getPhotos();
                            for (int i = 0; i < photos.size(); i++) {
                                Photo p = photos.get(i);
                                p.setId(UUID.randomUUID().toString());
                                p.setCreatedAt(created);
                                db.collection("photos").document(p.getId()).set(p);
                                albumPhoto.add(p);
                            }
                            db.collection("albums").document(album.getId()).set(album)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(SelectAlbumActivity.this, "Upload successfully!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                        }

                    }
                });
        return builder.create();
    }
}