package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.SparseBooleanArray;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferMusicApp;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.model.CustomKey;
import jp.pioneer.carsync.presentation.model.CustomKeyItem;
import jp.pioneer.carsync.presentation.model.SourceAppModel;
import jp.pioneer.carsync.presentation.model.YouTubeLinkSearchItem;
import jp.pioneer.carsync.presentation.view.YouTubeLinkSearchItemDialogView;
import jp.pioneer.carsync.presentation.view.adapter.YouTubeLinkSearchItemAdapter;
import timber.log.Timber;

@PresenterLifeCycle
public class YouTubeLinkSearchItemDialogPresenter extends Presenter<YouTubeLinkSearchItemDialogView> {
    @Inject
    GetStatusHolder mGetStatusHolder;
    @Inject
    Context mContext;
    @Inject
    EventBus mEventBus;
    @Inject
    AppSharedPreference mPreference;
    private MediaSourceType mCurrentSourceType;
    private ArrayList<YouTubeLinkSearchItem> mSearchItemList = new ArrayList<>();

    @Inject
    public YouTubeLinkSearchItemDialogPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        mCurrentSourceType = mGetStatusHolder.execute().getCarDeviceStatus().sourceType;
        updateView();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    private void updateView() {
        mSearchItemList.clear();
        switch (mCurrentSourceType) {
            case RADIO:
                mSearchItemList.add(YouTubeLinkSearchItem.INFORMATION);
                break;
            case DAB:
                mSearchItemList.add(YouTubeLinkSearchItem.DYNAMIC_LABEL);
                break;
            default:
                mSearchItemList.add(YouTubeLinkSearchItem.ARTIST);
                mSearchItemList.add(YouTubeLinkSearchItem.MUSIC_TITLE);
                break;
        }
        SparseBooleanArray checkedItemPositions = new SparseBooleanArray();
        int position = 0;
        for (YouTubeLinkSearchItem item : mSearchItemList) {
            boolean setting = mPreference.getYouTubeLinkSearchItemSetting(item);
            checkedItemPositions.put(position, setting);
            position++;
        }
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setListItem(mSearchItemList);
            view.setCheckedItemPositions(checkedItemPositions);
        });
    }

    /**
     * 選択後の処理
     */
    public void onItemChecked() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            SparseBooleanArray checkedItemPositions = view.getCheckedItemPositions();
            int position = 0;
            for (YouTubeLinkSearchItem item : mSearchItemList) {
                mPreference.setYouTubeLinkSearchItemSetting(item, checkedItemPositions.get(position));
                position++;
            }
            view.setCheckedItemPositions(checkedItemPositions);
        });
    }

    /**
     * ソース変更イベントハンドラ
     *
     * @param ev ソース変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent ev) {
        Optional.ofNullable(getView()).ifPresent(YouTubeLinkSearchItemDialogView::callbackClose);
    }
}