package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.util.PresetChannelDictionary;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 車載機のステータスやアプリ内の再生楽曲情報.
 */
public class StatusHolder {
    /** BT接続の場合の接続機器名. */
    private String mBtDeviceName;
    /** 通信路の状態. */
    private TransportStatus mTransportStatus = TransportStatus.UNUSED;
    /** 接続方式. */
    private ConnectionType mConnectionType;
    /** セッションの状態. */
    private SessionStatus mSessionStatus = SessionStatus.STOPPED;
    /** CarRemoteProtocolスペック情報. */
    private final ProtocolSpec mProtocolSpec = new ProtocolSpec();
    /** 車載機スペック情報. */
    private final CarDeviceSpec mCarDeviceSpec = new CarDeviceSpec();
    /** 車載機ステータス情報. */
    private final CarDeviceStatus mCarDeviceStatus = new CarDeviceStatus();
    /** システム設定ステータス. */
    private final SystemSettingStatus mSystemSettingStatus = new SystemSettingStatus();
    /** オーディオ設定ステータス. */
    private final AudioSettingStatus mAudioSettingStatus = new AudioSettingStatus();
    /** イルミ設定ステータス. */
    private final IlluminationSettingStatus mIlluminationSettingStatus = new IlluminationSettingStatus();
    /** Tuner(Radio) Function設定ステータス. */
    private final TunerFunctionSettingStatus mTunerFunctionSettingStatus = new TunerFunctionSettingStatus();
    /** DAB Function設定ステータス. */
    private final DabFunctionSettingStatus mDabFunctionSettingStatus = new DabFunctionSettingStatus();
    /** HD Radio Function設定ステータス. */
    private final HdRadioFunctionSettingStatus mHdRadioFunctionSettingStatus = new HdRadioFunctionSettingStatus();
    /** BT Audio Function設定ステータス. */
    private final BtAudioFunctionSettingStatus mBtAudioFunctionSettingStatus = new BtAudioFunctionSettingStatus();
    /** Phone設定ステータス. */
    private final PhoneSettingStatus mPhoneSettingStatus = new PhoneSettingStatus();
    /** パーキングセンサー設定ステータス. */
    private final ParkingSensorSettingStatus mParkingSensorSettingStatus = new ParkingSensorSettingStatus();
    /** 初期設定ステータス. */
    private final InitialSettingStatus mInitialSettingStatus = new InitialSettingStatus();
    /** Sound FX設定ステータス. */
    private final SoundFxSettingStatus mSoundFxSettingStatus = new SoundFxSettingStatus();
    /** パーキングセンサーステータス. */
    private final ParkingSensorStatus mParkingSensorStatus = new ParkingSensorStatus();
    /** スマートフォンステータス情報. */
    private final SmartPhoneStatus mSmartPhoneStatus = new SmartPhoneStatus();
    /** 各ソースの表示情報. */
    private final CarDeviceMediaInfoHolder mCarDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
    /** システム設定. */
    private final SystemSetting mSystemSetting = new SystemSetting();
    /** オーディオ設定. */
    private final AudioSetting mAudioSetting = new AudioSetting();
    /** イルミ設定. */
    private final IlluminationSetting mIlluminationSetting = new IlluminationSetting();
    /** Tuner(Radio)Function設定. */
    private final TunerFunctionSetting mTunerFunctionSetting = new TunerFunctionSetting();
    /** DAB Function設定. */
    private final DabFunctionSetting mDabFunctionSetting = new DabFunctionSetting();
    /** HD Radio Function設定. */
    private final HdRadioFunctionSetting mHdRadioFunctionSetting = new HdRadioFunctionSetting();
    /** パーキングセンサー設定. */
    private final ParkingSensorSetting mParkingSensorSetting = new ParkingSensorSetting();
    /** ナビガイド音声設定. */
    private final NaviGuideVoiceSetting mNaviGuideVoiceSetting = new NaviGuideVoiceSetting();
    /** 初期設定. */
    private final InitialSetting mInitialSetting = new InitialSetting();
    /** Phone設定. */
    private final PhoneSetting mPhoneSetting = new PhoneSetting();
    /** Sound FX 設定. */
    private final SoundFxSetting mSoundFxSetting = new SoundFxSetting();
    /** リスト情報. */
    private final ListInfo mListInfo = new ListInfo();
    /** 設定リスト情報. */
    private final SettingListInfoMap mSettingListInfoMap = new SettingListInfoMap();
    /** デバッグ情報. */
    private final DebugInfo mDebugInfo = new DebugInfo();
    /** 割り込み情報. */
    private CarDeviceInterrupt mCarDeviceInterrupt;
    /** 走行状態 */
    private final CarRunningStatus mCarRunningStatus = new CarRunningStatus();
    /** 99App状態 */
    private final AppStatus mAppStatus = new AppStatus();
    /** Preset番号とTuner設定(ソース、バンド、周波数)を記憶する */
    private PresetChannelDictionary mPresetChannelDictionary = new PresetChannelDictionary();
    /**
     * コンストラクタ.
     */
    @Inject
    public StatusHolder(AppSharedPreference preference) {
        reset();
        mSmartPhoneStatus.repeatMode = preference.getAppMusicRepeatMode();
        mSmartPhoneStatus.shuffleMode = preference.getAppMusicShuffleMode();
        mSoundFxSetting.customBandSettingA = preference.getCustomBandSettingA();
        mSoundFxSetting.customBandSettingB = preference.getCustomBandSettingB();
    }

