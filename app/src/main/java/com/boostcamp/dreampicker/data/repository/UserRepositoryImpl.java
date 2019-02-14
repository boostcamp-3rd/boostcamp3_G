package com.boostcamp.dreampicker.data.repository;

import com.boostcamp.dreampicker.data.model.MyFeed;
import com.boostcamp.dreampicker.data.model.UserDetail;
import com.boostcamp.dreampicker.data.paging.MyFeedDataSourceFactory;
import com.boostcamp.dreampicker.data.source.firestore.UserDataSource;
import com.boostcamp.dreampicker.data.source.firestore.model.UserDetailRemoteData;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.paging.PagedList;
import androidx.paging.RxPagedListBuilder;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class UserRepositoryImpl implements UserRepository {
    private static volatile UserRepository INSTANCE = null;

    public static UserRepository getInstance(@NonNull final UserDataSource remoteDataSource) {
        if (INSTANCE == null) {
            synchronized (UserRepositoryImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserRepositoryImpl(remoteDataSource);
                }
            }
        }
        return INSTANCE;
    }

    @NonNull
    private final UserDataSource remoteDataSource;

    private UserRepositoryImpl(@NonNull final UserDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    @NonNull
    @Override
    public Single<UserDetail> getUserDetail(@NonNull final String userId) {
        return remoteDataSource.getUserDetail(userId);
    }

    @NonNull
    @Override
    public Observable<PagedList<MyFeed>> getFeedListByUserId(@NonNull final String userId,
                                                         final int pageSize) {
        MyFeedDataSourceFactory factory =
                new MyFeedDataSourceFactory(remoteDataSource, userId);

        final PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(pageSize)
                .setPageSize(pageSize)
                .setPrefetchDistance(2)
                .setEnablePlaceholders(false)
                .build();

        return new RxPagedListBuilder<>(factory, config).buildObservable();
    }

    @NonNull
    @Override
    public Completable toggleVoteEnded(@NonNull final String userId,
                                       @NonNull final String feedId,
                                       final boolean isEnded) {
        return remoteDataSource.toggleVoteEnded(userId, feedId, isEnded);
    }

    @NonNull
    @Override
    public Completable insertNewUser(@NonNull final String userId,
                                     @NonNull UserDetailRemoteData userDetail) {
        return remoteDataSource.insertNewUser(userId, userDetail);
    }
}
