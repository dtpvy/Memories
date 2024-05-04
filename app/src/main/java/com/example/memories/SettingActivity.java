package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Initializable;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
    GoogleSignInClient googleSignInClient;
    LinearLayout signOutBtn, trashBtn, syncBtn, privateAlbumBtn, analyticsBtn;
    ImageView avatarView;
    TextView fullNameText, emailText;
    ImageButton backButton;
    String imageEncoded, deviceId, password = "", confirmPassword = "";
    int fileNumber = 0;
    History history;
    User user;
    ArrayList<Media> media = new ArrayList<>();
    CollectionReference dbUser, dbHistories, dbMedia, dbAlbum;
    int PICK_IMAGE_MULTIPLE = 1;
    ArrayList<Uri> imagesEncodedList;
    ArrayList<CustomObject> customObject = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        user = new User().getUser(this);

        Database db = new Database();
        dbUser = db.getDbUser();
        dbHistories = db.getDbHistories();
        dbMedia = db.getDbMedia();
        dbAlbum = db.getDbAlbum();

        signOutBtn = findViewById(R.id.signoutButton);
        trashBtn = findViewById(R.id.trashBtn);
        fullNameText = findViewById(R.id.fullnameText);
        emailText = findViewById(R.id.emailText);
        avatarView = findViewById(R.id.avatarView);
        backButton = findViewById(R.id.backButton);
        syncBtn = findViewById(R.id.syncBtn);
        privateAlbumBtn = findViewById(R.id.privateAlbum);
        analyticsBtn = findViewById(R.id.analyticsBtn);

        if (firebaseUser != null) {
            Glide.with(this).load(firebaseUser.getPhotoUrl()).into(avatarView);
            fullNameText.setText(firebaseUser.getDisplayName());
            emailText.setText(firebaseUser.getEmail());
        }
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        signOutBtn.setOnClickListener(view -> {
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

        analyticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, AnalyticsActivity.class);
                SettingActivity.this.startActivity(intent);
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

        privateAlbumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPassword();
            }
        });
    }

    private void chooseImages() {
        Intent intent = new Intent();
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "video/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
    }

    public void addHistoryData() {
        History newHistory = new History(user.getId(), new Date(), deviceId);
        dbHistories.document(newHistory.getId()).set(newHistory);
        history = newHistory;
    }

    public void createObject() {
        CustomObjectDetect customObjectDetect = new CustomObjectDetect();
        customObjectDetect.setCallback(new CustomObjectDetect.Callback() {
            @Override
            public void onCallback() {
                dbAlbum.document(user.getDefaultAlbum().getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Album album = documentSnapshot.toObject(Album.class);
                        ArrayList<Media> newMedia = album.getPhotos();
                        for (Media media : SettingActivity.this.media) {
                            newMedia.add(media);
                        }
                        dbAlbum.document(user.getDefaultAlbum().getId()).update("photos", newMedia);
                        Toast.makeText(SettingActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        customObjectDetect.detect(SettingActivity.this, customObject);
    }

    public String getName(String path) {
        File file = new File(path);
        return file.getName();
    }

    public String getMimeType(Uri uri) {
        ContentResolver cR = getContentResolver();
        String mime = cR.getType(uri);
        return mime;
    }

    public void uploadFile(Uri uri) throws FileNotFoundException {
        StorageReference storageRef = storage.getReference();

        Media newMedia = new Media(user.getId(), history.getDate(), history.getId(), getMimeType(uri));
        String childPath = user.getId() + "/" + newMedia.getId() + "-" + getName(uri.getPath());
        StorageReference mountainsRef = storageRef.child(childPath);

        InputStream stream = getContentResolver().openInputStream(uri);
        UploadTask uploadTask = mountainsRef.putStream(stream);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri _uri) {
                        newMedia.setImgUrl(_uri.toString());
                        dbMedia.document(newMedia.getId()).set(newMedia);
                        media.add(newMedia);
                        if (!newMedia.isVideo()) {
                            customObject.add(new CustomObject(uri, newMedia));
                        }
                        if (media.size() == fileNumber) {
                            if (customObject.isEmpty()) {
                                dbAlbum.document(user.getDefaultAlbum().getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Album album = documentSnapshot.toObject(Album.class);
                                        ArrayList<Media> newMedia = album.getPhotos();
                                        for (Media media : SettingActivity.this.media) {
                                            newMedia.add(media);
                                        }
                                        dbAlbum.document(user.getDefaultAlbum().getId()).update("photos", newMedia);
                                        Toast.makeText(SettingActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                createObject();
                            }
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
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data) {
                imagesEncodedList = new ArrayList<Uri>();
                if (data.getData() != null){
                    Uri uri = data.getData();
                    imagesEncodedList.add(uri);
                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            imagesEncodedList.add(uri);
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

    public void showPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.private_album, null);
        AlertDialog dialog = builder.setView(view).create();
        Boolean hasPassword = user.getPassword() != null;
        dialog.setTitle(hasPassword ? "Nhập mật khẩu" : "Nhập mật khẩu mới");

        PatternLockView mPatternLockView = view.findViewById(R.id.pattern_lock_view);
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

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
                        Intent intent = new Intent(SettingActivity.this, PhotoActivity.class);
                        intent.putExtra("album_id", user.getPrivateAlbum().getId());
                        SettingActivity.this.startActivity(intent);
                        dialog.dismiss();
                    }
                } else {
                    if (user.getPassword().compareTo(value) != 0) {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    } else {
                        Intent intent = new Intent(SettingActivity.this, PhotoActivity.class);
                        intent.putExtra("album_id", user.getPrivateAlbum().getId());
                        SettingActivity.this.startActivity(intent);
                        dialog.dismiss();
                    }
                }
            }
            @Override
            public void onCleared() {}
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