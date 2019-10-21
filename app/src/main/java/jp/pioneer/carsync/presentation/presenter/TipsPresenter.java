package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.presentation.event.DeviceConnectionSuppressEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.event.UpdateTipsItemEvent;
import jp.pioneer.carsync.presentation.model.TipsItem;
import jp.pioneer.carsync.presentation.util.TipsList;
import jp.pioneer.carsync.presentation.view.TipsView;
import jp.pioneer.carsync.presentation.view.argument.PermissionParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * TipsPresenter
 */
@PresenterLifeCycle
public class TipsPresenter extends Presenter<TipsView> {
    @Inject EventBus mEventBus;
    @Inject TipsList mTipsList;
    @Inject Context mContext;
    @Inject GetStatusHolder mStatusCase;
    private PermissionParams mParams;
    private ArrayList<TipsItem> mItems;
    private int mTab = 2;
    private boolean mSuppressNavigate = false;
    @Inject
    public TipsPresenter() {
    }

    @Override
    void onInitialize() {
        mTipsList.initialize();
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        if(mTipsList.isUpdate) {
            mTipsList.update();
            mTipsList.isUpdate = false;
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
        Optional.ofNullable(getView()).ifPresent(view -> {
            mTab = view.getSelectedTab();
        });
    }

    @Override
    void onTakeView() {
        updateView();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setSelectedTab(mTab);
        });
    }

    @Override
    void onDestroy() {
        mTipsList.cancel();
        mTipsList.isUpdate = true;
    }

    @Override
    void onSaveInstanceState(@NonNull Bundle outState) {
        mTipsList.cancel();
        mTipsList.isUpdate = true;
    }

    public void setArgument(Bundle args) {
        mParams = PermissionParams.from(args);
    }

    public void showTips(int position) {
        if(!mSuppressNavigate) {
            if (position < mItems.size()) {
                String url = mItems.get(position).link;
                Bundle args = new Bundle();
                args.putString("url", url);
                mEventBus.post(new NavigateEvent(ScreenId.TIPS_WEB, args));
            }
        }
    }

    public void onSettingAction() {
        if(!mSuppressNavigate){
            mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_CONTAINER, Bundle.EMPTY));
        }
    }

    public void onBtAction() {
        if(!mSuppressNavigate) {
            mEventBus.post(new NavigateEvent(ScreenId.EASY_PAIRING, Bundle.EMPTY));
        }
    }

    /**
     * TIPS記事更新イベント.
     *
     * @param event UpdateTipsItemEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateTipsItemEvent(UpdateTipsItemEvent event) {
        updateView();
    }

    /**
     * 連携抑制状態更新イベント.
     *
     * @param event UpdateTipsItemEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnectionSuppressEvent(DeviceConnectionSuppressEvent event) {
        updateView();
    }

    private void updateView() {
        AppStatus status = mStatusCase.execute().getAppStatus();
        mSuppressNavigate = status.deviceConnectionSuppress;
        mItems = mTipsList.items;
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (mTipsList.isError) {
                //TODO:スプラッシュスクリーン画像を表示する
                view.showError("isError");
                return;
            }
            view.setAdapter(mItems);
            view.setDisabled(mSuppressNavigate);
        });

    }

}
