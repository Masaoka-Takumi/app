package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.event.DabInfoChangeEvent;
import jp.pioneer.carsync.domain.event.ListInfoChangeEvent;
import jp.pioneer.carsync.domain.event.LocationMeshCodeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlDabSource;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlRadioSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.interactor.SelectDabFavorite;
import jp.pioneer.carsync.domain.interactor.SelectRadioFavorite;
import jp.pioneer.carsync.domain.model.CarDeviceDestinationInfo;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.HdRadioPresetItem;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import jp.pioneer.carsync.domain.util.PresetChannelDictionary;
import jp.pioneer.carsync.infrastructure.crp.event.CrpListUpdateEvent;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.model.AbstractPresetItem;
import jp.pioneer.carsync.presentation.model.DabPresetItem;
import jp.pioneer.carsync.presentation.model.RadioPresetItem;
import jp.pioneer.carsync.presentation.model.SxmPresetItem;
import jp.pioneer.carsync.presentation.util.FrequencyUtil;
import jp.pioneer.carsync.presentation.util.RadioTextUtil;
import jp.pioneer.carsync.presentation.view.RadioPresetView;
import timber.log.Timber;

/**
 * ラジオプリセットリストのpresenter
 */
@PresenterLifeCycle
public class RadioPresetPresenter extends Presenter<RadioPresetView> implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String KEY_BAND_TYPE = "band_type";
    private static final int LOADER_ID_PRESET = 0;
    private static final int LOADER_ID_USER_PRESET = 2;
    @Inject ControlMediaList mMediaCase;
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlRadioSource mControlCase;
    @Inject QueryTunerItem mTunerCase;
    @Inject AppSharedPreference mPreference;
    @Inject CarDeviceMediaRepository mCarDeviceMediaRepository;
    @Inject SelectRadioFavorite mRadioCase;
    @Inject SelectDabFavorite mDabCase;
    private MediaSourceType mSourceType;
    private RadioBandType mRadioBand;
    private SxmBandType mSxmBand;
    private DabBandType mDabBand;
    private HdRadioBandType mHdRadioBand;
    private LoaderManager mLoaderManager;
    private ArrayList<AbstractPresetItem> mUserPreset = new ArrayList<>();
    private Cursor mUserPresetCursor=null;
    private ArrayList<AbstractPresetItem> mRadioPresetList = new ArrayList<>();

    /**
     * コンストラクタ
     */
    @Inject
    public RadioPresetPresenter() {
    }

    @Override
    void onInitialize() {
        mUserPreset.clear();
        mRadioPresetList.clear();
        mSourceType = mStatusHolder.execute().getCarDeviceStatus().sourceType;
    }

    public MediaSourceType getSourceType() {
        return mStatusHolder.execute().getCarDeviceStatus().sourceType;
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setColor(mPreference.getUiColor().getResource()));
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        updatePresetList();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    public boolean isSphCarDevice() {
        return mStatusHolder.execute().getProtocolSpec().isSphCarDevice();
    }

    /**
     * LoaderManager登録
     *
     * @param loaderManager LoaderManager
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
		getUserPresetList();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == LOADER_ID_PRESET) {
            if (mSourceType == MediaSourceType.RADIO) {
                return mTunerCase.getPresetList(MediaSourceType.RADIO, null);
            } else if (mSourceType == MediaSourceType.SIRIUS_XM) {
                return mTunerCase.getPresetList(MediaSourceType.SIRIUS_XM, null);
            } else if (mSourceType == MediaSourceType.HD_RADIO) {
                return mTunerCase.getPresetList(MediaSourceType.HD_RADIO, null);
            }
        } else if(id == LOADER_ID_USER_PRESET){
            if (mSourceType == MediaSourceType.RADIO) {
                return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createRadioPreset());
            }else if (mSourceType == MediaSourceType.DAB) {
                return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createDabPreset(mDabBand));
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (id == LOADER_ID_PRESET) {
            if (mSourceType == MediaSourceType.RADIO || mSourceType == MediaSourceType.SIRIUS_XM || mSourceType == MediaSourceType.HD_RADIO) {
                Optional.ofNullable(getView()).ifPresent(view -> view.setPresetList(createPresetList(data)));
                updateSelected();
            }
        }else if(id == LOADER_ID_USER_PRESET){
            mUserPresetCursor = data;
            createUserPresetList(data);
        }
    }

    private ArrayList<AbstractPresetItem> createPresetList(Cursor data) {
        StatusHolder holder = mStatusHolder.execute();
        ArrayList<AbstractPresetItem> presetList = new ArrayList<>();
        boolean isEof = data.moveToFirst();
        if (mSourceType == MediaSourceType.RADIO) {
            mRadioPresetList.clear();
            while (isEof) {
                RadioBandType band = TunerContract.ListItemContract.Radio.getBandType(data);
                int pch = TunerContract.ListItemContract.Radio.getPchNumber(data);
                //TODO 周波数(番組名が取得できるようになる?)
                long frequency = TunerContract.ListItemContract.Radio.getFrequency(data);
                TunerFrequencyUnit unit = TunerContract.ListItemContract.Radio.getFrequencyUnit(data);
                String freqName = FrequencyUtil.toString(mContext, frequency, unit);
                String name = TunerContract.ListItemContract.Radio.getText(data);
                //連携中の車載機がJP仕向けで位置情報と周波数に対応する放送局名がある場合、放送局名を表示する
                if(mPreference.getLastConnectedCarDeviceDestination()== CarDeviceDestinationInfo.JP.code){
                    name = RadioTextUtil.getPsInfoForListJP(holder.getCarRunningStatus(), band, frequency);
                }
                boolean selected = false;
                RadioInfo currRadio = holder.getCarDeviceMediaInfoHolder().radioInfo;
                if (band==currRadio.getBand()&&freqName.equals(FrequencyUtil.toString(mContext, currRadio.currentFrequency, currRadio.frequencyUnit))) {
                    selected = true;
                }
                RadioPresetItem preset = new RadioPresetItem(band, pch, name, freqName, selected);
                //ユーザー登録Presetがあれば差し替え
                boolean isUserPreset = false;
                for (int i = 0; i < mUserPreset.size(); i++) {
                    RadioPresetItem userPreset = (RadioPresetItem) mUserPreset.get(i);
                    if(band==RadioBandType.LW){
                        band = RadioBandType.MW;
                    }
                    RadioBandType presetBand = userPreset.bandType;
                    if(presetBand==RadioBandType.LW){
                        presetBand=RadioBandType.MW;
                    }
                    //登録用Bandで比較
                    if(presetBand==band && userPreset.presetNumber==pch) {
                        presetList.add(userPreset);
                        isUserPreset = true;
                        break;
                    }
                }
                if(!isUserPreset){
                    presetList.add(preset);
                }
                isEof = data.moveToNext();
            }
            //mRadioPresetListには生BandTypeが入る
            mRadioPresetList = presetList;
        } else if (mSourceType == MediaSourceType.SIRIUS_XM) {
            while (isEof) {
                SxmBandType band = TunerContract.ListItemContract.SiriusXm.getBandType(data);
                int pch = TunerContract.ListItemContract.SiriusXm.getPchNumber(data);
                int number = TunerContract.ListItemContract.SiriusXm.getChNumber(data);
                String name = TunerContract.ListItemContract.SiriusXm.getText(data);
                boolean selected = false;
                SxmMediaInfo currSxm = holder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
                if (band==currSxm.getBand()&&number == currSxm.currentChannelNumber) {
                    selected = true;
                }
                SxmPresetItem preset = new SxmPresetItem(band, pch, name, String.format(Locale.ENGLISH, "CH %03d", number), selected);
                presetList.add(preset);

                isEof = data.moveToNext();
            }
        } else if (mSourceType == MediaSourceType.HD_RADIO) {
            while (isEof) {
                HdRadioBandType band = TunerContract.ListItemContract.HdRadio.getBandType(data);
                int pch = TunerContract.ListItemContract.HdRadio.getPchNumber(data);
                long frequency = TunerContract.ListItemContract.HdRadio.getFrequency(data);
                TunerFrequencyUnit unit = TunerContract.ListItemContract.HdRadio.getFrequencyUnit(data);
                String freqName = FrequencyUtil.toString(mContext, frequency, unit);
                String name = TunerContract.ListItemContract.HdRadio.getText(data);
                boolean selected = false;
                HdRadioInfo currHdRadio = holder.getCarDeviceMediaInfoHolder().hdRadioInfo;
                if (band==currHdRadio.getBand()&&freqName.equals(FrequencyUtil.toString(mContext, currHdRadio.currentFrequency, currHdRadio.frequencyUnit))) {
                    selected = true;
                }
                HdRadioPresetItem preset = new HdRadioPresetItem(band, pch, name, freqName, selected);
                presetList.add(preset);

                isEof = data.moveToNext();
            }
        }
        return presetList;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // no action
    }

    /**
     * プリセット選択ハンドラ
     *
     * @param number 選択プリセット番号
     */
    public void onSelectPresetNumber(int number) {
        if(isSphCarDevice()&&mSourceType==MediaSourceType.RADIO){
            onSelectPreset(number);
        }else if(mSourceType==MediaSourceType.DAB){
            onSelectPresetDab(number);
        } else {
            mMediaCase.selectListItem(number);
        }
    }

    /**
     * リスト情報更新イベントハンドラ
     *
     * @param ev リスト情報更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onCrpListUpdateEvent(CrpListUpdateEvent ev) {
        updatePresetList();
    }

    private void updatePresetList() {
        StatusHolder holder = mStatusHolder.execute();
        if (mSourceType == MediaSourceType.RADIO) {
            mRadioBand = holder.getCarDeviceMediaInfoHolder().radioInfo.band;
        } else if (mSourceType == MediaSourceType.SIRIUS_XM) {
            mSxmBand = holder.getCarDeviceMediaInfoHolder().sxmMediaInfo.band;
        } else if (mSourceType == MediaSourceType.DAB) {
            mDabBand = holder.getCarDeviceMediaInfoHolder().dabInfo.band;
        } else if (mSourceType == MediaSourceType.HD_RADIO) {
            mHdRadioBand = holder.getCarDeviceMediaInfoHolder().hdRadioInfo.band;
        }

        Bundle args = new Bundle();
        if (mSourceType == MediaSourceType.RADIO) {
            if (mRadioBand != null) {
                args.putByte(KEY_BAND_TYPE, (byte) (mRadioBand.getCode() & 0xFF));
            }
        } else if (mSourceType == MediaSourceType.SIRIUS_XM) {
            if (mSxmBand != null) {
                args.putByte(KEY_BAND_TYPE, (byte) (mSxmBand.getCode() & 0xFF));
            }
        }else if (mSourceType == MediaSourceType.DAB) {
            return;
        }else if (mSourceType == MediaSourceType.HD_RADIO) {
            if (mHdRadioBand != null) {
                args.putByte(KEY_BAND_TYPE, (byte) (mHdRadioBand.getCode() & 0xFF));
            }
        }
        mLoaderManager.restartLoader(LOADER_ID_PRESET, args, this);
    }

    /**
     * リスト情報更新イベントハンドラ
     *
     * @param ev リスト情報更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void OnListInfoChangeEvent(ListInfoChangeEvent ev) {
        if (mSourceType == MediaSourceType.RADIO||mSourceType == MediaSourceType.SIRIUS_XM||mSourceType == MediaSourceType.HD_RADIO) {
            updateSelected();
        }
    }

    /**
     * LocationMeshCodeChangeEventハンドラ
     * @param event LocationMeshCodeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationMeshCodeChangeEvent(LocationMeshCodeChangeEvent event) {
        if(mSourceType == MediaSourceType.RADIO) {
            updatePresetList();
        }
    }

    public void onSelectList(int listIndex, Cursor cursor) {
        mMediaCase.selectListItem(listIndex);
    }

    /** DAB情報変更通知イベントハンドラ
     *
     * @param event DAB情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDabInfoChangeEvent(DabInfoChangeEvent event) {
        StatusHolder holder = mStatusHolder.execute();
        if(holder.getCarDeviceMediaInfoHolder().dabInfo.band!=mDabBand){
            mDabBand =holder.getCarDeviceMediaInfoHolder().dabInfo.band;
            getUserPresetList();
        }
    }

    private void updateSelected(){
        StatusHolder holder = mStatusHolder.execute();
        ListInfo info = holder.getListInfo();
        int position = info.focusListIndex -1;
        //専用機では必ず1個目選択
        if(holder.getProtocolSpec().isSphCarDevice()){
            Optional.ofNullable(getView()).ifPresent(view -> view.setSelectedPosition(0));
            return;
        }
        if(position>=0) {
            Optional.ofNullable(getView()).ifPresent(view -> view.setSelectedPosition(position));
        }
    }

    /**
     * ユーザー登録PCHリスト取得
     */
    private void getUserPresetList(){
        StatusHolder holder = mStatusHolder.execute();
        if(isSphCarDevice()&&mSourceType==MediaSourceType.RADIO) {
            mRadioBand = holder.getCarDeviceMediaInfoHolder().radioInfo.band;
            if (mRadioBand != null && mLoaderManager != null) {
                Bundle args = new Bundle();
                args.putByte(KEY_BAND_TYPE, (byte) (mRadioBand.getCode() & 0xFF));
                mLoaderManager.restartLoader(LOADER_ID_USER_PRESET, args, this);
            }
        }else if(isSphCarDevice()&&mSourceType==MediaSourceType.DAB){
            mDabBand = holder.getCarDeviceMediaInfoHolder().dabInfo.band;
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
        mUserPreset.clear();
        data.moveToPosition(-1);
        if(mSourceType==MediaSourceType.RADIO) {
            while (data.moveToNext()) {
                long frequency = TunerContract.FavoriteContract.Radio.getFrequency(data);
                int presetNumber = TunerContract.FavoriteContract.Radio.getPresetNumber(data);
                RadioBandType band = TunerContract.FavoriteContract.Radio.getBandType(data);
                String description = TunerContract.FavoriteContract.Radio.getDescription(data);
                String freqFormat;
                try {
                    String[] data1 = description.split(" ");
                    freqFormat = data1[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    freqFormat = description;
                }
                String name = TunerContract.FavoriteContract.Radio.getName(data);
                RadioPresetItem preset = new RadioPresetItem(band, presetNumber, name, freqFormat, false);
                //mUserPresetには生Bandが入る
                mUserPreset.add(preset);
            }
            updatePresetList();
        }else if(mSourceType==MediaSourceType.DAB){
            while (data.moveToNext()) {
                long frequency = TunerContract.FavoriteContract.Dab.getFrequency(data);
                int presetNumber = TunerContract.FavoriteContract.Dab.getPresetNumber(data);
                DabBandType band = TunerContract.FavoriteContract.Dab.getBandType(data);
                String description = TunerContract.FavoriteContract.Dab.getDescription(data);
                String freqFormat;
                try {
                    String[] data1 = description.split(" ");
                    freqFormat = data1[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    freqFormat = description;
                }
                String name = TunerContract.FavoriteContract.Dab.getName(data);
                DabPresetItem preset = new DabPresetItem(band, presetNumber, name, freqFormat, false);
                //mUserPresetには生Bandが入る
                mUserPreset.add(preset);
            }
            updateDabPresetList();
        }
    }

    /**
     * DAB PCHリスト作成
     */
    private void updateDabPresetList(){
        ArrayList<AbstractPresetItem> presetList = new ArrayList<>();
        //初期PCHリスト取得
        Map<PresetChannelDictionary.PresetKey, Integer> initialPreset = mStatusHolder.execute().getPresetChannelDictionary().getInitialPresetInfo();
        for (PresetChannelDictionary.PresetKey key : initialPreset.keySet()) {
            //現Bandのみのリスト
            if(DabBandType.valueOf((byte)key.band) == mDabBand) {
                int number = 0;
                Integer numberInteger;
                numberInteger = initialPreset.get(key);
                if (numberInteger != null) {
                    number = numberInteger;
                }
                DabInfo currDab = mStatusHolder.execute().getCarDeviceMediaInfoHolder().dabInfo;
                String freqName = FrequencyUtil.toString(mContext, key.frequency, currDab.frequencyUnit);
                DabPresetItem preset = new DabPresetItem(DabBandType.valueOf((byte) key.band), number, "", freqName, false);

                //ユーザー登録Presetがあれば差し替え
                boolean isUserPreset = false;
                for (int i = 0; i < mUserPreset.size(); i++) {
                    DabPresetItem userPreset = (DabPresetItem) mUserPreset.get(i);
                    DabBandType presetBand = userPreset.bandType;
                    if (presetBand == DabBandType.valueOf((byte) key.band) && userPreset.presetNumber == number) {
                        presetList.add(userPreset);
                        isUserPreset = true;
                        break;
                    }
                }
                if (!isUserPreset) {
                    presetList.add(preset);
                }
            }
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setPresetList(presetList));
        updateSelected();
    }

    /**
     * PCH選局
     */
    private void onSelectPreset(int presetIndex){
        boolean isExist = false;
        if(mUserPresetCursor!=null) {
            mUserPresetCursor.moveToPosition(-1);
            while (mUserPresetCursor.moveToNext()) {
                //ユーザー登録用Band
                RadioBandType band = TunerContract.FavoriteContract.Radio.getBandTypePreset(mUserPresetCursor);
                int presetNumber = TunerContract.FavoriteContract.Radio.getPresetNumber(mUserPresetCursor);

                RadioBandType presetBand = ((RadioPresetItem)mRadioPresetList.get(presetIndex-1)).bandType;
                if(presetBand==RadioBandType.LW){
                    presetBand=RadioBandType.MW;
                }
                //ユーザー登録PCHリストと統合リストのユーザー登録用BandとPresetNumberが一致すればユーザー登録PCHが存在する
                if ((presetBand == band)&&mRadioPresetList.get(presetIndex-1).presetNumber == presetNumber) {
                    isExist = true;
                    break;
                }
            }
        }
        if(isExist) {
            mRadioCase.execute(
                    TunerContract.FavoriteContract.Radio.getIndex(mUserPresetCursor),
                    TunerContract.FavoriteContract.Radio.getBandType(mUserPresetCursor),
                    TunerContract.FavoriteContract.Radio.getPi(mUserPresetCursor));
            mMediaCase.exitList();
        }else{
            mMediaCase.selectListItem(presetIndex);
        }
    }

    /**
     * PCH選局(KM997のDAB用)
     */
    private void onSelectPresetDab(int presetIndex){
        if (isSphCarDevice() && presetIndex >= 1 && presetIndex <= 6) {
            boolean isExist = false;
            if (mUserPresetCursor != null) {
                mUserPresetCursor.moveToPosition(-1);
                while (mUserPresetCursor.moveToNext()) {
                    DabBandType band = TunerContract.FavoriteContract.Dab.getBandType(mUserPresetCursor);
                    int presetNumber = TunerContract.FavoriteContract.Dab.getPresetNumber(mUserPresetCursor);
                    //ユーザー登録PCHリストのBandとPresetNumberが一致すればユーザー登録PCHが存在する
                    if ((mDabBand == band) && (presetIndex == presetNumber)) {
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
                //初期PCHリストで選局
                PresetChannelDictionary.PresetKey presetKey = mStatusHolder.execute().getPresetChannelDictionary().getInitialPresetInfo(
                        MediaSourceType.DAB,
                        mDabBand.code,
                        presetIndex);
                if (presetKey != null) {
                    mDabCase.selectFavorite(
                            presetKey.index,
                            mDabBand,
                            presetKey.eid,
                            presetKey.sid,
                            presetKey.scids);
                }
            }
            mEventBus.post(new GoBackEvent());
        }
    }
}
