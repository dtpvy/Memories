package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OnBoardingActivity extends AppCompatActivity {
    String[] stepDescriptions = {
            "Welcome to Memories! Start editing like a pro with our intuitive tools and unleash your creativity today.",
            "Get ready to transform your photos with Memories! Dive in and discover endless possibilities for editing and enhancing your images.",
            "Welcome aboard! With Memories, your photos are about to get a stunning makeover. Let's start editing!"
    };
    int[] stepImages = { R.drawable.onboarding2, R.drawable.onboarding1, R.drawable.onboarding3};
    int step = 0, fileNumber;
    ImageView imgDescription;
    TextView description;
    Button prevBtn, nextBtn;
    ArrayList<View> stepView = new ArrayList<>();
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<Uri> imagesEncodedList;
    FirebaseStorage storage;
    User user;
    FirebaseFirestore db;
    History history;
    String deviceId;
    Album defaultAlbum;
    ArrayList<Photo> photos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        user = new User().getUser(this);
        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        description = findViewById(R.id.description);
        imgDescription = findViewById(R.id.imgDescription);
        prevBtn = findViewById(R.id.prevBtn);
        nextBtn = findViewById(R.id.nextBtn);
        stepView.add(findViewById(R.id.step1));
        stepView.add(findViewById(R.id.step2));
        stepView.add(findViewById(R.id.step3));
        setActive(0);

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActive(step-1);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (step + 1 == 3) {
                    syncImages();
                } else {
                    setActive(step+1);
                }
            }
        });
    }

    public void setActive(int step) {
        this.step = step;
        prevBtn.setVisibility(step > 0 ? View.VISIBLE : View.INVISIBLE);
        imgDescription.setImageResource(stepImages[step]);
        description.setText(stepDescriptions[step]);
        for (int i = 0; i < stepView.size(); ++i) {
            if (i == step) {
                stepView.get(i).setBackgroundResource(R.color.primary_button);
            } else {
                stepView.get(i).setBackgroundResource(R.color.grey);
            }
        }
    }

    public void syncImages() {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Do you want to sync images in your device to our system?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chooseImages();
                        dialog.dismiss();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    private void chooseImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data) {
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
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

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public String getName(String path) {
        File file = new File(path);
        return file.getName();
    }

    public void setupAlbum() {
        CollectionReference dbAlbum = db.collection("albums");
        CollectionReference dbPhoto = db.collection("photos");
        CollectionReference dbUser = db.collection("users");

        Album privateAlbum = new Album(user.getId(), getString(R.string.private_img), "Bảo mật", false);
        Album favouriteAlbum = new Album(user.getId(), getString(R.string.favourite_img), "Yêu thích", false );
        dbAlbum.document(defaultAlbum.getId()).set(defaultAlbum);
        dbAlbum.document(privateAlbum.getId()).set(privateAlbum);
        dbAlbum.document(favouriteAlbum.getId()).set(favouriteAlbum);
        dbAlbum.document(defaultAlbum.getId()).update("photos", photos);

        user.setDefaultAlbum(defaultAlbum);
        user.setFavouriteAlbum(favouriteAlbum);
        user.setPrivateAlbum(privateAlbum);
        user.setOnBoarding(true);
        dbUser.document(user.getId()).set(user);
    }

    public void syncSuccess() {
        if (user == null) return;
        setupAlbum();

        Toast.makeText(this, "Upload photos successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(OnBoardingActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        OnBoardingActivity.this.startActivity(intent);
        OnBoardingActivity.this.finish();
    }

    public void addHistoryData() {
        if (user == null) return;
        defaultAlbum = new Album(user.getId());

        CollectionReference dbHistory = db.collection("histories");
        History newHistory = new History(user.getId(), new Date(), deviceId);
        dbHistory.document(newHistory.getId()).set(newHistory);
        history = newHistory;
    }

    public void uploadFile(Uri uri) throws FileNotFoundException {
        StorageReference storageRef = storage.getReference();
        CollectionReference dbPhoto = db.collection("photos");

        UUID uuid = UUID.randomUUID();
        String childPath = user.getId() + "/" + uuid.toString() + "-" + getName(uri.getPath());
        StorageReference mountainsRef = storageRef.child(childPath);

        InputStream stream = getContentResolver().openInputStream(uri);
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
                            syncSuccess();
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
}