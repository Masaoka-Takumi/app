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
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ChangeScreen;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.CarDeviceScreen;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IncomingMessageColorSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.TransitionDirection;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.IncomingMessageColorView;

/**
 * Created by NSW00_008320 on 2018/03/14.
 */

public class IncomingMessageColorPresenter extends Presenter<IncomingMessageColorView> {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject PreferIllumination mPreferCase;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject ChangeScreen mScreenCase;
    private ArrayList<String> mTypeArray = new ArrayList<>();
    private MediaSourceType mCurrentMediaSourceType = null;

    private static final SparseArrayCompat<IncomingMessageColorSetting> INCOMING_MESSAGE_COLORS = new SparseArrayCompat<IncomingMessageColorSetting>(){{
        put(0,IncomingMessageColorSetting.WHITE);
        put(1,IncomingMessageColorSetting.RED);
        put(2,IncomingMessageColorSetting.AMBER);
        put(3,IncomingMessageColorSetting.ORANGE);
        put(4,IncomingMessageColorSetting.YELLOW);
        put(5,IncomingMessageColorSetting.PUREGREEN);
        put(6,IncomingMessageColorSetting.GREEN);
        put(7,IncomingMessageColorSetting.TUEQUOISE);
        put(8,IncomingMessageColorSetting.LIGHTBLUE);
        put(9,IncomingMessageColorSetting.BLUE);
        put(10,IncomingMessageColorSetting.PURPLE);
        put(11,IncomingMessageColorSetting.PINK);
        put(12,IncomingMessageColorSetting.CUSTOM);
        put(13,IncomingMessageColorSetting.REFER_TO_ZONE_1);
        put(14,IncomingMessageColorSetting.OFF);
    }};

    /**
     * コンストラクタ.
     */
    @Inject
    public IncomingMessageColorPresenter(){

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
        mScreenCase.execute(CarDeviceScreen.ILLUMI_PREVIEW_MESSAGE_COLOR, TransitionDirection.ENTER);

        mTypeArray.clear();
        for(int i = 0; i < INCOMING_MESSAGE_COLORS.size(); i++){
            mTypeArray.add(mContext.getString(INCOMING_MESSAGE_COLORS.get(i).label));
        }

        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mTypeArray));
        updateView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onDestroy() {
        mScreenCase.execute(CarDeviceScreen.ILLUMI_PREVIEW_MESSAGE_COLOR, TransitionDirection.EXIT);
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            IlluminationSetting setting = mGetStatusHolder.execute().getIlluminationSetting();
            view.setSelectedItem(INCOMING_MESSAGE_COLORS.indexOfValue(setting.incomingMessageColor));
        });
    }

    /**
     * リストアイテム選択.
     *
     * @param position 選択された設定
     */
    public void onSelectedItemAction(int position){
        mPreferCase.setIncomingMessageColor(INCOMING_MESSAGE_COLORS.get(position));
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