    /**
     * セッション状態取得.
     *
     * @return セッション状態
     */
    public SessionStatus getSessionStatus() {
        return mSessionStatus;
    }

    /**
     * セッション状態設定.
     *
     * @param status セッション状態
     */
    public void setSessionStatus(SessionStatus status) {
        mSessionStatus = status;
    }

    /**
     * CarRemoteProtocolスペック情報取得.
     *
     * @return CarRemoteProtocolスペック情報
     */
    public ProtocolSpec getProtocolSpec() {
        return mProtocolSpec;
    }

    /**
     * 車載機スペック情報取得.
     *
     * @return 車載機スペック情報
     */
    public CarDeviceSpec getCarDeviceSpec() {
        return mCarDeviceSpec;
    }

    /**
     * 車載機ステータス取得.
     *
     * @return 車載機ステータス
     */
    public CarDeviceStatus getCarDeviceStatus() {
        return mCarDeviceStatus;
    }

    /**
     * システム設定ステータス取得.
     *
     * @return システム設定ステータス
     */
    public SystemSettingStatus getSystemSettingStatus() {
        return mSystemSettingStatus;
    }

    /**
     * オーディオ設定ステータス取得.
     *
     * @return オーディオ設定ステータス
     */
    public AudioSettingStatus getAudioSettingStatus() {
        return mAudioSettingStatus;
    }

    /**
     * イルミ設定ステータス取得.
     *
     * @return イルミ設定ステータス
     */
    public IlluminationSettingStatus getIlluminationSettingStatus() {
        return mIlluminationSettingStatus;
    }

    /**
     * Tuner(Radio) Function設定ステータス取得.
     *
     * @return Tuner(Radio) Function設定ステータス
     */
    public TunerFunctionSettingStatus getTunerFunctionSettingStatus() {
        return mTunerFunctionSettingStatus;
    }

    /**
     * DAB Function設定ステータス取得.
     *
     * @return DAB Function設定ステータス
     */
    public DabFunctionSettingStatus getDabFunctionSettingStatus() {
        return mDabFunctionSettingStatus;
    }

    /**
     * HD Radio Function設定ステータス取得.
     *
     * @return HD Radio Function設定ステータス
     */
    public HdRadioFunctionSettingStatus getHdRadioFunctionSettingStatus() {
        return mHdRadioFunctionSettingStatus;
    }

    /**
     * BT Audio Function設定ステータス取得.
     *
     * @return BT Audio Function設定ステータス
     */
    public BtAudioFunctionSettingStatus getBtAudioFunctionSettingStatus() {
        return mBtAudioFunctionSettingStatus;
    }

