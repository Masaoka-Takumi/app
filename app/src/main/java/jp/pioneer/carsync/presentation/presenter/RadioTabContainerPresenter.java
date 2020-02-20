package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import jp.pioneer.carsync.domain.event.ListTypeChangeEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.SxmInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlDabSource;
import jp.pioneer.carsync.domain.interactor.ControlHdRadioSource;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlRadioSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatusChangeEvent;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
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
import jp.pioneer.carsync.presentation.event.SelectDabListItemEvent;
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
    private static final int ACTION_GUARD_INTERVAL = 3000;//Backボタンや他Tabの連打操作のガード(3秒)
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
    private final Handler mHandler = new Handler();
    private ListType mListType;
    private boolean mIsGuardNavigate = false;
    private boolean mListItemSelected = false;
    private Runnable mRunnableGuard = new Runnable() {
        @Override
        public void run() {
            mIsGuardNavigate = false;
        }
    };
    /**
     * コンストラクタ
     */
    @Inject
    public RadioTabContainerPresenter() {
    }

    @Override
    void onInitialize() {
        mListItemSelected = false;
        StatusHolder holder = mStatusHolder.execute();
        mSourceType = holder.getCarDeviceStatus().sourceType;
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (!holder.getProtocolSpec().isSphCarDevice()&&mSourceType == MediaSourceType.RADIO) {
                mTab = RadioTabType.FAVORITE;
                view.onNavigate(ScreenId.RADIO_FAVORITE_LIST, Bundle.EMPTY);
            }else if(mSourceType == MediaSourceType.DAB) {
                if(holder.getProtocolSpec().isSphCarDevice()) {
                    mListType = mStatusHolder.execute().getCarDeviceStatus().listType;
                    Timber.d("onInitialize:listType=" + mListType);
                    switch (mListType) {
                        case SERVICE_LIST:
                            mTab = RadioTabType.DAB_STATION;
                            view.onNavigate(ScreenId.DAB_SERVICE_LIST, Bundle.EMPTY);
                            break;
                        case PTY_NEWS_INFO_LIST:
                        case PTY_POPULER_LIST:
                        case PTY_CLASSICS_LIST:
                        case PTY_OTHERS_LIST:
                            mTab = RadioTabType.DAB_PTY;
                            view.onNavigate(ScreenId.DAB_SERVICE_LIST, Bundle.EMPTY);
                            break;
                        case ENSEMBLE_LIST:
                            mTab = RadioTabType.DAB_ENSEMBLE;
                            view.onNavigate(ScreenId.DAB_SERVICE_LIST, Bundle.EMPTY);
                            break;
                        case ENSEMBLE_CATEGORY:
                            mTab = RadioTabType.DAB_ENSEMBLE;
                            view.onNavigate(ScreenId.DAB_ENSEMBLE_LIST, Bundle.EMPTY);
                            break;
                        case NOT_LIST:
                        default:
                            mTab = mPreference.getDabSphListTabSelected();
                            if(mTab==RadioTabType.DAB_PTY){
                                mEventBus.post(new NavigateEvent(ScreenId.DAB_PTY_LIST, Bundle.EMPTY));
                            } else if (mTab == RadioTabType.DAB_PRESET) {
                                mEventBus.post(new NavigateEvent(ScreenId.RADIO_PRESET_LIST, Bundle.EMPTY));
                            }
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
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setColor(mPreference.getUiColor().getResource());
        });
    }

    @Override
    void onResume() {
        Timber.d("onResume");
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        mStatusHolder.execute().getAppStatus().isShowRadioTabContainer = true;
        updateView();
        //dispatchEnterListしてPause→Resume復帰した場合もリスト更新をする
        if (mStatusHolder.execute().getCarDeviceStatus().listType != mListType) {
            mListType = mStatusHolder.execute().getCarDeviceStatus().listType;
            Timber.d("onResume():listType=" + mListType);
            onListTypeChangeEvent(null);
        }
        super.onResume();
    }

    @Override
    void onPause() {
        Timber.d("onPause");
        mEventBus.unregister(this);
        mTunerStatus = null;
        saveSelectedTab();
        mStatusHolder.execute().getAppStatus().isShowRadioTabContainer=false;
        mHandler.removeCallbacks(mRunnableGuard);
        super.onPause();
    }

    private void saveSelectedTab(){
        mPreference.setDabSphListTabSelected(mTab);
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

    /**
     * Backキーアクション
     */
    public void onBackAction(){
        Timber.d("onBackAction:mIsGuardNavigate="+mIsGuardNavigate);
        if(mSourceType==MediaSourceType.DAB&&mStatusHolder.execute().getProtocolSpec().isSphCarDevice()){
            if(!mIsGuardNavigate) {
                mIsGuardNavigate = true;
                mHandler.postDelayed(mRunnableGuard, ACTION_GUARD_INTERVAL);
                switch (mListType) {
                    case SERVICE_LIST:
                    case PTY_NEWS_INFO_LIST:
                    case PTY_POPULER_LIST:
                    case PTY_CLASSICS_LIST:
                    case PTY_OTHERS_LIST:
                    case ENSEMBLE_CATEGORY:
                        mMediaCase.exitList();
                        //Debug
                        //mEventBus.post(new ListTypeChangeEvent());
                        break;
                    case ENSEMBLE_LIST:
                        mMediaCase.enterList(ListType.ENSEMBLE_CATEGORY);
                        //Debug
                        //mEventBus.post(new ListTypeChangeEvent());
                        break;
                    case NOT_LIST:
                    default:
                        //DAB_PTY/DAB_PRESET
                        Optional.ofNullable(getView()).ifPresent(RadioTabContainerView::closeDialog);
                        break;
                }
            }
        }else {
            onCloseAction();
        }
    }

    /**
     * リスト解除で閉じる処理.
     */
    @Override
    public void onClose() {
        Timber.d("onClose");
        if(mSourceType==MediaSourceType.DAB&&mStatusHolder.execute().getProtocolSpec().isSphCarDevice()){
            mListType = mStatusHolder.execute().getCarDeviceStatus().listType;
            if(mListType==ListType.NOT_LIST) {
                //ServiceListItemを選択したら閉じる
                if(mListItemSelected){
                    mListItemSelected = false;
                    Optional.ofNullable(getView()).ifPresent(RadioTabContainerView::closeDialog);
                }else if(mTab!=RadioTabType.DAB_PTY&&mTab!=RadioTabType.DAB_PRESET) {
                    Optional.ofNullable(getView()).ifPresent(RadioTabContainerView::closeDialog);
                }
            }else if(mListType==ListType.LIST_UNAVAILABLE){
                //リスト遷移不可状態では必ず閉じる
                Optional.ofNullable(getView()).ifPresent(RadioTabContainerView::closeDialog);
            }
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
        if(!mIsGuardNavigate) {
            mIsGuardNavigate = true;
            mHandler.postDelayed(mRunnableGuard, ACTION_GUARD_INTERVAL);
            mMediaCase.enterList(ListType.SERVICE_LIST);
        }
    }

    public void onDabPtyAction() {
        if(!mIsGuardNavigate) {
            mIsGuardNavigate = true;
            mHandler.postDelayed(mRunnableGuard, ACTION_GUARD_INTERVAL);
            mTab = RadioTabType.DAB_PTY;
            if (mListType == ListType.NOT_LIST) {
                saveSelectedTab();
                updateView();
                mEventBus.post(new NavigateEvent(ScreenId.DAB_PTY_LIST, Bundle.EMPTY));
            } else {
                mMediaCase.exitList();
            }
        }
    }

    public void onDabEnsembleAction() {
        if(!mIsGuardNavigate) {
            mIsGuardNavigate = true;
            mHandler.postDelayed(mRunnableGuard, ACTION_GUARD_INTERVAL);
            mMediaCase.enterList(ListType.ENSEMBLE_CATEGORY);
            //Debug
            //mEventBus.post(new ListTypeChangeEvent());
        }
    }

    public void onDabPresetAction() {
        if(!mIsGuardNavigate) {
            mIsGuardNavigate = true;
            mHandler.postDelayed(mRunnableGuard, ACTION_GUARD_INTERVAL);
            mTab = RadioTabType.DAB_PRESET;
            if (mListType == ListType.NOT_LIST) {
                saveSelectedTab();
                updateView();
                mEventBus.post(new NavigateEvent(ScreenId.RADIO_PRESET_LIST, Bundle.EMPTY));
            } else {
                mMediaCase.exitList();
            }
        }
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
                        ||mTab == RadioTabType.DAB_ENSEMBLE)) {
                    view.setUpdateButtonVisible(true);
                } else {
                    //DAB-01-02:DAB Favorite list画面/DAB Preset List画面(KM997と連携中)はUpdateボタン非表示
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
        }else if(mTab == RadioTabType.DAB_STATION
                ||mListType==ListType.ENSEMBLE_LIST
                ||mListType==ListType.PTY_NEWS_INFO_LIST||mListType==ListType.PTY_CLASSICS_LIST
                ||mListType==ListType.PTY_POPULER_LIST||mListType==ListType.PTY_OTHERS_LIST ){
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
     * リスト種別変更イベント.
     *
     * @param event ListTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onListTypeChangeEvent(ListTypeChangeEvent event) {
        super.onListTypeChangeEvent(event);
        Optional.ofNullable(getView()).ifPresent(view -> {
            StatusHolder holder = mStatusHolder.execute();
            if(holder.getProtocolSpec().isSphCarDevice()&&mSourceType==MediaSourceType.DAB) {
                mIsGuardNavigate = false;
                mListType = holder.getCarDeviceStatus().listType;
                Timber.d("onListTypeChangeEvent:listType=" + mListType);
                switch (mListType) {
                    case SERVICE_LIST:
                        mTab = RadioTabType.DAB_STATION;
                        view.onNavigate(ScreenId.DAB_SERVICE_LIST, Bundle.EMPTY);
                        break;
                    case PTY_NEWS_INFO_LIST:
                    case PTY_POPULER_LIST:
                    case PTY_CLASSICS_LIST:
                    case PTY_OTHERS_LIST:
                        mTab = RadioTabType.DAB_PTY;
                        view.onNavigate(ScreenId.DAB_SERVICE_LIST, Bundle.EMPTY);
                        break;
                    case ENSEMBLE_LIST:
                        mTab = RadioTabType.DAB_ENSEMBLE;
                        view.onNavigate(ScreenId.DAB_SERVICE_LIST, Bundle.EMPTY);
                        break;
                    case ENSEMBLE_CATEGORY:
                        mTab = RadioTabType.DAB_ENSEMBLE;
                        view.onNavigate(ScreenId.DAB_ENSEMBLE_LIST, Bundle.EMPTY);
                        break;
                    case NOT_LIST:
                        if (mTab == RadioTabType.DAB_PTY) {
                            mEventBus.post(new NavigateEvent(ScreenId.DAB_PTY_LIST, Bundle.EMPTY));
                        } else if (mTab == RadioTabType.DAB_PRESET) {
                            mEventBus.post(new NavigateEvent(ScreenId.RADIO_PRESET_LIST, Bundle.EMPTY));
                        }
                        break;
                    default:
                        break;
                }
                saveSelectedTab();
                updateView();
            }
        });
    }

    /**
     * DAB ServiceListItem選択イベント.
     * @param event SelectDabListItemEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSelectDabListItemEvent(SelectDabListItemEvent event){
        mListItemSelected = true;
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
