package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.ListTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.DabPtyListView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

public class DabPtyListPresenter extends Presenter<DabPtyListView> {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlMediaList mMediaCase;

    private static final ListType[] listTypes = new ListType[]{ListType.PTY_NEWS_INFO_LIST,ListType.PTY_POPULER_LIST,ListType.PTY_CLASSICS_LIST,ListType.PTY_OTHERS_LIST};
    /**
     * コンストラクタ
     */
    @Inject
    public DabPtyListPresenter() {

    }

    public void onSelectList(int listIndex) {
        if(listIndex<listTypes.length) {
            mMediaCase.enterList(listTypes[listIndex]);
            //Debug
            //mEventBus.post(new ListTypeChangeEvent());
        }
    }
}