    /**
     * Phone設定ステータス取得.
     *
     * @return Phone設定ステータス
     */
    public PhoneSettingStatus getPhoneSettingStatus() {
        return mPhoneSettingStatus;
    }

    /**
     * パーキングセンサー設定ステータス取得.
     *
     * @return パーキングセンサー設定ステータス
     */
    public ParkingSensorSettingStatus getParkingSensorSettingStatus() {
        return mParkingSensorSettingStatus;
    }

    /**
     * 初期設定ステータス取得.
     *
     * @return 初期設定ステータス
     */
    public InitialSettingStatus getInitialSettingStatus() {
        return mInitialSettingStatus;
    }

    /**
     * Sound FX設定ステータス取得.
     *
     * @return Sound FX設定ステータス
     */
    public SoundFxSettingStatus getSoundFxSettingStatus() {
        return mSoundFxSettingStatus;
    }

    /**
     * パーキングセンサーステータス取得.
     *
     * @return パーキングセンサーステータス
     */
    public ParkingSensorStatus getParkingSensorStatus() {
        return mParkingSensorStatus;
    }

    /**
     * スマートフォンステータス情報取得.
     *
     * @return スマートフォンステータス情報
     */
    public SmartPhoneStatus getSmartPhoneStatus() {
        return mSmartPhoneStatus;
    }

    /**
     * 各ソースの表示情報取得.
     *
     * @return 各ソースの表示情報
     */
    public CarDeviceMediaInfoHolder getCarDeviceMediaInfoHolder() {
        return mCarDeviceMediaInfoHolder;
    }

    /**
     * システム設定取得.
     *
     * @return システム設定
     */
    public SystemSetting getSystemSetting(){
        return mSystemSetting;
    }

    /**
     * オーディオ設定取得.
     *
     * @return オーディオ設定
     */
    public AudioSetting getAudioSetting() {
        return mAudioSetting;
    }

    /**
     * イルミ設定取得.
     *
     * @return イルミ設定
     */
    public IlluminationSetting getIlluminationSetting() {
        return mIlluminationSetting;
    }

    /**
     * Tuner(Radio) Function設定取得.
     *
     * @return Tuner(Radio) Function設定
     */
    public TunerFunctionSetting getTunerFunctionSetting() {
        return mTunerFunctionSetting;
    }

    /**
     * DAB Function設定取得.
     *
     * @return DAB Function設定
     */
    public DabFunctionSetting getDabFunctionSetting() {
        return mDabFunctionSetting;
    }

    /**
     * HD Radio Function設定取得.
     *
     * @return HD Radio Function設定
     */
    public HdRadioFunctionSetting getHdRadioFunctionSetting() {
        return mHdRadioFunctionSetting;
    }

    /**
     * パーキングセンサー設定取得.
     *
     * @return パーキングセンサー設定
     */
    public ParkingSensorSetting getParkingSensorSetting() {
        return mParkingSensorSetting;
    }

    /**
     * ナビガイド音声設定取得.
     *
     * @return ナビガイド音声設定
     */
    public NaviGuideVoiceSetting getNaviGuideVoiceSetting() {
        return mNaviGuideVoiceSetting;
    }

    /**
     * 初期設定取得.
     *
     * @return 初期設定
     */
    public InitialSetting getInitialSetting() {
        return mInitialSetting;
    }

    /**
     * Phone設定取得.
     *
     * @return Phone設定
     */
    public PhoneSetting getPhoneSetting() {
        return mPhoneSetting;
    }

    /**
     * Sound FX設定取得.
     *
     * @return Sound FX設定
     */
    public SoundFxSetting getSoundFxSetting() {
        return mSoundFxSetting;
    }

    /**
     * リスト情報取得.
     *
     * @return リスト情報
     */
    public ListInfo getListInfo() {
        return mListInfo;
    }

    /**
     * 設定リスト情報とトランザクションの管理取得.
     *
     * @return 設定リスト情報とトランザクションの管理
     */
    public SettingListInfoMap getSettingListInfoMap() {
        return mSettingListInfoMap;
    }

