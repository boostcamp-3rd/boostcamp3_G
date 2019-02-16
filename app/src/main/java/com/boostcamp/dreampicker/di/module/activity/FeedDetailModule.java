package com.boostcamp.dreampicker.di.module.activity;

import com.boostcamp.dreampicker.di.scope.FeedDetail;
import com.boostcamp.dreampicker.di.scope.FeedId;
import com.boostcamp.dreampicker.presentation.feed.detail.FeedDetailActivity;
import com.boostcamp.dreampicker.presentation.feed.detail.FeedDetailViewModelFactory;

import androidx.lifecycle.ViewModelProvider;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
abstract class FeedDetailModule {
    @FeedDetail
    @Binds
    abstract ViewModelProvider.Factory feedViewModelFactory(FeedDetailViewModelFactory factory);

    @FeedDetail
    @Provides
    @FeedId
    static String provideFeedId(FeedDetailActivity activity) {
        return activity.getIntent().getStringExtra(FeedDetailActivity.EXTRA_FEED_ID);
    }
}
