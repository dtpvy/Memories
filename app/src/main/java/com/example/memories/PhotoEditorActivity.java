package com.example.memories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.slider.RangeSlider;
import com.google.common.io.Files;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lyft.android.scissors.CropView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import ja.burhanrashid52.photoeditor.ViewType;

public class PhotoEditorActivity extends FragmentActivity implements PhotoEditorCallbacks {
    PhotoEditorView photoView;
    PhotoEditor photoEditor;
    CollectionReference dbMedia, dbAlbum;
    String mediaId, albumId;
    Media media;
    String Root_Frag = "root_fragment";
    PhotoEditorFragment photoEditorFragment;
    PhotoLightControlFragment photoLightControlFragment;
    PhotoEmojiFragment emojiFragment;
    TextFragment textFragment;
    PhotoFilterFragment filterFragment;
    FragmentTransaction ft;
    Bitmap croppedBitmap, currentBitmap;
    ColorFilter colorFilter;
    FirebaseStorage storage;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);

        user = new User().getUser(this);

        Intent intent = getIntent();
        mediaId = intent.getStringExtra("media_id");
        albumId = intent.getStringExtra("album_id");

        Database db = new Database();
        dbMedia = db.getDbMedia();
        dbAlbum = db.getDbAlbum();

        storage = FirebaseStorage.getInstance();

        photoView = findViewById(R.id.photo);
        photoEditor = new PhotoEditor.Builder(this, photoView)
                .setPinchTextScalable(true)
                .setDefaultEmojiTypeface(Typeface.createFromAsset(getAssets(), "emojione-android.ttf"))
                .build();

        ft = getSupportFragmentManager().beginTransaction();
        photoEditorFragment = PhotoEditorFragment.newInstance("first-photo-editor");
        emojiFragment = PhotoEmojiFragment.newInstance("first-photo-emoji");
        photoLightControlFragment = PhotoLightControlFragment.newInstance("first-photo-light");
        textFragment = TextFragment.newInstance("first-text");
        textFragment.setPhotoEditor(photoEditor);
        filterFragment = PhotoFilterFragment.newInstance("first-filter");

        loadFrag(photoEditorFragment, 0);

        photoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener() {
            @Override
            public void onEditTextChangeListener(View view, String s, int c) {
                textFragment.show(view, s, c);
            }

            @Override
            public void onAddViewListener(ViewType viewType, int i) {}

            @Override
            public void onRemoveViewListener(int i) {}

            @Override
            public void onRemoveViewListener(ViewType viewType, int i) {
                loadFrag(photoEditorFragment, 1);
            }

            @Override
            public void onStartViewChangeListener(ViewType viewType) {}

            @Override
            public void onStopViewChangeListener(ViewType viewType) {}
        });

        loadData();
    }

    public void loadData() {
        photoEditor.clearAllViews();
        dbMedia.document(mediaId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                media = documentSnapshot.toObject(Media.class);
                Glide.with(photoView).load(media.getImgUrl()).placeholder(R.drawable.stockphoto).into(photoView.getSource());
            }
        });

        if (albumId.contains("default")) albumId = user.getDefaultAlbum().getId();
    }

    public void loadFrag(Fragment fragment_name, int flag)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (flag == 0) {
            ft.add(R.id.controlView, fragment_name);
            fm.popBackStack(Root_Frag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ft.addToBackStack(Root_Frag);
        }
        else {
            ft.replace(R.id.controlView, fragment_name);
            ft.addToBackStack(null);
        }

        ft.commit();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onMsgFromFragToMain(String sender, String action) {
        if (sender.equals(photoEditorFragment.name)) {
            switch (action) {
                case "cancel": {
                    finish();
                    break;
                }
                case "crop": {
                    createCropDialog();
                    break;
                }
                case "light": {
                    colorFilter = photoView.getSource().getColorFilter();
                    photoLightControlFragment = PhotoLightControlFragment.newInstance("first-photo-light");
                    loadFrag(photoLightControlFragment, 1);
                    break;
                }
                case "emoji": {
                    loadFrag(emojiFragment, 1);
                    break;
                }
                case "text": {
                    textFragment.setAtt("", Color.BLACK);
                    photoEditor.addText("", Color.BLACK);
                    loadFrag(textFragment, 1);
                    break;
                }
                case "effect": {
                    loadFrag(filterFragment, 1);
                    break;
                }
                case "save": {
                    uploadFile();
                    break;
                }
                default: {
                    break;
                }
            }
        }
        else if (sender.equals(photoLightControlFragment.name)) {
            switch (action) {
                case "cancel": {
                    photoView.getSource().setColorFilter(colorFilter);
                    loadFrag(photoEditorFragment, 1);
                    break;
                }
                case "save": {
                    int multiplerBB = photoLightControlFragment.multiplerBB;
                    int additionBB = photoLightControlFragment.additionBB;

                    photoView.getSource().clearColorFilter();
                    BitmapDrawable drawable = (BitmapDrawable) photoView.getSource().getDrawable();
                    Bitmap original = drawable.getBitmap();
                    Bitmap bitmap = Bitmap.createBitmap(photoView.getSource().getWidth(), photoView.getSource().getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);

                    Rect dest = new Rect(0, 0, photoView.getSource().getWidth(), photoView.getSource().getHeight());
                    Paint paint = new Paint();
                    paint.setColorFilter(new LightingColorFilter(multiplerBB, additionBB));
                    canvas.drawBitmap(original, null, dest, paint);

                    photoView.getSource().setImageBitmap(bitmap);
                    loadFrag(photoEditorFragment, 1);
                    break;
                }
                case "change": {
                    photoView.getSource().setColorFilter(new LightingColorFilter(photoLightControlFragment.multiplerBB, photoLightControlFragment.additionBB));
                    break;
                }
                default: {
                    break;
                }
            }
        }
        else {
            switch (action) {
                case "cancel": {
                    photoEditor.clearAllViews();
                    loadFrag(photoEditorFragment, 1);
                    break;
                }
                case "save": {
                    photoEditor.saveAsBitmap(new OnSaveBitmap() {
                        @Override
                        public void onBitmapReady(Bitmap bitmap) {
                            photoView.getSource().setImageBitmap(bitmap);
                            loadFrag(photoEditorFragment, 1);
                            photoEditor.clearAllViews();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            loadFrag(photoEditorFragment, 1);
                        }
                    });
                    break;
                }
                default: {
                    if (sender.equals(emojiFragment.name)) {
                        photoEditor.addEmoji(action);
                    }
                    if (sender.equals(filterFragment.name)) {
                        photoEditor.setFilterEffect(PhotoFilter.valueOf(action));
                    }
                    break;
                }
            }
        }
    }

    public void createCropDialog() {
        Dialog dialog=new Dialog(this,android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_photo_crop);

        CropView cropView = dialog.findViewById(R.id.crop_view);
        TextView cancel = dialog.findViewById(R.id.cancelBtn);
        TextView save = dialog.findViewById(R.id.saveBtn);
        RangeSlider rotateSlider = dialog.findViewById(R.id.rotateRange);
        LinearLayout leftRotate, rightRotate, ratioAuto, ratio11, ratio23, ratio32, ratio34, ratio43, ratio169, ratio916;
        leftRotate = dialog.findViewById(R.id.leftRotate);
        rightRotate = dialog.findViewById(R.id.rightRotate);
        ratioAuto = dialog.findViewById(R.id.ratioAuto);
        ratio11 = dialog.findViewById(R.id.ratio11);
        ratio23 = dialog.findViewById(R.id.ratio23);
        ratio32 = dialog.findViewById(R.id.ratio32);
        ratio34 = dialog.findViewById(R.id.ratio34);
        ratio43 = dialog.findViewById(R.id.ratio43);
        ratio169 = dialog.findViewById(R.id.ratio169);
        ratio916 = dialog.findViewById(R.id.ratio916);

        setRatio(ratioAuto, cropView, 0);
        setRatio(ratio11, cropView, 1f);
        setRatio(ratio23, cropView, 2f/3f);
        setRatio(ratio32, cropView, 3f/2f);
        setRatio(ratio34, cropView, 3f/4f);
        setRatio(ratio43, cropView, 4f/3f);
        setRatio(ratio916, cropView, 9f/16f);
        setRatio(ratio169, cropView, 16f/9f);

        setRotate(leftRotate, cropView, -90);
        setRotate(rightRotate, cropView, 90);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentBitmap = cropView.crop();
                photoView.getSource().setImageBitmap(currentBitmap);
                dialog.dismiss();
            }
        });

        rotateSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                Matrix matrix = new Matrix();
                matrix.postRotate(value);
                croppedBitmap = Bitmap.createBitmap(currentBitmap, 0, 0, currentBitmap.getWidth(), currentBitmap.getHeight(), matrix, true);
                cropView.setImageBitmap(croppedBitmap);
            }
        });

        BitmapDrawable drawable = (BitmapDrawable) photoView.getSource().getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        currentBitmap = bitmap;
        cropView.setImageBitmap(currentBitmap);

        dialog.show();
    }

    public void setRatio(LinearLayout btn, CropView cropView, float ratio) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropView.setViewportRatio(ratio);
            }
        });
    }

    public void setRotate(LinearLayout btn, CropView cropView, int deg) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Matrix matrix = new Matrix();
                matrix.postRotate(deg);
                if (croppedBitmap == null) croppedBitmap = currentBitmap;
                croppedBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight(), matrix, true);
                cropView.setImageBitmap(croppedBitmap);
            }
        });
    }

    public void uploadFile() {
        StorageReference storageRef = storage.getReference();
        BitmapDrawable drawable = (BitmapDrawable) photoView.getSource().getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        byte[] data = bytes.toByteArray();

        Media newMedia = new Media(user.getId(), new Date());
        String childPath = newMedia.getUserId() + "/" + newMedia.getId() + ".png";
        StorageReference mountainsRef = storageRef.child(childPath);
        System.out.println(childPath);
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        newMedia.setImgUrl(uri.toString());
                        newMedia.setType("image/png");
                        dbMedia.document(newMedia.getId()).set(newMedia);
                        dbAlbum.document(albumId).update("photos", FieldValue.arrayUnion(newMedia));
                        Intent intent = new Intent(PhotoEditorActivity.this, PhotoDetailActivity.class);
                        intent.putExtra("media_id", newMedia.getId());
                        intent.putExtra("album_id", albumId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                        PhotoEditorActivity.this.startActivity(intent);
                        PhotoEditorActivity.this.finish();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PhotoEditorActivity.this, "Có lỗi xảy ra vui lòng thử lại", Toast.LENGTH_SHORT).show();
                System.out.println(e.getMessage());
            }
        });
    }
}