    /**
     * デバッグ情報取得.
     *
     * @return デバッグ情報
     */
    public DebugInfo getDebugInfo() {
        return mDebugInfo;
    }

    /**
     * 割り込み情報取得.
     *
     * @return 割り込み情報
     */
    public CarDeviceInterrupt getCarDeviceInterrupt() {
        return mCarDeviceInterrupt;
    }

    /**
     * 割り込み情報設定.
     *
     * @param interrupt 割り込み情報。割り込み解除時はnullを指定。
     */
    public void setCarDeviceInterrupt(CarDeviceInterrupt interrupt) {
        mCarDeviceInterrupt = interrupt;
    }

    /**
     * 割り込み中か否か取得.
     *
     * @return {@code true}:割り込み中である。{@code false}:それ以外。
     */
    public boolean isInterrupted() {
        return mCarDeviceInterrupt != null;
    }

    /**
     * 車載機のBTデバイス名取得.
     *
     * @return BTデバイス名
     */
    public String getBtDeviceName() {
        return mBtDeviceName;
    }

    /**
     * 車載機のBTデバイス名設定.
     *
     * @param name 車載機のBTデバイス名
     */
    public void setBtDeviceName(String name) {
        mBtDeviceName = name;
    }

    /**
     * 通信路の状態取得.
     *
     * @return 通信路の状態
     */
    public TransportStatus getTransportStatus() {
        return mTransportStatus;
    }

    /**
     * 通信路の状態設定.
     *
     * @param status 通信路の状態
     */
    public void setTransportStatus(TransportStatus status) {
        mTransportStatus = status;
    }

    /**
     * 接続方式取得.
     *
     * @return 接続方式
     */
    public ConnectionType getConnectionType() {
        return mConnectionType;
    }

    /**
     * 接続方式設定.
     *
     * @param type 接続方式
     */
    public void setConnectionType(ConnectionType type) {
        mConnectionType = type;
    }

    /**
     * 走行状態取得.
     *
     * @return 走行状態
     */
    public CarRunningStatus getCarRunningStatus() {
        return mCarRunningStatus;
    }

    /**
     * 99App状態取得.
     *
     * @return 99App状態
     */
    public AppStatus getAppStatus() {
        return mAppStatus;
    }

    public PresetChannelDictionary getPresetChannelDictionary() {
        return mPresetChannelDictionary;
    }
    /**
     * リセット.
     */
    public void reset() {
        Timber.i("reset()");

        mBtDeviceName = null;
        mTransportStatus = TransportStatus.UNUSED;
        mConnectionType = null;
        setSessionStatus(SessionStatus.STOPPED);
        mProtocolSpec.reset();
        mCarDeviceSpec.reset();
        mCarDeviceStatus.reset();
        mSystemSettingStatus.reset();
        mAudioSettingStatus.reset();
        mIlluminationSettingStatus.reset();
        mPhoneSettingStatus.reset();
        mParkingSensorSettingStatus.reset();
        mInitialSettingStatus.reset();
        mSoundFxSetting.reset();
        mParkingSensorStatus.reset();
        mTunerFunctionSettingStatus.reset();
        mDabFunctionSettingStatus.reset();
        mHdRadioFunctionSettingStatus.reset();
        mBtAudioFunctionSettingStatus.reset();
        mSmartPhoneStatus.reset();
        mCarDeviceMediaInfoHolder.reset();
        mSystemSetting.reset();
        mAudioSetting.reset();
        mIlluminationSetting.reset();
        mTunerFunctionSetting.reset();
        mDabFunctionSetting.reset();
        mHdRadioFunctionSetting.reset();
        mParkingSensorSetting.reset();
        mNaviGuideVoiceSetting.reset();
        mInitialSetting.reset();
        mPhoneSetting.reset();
        mSoundFxSetting.reset();
        mCarDeviceInterrupt = null;
        mListInfo.reset();
        mSettingListInfoMap.reset();
        mDebugInfo.reset();
        mCarRunningStatus.reset();
        mAppStatus.reset();
		mPresetChannelDictionary.reset();
    }

