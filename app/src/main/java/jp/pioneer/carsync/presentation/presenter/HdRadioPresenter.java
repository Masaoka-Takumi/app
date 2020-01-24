package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.HdRadioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.HdRadioInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlHdRadioSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.HdRadioStationStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.CloseDialogEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.util.FrequencyUtil;
import jp.pioneer.carsync.presentation.util.HdRadioTextUtil;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.HdRadioView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/*
HdRadioPresenter
 */
public class HdRadioPresenter extends PlayerPresenter<HdRadioView> implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID_FAVORITE = 0;
    private static final int LOADER_ID_PRESET = 1;
    private static final String KEY_BAND_TYPE = "band_type";
    public static final String TAG_BSM = "tag_bsm";

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlHdRadioSource mControlCase;
    @Inject QueryTunerItem mTunerCase;
    @Inject AppSharedPreference mPreference;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;
    private LoaderManager mLoaderManager;
    private HdRadioInfo mCurrHdRadio;
    private LongSparseArray<Long> mFavorites = new LongSparseArray<>();
    private HdRadioBandType mHdRadioBand;
    private TunerStatus mTunerStatus = null;
    private ArrayList<BandType> mHdRadioBandTypes = new ArrayList<>();
    private Cursor mFavoritesCursor=null;
    private int mPagerPosition=-1;
    /**
     * コンストラクタ
     */
    @Inject
    public HdRadioPresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setColor(mPreference.getUiColor().getResource()));
        getBandList();
        super.onTakeView();
    }

    public int getPagerPosition() {
        return mPagerPosition;
    }

    public void setPagerPosition(int pagerPosition) {
        mPagerPosition = pagerPosition;
    }

    @Override
    void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("Pager",mPagerPosition);
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        mPagerPosition = savedInstanceState.getInt("Pager");
        updateView();
    }

    @Override
    void onInitialize() {
        mEventBus.register(this);
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
        super.onPause();
    }

    @Override
    public void onShowList() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(!view.isShowRadioTabContainer()) {
                mEventBus.post(new BackgroundChangeEvent(true));
            }
            mEventBus.post(new NavigateEvent(ScreenId.RADIO_LIST_CONTAINER, Bundle.EMPTY));
        });

    }

    @Override
    void onListTypeChange() {
        updateView();
    }

    /**
     * HDラジオ情報変更通知イベントハンドラ
     *
     * @param event HDラジオ情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHdRadioInfoChangeEvent(HdRadioInfoChangeEvent event) {
        showBandChangeNotification();
        updateView();
    }

    /**
     * HDラジオ設定ステータス変更通知イベントハンドラ
     *
     * @param event HDラジオ設定ステータス変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHdRadioFunctionSettingStatusChangeEvent(HdRadioFunctionSettingStatusChangeEvent event) {
        updateView();
    }

    /**
     * AdasErrorEventハンドラ
     * @param event AdasErrorEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdasErrorEvent(AdasErrorEvent event) {
        setAdasIcon();
    }

    private void showBandChangeNotification(){
        HdRadioBandType bandType = mStatusHolder.execute().getCarDeviceMediaInfoHolder().hdRadioInfo.band;
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mHdRadioBand!=null&&bandType != mHdRadioBand) {
                view.displayEqFxMessage(mContext.getString(bandType.getLabel()));
                mHdRadioBand = bandType;
            }
        });
    }
    /**
     * 次のプリセット番号へ
     */
    public void onNextPresetAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(!mCurrHdRadio.isSearchStatus()) {
                view.showGesture(GestureType.PCH_UP);
                mControlCase.channelUp();
            }
        });
    }

    /**
     * 前のプリセット番号へ
     */
    public void onPreviousPresetAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(!mCurrHdRadio.isSearchStatus()) {
                view.showGesture(GestureType.PCH_DOWN);
                mControlCase.channelDown();
            }
        });
    }

    /**
     * BAND切替
     */
    public void onToggleBandAction() {
        mControlCase.toggleBand();
    }

    /**
     * 周波数押下処理
     */
    public void onFrequencyAction(){
        switch(mCurrHdRadio.tunerStatus){
            case BSM:
                mControlCase.setBsm(false);
                break;
            case SEEK:
                mControlCase.manualUp();
                break;
            default:
                mControlCase.seekUp();
                break;
        }
    }

    /**
     * お気に入り登録/解除
     */
    public void onFavoriteAction() {
        long id = isContainsFavorite(mCurrHdRadio.getBand(), mCurrHdRadio.currentFrequency, mCurrHdRadio.multicastChannelNumber);
        if (id!= -1L) {
            mTunerCase.unregisterFavorite(TunerContract.FavoriteContract.DeleteParamsBuilder.createParams(id));
        } else {
            StatusHolder holder = mStatusHolder.execute();
            CarDeviceStatus status = holder.getCarDeviceStatus();
            mTunerCase.registerFavorite(TunerContract.FavoriteContract.UpdateParamsBuilder.createHdRadio(mCurrHdRadio, mContext));
        }
    }


    /**
     * 画面更新
     */
    private void updateView() {
        StatusHolder holder = mStatusHolder.execute();
        CarDeviceStatus status = holder.getCarDeviceStatus();

        mCurrHdRadio = holder.getCarDeviceMediaInfoHolder().hdRadioInfo;
        mHdRadioBand = holder.getCarDeviceMediaInfoHolder().hdRadioInfo.band;

        getFavorite();
        getPresetChannel();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (mTunerStatus != mCurrHdRadio.tunerStatus) {
                switch (mCurrHdRadio.tunerStatus){
                    case BSM:
                        mEventBus.post(new CloseDialogEvent(ScreenId.SOURCE_SELECT));
                        view.showStatusView(true, HdRadioPresenter.TAG_BSM);
                        break;
                    default:
                        view.showStatusView(false,null);
                        break;
                }
                mTunerStatus = mCurrHdRadio.tunerStatus;
            }
            view.setViewPagerCurrentPage(mHdRadioBand);
            view.setBand(mCurrHdRadio.band);
            if (mCurrHdRadio.isSearchStatus()) {
                view.setFrequency(null);
                view.setFavoriteVisible(false);
                view.setPch(-1);
            } else {
                view.setFrequency(FrequencyUtil.toString(mContext, mCurrHdRadio.currentFrequency, mCurrHdRadio.frequencyUnit));
                view.setFavoriteVisible(true);
            }
            view.setStatus(mCurrHdRadio.tunerStatus);
            view.setPsInformation(HdRadioTextUtil.getStationInfoForPlayer(mContext, status, mCurrHdRadio));
            view.setMusicInfo(HdRadioTextUtil.getStationInfoForPlayer(mContext, status, mCurrHdRadio),(String) HdRadioTextUtil.getSongTitleForPlayer(mContext, mCurrHdRadio),HdRadioTextUtil.getArtistNameForPlayer(mContext, mCurrHdRadio));
            view.setAntennaLevel((float) mCurrHdRadio.antennaLevel / mCurrHdRadio.maxAntennaLevel);
            view.setAdasEnabled((mPreference.isAdasEnabled()&&mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN&&(mStatusHolder.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING))||mPreference.isAdasPseudoCooperation());
            view.setListEnabled(status.listType != ListType.LIST_UNAVAILABLE);
            view.setHdIcon(mCurrHdRadio.hdRadioStationStatus == HdRadioStationStatus.RECEIVING);
            view.setMulticastNumber(HdRadioTextUtil.getMulticastProgramNumber(mContext, mCurrHdRadio));
            view.setSignalStatus(HdRadioTextUtil.getDigitalAudioStatus(mContext, mCurrHdRadio));
        });
        setAdasIcon();
    }

    // MARK - loader

    /**
     * LoaderManager登録
     *
     * @param loaderManager LoaderManager
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        getFavorite();
        getPresetChannel();
    }

    /**
     * お気に入りリスト作成
     * <p>
     * カーソルローダではデータの照合を行えないため、リストに起こす
     *
     * @param data お気に入り情報カーソル
     */
    private void createFavoriteList(Cursor data) {
        mFavorites.clear();
        data.moveToPosition(-1);
        while (data.moveToNext()) {
            long frequency = TunerContract.FavoriteContract.HdRadio.getFrequency(data);
            long id = TunerContract.FavoriteContract.HdRadio.getId(data);
            mFavorites.put(frequency, id);
        }
    }

    /**
     * お気に入り判定
     *
     * @param band      視聴BAND
     * @param frequency 視聴周波数
     * @return お気に入り登録済みID
     */
    private long isContainsFavorite(HdRadioBandType band, long frequency, int multicastChNumber) {
        mFavoritesCursor.moveToPosition(-1);
        while (mFavoritesCursor.moveToNext()) {
            HdRadioBandType bandType = TunerContract.FavoriteContract.HdRadio.getBandType(mFavoritesCursor);
            long frequency1 = TunerContract.FavoriteContract.HdRadio.getFrequency(mFavoritesCursor);
            int multicastChNumber1 = TunerContract.FavoriteContract.HdRadio.getMulticastChNumber(mFavoritesCursor);
            long id = TunerContract.FavoriteContract.Dab.getId(mFavoritesCursor);
            if(frequency==frequency1&&multicastChNumber==multicastChNumber1){
                return id;
            }
        }
        return -1L;
    }

    private SparseArray createPresetList(Cursor data) {
        SparseArray<String> preset = new SparseArray<>();

        boolean isEof = data.moveToFirst();
        while (isEof) {
            HdRadioBandType band = TunerContract.ListItemContract.HdRadio.getBandType(data);
            if (mHdRadioBand == band) {
                int pch = TunerContract.ListItemContract.HdRadio.getPchNumber(data);
                //TODO 周波数(番組名が取得できるようになる?)
                long frequency = TunerContract.ListItemContract.HdRadio.getFrequency(data);
                TunerFrequencyUnit unit = TunerContract.ListItemContract.HdRadio.getFrequencyUnit(data);
                String name = FrequencyUtil.toString(mContext, frequency, unit);

                preset.put(pch, name);
            }
            isEof = data.moveToNext();
        }

        return preset;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_FAVORITE) {
            return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createHdRadio());
        } else if (id == LOADER_ID_PRESET) {
            return mTunerCase.getPresetList(MediaSourceType.HD_RADIO, HdRadioBandType.valueOf(args.getByte(KEY_BAND_TYPE)));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mCurrHdRadio!=null) {
                if (id == LOADER_ID_FAVORITE) {
                    mFavoritesCursor =data;
                    //createFavoriteList(data);
                    long isFavorite = isContainsFavorite(mCurrHdRadio.getBand(), mCurrHdRadio.currentFrequency,mCurrHdRadio.multicastChannelNumber);
                    view.setFavorite(isFavorite != -1L);
                } else {
                    SparseArray presetList = createPresetList(data);
                    boolean isPreset = false;
                    for (int i = 0; i < presetList.size(); i++) {
                        int key = presetList.keyAt(i);
                        // get the object by the key.
                        String name = (String) presetList.get(key);
                        if (name.equals(FrequencyUtil.toString(mContext, mCurrHdRadio.currentFrequency, mCurrHdRadio.frequencyUnit))) {
                            isPreset = true;
                            if(mCurrHdRadio.isSearchStatus()){
                                view.setPch(-1);
                            } else {
                                view.setPch(key);
                            }
                            view.setSelectedPreset(key);
                            break;
                        }
                    }
                    if (!isPreset) {
                        view.setPch(-1);
                        view.setSelectedPreset(-1);
                    }
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // no action
    }

    private void getFavorite() {
        if (mLoaderManager != null) {
            mLoaderManager.initLoader(LOADER_ID_FAVORITE, Bundle.EMPTY, this);
        }
    }

    private void getPresetChannel() {
        if (mHdRadioBand != null && mLoaderManager != null) {
            Bundle args = new Bundle();
            args.putByte(KEY_BAND_TYPE, (byte) (mHdRadioBand.getCode() & 0xFF));
            mLoaderManager.restartLoader(LOADER_ID_PRESET, args, this);
        }
    }

    // MARK - EQ FX

    @Override
    void onUpdateSoundFxButton() {
        SoundFxButtonInfo info = getSoundFxButtonInfo();
        String showText = info.isShowEqMessage ? info.textEqButton : info.isShowFxMessage ? info.textFxButton : null;

        Optional.ofNullable(getView()).ifPresent(view -> {

            view.setEqFxButtonEnabled(info.isEqEnabled, info.isFxEnabled);
            view.setEqButton(info.textEqButton);
            view.setFxButton(info.textFxButton);
            if(showText != null) {
                view.displayEqFxMessage(showText);
            }
        });
    }

    public void onCloseAction(){
        //BSM停止
        mControlCase.setBsm(false);
    }

    private void setAdasIcon(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            int status = 0;
            AppStatus appStatus = mStatusHolder.execute().getAppStatus();
            if(appStatus.adasDetected)status = 1;
            if(appStatus.isAdasError())status = 2;
            view.setAdasIcon(status);
        });
    }

    /**
     * ボリュームアップのアクション
     */
    public void onVolumeUpAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.VOLUME_UP));
        mControlCase.volumeUp();
    }

    /**
     * ボリュームダウンのアクション
     */
    public void onVolumeDownAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.VOLUME_DOWN));
        mControlCase.volumeDown();
    }

    @Override
    protected void updateShortcutButton() {
        super.updateShortcutButton();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setShortCutButtonEnabled(true);
            if(mShortCutKeyEnabledStatus.isShortCutKeyEnabled()) {
                view.setShortcutKeyItems(mShortCutKeyList);
            }
            view.setBandList(mHdRadioBandTypes);
        });
    }

    @Override
    protected void updateNotification() {
        super.updateNotification();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mShortCutKeyEnabledStatus.isShortCutKeyEnabled()) {
                view.setShortcutKeyItems(mShortCutKeyList);
            }
        });
    }

    @Override
    protected void updateAlexaNotification() {
        super.updateAlexaNotification();
        Optional.ofNullable(getView()).ifPresent(view ->{
            view.setAlexaNotification(isNeedUpdateAlexaNotification());
        });
    }

    /**
     * PCH選局
     */
    public void onSelectPreset(int presetNum){
        mControlCase.callPreset(presetNum);
    }

    private void getBandList() {
        mHdRadioBandTypes.clear();
        mHdRadioBandTypes.add(HdRadioBandType.FM1);
        mHdRadioBandTypes.add(HdRadioBandType.FM2);
        mHdRadioBandTypes.add(HdRadioBandType.FM3);
        mHdRadioBandTypes.add(HdRadioBandType.AM);
    }
}
