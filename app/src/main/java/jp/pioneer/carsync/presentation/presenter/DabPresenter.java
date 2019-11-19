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
import jp.pioneer.carsync.domain.event.DabFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.DabInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlDabSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.interactor.SelectDabFavorite;
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
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.util.DabTextUtil;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.view.DabView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

public class DabPresenter extends PlayerPresenter<DabView> implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID_FAVORITE = 0;
    private static final int LOADER_ID_USER_PRESET = 2;
    private static final String KEY_BAND_TYPE = "band_type";
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlDabSource mControlCase;
    @Inject QueryTunerItem mTunerCase;
    @Inject AppSharedPreference mPreference;
    @Inject CarDeviceMediaRepository mCarDeviceMediaRepository;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;
    @Inject SelectDabFavorite mDabCase;
    private LoaderManager mLoaderManager;
    private DabInfo mCurrDab;
    private LongSparseArray<Long> mFavorites = new LongSparseArray<>();
    private DabBandType mDabBand;
    private TunerStatus mTunerStatus = null;
    private ArrayList<BandType> mDabBandTypes = new ArrayList<>();
    private Cursor mFavoritesCursor=null;
    private SparseArray<Long> mUserPreset = new SparseArray<>();
    private Cursor mUserPresetCursor=null;
    private int mSelectedPreset = 0;
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
            if(bandType!=null&&mDabBand!=null&&bandType != mDabBand
                    &&!mCurrDab.isSearchStatus()) {
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
                if(isSphCarDevice()) {
                    int nextPreset = mSelectedPreset + 1;
                    if (nextPreset > 6) {
                        nextPreset = 1;
                    }
                    onSelectPreset(nextPreset);
                }else {
                    mControlCase.presetUp();
                }
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
                if(isSphCarDevice()) {
                    int previousPreset = mSelectedPreset - 1;
                    if (previousPreset <= 0) {
                        previousPreset = 6;
                    }
                    onSelectPreset(previousPreset);
                }else {
                    mControlCase.presetDown();
                }
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
        getUserPresetList();
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
        } else if(id == LOADER_ID_USER_PRESET){
            return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createDabPreset(DabBandType.valueOf(args.getByte(KEY_BAND_TYPE))));
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
                }else if(id == LOADER_ID_USER_PRESET){
                    createUserPresetList(data);
                    setSelectedPreset();
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

    private void makeDummyInfo(){
        mCurrDab.minimumFrequency = 0;
        mCurrDab.maximumFrequency = 0;
        mCurrDab.currentFrequency = 200L;
        mCurrDab.frequencyUnit = TunerFrequencyUnit.KHZ;
        mCurrDab.tunerStatus = TunerStatus.NORMAL;
        mCurrDab.band = DabBandType.BAND1;
        mCurrDab.eid = 1;
        mCurrDab.sid = 2;
        mCurrDab.scids = 3;
        mCurrDab.serviceComponentLabel = "test";
        mCurrDab.dynamicLabel = "test";
        mCurrDab.ptyInfo  = "test";
        mCurrDab.serviceNumber = "1/2";
        mCurrDab.timeShiftModeAvailable = false;
        mCurrDab.timeShiftMode = false;
        mCurrDab.playbackMode = PlaybackMode.PAUSE;
        mCurrDab.totalBufferTime = 0;
        mCurrDab.currentPosition = 0;
        mCurrDab.currentBufferTime = 0;
        mDabBand = DabBandType.BAND1;
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
        //makeDummyInfo();

        getFavorite();
        getUserPresetList();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (mTunerStatus != mCurrDab.tunerStatus) {

                mTunerStatus = mCurrDab.tunerStatus;
            }
            view.setViewPagerCurrentPage(mDabBand);
            view.setBand(mCurrDab.band);
            if (mCurrDab.isSearchStatus()||mCurrDab.isErrorStatus()) {
                view.setFavoriteVisible(false);
                view.setServiceNumber("");
            }else {
                view.setFavoriteVisible(!mCurrDab.timeShiftMode&&!isSphCarDevice());
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
     * PCH登録
     */
    public void onRegisterPresetChannel(int presetNumber) {
        //Error中/検索状態時（Seek中＆Service List Update中）ではなく、連携中の車載機がKM997モデルの場合PCH登録
        if(!mCurrDab.isErrorStatus()&&!mCurrDab.isSearchStatus()&&isSphCarDevice()) {
            mTunerCase.registerFavorite(TunerContract.FavoriteContract.UpdateParamsBuilder.createDabPreset(mCurrDab, presetNumber, mContext));
        }
    }
    /**
     * ユーザー登録PCHリスト取得
     */
    public void getUserPresetList(){
        if(isSphCarDevice()) {
            if (mDabBand != null && mLoaderManager != null) {
                Bundle args = new Bundle();
                args.putByte(KEY_BAND_TYPE, (byte) (mDabBand.getCode() & 0xFF));
                mLoaderManager.restartLoader(LOADER_ID_USER_PRESET, args, this);
            }
        }
    }
    /**
     * PCH登録リスト作成
     * <p>
     * カーソルローダではデータの照合を行えないため、リストに起こす
     *
     * @param data PCH登録情報カーソル
     */
    private void createUserPresetList(Cursor data) {
        mUserPresetCursor = data;
        mUserPreset.clear();
        data.moveToPosition(-1);
        while (data.moveToNext()) {
            long frequency = TunerContract.FavoriteContract.Dab.getFrequency(data);
            int presetNumber = TunerContract.FavoriteContract.Dab.getPresetNumber(data);
            mUserPreset.put(presetNumber, frequency);
        }
    }

    /**
     * PCH選局
     */
    public void onSelectPreset(int presetNum){
        //PCH切り換えで車載器側でTimeshift Modeは解除される

        if(isSphCarDevice()) {
            boolean isExist = false;
            if (mUserPresetCursor != null) {
                mUserPresetCursor.moveToPosition(-1);
                while (mUserPresetCursor.moveToNext()) {
                    int presetNumber = TunerContract.FavoriteContract.Dab.getPresetNumber(mUserPresetCursor);
                    if (presetNumber == presetNum) {
                        isExist = true;
                        break;
                    }
                }
            }
            //専用機で登録PCHがあればお気に入り選局
            if (isExist) {
                mDabCase.selectFavorite(
                        TunerContract.FavoriteContract.Dab.getIndex(mUserPresetCursor),
                        TunerContract.FavoriteContract.Dab.getBandType(mUserPresetCursor),
                        TunerContract.FavoriteContract.Dab.getEid(mUserPresetCursor),
                        TunerContract.FavoriteContract.Dab.getSid(mUserPresetCursor),
                        TunerContract.FavoriteContract.Dab.getScids(mUserPresetCursor));
            } else {
                //上記以外:プリセットキー1～プリセットキー6を送信
                mControlCase.callPreset(presetNum);
            }
        }else{
            mControlCase.callPreset(presetNum);
        }
        mSelectedPreset = presetNum;
    }

    private void getBandList() {
        mDabBandTypes.clear();
        mDabBandTypes.add(DabBandType.BAND1);
        mDabBandTypes.add(DabBandType.BAND2);
        mDabBandTypes.add(DabBandType.BAND3);
    }

    /**
     * 現在のプリセット番号設定
     */
    private void setSelectedPreset(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            int presetNum=0;
            int userPresetNum=0;
            boolean isPreset = false;
            boolean isUserPreset = false;
            //①従来通りの方法でP.CH番号を調べる(P.CH登録データに登録済みのP.CH番号は除く)
            int presetNumber = -1;
            if (mCurrDab != null&&mDabBand != null) {
                presetNumber = mStatusHolder.execute().getPresetChannelDictionary().findPresetChannelNumber(
                        MediaSourceType.DAB,
                        mDabBand.getCode(),
                        mCurrDab.currentFrequency
                );
                Timber.d("presetNumber=" + presetNumber);
                if(presetNumber!=-1&&mUserPreset.get(presetNumber,-1L)==-1L){
                    isPreset = true;
                    presetNum = presetNumber;
                }
            }
            //②P.CH登録データで周波数(extraData)が一致するP.CH番号(tunerUniqueID)を調べる
            for (int i = 0; i < mUserPreset.size(); i++) {
                int key = mUserPreset.keyAt(i);
                long frequency = mUserPreset.get(key);
                if (frequency == mCurrDab.currentFrequency) {
                    isUserPreset = true;
                    userPresetNum = key;
                    break;
                }
            }
            if(isPreset&&isUserPreset) {
                //①と②の両方で見つかった場合、値の小さい方をP.CH番号とする
                mSelectedPreset = Math.min(presetNum, userPresetNum);
                view.setSelectedPreset(mSelectedPreset);
            }else if(isPreset){
                //①のみで見つかった場合、P.CH登録データに同じP.CH番号を登録していなければ①をP.CH番号とする
                if(mUserPreset.get(presetNum,-1L)==-1L){
                    mSelectedPreset = presetNum;
                    view.setSelectedPreset(mSelectedPreset);
                }else{
                    mSelectedPreset = 0;
                    view.setSelectedPreset(-1);
                }
            }else if(isUserPreset){
                //②のみで見つかった場合、②をP.CH番号とする
                mSelectedPreset = userPresetNum;
                view.setSelectedPreset(mSelectedPreset);
            }else{
                //いずれも該当しない場合、P.CH番号なしとする
                mSelectedPreset = 0;
                view.setSelectedPreset(-1);
            }
        });
    }
    private boolean isSphCarDevice(){
        return mStatusHolder.execute().getProtocolSpec().isSphCarDevice();
    }
}