    /**
     * メディア系共通情報取得.
     * <p>
     * {@link AbstractMediaInfo}の型で共通で処理したい時用。
     * 通常は{@link #getCarDeviceMediaInfoHolder()}の個々の情報を直接参照する。
     *
     * @param type ソース種別（CD、USB、PANDORA、SPOTIFY、BT_AUDIO）
     * @return メディア系共通情報
     * @throws NullPointerException     {@code type}がnull
     * @throws IllegalArgumentException {@code type}が不正
     */
    public AbstractMediaInfo findMediaInfoByMediaSourceType(@NonNull MediaSourceType type) {
        CarDeviceMediaInfoHolder holder = getCarDeviceMediaInfoHolder();
        switch (checkNotNull(type)) {
            case CD:
                return holder.cdInfo;
            case USB:
                return holder.usbMediaInfo;
            case PANDORA:
                return holder.pandoraMediaInfo;
            case SPOTIFY:
                return holder.spotifyMediaInfo;
            case BT_AUDIO:
                return holder.btAudioInfo;
            default:
                throw new IllegalArgumentException("Not car device media source.");
        }
    }

    /**
     * Tuner系共通情報取得.
     * <p>
     * {@link AbstractTunerInfo}の型で共通で処理したい時用。
     * 通常は{@link #getCarDeviceMediaInfoHolder()}の個々の情報を直接参照する。
     *
     * @param type ソース種別（RADIO、DAB、HD_RADIO、SIRIUS_XM）
     * @return Tuner系共通情報
     * @throws NullPointerException     {@code type}がnull
     * @throws IllegalArgumentException {@code type}が不正
     */
    public AbstractTunerInfo findTunerInfoByMediaSourceType(@NonNull MediaSourceType type) {
        CarDeviceMediaInfoHolder holder = getCarDeviceMediaInfoHolder();
        switch (checkNotNull(type)) {
            case RADIO:
                return holder.radioInfo;
            case DAB:
                return holder.dabInfo;
            case HD_RADIO:
                return holder.hdRadioInfo;
            case SIRIUS_XM:
                return holder.sxmMediaInfo;
            default:
                throw new IllegalArgumentException("Not tuner source.");
        }
    }

    /**
     * 車載機がリストをサポートしているか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * 車載機によってはリストをサポートしていない場合がある。
     * 車載機スペックにはリストをサポートているか否かの情報はないので、
     * モデルで判定している。
     * 恐らくリスト種別を{@link ListType#LIST_UNAVAILABLE}にすることで
     * リスト情報を要求しないことを車載機は想定していると思われるが、
     * P.CHリストは構わず取得している。
     * リストをサポートしていても{@link ListType#LIST_UNAVAILABLE}の場合は
     * リストを取得出来ないのであれば、本メソッドは不要でありリスト種別を見て
     * 取得しないようにすれば良い。が、裏が取れないので止むを得ずARCの実装に
     * 従って本メソッドが存在している。
     *
     * @return {@code true}:サポートしている。{@code false}:それ以外。
     */
    public boolean isListSupported() {
        return !mCarDeviceSpec.jasperAudioSettingSupported;
    }

    /**
     * システム設定情報要求を送信すべきか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:送信すべきである。{@code false}:それ以外。
     */
    public boolean shouldSendSystemSettingRequests() {
        return mCarDeviceStatus.systemSettingEnabled
                && (mSystemSetting.requestStatus == RequestStatus.NOT_SENT ||
                    mSystemSetting.requestStatus == RequestStatus.SENT_INCOMPLETE)
                ;
    }

