package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AlbumActivity extends AppCompatActivity {
    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<Album> selected = new ArrayList<>();;
    ImageButton backBtn, moreBtn;
    User user;
    RecyclerView albumView;
    ConstraintLayout albumControl;
    LinearLayout editBtn, removeBtn, cancelBtn, archiveBtn, selectAllBtn;
    AlbumAdapter albumHomeAdapter;
    AlertDialog removeDialog, editAlbumDialog;
    FirebaseStorage storage;
    String editPath = "", password = "";
    ImageView imageView;
    CollectionReference dbAlbum, dbUser, dbMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        storage = FirebaseStorage.getInstance();

        Database db = new Database();
        dbAlbum = db.getDbAlbum();
        dbMedia = db.getDbMedia();
        dbUser = db.getDbUser();

        user = new User().getUser(this);

        removeDialog = onCreateDialogRemove();

        albumControl = findViewById(R.id.albumControl);
        editBtn = findViewById(R.id.editAlbum);
        removeBtn = findViewById(R.id.removeAlbum);
        archiveBtn = findViewById(R.id.archiveAlbum);
        cancelBtn = findViewById(R.id.cancelAction);
        selectAllBtn = findViewById(R.id.selectAllAlbum);
        moreBtn = findViewById(R.id.moreBtn);

        albumView = findViewById(R.id.albumList);
        GridLayoutManager albumLayoutManager = new GridLayoutManager(this, 3);
        albumView.setLayoutManager(albumLayoutManager);

        loadData("createdAt", Query.Direction.ASCENDING);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                albumControl.setVisibility(View.INVISIBLE);
                albumView.setPadding(0, 0, 0, 0);
                albumHomeAdapter.setShowCheck(false);
            }
        });

        selectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                albumHomeAdapter.selectAll();
            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeDialog.show();
            }
        });

        archiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Album album: selected) {
                    for (Media media : album.getPhotos()) {
                        dbMedia.document(media.getId()).update("deletedAt", new Date());
                    }
                    dbAlbum.document(album.getId()).delete();
                }
                albumControl.setVisibility(View.INVISIBLE);
                albumView.setPadding(0, 0, 0, 0);
                albumHomeAdapter.setShowCheck(false);
                loadData("createdAt", Query.Direction.ASCENDING);
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected.size() < 0) return;
                editAlbumDialog = editAlbum();
                editAlbumDialog.show();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSort();
            }
        });
    }

    public void loadData(String field, Query.Direction direction) {
        albums = new ArrayList<>();
        dbAlbum.whereEqualTo("userId", user.getId()).orderBy(field, direction).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Album album = document.toObject(Album.class);
                        if (album.getImgUrl() == null) {
                            ArrayList<Media> media = album.getPhotos();
                            if (media.size() > 0) {
                                album.setImgUrl(media.get(media.size()-1).getImgUrl());
                            } else {
                                album.setImgUrl(getString(R.string.empty_img));
                            }
                        }
                        albums.add(album);
                    }
                    albumHomeAdapter = new AlbumAdapter(AlbumActivity.this, albums);
                    albumHomeAdapter.setCallback(new AlbumAdapter.Callback() {
                        @Override
                        public void onLongClick() {
                            albumControl.setVisibility(View.VISIBLE);
                            albumView.setPadding(0, 0, 0, 100);
                        }

                        @Override
                        public void onCheck(ArrayList<Album> albums) {
                            selected = albums;
                            editBtn.setEnabled(selected.size() == 1);
                            editBtn.setBackgroundResource(selected.size() == 1 ? R.color.white : R.color.border);
                        }

                        @Override
                        public void onCheckAll(ArrayList<Album> albums) {
                            selected = albums;
                            editBtn.setEnabled(selected.size() == 1);
                            editBtn.setBackgroundResource(selected.size() == 1 ? R.color.white : R.color.border);
                        }

                        @Override
                        public void onClick(Album album) {
                            if (album.getId().compareTo(user.getPrivateAlbum().getId()) == 0) {
                                showPassword();
                            } else {
                                Intent intent = new Intent(AlbumActivity.this, PhotoActivity.class);
                                intent.putExtra("album_id", album.getId());
                                AlbumActivity.this.startActivity(intent);
                            }
                        }
                    });
                    albumView.setAdapter(albumHomeAdapter);
                }
            }
        });
    }

    public AlertDialog onCreateDialogRemove() {
        // Use the Builder class for convenient dialog construction.
        AlertDialog.Builder builder = new AlertDialog.Builder(AlbumActivity.this);
        builder.setTitle("Xoá album")
                .setMessage("Bằng việc xác nhận album của bạn sẽ bị xoá vĩnh viễn")
                .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (Album album: selected) {
                            for (Media media : album.getPhotos()) {
                                dbMedia.document(media.getId()).delete();
                            }
                            dbMedia.document(album.getId()).delete();
                        }
                        albumControl.setVisibility(View.INVISIBLE);
                        albumView.setPadding(0, 0, 0, 0);
                        albumHomeAdapter.setShowCheck(false);
                        loadData("createdAt", Query.Direction.ASCENDING);
                    }
                })
                .setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    public Boolean checkAlbumName(String name) {
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getName().compareTo(name) == 0) {
                return false;
            }
        }
        return true;
    }

    private void chooseImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                String imageEncoded = "";
                if (data.getData() != null){
                    Uri mImageUri = data.getData();
                    Cursor cursor = getContentResolver().query(mImageUri, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);

                    cursor.close();
                }
                uploadFile(imageEncoded);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getName(String path) {
        File file = new File(path);
        return file.getName();
    }

    public void uploadFile(String path) throws FileNotFoundException {
        StorageReference storageRef = storage.getReference();

        UUID uuid = UUID.randomUUID();
        String childPath = user.getId() + "/" + uuid.toString() + "-" + getName(path);
        StorageReference mountainsRef = storageRef.child(childPath);

        InputStream stream = new FileInputStream(new File(path));
        UploadTask uploadTask = mountainsRef.putStream(stream);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        editPath = uri.toString();
                        Glide.with(imageView).load(editPath).into(imageView);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    public AlertDialog editAlbum() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_album, null);
        AlertDialog dialog = builder.setView(view).create();

        imageView = (ImageView) view.findViewById(R.id.albumImage);
        EditText nameInput = (EditText) view.findViewById(R.id.albumName);
        TextView error = (TextView) view.findViewById(R.id.errorAlbumName);
        Button addBtn = (Button) view.findViewById(R.id.addBtn);

        editPath = selected.get(0).getImgUrl();
        Glide.with(imageView).load(editPath).into(imageView);
        nameInput.setText(selected.get(0).getName());

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImages();
            }
        });

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
                    dbAlbum.document(selected.get(0).getId()).update("name", nameInput.getText().toString());
                    dbAlbum.document(selected.get(0).getId()).update("imgUrl", editPath);
                    loadData("createdAt", Query.Direction.ASCENDING);

                    albumControl.setVisibility(View.INVISIBLE);
                    albumView.setPadding(0, 0, 0, 0);
                    albumHomeAdapter.setShowCheck(false);
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

        return dialog;
    }

    public void showSort() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AlbumActivity.this);
        builder.setTitle("Sắp xếp");
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.sort_bottom_sheet, null);
        AlertDialog dialog = builder.setView(view).create();

        TextView nameAsc = view.findViewById(R.id.nameAsc);
        TextView nameDesc = view.findViewById(R.id.nameDesc);
        TextView dateAsc = view.findViewById(R.id.dateAsc);
        TextView dateDesc = view.findViewById(R.id.dateDesc);

        nameAsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("name", Query.Direction.ASCENDING);
            }
        });

        nameDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("name", Query.Direction.DESCENDING);
            }
        });

        dateAsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("createdAt", Query.Direction.ASCENDING);
            }
        });

        dateDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData("createdAt", Query.Direction.DESCENDING);
            }
        });


        dialog.show();
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showPassword() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.private_album, null);
        android.app.AlertDialog dialog = builder.setView(view).create();
        Boolean hasPassword = user.getPassword() != null;
        dialog.setTitle(hasPassword ? "Nhập mật khẩu" : "Nhập mật khẩu mới");

        PatternLockView mPatternLockView = view.findViewById(R.id.pattern_lock_view);
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {}

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {}

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                String value = PatternLockUtils.patternToString(mPatternLockView, pattern);
                if (!hasPassword && password.length() == 0) {
                    password = value;
                    dialog.setTitle("Xác nhận mật khẩu");
                    mPatternLockView.clearPattern();
                } else if (!hasPassword) {
                    if (password.compareTo(value) != 0) {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    } else {
                        user.setPassword(value);
                        dbUser.document(user.getId()).update("password", value);
                        Intent intent = new Intent(AlbumActivity.this, PhotoActivity.class);
                        intent.putExtra("album_id", user.getPrivateAlbum().getId());
                        AlbumActivity.this.startActivity(intent);
                        dialog.dismiss();
                    }
                } else {
                    if (user.getPassword().compareTo(value) != 0) {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    } else {
                        Intent intent = new Intent(AlbumActivity.this, PhotoActivity.class);
                        intent.putExtra("album_id", user.getPrivateAlbum().getId());
                        AlbumActivity.this.startActivity(intent);
                        dialog.dismiss();
                    }
                }
            }
            @Override
            public void onCleared() {

            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                password = "";
            }
        });

        dialog.show();
    }
}