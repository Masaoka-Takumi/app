package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.DabPtyListView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

public class DabPtyListPresenter extends Presenter<DabPtyListView> {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlMediaList mMediaCase;

    /**
     * コンストラクタ
     */
    @Inject
    public DabPtyListPresenter() {

    }

    public void onSelectList(int listIndex) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("stack",true);
        mEventBus.post(new NavigateEvent(ScreenId.DAB_SERVICE_LIST, bundle));
    }
}
