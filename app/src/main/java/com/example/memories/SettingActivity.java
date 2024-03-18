package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

public class SettingActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseStorage storage;
    FirebaseFirestore db;
    GoogleSignInClient googleSignInClient;
    LinearLayout signoutBtn, trashBtn, syncBtn;
    ImageView avatarView;
    TextView fullNameText, emailText;
    ImageButton backButton;
    String imageEncoded;
    List<String> imagesEncodedList;
    int fileNumber = 0;
    History history;
    String deviceId;
    User user;
    ArrayList<Photo> photos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        user = new User().getUser(this);

        signoutBtn = findViewById(R.id.signoutButton);
        trashBtn = findViewById(R.id.trashBtn);
        fullNameText = findViewById(R.id.fullnameText);
        emailText = findViewById(R.id.emailText);
        avatarView = findViewById(R.id.avatarView);
        backButton = findViewById(R.id.backButton);
        syncBtn = findViewById(R.id.syncBtn);

        if (firebaseUser != null) {
            Glide.with(this).load(firebaseUser.getPhotoUrl()).into(avatarView);
            fullNameText.setText(firebaseUser.getDisplayName());
            emailText.setText(firebaseUser.getEmail());
        }
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        signoutBtn.setOnClickListener(view -> {
            googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        firebaseAuth.signOut();
                        Toast.makeText(getApplicationContext(), "Logout successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(view.getContext(), MainActivity.class);
                        view.getContext().startActivity(intent);
                    }
                }
            });
        });

        trashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, PhotoActivity.class);
                intent.putExtra("album_id", "trash");
                SettingActivity.this.startActivity(intent);
                finish();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImages();
            }
        });
    }

    private void chooseImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
    }

    public void addHistoryData() {
        CollectionReference dbHistory = db.collection("histories");
        History newHistory = new History(user.getId(), new Date(), deviceId);
        dbHistory.document(newHistory.getId()).set(newHistory);
        history = newHistory;
    }

    public String getName(String path) {
        File file = new File(path);
        return file.getName();
    }

    public void uploadFile(String path) throws FileNotFoundException {
        StorageReference storageRef = storage.getReference();
        CollectionReference dbPhoto = db.collection("photos");

        UUID uuid = UUID.randomUUID();
        String childPath = user.getId() + "/" + uuid.toString() + "-" + getName(path);
        StorageReference mountainsRef = storageRef.child(childPath);

        InputStream stream = new FileInputStream(new File(path));
        UploadTask uploadTask = mountainsRef.putStream(stream);

        Photo newPhoto = new Photo(user.getId(), history.getDate(), history.getId());
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        newPhoto.setImgUrl(uri.toString());
                        dbPhoto.document(newPhoto.getId()).set(newPhoto);
                        photos.add(newPhoto);

                        if (photos.size() == fileNumber) {
                            db.collection("albums").document(user.getDefaultAlbum().getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Album album = documentSnapshot.toObject(Album.class);
                                    ArrayList<Photo> newPhotos = album.getPhotos();
                                    for (Photo photo: photos) {
                                        newPhotos.add(photo);
                                    }
                                    db.collection("albums").document(user.getDefaultAlbum().getId()).update("photos", newPhotos);
                                    Toast.makeText(SettingActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();
                if (data.getData() != null){
                    Uri mImageUri = data.getData();
                    Cursor cursor = getContentResolver().query(mImageUri, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);
                    imagesEncodedList.add(imageEncoded);
                    cursor.close();
                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();
                        }
                    }
                }
            }
            fileNumber = imagesEncodedList.size();
            addHistoryData();
            for (int i = 0; i < imagesEncodedList.size(); i++) {
                uploadFile(imagesEncodedList.get(i));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}