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

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.LocationMeshCodeChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ActionSoftwareShortcutKey;
import jp.pioneer.carsync.domain.interactor.ControlRadioSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferRadioFunction;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.interactor.SelectRadioFavorite;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceDestinationInfo;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.PtySearchSetting;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.RdsInterruptionType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.TunerFunctionSetting;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.CloseDialogEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.GestureType;
import jp.pioneer.carsync.presentation.util.FrequencyUtil;
import jp.pioneer.carsync.presentation.util.RadioTextUtil;
import jp.pioneer.carsync.presentation.util.ShortCutKeyEnabledStatus;
import jp.pioneer.carsync.presentation.view.RadioView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;

/**
 * ラジオ再生画面のpresenter
 */
@PresenterLifeCycle
public class RadioPresenter extends PlayerPresenter<RadioView> implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID_FAVORITE = 0;
    private static final int LOADER_ID_PRESET = 1;
    private static final int LOADER_ID_USER_PRESET = 2;
    private static final String KEY_BAND_TYPE = "band_type";
    public static final String TAG_BSM = "tag_bsm";
    public static final String TAG_PTY_SEARCH = "tag_pty_search";
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlRadioSource mControlCase;
    @Inject QueryTunerItem mTunerCase;
    @Inject AppSharedPreference mPreference;
    @Inject ActionSoftwareShortcutKey mShortcutCase;
    @Inject SelectRadioFavorite mRadioCase;
    @Inject PreferRadioFunction mPreferRadioFunction;
    @Inject ShortCutKeyEnabledStatus mShortCutKeyEnabledStatus;
    private LoaderManager mLoaderManager;
    private RadioInfo mCurrRadio;
    private LongSparseArray<Long> mFavorites = new LongSparseArray<>();
    private RadioBandType mRadioBand;
    private TunerStatus mTunerStatus = null;
    private ArrayList<BandType> mRadioBandTypes = new ArrayList<>();
    private SparseArray<String> mPresets = new SparseArray<>();
    private SparseArray<Long> mUserPreset = new SparseArray<>();
    private Cursor mUserPresetCursor=null;
    private int mSelectedPreset = 0;
    private RdsInterruptionType mInterruptionType=null;
    private int mPagerPosition=-1;
    /**
     * コンストラクタ
     */
    @Inject
    public RadioPresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setColor(mPreference.getUiColor().getResource()));
        getBandList();
        StatusHolder holder = mStatusHolder.execute();
        mCurrRadio = holder.getCarDeviceMediaInfoHolder().radioInfo;
        mInterruptionType=null;
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
     * ラジオ情報変更通知イベントハンドラ
     *
     * @param event ラジオ情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioInfoChangeEvent(RadioInfoChangeEvent event) {
        updateView();
    }

    /**
     * ラジオ設定ステータス変更通知イベントハンドラ
     *
     * @param event ラジオ設定ステータス変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioFunctionSettingStatusChangeEvent(RadioFunctionSettingStatusChangeEvent event) {
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

    /**
     * LocationMeshCodeChangeEventハンドラ
     * @param event LocationMeshCodeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationMeshCodeChangeEvent(LocationMeshCodeChangeEvent event) {
        updateView();
    }

    private void showBandChangeNotification(){
        RadioBandType bandType = mStatusHolder.execute().getCarDeviceMediaInfoHolder().radioInfo.band;
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(bandType!=null&&mRadioBand!=null&&bandType != mRadioBand
                    &&!mCurrRadio.isSearchStatus()) {
                view.displayEqFxMessage(mContext.getString(bandType.getLabel()));
                mRadioBand = bandType;
            }
        });
    }

    public void showPtySelect(){
        Bundle bundle = new Bundle();
        bundle.putString(SingleChoiceDialogFragment.TITLE, mContext.getResources().getString(R.string.ply_042));
        bundle.putStringArray(SingleChoiceDialogFragment.DATA, mContext.getResources().getStringArray(R.array.radio_pty_search_values));     // Require ArrayList
        bundle.putInt(SingleChoiceDialogFragment.SELECTED, 0);
        mEventBus.post(new NavigateEvent(ScreenId.RADIO_PTY_SELECT, bundle));
    }

    /**
     * 次のプリセット番号へ
     */
    public void onNextPresetAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(!mCurrRadio.isSearchStatus()) {
                TunerFunctionSetting setting = mStatusHolder.execute().getTunerFunctionSetting();
                if(setting.pchManualSetting == PCHManualSetting.MANUAL||isSphCarDevice()){
                    view.showGesture(GestureType.MANUAL_UP);
                } else {
                    view.showGesture(GestureType.PCH_UP);
                }
                mControlCase.channelUp();
            }
        });
    }

    /**
     * 前のプリセット番号へ
     */
    public void onPreviousPresetAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(!mCurrRadio.isSearchStatus()) {
                TunerFunctionSetting setting = mStatusHolder.execute().getTunerFunctionSetting();
                if(setting.pchManualSetting == PCHManualSetting.MANUAL||isSphCarDevice()){
                    view.showGesture(GestureType.MANUAL_DOWN);
                } else {
                    view.showGesture(GestureType.PCH_DOWN);
                }
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
        switch(mCurrRadio.tunerStatus){
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
        long id = getId(mCurrRadio.currentFrequency);
        //KM818以外と連携中
        if(!isSphCarDevice()) {
            if (id != -1) {
                mTunerCase.unregisterFavorite(TunerContract.FavoriteContract.DeleteParamsBuilder.createParams(id));
            } else {
                StatusHolder holder = mStatusHolder.execute();
                CarDeviceStatus status = holder.getCarDeviceStatus();
                String psInfoText = RadioTextUtil.getPsInfoForFavorite(mPreference.getLastConnectedCarDeviceDestination(),holder.getCarRunningStatus(),mCurrRadio);
                mTunerCase.registerFavorite(TunerContract.FavoriteContract.UpdateParamsBuilder.createRadio(mCurrRadio, status.seekStep, mContext, psInfoText));
            }
        }
    }

    /**
     * PTYサーチアクション
     */
    public void onPtySearchAction(int position) {
        PtySearchSetting ptySearchSetting =  PtySearchSetting.valueOf((byte)position);
        mControlCase.startPtySearch(ptySearchSetting);
    }

    /**
     * 画面更新
     */
    private void updateView() {
        showBandChangeNotification();

        StatusHolder holder = mStatusHolder.execute();
        CarDeviceStatus status = holder.getCarDeviceStatus();

        mCurrRadio = holder.getCarDeviceMediaInfoHolder().radioInfo;
        mRadioBand = holder.getCarDeviceMediaInfoHolder().radioInfo.band;

        getFavorite();
        getPresetChannel();
        getUserPresetList();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mCurrRadio.rdsInterruptionType!=null) {
                if(mInterruptionType!=mCurrRadio.rdsInterruptionType) {
                    updateShortcutButton();
                    onUpdateSoundFxButton();
                }
                view.setRdsInterruption(mCurrRadio.rdsInterruptionType, RadioTextUtil.getPsInfoForPlayer(mContext, mPreference.getLastConnectedCarDeviceDestination(),holder.getCarRunningStatus(), mCurrRadio));
                mInterruptionType = mCurrRadio.rdsInterruptionType;
            }
            if (mTunerStatus != mCurrRadio.tunerStatus) {
                switch (mCurrRadio.tunerStatus){
                    case BSM:
                        mEventBus.post(new CloseDialogEvent(ScreenId.SOURCE_SELECT));
                        view.showStatusView(true, RadioPresenter.TAG_BSM);
                        break;
                    case PTY_SEARCH:
                        mEventBus.post(new CloseDialogEvent(ScreenId.SOURCE_SELECT));
                        view.showStatusView(true, RadioPresenter.TAG_PTY_SEARCH);
                        break;
                    case SEEK:
                        mEventBus.post(new CloseDialogEvent(ScreenId.RADIO_PTY_SELECT));
                    default:
                        view.showStatusView(false,null);
                        break;
                }

                mTunerStatus = mCurrRadio.tunerStatus;
            }
            view.setPtySearchEnabled(holder.getTunerFunctionSettingStatus().ptySearchSettingEnabled);
			view.setViewPagerCurrentPage(mRadioBand==RadioBandType.LW?RadioBandType.MW:mRadioBand);
            view.setBand(mRadioBand);
            if (mCurrRadio.isSearchStatus()) {
                view.setFrequency(null);
                view.setFavoriteVisible(false);
                view.setPch(-1);
            } else {
                view.setFrequency(FrequencyUtil.toString(mContext, mCurrRadio.currentFrequency, mCurrRadio.frequencyUnit));
                view.setFavoriteVisible(!isSphCarDevice());
            }
            view.setStatus(mCurrRadio.tunerStatus);
            String psInfoText = RadioTextUtil.getPsInfoForPlayer(mContext, mPreference.getLastConnectedCarDeviceDestination(),holder.getCarRunningStatus(), mCurrRadio);
            view.setPsInformation(psInfoText);
            view.setMusicInfo(psInfoText,(String) RadioTextUtil.getPtyInfoForPlayer(mContext, mCurrRadio),RadioTextUtil.getArtistNameForPlayer(mContext, mCurrRadio));
            view.setAntennaLevel((float) mCurrRadio.antennaLevel / mCurrRadio.maxAntennaLevel);

            view.setAdasEnabled((mPreference.isAdasEnabled()&&mPreference.getLastConnectedCarDeviceClassId()!= CarDeviceClassId.MARIN&&(mStatusHolder.execute().getAppStatus().adasPurchased||mPreference.getAdasTrialState() == AdasTrialState.TRIAL_DURING))||mPreference.isAdasPseudoCooperation());
            view.setListEnabled(true);
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
            long frequency = TunerContract.FavoriteContract.Radio.getFrequency(data);
            long id = TunerContract.FavoriteContract.Radio.getId(data);
            mFavorites.put(frequency, id);
        }
    }

    /**
     * お気に入り判定
     *
     * @param band      視聴BAND
     * @param frequency 視聴周波数
     * @return お気に入り登録済みか否か
     */
    private boolean isContainsFavorite(RadioBandType band, long frequency) {
        return (getId(frequency) != -1L);
    }

    /**
     * お気に入りID取得
     * <p>
     * 前提として{@link RadioPresenter#isContainsFavorite}がtrueであること
     * お気に入り登録されていない場合は-1を返す
     *
     * @param frequency 視聴周波数
     * @return お気に入りID
     */
    private long getId(long frequency) {
        return mFavorites.get(frequency, -1L);
    }

    private void createPresetList(Cursor data) {
        mPresets.clear();
        boolean isEof = data.moveToFirst();
        while (isEof) {
            RadioBandType band = TunerContract.ListItemContract.Radio.getBandType(data);
            if (mRadioBand == band) {
                int pch = TunerContract.ListItemContract.Radio.getPchNumber(data);
                //TODO 周波数(番組名が取得できるようになる?)
                long frequency = TunerContract.ListItemContract.Radio.getFrequency(data);
                TunerFrequencyUnit unit = TunerContract.ListItemContract.Radio.getFrequencyUnit(data);
                String name = FrequencyUtil.toString(mContext, frequency, unit);

                mPresets.put(pch, name);
            }
            isEof = data.moveToNext();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_FAVORITE) {
            return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createRadio());
        } else if (id == LOADER_ID_PRESET) {
            return mTunerCase.getPresetList(MediaSourceType.RADIO, RadioBandType.valueOf(args.getByte(KEY_BAND_TYPE)));
        } else if(id == LOADER_ID_USER_PRESET){
            return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createRadioPreset(RadioBandType.valueOf(args.getByte(KEY_BAND_TYPE))));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mCurrRadio!=null) {
                if (id == LOADER_ID_FAVORITE) {
                    createFavoriteList(data);
                    boolean isFavorite = isContainsFavorite(mCurrRadio.getBand(), mCurrRadio.currentFrequency);
                    view.setFavorite(isFavorite);
                } else if(id == LOADER_ID_PRESET){
                    createPresetList(data);
                    setSelectedPreset();
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
        if(!isSphCarDevice()) {
            if (mLoaderManager != null) {
                mLoaderManager.initLoader(LOADER_ID_FAVORITE, Bundle.EMPTY, this);
            }
        }
    }

    private void getPresetChannel() {
        mPresets.clear();
        if (mRadioBand != null && mLoaderManager != null) {
            Bundle args = new Bundle();
            args.putByte(KEY_BAND_TYPE, (byte) (mRadioBand.getCode() & 0xFF));
            mLoaderManager.restartLoader(LOADER_ID_PRESET, args, this);
        }
    }

    // MARK - EQ FX

    @Override
    void onUpdateSoundFxButton() {
        SoundFxButtonInfo info = getSoundFxButtonInfo();
        String showText = info.isShowEqMessage ? info.textEqButton : info.isShowFxMessage ? info.textFxButton : null;

        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mCurrRadio.rdsInterruptionType != null && mCurrRadio.rdsInterruptionType != RdsInterruptionType.NORMAL){
                view.setEqFxButtonEnabled(false, false);
            } else {
                view.setEqFxButtonEnabled(info.isEqEnabled, info.isFxEnabled);
            }
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
            view.setShortCutButtonEnabled(mCurrRadio.rdsInterruptionType==null
                                        ||mCurrRadio.rdsInterruptionType == RdsInterruptionType.NORMAL
                                        ||mShortCutKeyEnabledStatus.isShortCutKeyEnabled());
            if(mShortCutKeyEnabledStatus.isShortCutKeyEnabled()) {
                view.setShortcutKeyItems(mShortCutKeyList);
            }
            if(mCurrRadio.rdsInterruptionType==null||mCurrRadio.rdsInterruptionType == RdsInterruptionType.NORMAL) {
                view.setBandList(mRadioBandTypes);
            }
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
        //Seek中/BSM中/PTY Search中ではなく、連携中の車載器がKM818モデルの場合PCH登録
        if(!mCurrRadio.isSearchStatus()&&isSphCarDevice()) {
        	StatusHolder holder = mStatusHolder.execute();
        	CarDeviceStatus status = holder.getCarDeviceStatus();
            String psInfoText = RadioTextUtil.getPsInfoForFavorite(mPreference.getLastConnectedCarDeviceDestination(),holder.getCarRunningStatus(),mCurrRadio);
            mTunerCase.registerFavorite(TunerContract.FavoriteContract.UpdateParamsBuilder.createRadioPreset(mCurrRadio, presetNumber, status.seekStep, mContext,psInfoText));
            saveLastAmStepSetting();
        }
    }

    /**
     * ユーザー登録PCHリスト取得
     */
    private void getUserPresetList(){
        mUserPreset.clear();
        if(isSphCarDevice()) {
            if (mRadioBand != null && mLoaderManager != null) {
                Bundle args = new Bundle();
                args.putByte(KEY_BAND_TYPE, (byte) (mRadioBand.getCode() & 0xFF));
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
            long frequency = TunerContract.FavoriteContract.Radio.getFrequency(data);
            int presetNumber = TunerContract.FavoriteContract.Radio.getPresetNumber(data);
            mUserPreset.put(presetNumber, frequency);
        }
    }
    /**
     * PCH選局
     */
    public void onSelectPreset(int presetNum){
        if(isSphCarDevice()) {
            boolean isExist = false;
            if (mUserPresetCursor != null) {
                mUserPresetCursor.moveToPosition(-1);
                while (mUserPresetCursor.moveToNext()) {
                    int presetNumber = TunerContract.FavoriteContract.Radio.getPresetNumber(mUserPresetCursor);
                    if (presetNumber == presetNum) {
                        isExist = true;
                        break;
                    }
                }
            }
            //専用機で登録PCHがあればお気に入り選局
            if (isExist) {
                mRadioCase.execute(
                        TunerContract.FavoriteContract.Radio.getIndex(mUserPresetCursor),
                        TunerContract.FavoriteContract.Radio.getBandType(mUserPresetCursor),
                        TunerContract.FavoriteContract.Radio.getPi(mUserPresetCursor));
            } else {
                //上記以外:プリセットキー1～プリセットキー6を送信
                mControlCase.callPreset(presetNum);
            }
        }else{
            mControlCase.callPreset(presetNum);
        }
        mSelectedPreset = presetNum;
    }
    
    /**
     * 仕向け毎のBandリスト取得
     */
    private void getBandList() {
        mRadioBandTypes.clear();
        CarDeviceDestinationInfo carDeviceDestinationInfo = mStatusHolder.execute().getCarDeviceSpec().carDeviceDestinationInfo;
        switch(carDeviceDestinationInfo){
            case EW5:
                mRadioBandTypes.add(RadioBandType.FM1);
                mRadioBandTypes.add(RadioBandType.FM2);
                mRadioBandTypes.add(RadioBandType.FM3);
                mRadioBandTypes.add(RadioBandType.MW);
                //LWはMWと同Pageを表示する
                break;
            case UC:
            case CS:
            case BR:
            case ME:
                mRadioBandTypes.add(RadioBandType.FM1);
                mRadioBandTypes.add(RadioBandType.FM2);
                mRadioBandTypes.add(RadioBandType.FM3);
                mRadioBandTypes.add(RadioBandType.AM);
                break;
            case ES:
            case GS:
            case ID:
                mRadioBandTypes.add(RadioBandType.FM1);
                mRadioBandTypes.add(RadioBandType.FM2);
                mRadioBandTypes.add(RadioBandType.FM3);
                mRadioBandTypes.add(RadioBandType.AM);
                mRadioBandTypes.add(RadioBandType.SW1);
                mRadioBandTypes.add(RadioBandType.SW2);
                break;
            case JP:
                mRadioBandTypes.add(RadioBandType.FM1);
                mRadioBandTypes.add(RadioBandType.FM2);
                mRadioBandTypes.add(RadioBandType.AM1);
                mRadioBandTypes.add(RadioBandType.AM2);
                break;
        }
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
            for (int i = 0; i < mPresets.size(); i++) {
                int key = mPresets.keyAt(i);
                String name = mPresets.get(key);
                if(mUserPreset.get(key,-1L)==-1L) {
                    if (name.equals(FrequencyUtil.toString(mContext, mCurrRadio.currentFrequency, mCurrRadio.frequencyUnit))) {
                        isPreset = true;
                        presetNum = key;
                        break;
                    }
                }
            }
            //②P.CH登録データで周波数(extraData)が一致するP.CH番号(tunerUniqueID)を調べる
            for (int i = 0; i < mUserPreset.size(); i++) {
                int key = mUserPreset.keyAt(i);
                long frequency = mUserPreset.get(key);
                if (frequency == mCurrRadio.currentFrequency) {
                    isUserPreset = true;
                    userPresetNum = key;
                    break;
                }
            }
            if(isPreset&&isUserPreset) {
                //①と②の両方で見つかった場合、値の小さい方をP.CH番号とする
                mSelectedPreset = Math.min(presetNum, userPresetNum);
                view.setPch(mSelectedPreset);
                view.setSelectedPreset(mSelectedPreset);
            }else if(isPreset){
                //①のみで見つかった場合、P.CH登録データに同じP.CH番号を登録していなければ①をP.CH番号とする
                if(mUserPreset.get(presetNum,-1L)==-1L){
                    mSelectedPreset = presetNum;
                    view.setPch(mSelectedPreset);
                    view.setSelectedPreset(mSelectedPreset);
                }else{
                    mSelectedPreset = 0;
                    view.setPch(-1);
                    view.setSelectedPreset(-1);
                }
            }else if(isUserPreset){
                //②のみで見つかった場合、②をP.CH番号とする
                mSelectedPreset = userPresetNum;
                view.setPch(mSelectedPreset);
                view.setSelectedPreset(mSelectedPreset);
            }else{
                //いずれも該当しない場合、P.CH番号なしとする
                mSelectedPreset = 0;
                view.setPch(-1);
                view.setSelectedPreset(-1);
            }
        });
    }

    private void saveLastAmStepSetting(){
        //専用機のAM SEEK STEPを記憶
        if(isSphCarDevice()&&mRadioBand.isAMVariant()){
            StatusHolder holder = mStatusHolder.execute();
            CarDeviceStatus status = holder.getCarDeviceStatus();
            if(status.seekStep!=null) {
                mPreference.setLastConnectedCarDeviceAmStep(status.seekStep);
            }
        }
    }

    private boolean isSphCarDevice(){
        return mStatusHolder.execute().getProtocolSpec().isSphCarDevice();
    }

}
