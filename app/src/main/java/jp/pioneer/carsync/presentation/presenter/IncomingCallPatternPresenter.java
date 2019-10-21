package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ChangeScreen;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.CarDeviceScreen;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.TransitionDirection;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.IncomingCallPatternView;

/**
 * Incoming Call Pattern設定画面のPresenter.
 */
@PresenterLifeCycle
public class IncomingCallPatternPresenter extends Presenter<IncomingCallPatternView> {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject PreferIllumination mPreferCase;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject ChangeScreen mScreenCase;
    private ArrayList<String> mTypeArray = new ArrayList<>();
    private MediaSourceType mCurrentMediaSourceType = null;

    /**
     * コンストラクタ.
     */
    @Inject
    public IncomingCallPatternPresenter(){

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        judgeMediaSourceType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * イルミネーション設定変更イベントハンドラ.
     *
     * @param ev イルミネーション設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingChangeEvent(IlluminationSettingChangeEvent ev){
        updateView();
    }

    /**
     * イルミネーション設定ステータス変更イベントハンドラ.
     *
     * @param ev イルミネーション設定ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingStatusChangeEvent(IlluminationSettingStatusChangeEvent ev){
        updateView();
    }

    /**
     * ソース種別変更イベントハンドラ.
     *
     * @param ev ソース種別変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent ev){
        judgeMediaSourceType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onTakeView() {
        mScreenCase.execute(CarDeviceScreen.ILLUMI_PREVIEW_PHONE_COLOR, TransitionDirection.ENTER);

        mTypeArray.clear();
        mTypeArray.add(mContext.getString(R.string.val_036));
        mTypeArray.add(mContext.getString(R.string.val_037));
        mTypeArray.add(mContext.getString(R.string.val_038));
        mTypeArray.add(mContext.getString(R.string.val_039));
        mTypeArray.add(mContext.getString(R.string.val_040));
        mTypeArray.add(mContext.getString(R.string.val_041));
        mTypeArray.add(mContext.getString(R.string.val_042));
        mTypeArray.add(mContext.getString(R.string.val_119));

        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mTypeArray));
        updateView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onDestroy() {
        mScreenCase.execute(CarDeviceScreen.ILLUMI_PREVIEW_PHONE_COLOR, TransitionDirection.EXIT);
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            IlluminationSetting setting = mGetStatusHolder.execute().getIlluminationSetting();
            view.setSelectedItem(setting.btPhoneColor);
        });
    }

    /**
     * リストアイテム選択.
     *
     * @param setting 選択された設定
     */
    public void onSelectedItemAction(BtPhoneColor setting){
        mPreferCase.setBtPhoneColor(setting);
    }

    private void judgeMediaSourceType(){
        if(mCurrentMediaSourceType == null){
            mCurrentMediaSourceType = mGetStatusHolder.execute().getCarDeviceStatus().sourceType;
        } else {
            if(mCurrentMediaSourceType != mGetStatusHolder.execute().getCarDeviceStatus().sourceType){
                if (App.getApp(mContext).isForeground()) {
                    mEventBus.post(new GoBackEvent());
                }
            }
        }
    }
}

