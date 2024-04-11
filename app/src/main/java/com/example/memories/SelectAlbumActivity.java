package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SelectAlbumActivity extends AppCompatActivity {
    User user;
    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<Media> media = new ArrayList<>();
    ListView listView;
    ImageView backBtn;
    LinearLayout addAlbumBtn;
    String albumId;
    Album currentAlbum;
    CollectionReference dbAlbum, dbMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_album);

        Database db = new Database();
        dbAlbum = db.getDbAlbum();
        dbMedia = db.getDbMedia();

        user = new User().getUser(this);
        Intent intent = getIntent();
        String intentData = intent.getStringExtra("photos");
        albumId = intent.getStringExtra("album_id");
        System.out.println(albumId);

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Media>>(){}.getType();
        media = gson.fromJson(intentData, listType);

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
        dbAlbum.whereEqualTo("userId", user.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        Album album = queryDocumentSnapshot.toObject(Album.class);
                        if (albumId != null && album.getId().compareTo(albumId) == 0) {
                            currentAlbum = album;
                        } else
                            albums.add(album);
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
                    Album album = new Album(user.getId(), media.get(0).getImgUrl(), nameInput.getText().toString());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectAlbumActivity.this);
        if (currentAlbum != null) {
            String[] items = {"Di chuyển", "Sao chép"};
            builder.setTitle("Đang thêm ảnh")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int pos) {
                            if (pos == 0) {
                                move(album);
                            } else {
                                copy(album);
                            }

                        }
                    });
        } else {
            String[] items = { "Sao chép"};
            builder.setTitle("Đang thêm ảnh")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int pos) {
                            if (pos == 0) {
                                copy(album);
                            }
                        }
                    });
        }

        return builder.create();
    }

    public void move(Album album) {
        ArrayList<Media> aMedia = currentAlbum.getPhotos();
        ArrayList<Media> newMedia = new ArrayList<>();
        for (Media media : aMedia) {
            Boolean include = false;
            for (int k = 0; k < SelectAlbumActivity.this.media.size(); k++) {
                if (SelectAlbumActivity.this.media.get(k).getId().compareTo(media.getId()) == 0) {
                    include = true;
                    break;
                }
            }
            if (!include) {
                newMedia.add(media);
            }
        }
        dbAlbum.document(currentAlbum.getId()).update("photos", newMedia);

        ArrayList<Media> albumMedia = album.getPhotos();
        for (Media media : SelectAlbumActivity.this.media) {
            albumMedia.add(media);
        }
        dbAlbum.document(album.getId()).set(album)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SelectAlbumActivity.this, "Upload successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    public void copy(Album album) {
        Date created = new Date();
        ArrayList<Media> albumPhoto = album.getPhotos();
        for (int i = 0; i < media.size(); i++) {
            Media p = media.get(i);
            p.setId(UUID.randomUUID().toString());
            p.setCreatedAt(created);
            dbMedia.document(p.getId()).set(p);
            albumPhoto.add(p);
        }
        dbAlbum.document(album.getId()).set(album)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SelectAlbumActivity.this, "Upload successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}