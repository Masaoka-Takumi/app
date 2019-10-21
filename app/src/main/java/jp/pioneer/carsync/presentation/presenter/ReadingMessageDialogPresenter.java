package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ActionSoftwareShortcutKey;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PrepareReadNotification;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.ReadingMessageDialogView;

/**
 * Radio BSM DialogのPresenter
 */
@PresenterLifeCycle
public class ReadingMessageDialogPresenter extends Presenter<ReadingMessageDialogView> {
    public static final String TYPE = "type";
    public static final String TAG_READING = "tag_reading";
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject PrepareReadNotification mPrepareReadCase;
    @Inject ActionSoftwareShortcutKey mShortcutCase;
    private String mType = "";

    /**
     * コンストラクタ
     */
    @Inject
    public ReadingMessageDialogPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        int title;
        switch (mType) {
            case TAG_READING:
                title = R.string.mes_008;
                break;
            default:
                title = R.string.mes_008;
                break;
        }
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setTitleText(title);
            view.setAnimation(mType);
        });
        updateView();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * 選択中Customタイプの設定
     *
     * @param args Bundle
     */
    public void setArgument(Bundle args) {
        mType = args.getString(TYPE);
    }

    /**
     * ラジオ情報変更通知イベントハンドラ
     *
     * @param event ラジオ情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioInfoChangeEvent(RadioInfoChangeEvent event) {
        updateView();
    }

    /**
     * ソース種別変更イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeAction(MediaSourceTypeChangeEvent event) {
        updateView();
    }

    public void onDialogClickAction(){
        switch (mType) {
            case TAG_READING:
                mShortcutCase.execute(ShortcutKey.MESSAGE);
                break;
            default:
                break;
        }
    }

    public void onCloseAction(){
        switch (mType) {
            case TAG_READING:
                //通知読み上げ停止
                mPrepareReadCase.finish();
                Optional.ofNullable(getView()).ifPresent(ReadingMessageDialogView::callbackClose);
                break;
            default:
                break;
        }
    }

    private void updateView(){
        StatusHolder holder = mStatusHolder.execute();
        RadioInfo info = holder.getCarDeviceMediaInfoHolder().radioInfo;
        MediaSourceType sourceType = holder.getCarDeviceStatus().sourceType;
        switch (mType) {
            case TAG_READING:
                //ReadingMessage中ダイアログ閉幕
                if(sourceType != MediaSourceType.TTS){
                    mPrepareReadCase.finish();
                    Optional.ofNullable(getView()).ifPresent(ReadingMessageDialogView::callbackClose);
                }
                break;
            default:
                break;
        }

    }
}
