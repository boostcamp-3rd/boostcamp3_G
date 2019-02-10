package com.boostcamp.dreampicker.data.repository;

import com.boostcamp.dreampicker.data.model.Feed;
import com.boostcamp.dreampicker.data.model.FeedUploadRequest;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface FeedRepository {
    @NonNull
    Single<List<Feed>> getNotEndedMyFollowerFeedList(@Nullable List<String> followerList,
                                                     @NonNull final Date startAfter,
                                                     final int pageSize);

    @NonNull
    Single<Feed> vote(@NonNull final String feedId, @NonNull final String selectionId);

    // [업로드] 사용자가 작성한 피드를 서버에 업로드한다.
    @NonNull
    Completable uploadFeed(@NonNull final FeedUploadRequest feed);
}