    /**
     * Function設定情報要求を送信すべきか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:送信すべきである。{@code false}:それ以外。
     */
    public boolean shouldSendFunctionSettingRequests() {
        if (!mCarDeviceStatus.functionSettingEnabled) {
            return false;
        }

        switch (mCarDeviceStatus.sourceType) {
            case RADIO:
                return mCarDeviceStatus.tunerFunctionSettingEnabled
                        ? mTunerFunctionSetting.requestStatus == RequestStatus.NOT_SENT
                        : false;
            case DAB:
                return mCarDeviceStatus.dabFunctionSettingEnabled
                        ? mDabFunctionSetting.requestStatus == RequestStatus.NOT_SENT
                        : false;
            case HD_RADIO:
                return mCarDeviceStatus.hdRadioFunctionSettingEnabled
                        ? mHdRadioFunctionSetting.requestStatus == RequestStatus.NOT_SENT
                        : false;
            case BT_AUDIO:
                return false; // 設定情報が無いので送信不要
            default:
                return false;
        }
    }

    /**
     * パーキングセンサー設定情報要求を送信すべきか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:送信すべきである。{@code false}:それ以外。
     */
    public boolean shouldSendParkingSensorSettingRequests() {
        return mCarDeviceStatus.parkingSensorSettingEnabled
                && (mParkingSensorSetting.requestStatus == RequestStatus.NOT_SENT ||
                    mParkingSensorSetting.requestStatus == RequestStatus.SENT_INCOMPLETE)
                ;
    }

    /**
     * ナビガイド音声設定情報要求を送信すべきか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:送信すべきである。{@code false}:それ以外。
     */
    public boolean shouldSendNaviGuideVoiceSettingRequests() {
        return mCarDeviceStatus.naviGuideVoiceSettingEnabled
                && (mNaviGuideVoiceSetting.requestStatus == RequestStatus.NOT_SENT ||
                    mNaviGuideVoiceSetting.requestStatus == RequestStatus.SENT_INCOMPLETE)
                ;
    }

    /**
     * 初期設定情報要求を送信すべきか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:送信すべきである。{@code false}:それ以外。
     */
    public boolean shouldSendInitialSettingRequests() {
        return mCarDeviceStatus.initialSettingEnabled
                && (mInitialSetting.requestStatus == RequestStatus.NOT_SENT ||
                    mInitialSetting.requestStatus == RequestStatus.SENT_INCOMPLETE)
                ;
    }

    /**
     * Phone設定情報要求を送信すべきか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:送信すべきである。{@code false}:それ以外。
     */
    public boolean shouldSendPhoneSettingRequests() {
        return mCarDeviceStatus.phoneSettingEnabled
                && (mPhoneSetting.requestStatus == RequestStatus.NOT_SENT ||
                    mPhoneSetting.requestStatus == RequestStatus.SENT_INCOMPLETE)
                ;
    }

    /**
     * Sound FX設定情報要求を送信すべきか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:送信すべきである。{@code false}:それ以外。
     */
    public boolean shouldSendSoundFxSettingRequests() {
        return mCarDeviceStatus.soundFxSettingEnabled
                && (mSoundFxSetting.requestStatus == RequestStatus.NOT_SENT ||
                    mSoundFxSetting.requestStatus == RequestStatus.SENT_INCOMPLETE)
                ;
    }

    /**
     * オーディオ設定情報要求を送信すべきか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:送信すべきである。{@code false}:それ以外。
     */
    public boolean shouldSendAudioSettingRequests() {
        return isAudioSettingEnabled()
                && (mAudioSetting.requestStatus == RequestStatus.NOT_SENT ||
                    mAudioSetting.requestStatus == RequestStatus.SENT_INCOMPLETE);
    }

    /**
     * Audio設定をサポートしているか否か取得.
     * <p>
     * presentation層向け。
     *
     * @return {@code true}:サポートしている。{@code false}:それ以外。
     */
    public boolean isAudioSettingSupported() {
        return mCarDeviceSpec.audioSettingSupported
                || mCarDeviceSpec.jasperAudioSettingSupported
                || mCarDeviceSpec.ac2AudioSettingSupported;
    }

    /**
     * Audio設定可能か否か取得.
     * <p>
     * presentation層向け。
     *
     * @return {@code true}:設定可能である。{@code false}:それ以外。
     */
    public boolean isAudioSettingEnabled() {
        return mCarDeviceStatus.audioSettingEnabled
                || mCarDeviceStatus.jasperAudioSettingEnabled
                || mCarDeviceStatus.ac2AudioSettingEnabled;
    }

