package com.boostcamp.dreampicker.data.repository;

import android.net.Uri;

import com.boostcamp.dreampicker.data.model.FeedUploadRequest;
import com.boostcamp.dreampicker.data.source.firebase.model.FeedRemoteData;
import com.boostcamp.dreampicker.data.source.firebase.model.mapper.FeedMapper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class FeedRepositoryImpl implements FeedRepository {

    private static final String COLLECTION_FEED = "feed";
    private static final String FIELD_FEED_VOTESELECTIONITEM_IMAGEURL = "voteSelectionItem.imageURL";
    private static final String STORAGE_FEED_IMAGE_PATH = "feedImages";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static volatile FeedRepositoryImpl INSTANCE;

    private FeedRepositoryImpl() {
    }

    public static FeedRepositoryImpl getInstance() {
        if (INSTANCE == null) {
            synchronized (FeedRepositoryImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FeedRepositoryImpl();
                }
            }
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public Completable uploadFeed(@NonNull final FeedUploadRequest uploadFeed) {
        return Completable.create(emitter -> {

            FeedRemoteData feedRemoteData = FeedMapper.setFeed(uploadFeed);
            uploadImageStorage(feedRemoteData, Uri.parse(uploadFeed.getImagePathA()));
            uploadImageStorage(feedRemoteData, Uri.parse(uploadFeed.getImagePathB()));

            db.collection(COLLECTION_FEED)
                    .document(feedRemoteData.getId())
                    .set(feedRemoteData)
                    .addOnSuccessListener(documentReference -> emitter.onComplete())
                    .addOnFailureListener(Throwable::printStackTrace);

        }).subscribeOn(Schedulers.io());
    }

    private void uploadImageStorage(@NonNull final FeedRemoteData feed, final Uri uri) {

        StorageReference feedImages = FirebaseStorage.getInstance().getReference()
                .child(STORAGE_FEED_IMAGE_PATH + "/" + feed.getId() + "/" + uri.getLastPathSegment());

        feedImages.putFile(uri).continueWithTask(task -> {
            if (task.isSuccessful()) {
                return feedImages.getDownloadUrl();
            } else {
                throw task.getException();
            }
        }).addOnCompleteListener(result -> {
            if (result.isSuccessful()) {
                if (result.getResult() != null) {
                    db.collection(COLLECTION_FEED)
                            .document(feed.getId())
                            .update(FIELD_FEED_VOTESELECTIONITEM_IMAGEURL, result.getResult().toString());
                }
            }
        })
                .addOnFailureListener(Throwable::printStackTrace);

    }


}
