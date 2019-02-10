package com.boostcamp.dreampicker.data.repository;

import com.boostcamp.dreampicker.data.model.Feed;
import com.boostcamp.dreampicker.data.model.FeedUploadRequest;
import com.boostcamp.dreampicker.data.model.MyFeed;
import com.boostcamp.dreampicker.data.source.firestore.mapper.FeedResponseMapper;
import com.boostcamp.dreampicker.data.source.firestore.model.FeedRemoteData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Single;

public class FeedRepositoryImpl implements FeedRepository {
    private static final String COLLECTION_FEED = "feed";

    private static final String FIELD_DATE = "date";
    private static final String FIELD_ENDED = "ended";

    private static final String COLLECTION_USER = "user";
    private static final String SUBCOLLECTION_MYFEEDS = "myFeeds";

    @NonNull
    private final FirebaseFirestore firestore;

    private static volatile FeedRepositoryImpl INSTANCE;

    private FeedRepositoryImpl(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public static FeedRepositoryImpl getInstance(@NonNull FirebaseFirestore db) {
        if (INSTANCE == null) {
            synchronized (FeedRepositoryImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FeedRepositoryImpl(db);
                }
            }
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public Single<List<Feed>> getNotEndedFeedList(@NonNull final Date startAfter, final int pageSize) {
        return Single.create(emitter -> firestore.collection(COLLECTION_FEED)
                .whereEqualTo(FIELD_ENDED, false)
                .orderBy(FIELD_DATE, Query.Direction.DESCENDING) // 시간 정렬
                .startAfter(startAfter)
                .limit(pageSize)
                .get()
                .addOnCompleteListener(task -> {
                    final List<Feed> feedList = new ArrayList<>();
                    if (task.isSuccessful()) {
                        if(task.getResult() != null) {
                            for (final QueryDocumentSnapshot snapshots : task.getResult()) {
                                final FeedRemoteData data = snapshots.toObject(FeedRemoteData.class);
                                feedList.add(FeedResponseMapper.toFeed(snapshots.getId(), data));
                            }
                        }
                        emitter.onSuccess(feedList);
                    } else {
                        emitter.onError(task.getException());
                    }
                }));
    }

    @NonNull
    @Override
    public Single<Feed> vote(@NonNull final String userId,
                             @NonNull final String feedId,
                             @NonNull final String selectionId) {
        final DocumentReference docRef = firestore.collection(COLLECTION_FEED).document(feedId);

        return Single.create(emitter ->
                firestore.runTransaction(transaction ->  {
                    final DocumentSnapshot snapshot = transaction.get(docRef);
                    final FeedRemoteData data = snapshot.toObject(FeedRemoteData.class);

                    if(data != null) {
                        final Map<String, String> map = data.getVotedUserMap();
                        map.put(userId, selectionId);
                        transaction.set(docRef, data, SetOptions.merge());
                    }
                    return null;
                }).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        emitter.onSuccess(feedId);
                    } else {
                        emitter.onError(task.getException());
                    }
                })).flatMap(__ ->
                Single.create(emitter ->
                        docRef.get().addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                final DocumentSnapshot snapshot = task.getResult();
                                if(snapshot != null) {
                                    final FeedRemoteData data = snapshot.toObject(FeedRemoteData.class);
                                    if(data != null && !data.isEnded()) {
                                        final Feed feed = FeedResponseMapper.toFeed(feedId, data);
                                        emitter.onSuccess(feed);
                                    }
                                }
                            } else {
                                emitter.onError(task.getException());
                            }
                        })));
    }

    @NonNull
    @Override
    public Completable uploadFeed(@NonNull FeedUploadRequest feed) {
        return null;
    }

    @NonNull
    @Override
    public Single<List<MyFeed>> getFeedListByUserId(@NonNull String userId, Date startAfter, int pageSize) {
        return Single.create(emitter ->
                firestore.collection(COLLECTION_USER).document(userId)
                        .collection(SUBCOLLECTION_MYFEEDS)
                        .orderBy(FIELD_DATE, Query.Direction.DESCENDING)
                        .startAfter(startAfter)
                        .limit(pageSize)
                        .get()
                        .addOnSuccessListener(task -> {
                            List<MyFeed> feedList = new ArrayList<>();
                            if (!task.isEmpty()) {
                                for(DocumentSnapshot document : task.getDocuments()){
                                    final MyFeed feed = document.toObject(MyFeed.class);
                                    if(feed != null){
                                        feed.setId(document.getId());
                                        feedList.add(feed);
                                    }
                                }
                            }
                            emitter.onSuccess(feedList);
                        })
                        .addOnFailureListener(emitter::onError));
    }
}