    /**
     * LISTENING POSITION設定情報要求が可能か否か取得.
     * <p>
     * infrastructure層向け。
     *
     * 車種専用データありの場合は、LISTENING POSITION設定が有効かに関係なく設定値を要求する。
     *
     * @return {@code true}:可能である。{@code false}:それ以外。
     */
    public boolean isListeningPositionSettingRequestEnabled() {
        return mAudioSettingStatus.listeningPositionSettingEnabled
                || mCarDeviceSpec.carModelSpecializedSetting.audioSettingSupported;
    }

    /**
     * イコライザーのバンドの表示・操作が可能か否か取得.
     * <p>
     * presentation層向け。
     *
     * 車種専用データがあり、且つプリセットがCUSTOM以外の場合、バンド表示や操作は行えない。
     *
     * @return {@code true}:可能である。{@code false}:それ以外。
     */
    public boolean isEqualizerBandEnabled() {
        return !mCarDeviceSpec.carModelSpecializedSetting.audioSettingSupported
                || mAudioSetting.equalizerSetting.audioSettingEqualizerType == AudioSettingEqualizerType.COMMON_CUSTOM;
    }

    /**
     * イコライザーのレベル設定が有効か否か取得.
     * <p>
     * 車種専用データがあり、且つ、プリセットがCUSTOM/FLAT以外の場合、レベル設定が有効となる。
     *
     * @return {@code true}:有効である。{@code false}:それ以外。
     */
    public boolean isEqualizerLevelControlEnabled() {
        return mCarDeviceSpec.carModelSpecializedSetting.audioSettingSupported
                && mAudioSettingStatus.levelSettingEnabled
                && mAudioSetting.equalizerSetting.audioSettingEqualizerType != AudioSettingEqualizerType.FLAT
                && mAudioSetting.equalizerSetting.audioSettingEqualizerType != AudioSettingEqualizerType.COMMON_CUSTOM;
    }

    /**
     * イコライザーのバンドレベル取得.
     *
     * @return イコライザーのバンドレベル群。イコライザーのバンドの表示・操作を行えない場合、空配列となる。
     */
    public int[] getEqualizerBandLevels() {
        EqualizerSetting setting = mAudioSetting.equalizerSetting;

        int bandsCount;
        if (isEqualizerBandEnabled()) {
            if (mCarDeviceSpec.audioSettingSupported) {
                // OPAL
                bandsCount = 13;
            } else if (mCarDeviceSpec.jasperAudioSettingSupported) {
                // JASPER
                bandsCount = 5;
            } else if (mCarDeviceSpec.ac2AudioSettingSupported) {
                // AC2
                if (mCarDeviceSpec.carModelSpecializedSetting.audioSettingSupported) {
                    bandsCount = 5;
                } else {
                    bandsCount = 13;
                }
            } else {
                bandsCount = 0;
            }
        } else {
            bandsCount = 0;
        }

        int[] bandLevels;
        if (bandsCount == 13) {
            bandLevels = new int[]{
                    setting.band1, setting.band2, setting.band3, setting.band4, setting.band5, setting.band6,
                    setting.band7, setting.band8, setting.band9, setting.band10, setting.band11, setting.band12,
                    setting.band13
            };
        } else if (bandsCount == 5) {
            bandLevels = new int[]{
                    setting.band1, setting.band2, setting.band3, setting.band4, setting.band5
            };
        } else {
            bandLevels = new int[0];
        }

        return bandLevels;
    }

    /**
     * Time Alignment設定が可能な否か取得.
     * <p>
     * presentation層向け。
     *
     * @return {@code true}:可能である。{@code false}:それ以外。
     */
    public boolean isTimeAlignmentSettingEnabled() {
        boolean enabled = isAudioSettingEnabled()
                && mAudioSettingStatus.timeAlignmentSettingEnabled;
        if (!mCarDeviceSpec.carModelSpecializedSetting.audioSettingSupported) {
            ListeningPositionSetting listeningPosition = mAudioSetting.listeningPositionSetting;
            enabled &= (listeningPosition == ListeningPositionSetting.FRONT_LEFT
                    || listeningPosition == ListeningPositionSetting.FRONT_RIGHT);
        }

        return enabled;
    }

