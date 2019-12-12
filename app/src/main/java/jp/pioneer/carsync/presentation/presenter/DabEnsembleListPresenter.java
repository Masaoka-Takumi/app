package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.DabEnsembleListView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

public class DabEnsembleListPresenter extends Presenter<DabEnsembleListView> {
    private static final String KEY_BAND_TYPE = "band_type";
    private static final int LOADER_ID_SERVICE_LIST = 0;
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlMediaList mMediaCase;
    private MediaSourceType mSourceType;
    private LoaderManager mLoaderManager;

    /**
     * コンストラクタ
     */
    @Inject
    public DabEnsembleListPresenter() {

    }

    @Override
    void onInitialize() {
        mSourceType = mStatusHolder.execute().getCarDeviceStatus().sourceType;
    }

    @Override
    void onResume() {
/*        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }*/
    }

    @Override
    void onPause() {
        //mEventBus.unregister(this);
    }


    public void onSelectList(int listIndex, Cursor cursor) {
        //mMediaCase.selectListItem(listIndex);
        Bundle bundle = new Bundle();
        bundle.putBoolean("stack", true);
        mEventBus.post(new NavigateEvent(ScreenId.DAB_SERVICE_LIST, bundle));
    }
}
