package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ChangeScreen;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.CarDeviceScreen;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;
import jp.pioneer.carsync.domain.model.TransitionDirection;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.IncomingCallColorView;

/**
 * Incoming Call Color設定画面のPresenter.
 */
@PresenterLifeCycle
public class IncomingCallColorPresenter extends Presenter<IncomingCallColorView> {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject PreferIllumination mPreferCase;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject ChangeScreen mScreenCase;
    private ArrayList<String> mTypeArray = new ArrayList<>();
    private MediaSourceType mCurrentMediaSourceType = null;

    private static final SparseArrayCompat<SphBtPhoneColorSetting> BT_PHONE_COLORS = new SparseArrayCompat<SphBtPhoneColorSetting>(){{
        put(0,SphBtPhoneColorSetting.WHITE);
        put(1,SphBtPhoneColorSetting.RED);
        put(2,SphBtPhoneColorSetting.AMBER);
        put(3,SphBtPhoneColorSetting.ORANGE);
        put(4,SphBtPhoneColorSetting.YELLOW);
        put(5,SphBtPhoneColorSetting.PUREGREEN);
        put(6,SphBtPhoneColorSetting.GREEN);
        put(7,SphBtPhoneColorSetting.TUEQUOISE);
        put(8,SphBtPhoneColorSetting.LIGHTBLUE);
        put(9,SphBtPhoneColorSetting.BLUE);
        put(10,SphBtPhoneColorSetting.PURPLE);
        put(11,SphBtPhoneColorSetting.PINK);
        put(12,SphBtPhoneColorSetting.CUSTOM);
        put(13,SphBtPhoneColorSetting.REFER_TO_ZONE_1);
        put(14,SphBtPhoneColorSetting.OFF);
    }};

    /**
     * コンストラクタ.
     */
    @Inject
    public IncomingCallColorPresenter(){

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
     * {@inheritDoc}
     */
    @Override
    void onDestroy() {
        mScreenCase.execute(CarDeviceScreen.ILLUMI_PREVIEW_PHONE_COLOR, TransitionDirection.EXIT);
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
        for(int i = 0; i < BT_PHONE_COLORS.size(); i++){
            mTypeArray.add(mContext.getString(BT_PHONE_COLORS.get(i).label));
        }

        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mTypeArray));
        updateView();
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            IlluminationSetting setting = mGetStatusHolder.execute().getIlluminationSetting();
            view.setSelectedItem(BT_PHONE_COLORS.indexOfValue(setting.sphBtPhoneColorSetting));
        });
    }

    /**
     * リストアイテム選択.
     *
     * @param position 選択された設定
     */
    public void onSelectedItemAction(int position){
        mPreferCase.setSphBtPhoneColor(BT_PHONE_COLORS.get(position));
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

