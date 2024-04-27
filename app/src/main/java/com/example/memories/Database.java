package com.example.memories;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Database {
    FirebaseFirestore db;
    CollectionReference dbAlbum;
    CollectionReference dbMedia;
    CollectionReference dbUser;
    CollectionReference dbHistories;
    CollectionReference dbObject;

    public Database() {
        db = FirebaseFirestore.getInstance();
        dbAlbum = db.collection("albums");
        dbMedia = db.collection("media");
        dbUser = db.collection("users");
        dbHistories = db.collection("histories");
        dbObject = db.collection("objects");
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

    public CollectionReference getDbObject() { return dbObject; }
}
