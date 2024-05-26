package me.zrunner.olympicsdatapersistency;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public final class FirebaseClient {

    private final @Nonnull Firestore db;

    public FirebaseClient(@Nonnull InputStream fileStream, @Nonnull String firebaseURL) throws IOException {
        this.connect(fileStream, firebaseURL);
        this.db = FirestoreClient.getFirestore();
    }

    private void connect(@Nonnull InputStream fileStream, @Nonnull String firebaseURL) throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(fileStream))
                .setDatabaseUrl(firebaseURL)
                .build();

        FirebaseApp.initializeApp(options);
    }

    public FirebaseUser getUserFromFirebaseID(@Nonnull String firebaseID) {
        ApiFuture<DocumentSnapshot> user = db.collection("users")
                .document(firebaseID)
                .get();
        try {
            DocumentSnapshot document = user.get();
            if (document.exists()) {
                return document.toObject(FirebaseUser.class);
            } else {
                System.out.println("No such document!");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public FirebaseUser getUserFromMinecraftUUID(@Nonnull String uuid) {
        try {
            QueryDocumentSnapshot doc = getUserDocumentSnapshotFromMinecraftUUID(uuid);
            if (doc == null) {
                System.out.println("No such document!");
                return null;
            } else {
                return doc.toObject(FirebaseUser.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setUserScore(@Nonnull String uuid, @Nonnull String objectiveName, int scoreValue) throws ExecutionException, InterruptedException {
        QueryDocumentSnapshot doc = getUserDocumentSnapshotFromMinecraftUUID(uuid);
        if (doc == null) {
            System.out.println("No such document!");
            return;
        }
        DocumentReference docRef = doc.getReference();
        docRef.update("scores." + objectiveName, scoreValue);
    }

    public void addUserAdvancement(@Nonnull String uuid, String advancement) throws ExecutionException, InterruptedException {
        QueryDocumentSnapshot doc = getUserDocumentSnapshotFromMinecraftUUID(uuid);
        if (doc == null) {
            System.out.println("No such document!");
            return;
        }
        DocumentReference docRef = doc.getReference();
        docRef.update("achievements", FieldValue.arrayUnion(advancement));
    }


    private QueryDocumentSnapshot getUserDocumentSnapshotFromMinecraftUUID(@Nonnull String uuid) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> user = db.collection("users")
                .whereEqualTo("mcUUID", uuid)
                .limit(1)
                .get();
        QuerySnapshot query = user.get();
        if (query.isEmpty()) {
            return null;
        }
        return query.getDocuments().get(0);
    }

}
