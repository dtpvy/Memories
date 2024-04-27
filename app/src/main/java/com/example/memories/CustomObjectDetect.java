package com.example.memories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CustomObjectDetect {
    ObjectDetector objectDetector;
    CollectionReference dbObject, dbMedia;
    User user;
    Callback callback;
    ArrayList<Object> objects;

    interface Callback {
        void onCallback();
    }

    public CustomObjectDetect() {
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();

        objectDetector = ObjectDetection.getClient(options);
        dbObject = new Database().getDbObject();
        dbMedia = new Database().getDbMedia();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    Bitmap cropImage(Context context, Uri uri, Rect rect) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), null, false);
    }

    void createImage(Bitmap bitmap, Object object, int idx) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        UUID uuid = UUID.randomUUID();
        String childPath = user.getId() + "/" + uuid.toString() + ".png";
        StorageReference mountainsRef = storageRef.child(childPath);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        byte[] data = bytes.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        object.setImgUrl(uri.toString());
                        objects.get(idx).setImgUrl(uri.toString());
                        dbObject.document(object.getId()).update("imgUrl", uri.toString());
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

    void uploadObject() {
        for (Object object: objects) {
            dbObject.document(object.getId()).set(object);
        }
        callback.onCallback();
    }

    Object findObject(String label) {
        for (Object object: objects) {
            if (object.getLabel().compareTo(label) == 0) {
                return object;
            }
        }
        return null;
    }

    public void solve(Context context, ArrayList<CustomObject> customObjects) {
        for (int index = 0; index < customObjects.size(); index++) {
            CustomObject customObject = customObjects.get(index);
            int i = index;
            try {
                InputImage image = InputImage.fromFilePath(context, customObject.getUri());
                objectDetector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<DetectedObject>>() {
                                    @Override
                                    public void onSuccess(List<DetectedObject> detectedObjects) {
                                        ArrayList<String> photoLabels = new ArrayList<>();
                                        for (DetectedObject detectedObject : detectedObjects) {
                                            Rect boundingBox = detectedObject.getBoundingBox();
                                            List<DetectedObject.Label> labels = detectedObject.getLabels();
                                            String name = labels.size() > 0 ? labels.get(0).getText() : "Unknown";
                                            Object object = findObject(name);
                                            if (name.compareTo("Unknown") != 0) {
                                                photoLabels.add(name);
                                            }
                                            if (object == null) {
                                                Object newObject = new Object(user.getId(), name);
                                                newObject.getPhotos().add(customObject.getMedia());
                                                objects.add(newObject);
                                                try {
                                                    Bitmap bitmap = cropImage(context, customObject.getUri(), boundingBox);
                                                    createImage(bitmap, newObject, objects.size()-1);
                                                } catch (IOException e) {}
                                            } else {
                                                object.getPhotos().add(customObject.getMedia());
                                            }
                                        }
                                        dbMedia.document(customObject.getMedia().getId()).update("labels", photoLabels);
                                        if (i+1 == customObjects.size()) {
                                            uploadObject();
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void detect(Context context, ArrayList<CustomObject> customObjects) {
        user = new User().getUser(context);
        objects = new ArrayList<>();
        dbObject.whereEqualTo("userId", user.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Object> objects = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        Object object = queryDocumentSnapshot.toObject(Object.class);
                        System.out.println(object.getId());
                        objects.add(object);
                    }
                    solve(context, customObjects);
                }
            }
        });

    }
}
