package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.DimmerTimeType;
import jp.pioneer.carsync.domain.model.IlluminationSettingSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TimeFormatSetting;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.DimmerListType;
import jp.pioneer.carsync.presentation.view.DimmerSettingView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

/**
 * ディマー設定画面のpresenter
 */
@PresenterLifeCycle
public class DimmerSettingPresenter extends Presenter<DimmerSettingView> {

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject PreferIllumination mPreferIllumination;
    @Inject AppSharedPreference mPreference;
    private static final int DIMMER_OFF = 0;
    private static final int DIMMER_ON = 1;
    private static final int DIMMER_ILLUMI_LINE = 2;
    private static final int DIMMER_SYNC_CLOCK = 3;
    private static final int DIMMER_SYNC_CLOCK_START = 4;
    private static final int DIMMER_SYNC_CLOCK_STOP = 5;

    private ArrayList<DimmerListType> mTypeArray = new ArrayList<>();
    private Bundle mArguments;
    private int mPage=0;
    /**
     * コンストラクタ
     */
    @Inject
    public DimmerSettingPresenter() {
    }
    @Override
    void onTakeView() {
        Set<DimmerSetting.Dimmer> supportedDimmers = mGetStatusHolder.execute().getCarDeviceSpec().illuminationSettingSpec.supportedDimmers;

        mTypeArray.clear();
        if(supportedDimmers.contains(DimmerSetting.Dimmer.OFF)){
            mTypeArray.add(DimmerListType.OFF);
        }
        if(supportedDimmers.contains(DimmerSetting.Dimmer.MANUAL)){
            mTypeArray.add(DimmerListType.MANUAL);
        }
        if(supportedDimmers.contains(DimmerSetting.Dimmer.ILLUMI_LINE)){
            mTypeArray.add(DimmerListType.ILLUMI_LINE);
        }
        if(supportedDimmers.contains(DimmerSetting.Dimmer.SYNC_CLOCK)){
            mTypeArray.add(DimmerListType.SYNC_CLOCK);
            mTypeArray.add(DimmerListType.SYNC_CLOCK_START);
            mTypeArray.add(DimmerListType.SYNC_CLOCK_STOP);
        }

        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mTypeArray));
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        setPage();
        updateView();
        setEnable();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }
    public void setArgument(Bundle args) {
        mArguments = args;
        mPage=0;
        SettingsParams params = SettingsParams.from(mArguments);
        if (params.mScreenId == ScreenId.ILLUMINATION_DIMMER_SETTING) {
            mPage = 1;
        }
    }

    private void setPage(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setPage(mPage);
        });
    }

    public void onBackAction() {
        mEventBus.post(new GoBackEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingChangeEvent(IlluminationSettingChangeEvent event) {
        updateView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingStatusChangeEvent(IlluminationSettingStatusChangeEvent event) {
        setEnable();
    }

    private void updateView() {
        StatusHolder holder = mGetStatusHolder.execute();
        DimmerSetting dimmer = holder.getIlluminationSetting().dimmerSetting;
        if(dimmer.dimmer == null){
            return;
        }

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setSelectedItem(dimmer.dimmer);
            view.setDimmerSchedule(dimmer.startHour, dimmer.startMinute, dimmer.endHour, dimmer.endMinute);
        });
    }

    /**
     * ディマー種別選択処理
     *
     * @param type ディマー種別
     */
    public void onSelectDimmerAction(DimmerListType type) {
        StatusHolder holder = mGetStatusHolder.execute();
        DimmerSetting dimmer = holder.getIlluminationSetting().dimmerSetting;
        switch (type) {
            case OFF:
            case MANUAL:
            case ILLUMI_LINE:
            case SYNC_CLOCK:
                mPreferIllumination.setDimmer(type.dimmer);
                break;
            case SYNC_CLOCK_START:
            case SYNC_CLOCK_STOP:
                goTimerPicker();
                break;
            default:
                Timber.w("This case is impossible.");
                break;
        }
    }

    /**
     * ディマー時間設定処理
     *
     * @param type   ディマー時間種別
     * @param hour   設定時間(時)
     * @param minute 設定時間(分)
     */
    public void onSelectDimmerTimeAction(DimmerTimeType type, int hour, int minute) {
        mPreferIllumination.setDimmerTime(type, hour, minute);
    }

    private void setEnable(){
        StatusHolder holder = mGetStatusHolder.execute();
        IlluminationSettingSpec spec = holder.getCarDeviceSpec().illuminationSettingSpec;
        IlluminationSettingStatus status = holder.getIlluminationSettingStatus();
        Optional.ofNullable(getView()).ifPresent(view -> view.setEnable(spec.dimmerSettingSupported && status.dimmerSettingEnabled));
    }

    public TimeFormatSetting getTimeFormatSetting() {
        if (DateFormat.is24HourFormat(mContext)) {
            return TimeFormatSetting.TIME_FORMAT_24;
        } else {
            return TimeFormatSetting.TIME_FORMAT_12;
        }
        //return mPreference.getTimeFormatSetting();
    }

    private void goTimerPicker(){
        mEventBus.post(new NavigateEvent(ScreenId.ILLUMINATION_DIMMER_SETTING, createSettingsParams(ScreenId.ILLUMINATION_DIMMER_SETTING,mContext.getString(R.string.set_212))));
    }

    private Bundle createSettingsParams(ScreenId screenId, String pass) {
        SettingsParams params = new SettingsParams();
        params.mScreenId = screenId;
        params.pass = pass;
        return params.toBundle();
    }
}
