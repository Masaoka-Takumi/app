package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.SxmInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlSiriusXmSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.util.SxmTextUtil;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.SxmView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * SiriusXM再生画面のpresenter
 */
@PresenterLifeCycle
public class SxmPresenter extends PlayerPresenter<SxmView> implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID_FAVORITE = 0;
    private static final int LOADER_ID_PRESET = 1;
    private static final String KEY_BAND_TYPE = "band_type";

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlSiriusXmSource mControlCase;
    @Inject QueryTunerItem mTunerCase;
    @Inject AppSharedPreference mPreference;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;
    private LoaderManager mLoaderManager;
    private SxmMediaInfo mCurrSxm;
    private SparseArray<Long> mFavorites = new SparseArray<>();
    private SxmBandType mSxmBand;

    /**
     * コンストラクタ
     */
    @Inject
    public SxmPresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setColor(mPreference.getUiColor().getResource()));

        super.onTakeView();
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        updateView();
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
     * SXM情報変更通知イベントハンドラ
     *
     * @param event SXM情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSxmInfoChangeEvent(SxmInfoChangeEvent event) {
        showBandChangeNotification();
        updateView();
    }

    private void showBandChangeNotification(){
        SxmBandType bandType = mStatusHolder.execute().getCarDeviceMediaInfoHolder().sxmMediaInfo.band;
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(bandType != mSxmBand) {
                view.displayEqFxMessage(mContext.getString(bandType.getLabel()));
                mSxmBand = bandType;
            }
        });
    }

    /**
     * 画面更新
     */
    private void updateView() {
        StatusHolder holder = mStatusHolder.execute();
        mCurrSxm = holder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
        mSxmBand = holder.getCarDeviceMediaInfoHolder().sxmMediaInfo.band;
        boolean isScan = mCurrSxm.tunerStatus == TunerStatus.SCAN;
        ListType type = mStatusHolder.execute().getCarDeviceStatus().listType;
        getPresetChannel();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setBand(SxmTextUtil.getBandName(mCurrSxm));
            view.setChannelNumber(SxmTextUtil.getCurrentChannel(mCurrSxm));
            view.setChannelName(SxmTextUtil.getChannelName(mCurrSxm));
            view.setMusicInfo(SxmTextUtil.getSongTitle(mCurrSxm),SxmTextUtil.getArtistName(mCurrSxm),SxmTextUtil.getCategoryName(mCurrSxm));
            view.setMaxProgress(mCurrSxm.totalBufferTime);
            view.setCurrentProgress(mCurrSxm.currentPosition);
            view.setSecondaryProgress(mCurrSxm.currentBufferTime);
            view.setAntennaLevel((float) mCurrSxm.antennaLevel / mCurrSxm.maxAntennaLevel);
            view.setReplayMode(mCurrSxm.inReplayMode);
            view.setSxmIcon(!mCurrSxm.isCheckTuner());
            view.setPlaybackMode(mCurrSxm.playbackMode);
            view.setTuneMix(mCurrSxm.inTuneMix);
            view.setTuneMixEnabled(mCurrSxm.tuneMixAvailable);
            view.setTuneMixVisibility(!isScan && holder.getCarDeviceSpec().tuneMixSupported);
            if(mCurrSxm.inReplayMode){
                view.setChButtonEnabled(!isScan);
                view.setLiveButtonEnabled(!isScan);
            } else {
                view.setReplayButtonEnabled(mCurrSxm.replayModeAvailable);
                view.setReplayButtonVisibility(!isScan);
                view.setFavoriteVisibility(mCurrSxm.isFavoriteAvailable());
            }
            view.setAdasEnabled((mPreference.isAdasEnabled()&&mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN&&(mStatusHolder.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING))||mPreference.isAdasPseudoCooperation());

            view.setListEnabled(type != ListType.LIST_UNAVAILABLE);
        });
        //Mode設定後
        getFavorite();
        setAdasIcon();
    }

    /**
     * 次のプリセット番号へ
     */
    public void onNextPresetAction() {
		//Error「Check Antenna」時操作不可
        if(!mCurrSxm.inReplayMode&&!mCurrSxm.isCheckAntenna()) {
            Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.PCH_UP));
            mControlCase.presetUp();
        }
    }

    /**
     * 前のプリセット番号へ
     */
    public void onPreviousPresetAction() {
		//Error「Check Antenna」時操作不可
        if(!mCurrSxm.inReplayMode&&!mCurrSxm.isCheckAntenna()) {
            Optional.ofNullable(getView()).ifPresent(view -> view.showGesture(GestureType.PCH_DOWN));
            mControlCase.presetDown();
        }
    }

    /**
     * BAND切替
     */
    public void onToggleBandAction() {
        mControlCase.toggleBand();
    }

    /**
     * お気に入り登録/解除
     */
    public void onFavoriteAction() {
        if (mCurrSxm.getBand() != null) {
            long id = getId(mCurrSxm.sid);
            if (id != -1) {
                mTunerCase.unregisterFavorite(TunerContract.FavoriteContract.DeleteParamsBuilder.createParams(id));
            } else {
                mTunerCase.registerFavorite(TunerContract.FavoriteContract.UpdateParamsBuilder.createSiriusXm(mCurrSxm, mContext));
            }
        }
    }

    /**
     * ジャケット押下処理.
     */
    public void onClickJacketAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (!mCurrSxm.inReplayMode) {
				//Error「Check Antenna」時操作不可
                if(mCurrSxm.isCheckAntenna())return;
                if (mCurrSxm.tunerStatus == TunerStatus.SCAN) {
                    view.showGesture(GestureType.SCAN_UP);
                    mControlCase.scanUp();
                }else{
                    if (mCurrSxm.playbackMode == PlaybackMode.PAUSE) {
                        view.showGesture(GestureType.PLAY);
                        mControlCase.togglePlay();
                    } else {
                        view.showGesture(GestureType.CHANNEL_UP);
                        mControlCase.channelUp();
                    }
                }
            } else {
                if (mCurrSxm.tunerStatus == TunerStatus.SCAN) {
                    view.showGesture(GestureType.SCAN_UP);
                    mControlCase.scanUp();
                } else {
                    if (!mCurrSxm.isCh000() && !mCurrSxm.isErrorStatus()) {
                        if (mCurrSxm.playbackMode == PlaybackMode.PAUSE) {
                            view.showGesture(GestureType.PLAY);
                        }
                        mControlCase.togglePlay();
                    }
                }
            }
        });
    }

    /**
     * ジャケット長押し処理
     */
    public void onLongClickJacketAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (!mCurrSxm.inReplayMode) {
				//Error「Check Antenna」時操作不可
                if(mCurrSxm.isCheckAntenna())return;
                if (mCurrSxm.tunerStatus == TunerStatus.SCAN) {
                    view.showGesture(GestureType.SCAN_DOWN);
                    mControlCase.scanDown();
                }else{
                    if (mCurrSxm.playbackMode == PlaybackMode.PAUSE) {
                        view.showGesture(GestureType.PLAY);
                        mControlCase.togglePlay();
                    } else {
                        view.showGesture(GestureType.CHANNEL_DOWN);
                        mControlCase.channelDown();
                    }
                }
            } else {
                if (mCurrSxm.tunerStatus == TunerStatus.SCAN) {
                    view.showGesture(GestureType.SCAN_DOWN);
                    mControlCase.scanDown();
                } else {
                    if (!mCurrSxm.isCh000() && !mCurrSxm.isErrorStatus()) {
                        if(mCurrSxm.playbackMode == PlaybackMode.PAUSE){
                            view.showGesture(GestureType.PLAY);
                        }
                        mControlCase.togglePlay();
                    }
                }
            }
        });
    }

    /**
     * リプレイモード切替
     */
    public void onReplayAction() {
        mControlCase.toggleReplayMode();
    }

    /**
     * TuneMix
     */
    public void onTuneMixAction() {
        mControlCase.toggleTuneMix();
    }

    /**
     * LiveMode切替
     */
    public void onLiveAction() {
        mControlCase.toggleLiveMode();
    }

    /**
     * ChannelMode切替
     */
    public void onChannelAction() {
        mControlCase.toggleChannelMode();
    }

    /**
     * 購読更新ダイアログ承認
     */
    public void onReleaseSubscription() {
        mControlCase.releaseSubscriptionUpdating();
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_FAVORITE) {
            return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createSiriusXm());
        } else if (id == LOADER_ID_PRESET) {
            return mTunerCase.getPresetList(MediaSourceType.SIRIUS_XM, SxmBandType.valueOf(args.getByte(KEY_BAND_TYPE)));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (id == LOADER_ID_FAVORITE) {
                createFavoriteList(data);
                boolean isFavorite = isContainsFavorite(mCurrSxm.sid);
                view.setFavorite(isFavorite);
            } else {
                SparseIntArray presetList = createPresetList(data);
                boolean isPreset = false;
                for (int i = 0; i < presetList.size(); i++) {
                    int key = presetList.keyAt(i);
                    // get the object by the key.
                    Integer number = presetList.get(key);
                    if (number.equals(mCurrSxm.currentChannelNumber)) {
                        isPreset = true;
                        view.setPch(key);
                        break;
                    }
                }
                if (!isPreset) {
                    view.setPch(-1);
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
        if (mSxmBand != null && mLoaderManager != null) {
            Bundle args = new Bundle();
            args.putByte(KEY_BAND_TYPE, (byte) (mSxmBand.getCode() & 0xFF));
            mLoaderManager.restartLoader(LOADER_ID_PRESET, args, this);
        }
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
            int sid = TunerContract.FavoriteContract.SiriusXm.getSid(data);
            long id = TunerContract.FavoriteContract.SiriusXm.getId(data);

            if (!isContainsFavorite(sid)) {
                mFavorites.put(sid, id);
            }
        }
    }

    /**
     * お気に入り判定
     *
     * @param sid sid
     * @return お気に入り登録済みか否か
     */
    private boolean isContainsFavorite(int sid) {
        return (getId(sid) != -1L);
    }

    /**
     * お気に入りID取得
     * <p>
     * 前提として{@link SxmPresenter#isContainsFavorite}がtrueであること
     * お気に入り登録されていない場合は-1を返す
     *
     * @param sid sid
     * @return お気に入りID
     */
    private long getId(int sid) {
        Long id = mFavorites.get(sid);

        return id == null ? -1 : id;
    }

    private SparseIntArray createPresetList(Cursor data) {
        SparseIntArray preset = new SparseIntArray();

        boolean isEof = data.moveToFirst();
        while (isEof) {
            SxmBandType band = TunerContract.ListItemContract.SiriusXm.getBandType(data);
            if (mSxmBand == band) {
                int pch = TunerContract.ListItemContract.SiriusXm.getPchNumber(data);
                Integer number = TunerContract.ListItemContract.SiriusXm.getChNumber(data);

                preset.put(pch, number);
            }
            isEof = data.moveToNext();
        }

        return preset;
    }

    // MARK - EQ FX

    @Override
    void onUpdateSoundFxButton() {
        SoundFxButtonInfo info = getSoundFxButtonInfo();
        String showText = info.isShowEqMessage ? info.textEqButton : info.isShowFxMessage ? info.textFxButton : null;

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setEqFxButtonEnabled(info.isEqEnabled,info.isFxEnabled);
            view.setEqButton(info.textEqButton);
            view.setFxButton(info.textFxButton);
            if(showText != null) {
                view.displayEqFxMessage(showText);
            }
        });
    }

    /**
     * AdasErrorEventハンドラ
     * @param event AdasErrorEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdasErrorEvent(AdasErrorEvent event) {
        setAdasIcon();
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
            view.setShortCutButtonEnabled(mShortCutKeyEnabledStatus.isShortCutKeyEnabled());
            view.setShortcutKeyItems(mShortCutKeyList);
        });
    }

    @Override
    protected void updateNotification() {
        super.updateNotification();
        Optional.ofNullable(getView()).ifPresent(view -> view.setShortcutKeyItems(mShortCutKeyList));
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
}
