package jp.pioneer.carsync.presentation.presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.ListTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;

/**
 * リスト画面共通のPresenter.
 */
public class ListPresenter<T> extends Presenter<T> {
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlMediaList mMediaCase;
    @Inject EventBus mEventBus;

    /**
     * コンストラクタ.
     */
    @Inject
    public ListPresenter(){

    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        goCloseIfListUnDisplayable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * 閉じる処理.
     */
    public void onCloseAction(){
        CarDeviceStatus status = mStatusHolder.execute().getCarDeviceStatus();

        if(status.listType == ListType.NOT_LIST){
            onClose();
        }

        if(status.sourceType.isListSupported()){
            mMediaCase.exitList();
        }
    }

    /**
     * 閉じる.
     */
    public void onClose(){
    }

    /**
     * リスト種別変更イベント.
     *
     * @param event ListTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onListTypeChangeEvent(ListTypeChangeEvent event) {
        goCloseIfListUnDisplayable();
    }


    /**
     * 閉じる判定.
     */
    private void goCloseIfListUnDisplayable(){
        ListType listType = mStatusHolder.execute().getCarDeviceStatus().listType;
        MediaSourceType sourceType = mStatusHolder.execute().getCarDeviceStatus().sourceType;

        if(!listType.types.contains(sourceType)){
            onClose();
        }
    }
}