package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.DabInfoChangeEvent;
import jp.pioneer.carsync.domain.event.HdRadioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.SxmInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlDabSource;
import jp.pioneer.carsync.domain.interactor.ControlHdRadioSource;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlRadioSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatusChangeEvent;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.PlayerTabContainerView;
import jp.pioneer.carsync.presentation.view.RadioTabContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

/**
 * Created by NSW00_008316 on 2017/05/19.
 */
@PresenterLifeCycle
public class RadioTabContainerPresenter extends ListPresenter<RadioTabContainerView> {
    /**
     * ラジオリストタブ
     */
    public enum RadioTabType {
        PRESET,     // プリセットリスト
        FAVORITE,   // お気に入りリスト
        DAB_STATION,
        DAB_PTY,
        DAB_ENSEMBLE,
        DAB_PRESET,
    }

    private static final String KEY_PREFERENCE_TAB = "radio_tab";

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject AppSharedPreference mPreference;
    @Inject ControlRadioSource mControlRadioCase;
    @Inject ControlDabSource mControlDabCase;
    @Inject ControlHdRadioSource mControlHdRadioCase;
    @Inject ControlMediaList mMediaCase;
    private RadioTabType mTab;
    private MediaSourceType mSourceType;
    private RadioBandType mRadioBand;
    private SxmBandType mSxmBand;
    private DabBandType mDabBand;
    private HdRadioBandType mHdRadioBand;
    private TunerStatus mTunerStatus = null;
    /**
     * コンストラクタ
     */
    @Inject
    public RadioTabContainerPresenter() {
    }

