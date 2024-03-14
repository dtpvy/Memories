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
import java.util.UUID;

public class OnBoardingActivity extends AppCompatActivity {
    String[] stepDescriptions = {
            "Welcome to Memories! Start editing like a pro with our intuitive tools and unleash your creativity today.",
            "Get ready to transform your photos with Memories! Dive in and discover endless possibilities for editing and enhancing your images.",
            "Welcome aboard! With Memories, your photos are about to get a stunning makeover. Let's start editing!"
    };
    int[] stepImages = { R.drawable.onboarding2, R.drawable.onboarding1, R.drawable.onboarding3};
    int step = 0;
    ImageView imgDescription;
    TextView description;
    Button prevBtn, nextBtn;
    ArrayList<View> stepView = new ArrayList<>();
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<String> imagesEncodedList;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    History history;
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
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
            addHistoryData();
            System.out.println(imagesEncodedList.size());
            for (int i = 0; i < imagesEncodedList.size(); i++) {
                uploadFile(imagesEncodedList.get(i));
            }
            syncSuccess();
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

    public void syncPhotoData(String url) {
        System.out.println(url);
        CollectionReference dbPhoto = db.collection("photos");
        Photo newPhoto = new Photo(user.getUid(), url, history.getDate(), history.getId());
        dbPhoto.add(newPhoto).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                newPhoto.setId(documentReference.getId());
                documentReference.set(newPhoto);
            }
        });
    }

    public void syncSuccess() {
        if (user == null) return;
        CollectionReference dbUser = db.collection("users");
        dbUser.document(user.getUid()).update("onBoarding", true);

        Toast.makeText(this, "Upload photos successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(OnBoardingActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        OnBoardingActivity.this.startActivity(intent);
        OnBoardingActivity.this.finish();
    }

    public void addHistoryData() {
        if (user == null) return;
        CollectionReference dbHistory = db.collection("histories");
        History newHistory = new History(user.getUid(), new Date(), deviceId);
        dbHistory.add(newHistory).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                newHistory.setId(documentReference.getId());
                documentReference.set(newHistory);
                history = newHistory;
            }
        });
    }

    public void uploadFile(String path) throws FileNotFoundException {
        StorageReference storageRef = storage.getReference();

        UUID uuid = UUID.randomUUID();
        String childPath = user.getUid() + "/" + uuid.toString() + "-" + getName(path);
        StorageReference mountainsRef = storageRef.child(childPath);

        InputStream stream = new FileInputStream(new File(path));
        UploadTask uploadTask = mountainsRef.putStream(stream);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        syncPhotoData(uri.toString());
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