    /**
     * イルミ設定情報要求を送信すべきか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:送信すべきである。{@code false}:それ以外。
     */
    public boolean shouldSendIlluminationSettingRequests() {
        return mCarDeviceStatus.illuminationSettingEnabled
                && (mIlluminationSetting.requestStatus == RequestStatus.NOT_SENT ||
                    mIlluminationSetting.requestStatus == RequestStatus.SENT_INCOMPLETE);
    }

    /**
     * イルミカラー設定をサポートしているか否か取得.
     * <p>
     * presentation層向け。
     *
     * @return {@code true}:サポートしている。{@code false}:それ以外。
     */
    public boolean isIlluminationColorSettingSupported() {
        return mCarDeviceSpec.illuminationSettingSpec.keyColorSettingSupported
                || mCarDeviceSpec.illuminationSettingSpec.dispColorSettingSupported;
    }

    /**
     * イルミカラー設定が有効か否か取得.
     * <p>
     * presentation層向け。
     *
     * @return {@code true}:有効である。{@code false}:それ以外。
     */
    public boolean isIlluminationColorSettingEnabled() {
        return mIlluminationSettingStatus.keyColorSettingEnabled
                || mIlluminationSettingStatus.dispColorSettingEnabled;
    }

    /**
     * CUSTOM発光パターン通知を送信すべきか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:送信すべきである。{@code false}:それ以外。
     */
    public boolean shouldSendCustomFlashPatternRequests() {
        return mIlluminationSettingStatus.customFlashPatternSettingEnabled
                && mIlluminationSetting.customFlashPatternRequestStatus == RequestStatus.NOT_SENT;
    }

    /**
     * Sirius XMのSubscription Update表示中か否か取得.
     * <p>
     * presentation層向け。
     *
     * @return {@code true}:表示中である。{@code false}:それ以外。
     */
    public boolean isSxmSubscriptionUpdate() {
        return mCarDeviceStatus.sourceType == MediaSourceType.SIRIUS_XM
                && mCarDeviceMediaInfoHolder.sxmMediaInfo.subscriptionUpdatingShowing;
    }

    /**
     * [JASPER] Fader/Balance設定が有効か否か取得.
     * <p>
     * presentation層向け。
     *
     * @return {@code true}:有効である。{@code false}:それ以外。
     */
    public boolean isJasperFaderBalanceSettingEnabled() {
        return mCarDeviceSpec.jasperAudioSettingSupported
                && (mAudioSettingStatus.faderSettingEnabled || mAudioSettingStatus.balanceSettingEnabled);
    }

    /**
     * 設定リストをサポートしているか否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:サポートしている。{@code false}:それ以外。
     */
    public boolean isSettingListSupported() {
        return mCarDeviceSpec.phoneSettingSupported;
    }

    /**
     * 設定リストが有効か否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @param type 設定リスト種別
     * @return {@code true}:有効である。{@code false}:それ以外。
     * @throws NullPointerException {@code type}がnull
     */
    public boolean isSettingListEnabled(@NonNull SettingListType type) {
        switch (checkNotNull(type)) {
            case DEVICE_LIST:
                return mPhoneSettingStatus.deviceListEnabled;
            case SEARCH_LIST:
                return mPhoneSettingStatus.inquiryEnabled;
            default:
                throw new AssertionError("can't happen.");
        }
    }

    /**
     * Auto Pairingが有効か否か取得.
     * <p>
     * infrastructure層向け。
     *
     * @return {@code true}:有効である。{@code false}:それ以外。
     */
    public boolean isAutoPainingEnabled() {
        return !("02:00:00:00:00:00".equalsIgnoreCase(mCarDeviceSpec.bdAddress));
    }
}