    @Override
    void onInitialize() {
        StatusHolder holder = mStatusHolder.execute();
        mSourceType = holder.getCarDeviceStatus().sourceType;
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (!holder.getProtocolSpec().isSphCarDevice()&&mSourceType == MediaSourceType.RADIO) {
                mTab = RadioTabType.FAVORITE;
                view.onNavigate(ScreenId.RADIO_FAVORITE_LIST, Bundle.EMPTY);
            }else if(mSourceType == MediaSourceType.DAB) {
                if(holder.getProtocolSpec().isSphCarDevice()) {
                    ListType listType = mStatusHolder.execute().getCarDeviceStatus().listType;
                    Timber.d("listType=" + listType);
                    Bundle bundle = new Bundle();
                    switch (listType) {
                        case PTY_NEWS_INFO_LIST:
                        case PTY_POPULER_LIST:
                        case PTY_CLASSICS_LIST:
                        case PTY_OYHERS_LIST:
                            mTab = RadioTabType.DAB_PTY;
                            view.onNavigate(ScreenId.DAB_PTY_LIST, Bundle.EMPTY);
                            bundle.putBoolean("stack", true);
                            view.onNavigate(ScreenId.DAB_SERVICE_LIST, bundle);
                            return;
                        case ENSEMBLE_LIST:
                            mTab = RadioTabType.DAB_ENSEMBLE;
                            view.onNavigate(ScreenId.DAB_ENSEMBLE_LIST, Bundle.EMPTY);
                            bundle.putBoolean("stack", true);
                            view.onNavigate(ScreenId.DAB_SERVICE_LIST, bundle);
                            return;
                        case ENSEMBLE_CATEGORY:
                            mTab = RadioTabType.DAB_ENSEMBLE;
                            view.onNavigate(ScreenId.DAB_ENSEMBLE_LIST, Bundle.EMPTY);
                            return;
                        case SERVICE_LIST:
                        case ABC_SEARCH_LIST:
                        default:
                            break;
                    }
                    mTab = holder.getAppStatus().dabListType;
                    switch (mTab) {
                        case DAB_PTY:
                            view.onNavigate(ScreenId.DAB_PTY_LIST, Bundle.EMPTY);
                            break;
                        case DAB_ENSEMBLE:
                            //上で抜けるので通らないはず
                            view.onNavigate(ScreenId.DAB_ENSEMBLE_LIST, Bundle.EMPTY);
                            break;
                        case DAB_PRESET:
                            view.onNavigate(ScreenId.RADIO_PRESET_LIST, Bundle.EMPTY);
                            break;
                        case DAB_STATION:
                        default:
                            view.onNavigate(ScreenId.DAB_SERVICE_LIST, Bundle.EMPTY);
                            break;
                    }
                }else{
                    mTab = RadioTabType.PRESET;
                    view.onNavigate(ScreenId.DAB_SERVICE_LIST, Bundle.EMPTY);
                }
            }else{
                mTab = RadioTabType.PRESET;
                view.onNavigate(ScreenId.RADIO_PRESET_LIST, Bundle.EMPTY);
            }
            updateView();
        });
    }

    @Override
    void onTakeView() {
        StatusHolder holder = mStatusHolder.execute();
        mSourceType = holder.getCarDeviceStatus().sourceType;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setColor(mPreference.getUiColor().getResource());
            if(mSourceType == MediaSourceType.RADIO) {
                view.setTabVisible(!holder.getProtocolSpec().isSphCarDevice());
            }else{
                view.setTabVisible(true);
            }
        });
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        updateView();
        super.onResume();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
        mTunerStatus = null;
        saveCategory();
        super.onPause();
    }

    private void saveCategory(){
        StatusHolder holder = mStatusHolder.execute();
        holder.getAppStatus().dabListType = mTab;
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        mTab = RadioTabType.valueOf(savedInstanceState.getString(KEY_PREFERENCE_TAB));
        updateView();
    }

    @Override
    void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_PREFERENCE_TAB, mTab.name());
    }

    @Override
    public void onClose() {
        if(mSourceType==MediaSourceType.DAB&&mStatusHolder.execute().getProtocolSpec().isSphCarDevice()){
            Optional.ofNullable(getView()).ifPresent(RadioTabContainerView::closeDialog);
        }else {
            mEventBus.post(new GoBackEvent());
        }
    }

    /**
     * プリセットタブ選択
     */
    public void onPresetAction() {
        mTab = RadioTabType.PRESET;
        updateView();
        if(mSourceType==MediaSourceType.DAB){
            mEventBus.post(new NavigateEvent(ScreenId.DAB_SERVICE_LIST, Bundle.EMPTY));
        }else {
            mEventBus.post(new NavigateEvent(ScreenId.RADIO_PRESET_LIST, Bundle.EMPTY));
        }
    }

    /**
     * お気に入りタブ選択
     */
    public void onFavoriteAction() {
        mTab = RadioTabType.FAVORITE;
        updateView();
        mEventBus.post(new NavigateEvent(ScreenId.RADIO_FAVORITE_LIST, Bundle.EMPTY));
    }

    public void onDabStationAction() {
        mTab = RadioTabType.DAB_STATION;
        saveCategory();
        updateView();
        mEventBus.post(new NavigateEvent(ScreenId.DAB_SERVICE_LIST, Bundle.EMPTY));
        mMediaCase.enterList(ListType.SERVICE_LIST);
    }

    public void onDabPtyAction() {
        mTab = RadioTabType.DAB_PTY;
        saveCategory();
        updateView();
        mEventBus.post(new NavigateEvent(ScreenId.DAB_PTY_LIST, Bundle.EMPTY));
    }

    public void onDabEnsembleAction() {
        mTab = RadioTabType.DAB_ENSEMBLE;
        saveCategory();
        updateView();
        mEventBus.post(new NavigateEvent(ScreenId.DAB_ENSEMBLE_LIST, Bundle.EMPTY));
        mMediaCase.enterList(ListType.ENSEMBLE_CATEGORY);
    }

    public void onDabPresetAction() {
        mTab = RadioTabType.DAB_PRESET;
        saveCategory();
        updateView();
        mEventBus.post(new NavigateEvent(ScreenId.RADIO_PRESET_LIST, Bundle.EMPTY));
    }

    private void updateView() {
        StatusHolder holder = mStatusHolder.execute();
        mSourceType = holder.getCarDeviceStatus().sourceType;

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setTabLayout(mSourceType, holder.getProtocolSpec().isSphCarDevice());
            if (mSourceType == MediaSourceType.RADIO) {
                RadioInfo info = holder.getCarDeviceMediaInfoHolder().radioInfo;
                if (mTunerStatus != info.tunerStatus) {
                    if (info.tunerStatus == TunerStatus.BSM) {
                        view.showStatusView(true, RadioPresenter.TAG_BSM);
                    } else if (info.tunerStatus == TunerStatus.PTY_SEARCH) {
                        view.showStatusView(true, RadioPresenter.TAG_PTY_SEARCH);
                    } else {
                        view.showStatusView(false, null);
                    }
                    mTunerStatus = info.tunerStatus;
                }
                if (holder.getTunerFunctionSettingStatus().bsmSettingEnabled && mTab == RadioTabType.PRESET) {
                    view.setBsmButtonVisible(true);
                } else {
                    view.setBsmButtonVisible(false);
                }
                view.setUpdateButtonVisible(false);
            } else if ((mSourceType == MediaSourceType.DAB)) {
                DabInfo info = holder.getCarDeviceMediaInfoHolder().dabInfo;
                if (mTunerStatus != info.tunerStatus) {
                    if (info.tunerStatus == TunerStatus.BSM) {
                        view.showStatusView(true, RadioPresenter.TAG_BSM);
                    } else {
                        view.showStatusView(false, null);
                    }
                    mTunerStatus = info.tunerStatus;
                }
                if ((mTab == RadioTabType.PRESET||mTab == RadioTabType.DAB_STATION||mTab == RadioTabType.DAB_PTY
                        ||mTab == RadioTabType.DAB_ENSEMBLE||mTab == RadioTabType.DAB_PRESET)) {
                    view.setUpdateButtonVisible(true);
                } else {
                    view.setUpdateButtonVisible(false);
                }
                view.setBsmButtonVisible(false);
            } else if ((mSourceType == MediaSourceType.HD_RADIO)) {
                HdRadioInfo info = holder.getCarDeviceMediaInfoHolder().hdRadioInfo;
                if (mTunerStatus != info.tunerStatus) {
                    if (info.tunerStatus == TunerStatus.BSM) {
                        view.showStatusView(true, RadioPresenter.TAG_BSM);
                    } else if (info.tunerStatus == TunerStatus.PTY_SEARCH) {
                        view.showStatusView(true, RadioPresenter.TAG_PTY_SEARCH);
                    } else {
                        view.showStatusView(false, null);
                    }
                    mTunerStatus = info.tunerStatus;
                }
                if (holder.getHdRadioFunctionSettingStatus().bsmSettingEnabled && mTab == RadioTabType.PRESET) {
                    view.setBsmButtonVisible(true);
                } else {
                    view.setBsmButtonVisible(false);
                }
                view.setUpdateButtonVisible(false);
            }else{
                view.setBsmButtonVisible(false);
                view.setUpdateButtonVisible(false);
            }
            view.setTab(mTab);
            view.setBsmButtonEnabled(!holder.getAppStatus().isRunningListTask);
            view.setUpdateButtonEnabled(!holder.getAppStatus().isRunningListTask);
        });

        if (mTab == RadioTabType.PRESET) {
            if (mSourceType == MediaSourceType.RADIO) {
                RadioInfo info = holder.getCarDeviceMediaInfoHolder().radioInfo;
                mRadioBand = info.band;
                if (mRadioBand != null) {
                    //Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mContext.getResources().getString(mRadioBand.label)));
                } else {
                    Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mTab.name()));
                }
            } else if (mSourceType == MediaSourceType.SIRIUS_XM) {
                mSxmBand = holder.getCarDeviceMediaInfoHolder().sxmMediaInfo.band;
                if (mSxmBand != null) {
                    //Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mContext.getResources().getString(mSxmBand.label)));
                } else {
                    Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mTab.name()));
                }
            } else if (mSourceType == MediaSourceType.DAB) {
                mDabBand = holder.getCarDeviceMediaInfoHolder().dabInfo.band;
                Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mContext.getString(R.string.ply_097)));
            } else if (mSourceType == MediaSourceType.HD_RADIO) {
                HdRadioInfo info = holder.getCarDeviceMediaInfoHolder().hdRadioInfo;
                mHdRadioBand = info.band;
                if (mHdRadioBand != null) {
                    //Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mContext.getResources().getString(mRadioBand.label)));
                } else {
                    Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mTab.name()));
                }
            }
        } else if(mTab == RadioTabType.FAVORITE) {
            Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mContext.getResources().getString(R.string.ply_013)));
        }else if(mTab == RadioTabType.DAB_STATION){
            Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mContext.getResources().getString(R.string.ply_100)));
        }else if(mTab == RadioTabType.DAB_PTY){
            Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mContext.getResources().getString(R.string.ply_101)));
        }else if(mTab == RadioTabType.DAB_ENSEMBLE){
            Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mContext.getResources().getString(R.string.ply_102)));
        }else if(mTab == RadioTabType.DAB_PRESET){
            mDabBand = holder.getCarDeviceMediaInfoHolder().dabInfo.band;
            if (mDabBand != null) {
                //Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(mContext.getResources().getString(mRadioBand.label)));
            } else {
                Optional.ofNullable(getView()).ifPresent(view -> view.setTitle("Preset"));
            }
        }

        // 右上×ボタンは、SiriusXM の場合のみ表示
        if (mSourceType == MediaSourceType.SIRIUS_XM) {
            Optional.ofNullable(getView()).ifPresent(view -> view.setCloseButtonVisible(true));
        } else {
            Optional.ofNullable(getView()).ifPresent(view -> view.setCloseButtonVisible(false));
        }
    }

    /**
     * BSM
     */
    public void onBsmAction() {
        if (mSourceType == MediaSourceType.RADIO) {
            mControlRadioCase.setBsm(true);
        } else if (mSourceType == MediaSourceType.HD_RADIO) {
            mControlHdRadioCase.setBsm(true);
        }
    }

    /**
     * Update
     */
    public void onUpdateAction() {
        mControlDabCase.updateList();
        mMediaCase.exitList();
    }

    public void onStatusCloseAction() {
        //BSM停止
        if (mSourceType == MediaSourceType.RADIO) {
            mControlRadioCase.setBsm(false);
        } else if (mSourceType == MediaSourceType.HD_RADIO) {
            mControlHdRadioCase.setBsm(false);
        }
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
     * SXM情報変更通知イベントハンドラ
     *
     * @param event SXM情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSxmInfoChangeEvent(SxmInfoChangeEvent event) {
        updateView();
    }

    /**
     * DAB情報変更通知イベントハンドラ
     *
     * @param event DAB情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDabInfoChangeEvent(DabInfoChangeEvent event) {
        updateView();
    }

    /**
     * HD RADIO情報変更通知イベントハンドラ
     *
     * @param event HD RADIO情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHdRadioInfoChangeEvent(HdRadioInfoChangeEvent event) {
        updateView();
    }

    /**
     * AppStatus変更イベントハンドラ
     *
     * @param event AppStatus変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppStatusChangeEvent(AppStatusChangeEvent event) {
        updateView();
    }
}
