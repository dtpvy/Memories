package com.example.memories;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Database {
    FirebaseFirestore db;
    CollectionReference dbAlbum;
    CollectionReference dbMedia;
    CollectionReference dbUser;
    CollectionReference dbHistories;

    public Database() {
        db = FirebaseFirestore.getInstance();
        dbAlbum = db.collection("albums");
        dbMedia = db.collection("media");
        dbUser = db.collection("users");
        dbHistories = db.collection("histories");
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public CollectionReference getDbAlbum() {
        return dbAlbum;
    }

    public CollectionReference getDbMedia() {
        return dbMedia;
    }

    public CollectionReference getDbUser() {
        return dbUser;
    }

    public CollectionReference getDbHistories() {
        return dbHistories;
    }
}
