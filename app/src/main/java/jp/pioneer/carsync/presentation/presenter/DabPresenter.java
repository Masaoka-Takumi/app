package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.DabFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.DabInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlDabSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.util.DabTextUtil;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.DabView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

public class DabPresenter extends PlayerPresenter<DabView> implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID_FAVORITE = 0;
    private static final int LOADER_ID_PRESET = 1;
    private static final String KEY_BAND_TYPE = "band_type";
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlDabSource mControlCase;
    @Inject QueryTunerItem mTunerCase;
    @Inject AppSharedPreference mPreference;
    @Inject CarDeviceMediaRepository mCarDeviceMediaRepository;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;
    private LoaderManager mLoaderManager;
    private DabInfo mCurrDab;
    private LongSparseArray<Long> mFavorites = new LongSparseArray<>();
    private DabBandType mDabBand;
    private TunerStatus mTunerStatus = null;
    private ArrayList<BandType> mDabBandTypes = new ArrayList<>();
    private Cursor mFavoritesCursor=null;
    private int mPagerPosition=-1;
    /**
     * コンストラクタ
     */
    @Inject
    public DabPresenter() {
    }

    @Override
    void onTakeView() {
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
     * DAB情報変更通知イベントハンドラ
     *
     * @param event DAB情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDabInfoChangeEvent(DabInfoChangeEvent event) {
        updateView();
    }

    /**
     * DAB設定ステータス変更通知イベントハンドラ
     *
     * @param event DAB設定ステータス変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDabFunctionSettingStatusChangeEvent(DabFunctionSettingStatusChangeEvent event) {
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
        DabBandType bandType = mStatusHolder.execute().getCarDeviceMediaInfoHolder().dabInfo.band;
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(bandType!=null&&mDabBand!=null&&bandType != mDabBand) {
                view.displayEqFxMessage(mContext.getString(bandType.getLabel()));
                mDabBand = bandType;
            }
        });
    }

    /**
     * 次のプリセット番号へ
     */
    public void onNextPresetAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(!mCurrDab.isSearchStatus()) {
                view.showGesture(GestureType.PCH_UP);
                mControlCase.presetUp();
            }
        });
    }

    /**
     * 前のプリセット番号へ
     */
    public void onPreviousPresetAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(!mCurrDab.isSearchStatus()) {
                view.showGesture(GestureType.PCH_DOWN);
                mControlCase.presetDown();
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
    public void onServiceNameAction(){
        switch(mCurrDab.tunerStatus){
            case SEEK:
                mControlCase.toggleSeek();
                break;
            default:
                mControlCase.toggleSeek();
                break;
        }
    }

    /**
     * お気に入り登録/解除
     */
    public void onFavoriteAction() {
        long id  = isContainsFavorite(mCurrDab.getBand(), mCurrDab.currentFrequency, mCurrDab.eid,mCurrDab.sid, mCurrDab.scids);
        if (id!= -1L) {
            mTunerCase.unregisterFavorite(TunerContract.FavoriteContract.DeleteParamsBuilder.createParams(id));
        } else {
            StatusHolder holder = mStatusHolder.execute();
            CarDeviceStatus status = holder.getCarDeviceStatus();
            mTunerCase.registerFavorite(TunerContract.FavoriteContract.UpdateParamsBuilder.createDab(mCurrDab, mContext));
        }
    }

    /**
     * PlayPauseアクション
     */
    public void onPlayPauseAction() {
        mControlCase.togglePlay();
    }

    /**
     * TimeShiftアクション
     */
    public void onTimeShiftAction() {
        mControlCase.toggleMode();
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
            long frequency = TunerContract.FavoriteContract.Dab.getFrequency(data);
            long id = TunerContract.FavoriteContract.Dab.getId(data);
            mFavorites.put(frequency, id);
        }
    }

    /**
     * お気に入り判定
     *
     * @param band      視聴BAND
     * @param frequency 視聴周波数
     * @return お気に入り登録済ID
     */
    private long isContainsFavorite(DabBandType band, long frequency,int eid,long sid,int scids) {
        mFavoritesCursor.moveToPosition(-1);
        while (mFavoritesCursor.moveToNext()) {
            long frequency1 = TunerContract.FavoriteContract.Dab.getFrequency(mFavoritesCursor);
            int eid1 = TunerContract.FavoriteContract.Dab.getEid(mFavoritesCursor);
            long sid1 = TunerContract.FavoriteContract.Dab.getSid(mFavoritesCursor);
            int scids1 = TunerContract.FavoriteContract.Dab.getScids(mFavoritesCursor);
            long id = TunerContract.FavoriteContract.Dab.getId(mFavoritesCursor);
            if(frequency==frequency1&&eid==eid1&&sid==sid1&&scids==scids1){
                return id;
            }
        }
        return -1L;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_FAVORITE) {
            return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createDab());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mCurrDab!=null) {
                if (id == LOADER_ID_FAVORITE) {
                    //createFavoriteList(data);
					mFavoritesCursor = data;
                    long isFavorite = isContainsFavorite(mCurrDab.getBand(), mCurrDab.currentFrequency, mCurrDab.eid,mCurrDab.sid, mCurrDab.scids);
                    view.setFavorite(isFavorite!= -1L);
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

    /**
     * 画面更新
     */
    private void updateView() {
        showBandChangeNotification();

        StatusHolder holder = mStatusHolder.execute();
        CarDeviceStatus status = holder.getCarDeviceStatus();

        mCurrDab = holder.getCarDeviceMediaInfoHolder().dabInfo;
        mDabBand = holder.getCarDeviceMediaInfoHolder().dabInfo.band;

        getFavorite();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (mTunerStatus != mCurrDab.tunerStatus) {

                mTunerStatus = mCurrDab.tunerStatus;
            }
            view.setViewPagerCurrentPage(mDabBand);
            view.setBand(mCurrDab.band);
            if (mCurrDab.isSearchStatus()||mCurrDab.isErrorStatus()) {
                view.setFavoriteVisible(false);
                view.setServiceNumber("");
            } else {
                view.setFavoriteVisible(!mCurrDab.timeShiftMode);
            }
            view.setTimeShiftVisible(mStatusHolder.execute().getCarDeviceSpec().timeShiftSupported);
            view.setTimeShift(mCurrDab.timeShiftMode,mCurrDab.timeShiftModeAvailable);
            view.setPlayPauseVisible(mStatusHolder.execute().getCarDeviceSpec().timeShiftSupported&&!mCurrDab.timeShiftMode);
            view.setPlayPause(mCurrDab.playbackMode == PlaybackMode.PLAY,mCurrDab.timeShiftModeAvailable);
            view.setMaxProgress(mCurrDab.totalBufferTime);
            view.setCurrentProgress(mCurrDab.currentPosition);
            view.setSecondaryProgress(mCurrDab.currentBufferTime);
            view.setServiceName(DabTextUtil.getServiceComponentLabelForPlayer(mContext,mCurrDab));
            view.setServiceNumber(DabTextUtil.getServiceNumberForPlayer(mCurrDab));
            view.setStatus(mCurrDab.playbackMode,mCurrDab.tunerStatus);
            view.setDynamicLabelText(DabTextUtil.getDynamicLabelForPlayer(status,mCurrDab));
            //view.setPtyName((String) DabTextUtil.getPtyInfoForPlayer(mContext,status, mCurrDab));
            view.setFmLink(DabTextUtil.getFmLink(mContext, mCurrDab));
            view.setAntennaLevel((float) mCurrDab.antennaLevel / mCurrDab.maxAntennaLevel);
            view.setAdasEnabled((mPreference.isAdasEnabled()&&mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN&&(mStatusHolder.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING))||mPreference.isAdasPseudoCooperation());
            view.setListEnabled(!mCurrDab.timeShiftMode&&status.listType != ListType.LIST_UNAVAILABLE);

/*            view.setServiceName("WWWWWWWWWWyyyyyyyyyyyyyyyyWWWWWWWWWWWWWWWWWWWWWWWW");
            view.setDynamicLabelText("WWWWWWWWWWWWWWWWljjjjjjjjjjjjjjjjjjjyyyyyyyyyyyxxxxxxxxxjjjjjjfhmhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhxxyhhyyyyyyyyyyyyyyy");
            view.setPtyName("WWWWWWWWWWyyyyyyyyyyyyyyyyWWWWWWWWWWWWWWWWWWWWWWWW");
            view.setFmLink("FmLink");*/
        });
        setAdasIcon();
        setPresetNumber();
    }

    // MARK - EQ FX

    @Override
    void onUpdateSoundFxButton() {
        SoundFxButtonInfo info = getSoundFxButtonInfo();
        String showText = info.isShowEqMessage ? info.textEqButton : info.isShowFxMessage ? info.textFxButton : null;

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setEqFxButtonEnabled(info.isEqEnabled, info.isFxEnabled);view.setEqButton(info.textEqButton);
            view.setFxButton(info.textFxButton);
            if(showText != null) {
                view.displayEqFxMessage(showText);
            }
        });
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
            view.setBandList(mDabBandTypes);
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
            AppStatus appStatus = mStatusHolder.execute().getAppStatus();
            boolean notificationQueued = false;
            if(mPreference.getVoiceRecognitionType()== VoiceRecognizeType.ALEXA){
                notificationQueued = mStatusHolder.execute().getAppStatus().alexaNotification;
            }
            view.setAlexaNotification(notificationQueued);
        });
    }

    /**
     * PCH選局
     */
    public void onSelectPreset(int presetNum){
        mControlCase.callPreset(presetNum);
        //PCH切り換えで車載器側でTimeshift Modeは解除される
    }

    private void getBandList() {
        mDabBandTypes.clear();
        mDabBandTypes.add(DabBandType.BAND1);
        mDabBandTypes.add(DabBandType.BAND2);
        mDabBandTypes.add(DabBandType.BAND3);
    }

    private void setPresetNumber(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            int presetNumber = -1;
            RadioInfo radioInfo = mStatusHolder.execute().getCarDeviceMediaInfoHolder().radioInfo;
            if (mCurrDab != null&&mDabBand != null) {
                presetNumber = mStatusHolder.execute().getPresetChannelDictionary().findPresetChannelNumber(
                        MediaSourceType.DAB,
                        mDabBand.getCode(),
                        mCurrDab.currentFrequency
                );
            }
/*            presetNumber = mStatusHolder.execute().getPresetChannelDictionary().findPresetChannelNumber(
                    //MediaSourceType.DAB
                    MediaSourceType.RADIO,
                    radioInfo.band.getCode(),
                    radioInfo.currentFrequency
            );*/
            Timber.d("presetNumber=" + presetNumber);
            view.setSelectedPreset(presetNumber);
        });
    }

}
