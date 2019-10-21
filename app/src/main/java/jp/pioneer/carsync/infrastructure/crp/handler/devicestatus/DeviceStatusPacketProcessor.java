package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import java.util.EnumSet;
import java.util.Set;

import jp.pioneer.carsync.domain.model.AttMode;
import jp.pioneer.carsync.domain.model.CarDeviceControlLevel;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.MixtraxSettingStatus;
import jp.pioneer.carsync.domain.model.MuteMode;
import jp.pioneer.carsync.domain.model.ParkingStatus;
import jp.pioneer.carsync.domain.model.ReverseStatus;
import jp.pioneer.carsync.domain.model.ShortPlayback;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerSeekStep;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.model.MediaSourceType.APP_MUSIC;
import static jp.pioneer.carsync.domain.model.MediaSourceType.AUX;
import static jp.pioneer.carsync.domain.model.MediaSourceType.BT_AUDIO;
import static jp.pioneer.carsync.domain.model.MediaSourceType.BT_PHONE;
import static jp.pioneer.carsync.domain.model.MediaSourceType.CD;
import static jp.pioneer.carsync.domain.model.MediaSourceType.DAB;
import static jp.pioneer.carsync.domain.model.MediaSourceType.HD_RADIO;
import static jp.pioneer.carsync.domain.model.MediaSourceType.IPOD;
import static jp.pioneer.carsync.domain.model.MediaSourceType.OFF;
import static jp.pioneer.carsync.domain.model.MediaSourceType.PANDORA;
import static jp.pioneer.carsync.domain.model.MediaSourceType.RADIO;
import static jp.pioneer.carsync.domain.model.MediaSourceType.SIRIUS_XM;
import static jp.pioneer.carsync.domain.model.MediaSourceType.SPOTIFY;
import static jp.pioneer.carsync.domain.model.MediaSourceType.TI;
import static jp.pioneer.carsync.domain.model.MediaSourceType.USB;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.getBitsValue;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * 車載機ステータスパケットプロセッサ.
 * <p>
 * 車載機ステータス応答と通知で使用する。
 */
public class DeviceStatusPacketProcessor {
    private StatusHolder mStatusHolder;
    private boolean mIsUpdated;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}、{@code preference}がnull
     */
    public DeviceStatusPacketProcessor(@NonNull StatusHolder statusHolder) {
        mStatusHolder = checkNotNull(statusHolder);
    }

    /**
     * 更新されたか否か取得.
     * <p>
     * 車載機ステータスは定期通信されるので用意した。
     *
     * @return {@code true}:更新された。{@code false}:それ以外。
     */
    public boolean isUpdated() {
        return mIsUpdated;
    }

    /**
     * 処理.
     *
     * @param packet 受信パケット
     * @return {@link Boolean#TRUE}:成功。{@link Boolean#FALSE}:それ以外。
     * @throws NullPointerException {@code packet}がnull
     */
    @NonNull
    public Boolean process(@NonNull IncomingPacket packet) {
        try {
            mIsUpdated = false;
            byte[] data = packet.getData();

            int majorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().major;
            checkPacketDataLength(data, getDataLength(majorVer));
            CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
            // 車載機ステータスは定期通信されるため、値の変化を真面目に判定する
            CarDeviceStatus old = new CarDeviceStatus(status);
            byte b;
            // D1:連携モードレベル
            b = data[1];
            status.controlLevel = CarDeviceControlLevel.valueOf(b);
            // D2:ソース情報
            b = data[2];
            status.sourceType = MediaSourceType.valueOf(b);
            // D3:ソース状態
            b = data[3];
            status.sourceStatus = MediaSourceStatus.valueOf(b);
            // D4:有効ソース1
            b = data[4];
            Set<MediaSourceType> availableSources = EnumSet.noneOf(MediaSourceType.class);
            addIfSupported(availableSources, b, 7, BT_AUDIO);
            addIfSupported(availableSources, b, 6, AUX);
            addIfSupported(availableSources, b, 5, USB);
            addIfSupported(availableSources, b, 4, CD);
            addIfSupported(availableSources, b, 3, HD_RADIO);
            addIfSupported(availableSources, b, 2, SIRIUS_XM);
            addIfSupported(availableSources, b, 1, DAB);
            addIfSupported(availableSources, b, 0, RADIO);
            // D5:有効ソース2
            b = data[5];
            addIfSupported(availableSources, b, 6, TI);
            addIfSupported(availableSources, b, 5, IPOD);
            addIfSupported(availableSources, b, 4, APP_MUSIC);
            addIfSupported(availableSources, b, 3, SPOTIFY);
            addIfSupported(availableSources, b, 2, PANDORA);
            addIfSupported(availableSources, b, 1, OFF);
            addIfSupported(availableSources, b, 0, BT_PHONE);
            status.availableSourceTypes = availableSources;
            // D6:有効ソース3
            //  (RESERVED)
            // D7:有効ソース4
            //  (RESERVED)
            // D8:楽曲再生情報1
            //  非推奨
            // D9:楽曲再生情報2
            //  (RESERVED)
            // D10:Tuner情報1
            //  非推奨
            // D11:Tuner情報2
            //  非推奨
            // D12:Tuner情報3
            b = data[12];
            status.seekStep = TunerSeekStep.valueOf((byte) getBitsValue(b, 2, 1));
            // D13:その他1
            b = data[13];
            status.attMode = AttMode.valueOf((byte) getBitsValue(b, 1, 1));
            status.muteMode = MuteMode.valueOf((byte) getBitsValue(b, 0, 1));
            // D14:その他2
            //  (RESERVED)

            if (majorVer >= 2) {
                v2(data, status);
            }

            if (majorVer >= 3) {
                v3(data, status);
            }

            if (majorVer >= 4) {
                v4(data, status);
            }

            if(!old.equals(status)) {
                status.updateVersion();
                mIsUpdated = true;
            }

            Timber.d("process() DeviceStatus = " + status);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }

    private void v2(byte[] data, CarDeviceStatus status) {
        byte b;

        CarDeviceSpec spec = mStatusHolder.getCarDeviceSpec();
        // D15:リスト状態
        b = data[15];
        ListType oldListType = status.listType;
        status.listType = ListType.valueOf((byte) getBitsValue(b, 0, 3), status.sourceType);
        //  ABCサーチリストが解除されたら、ABCサーチのインデックス文字をクリアする
        if (oldListType == ListType.ABC_SEARCH_LIST && status.listType != ListType.ABC_SEARCH_LIST) {
            mStatusHolder.getListInfo().abcSearchWord = "";
            mStatusHolder.getListInfo().updateVersion();
        }
        // D16:設定状態1
        b = data[16];
        status.jasperAudioSettingEnabled = spec.jasperAudioSettingSupported ? isBitOn(b, 5) : false;
        status.functionSettingEnabled = spec.functionSettingSupported ? isBitOn(b, 4) : false;
        status.mixtraxSettingEnabled = spec.mixtraxSettingSupported ? isBitOn(b, 3) : false;
        status.illuminationSettingEnabled = spec.illuminationSettingSupported ? isBitOn(b, 2) : false;
        status.audioSettingEnabled = spec.audioSettingSupported ? isBitOn(b, 1) : false;
        status.systemSettingEnabled = spec.systemSettingSupported ? isBitOn(b, 0) : false;
        // D17:設定状態2
        //  (RESERVED)
        // D18:有効Function設定
        b = data[18];
        status.dabFunctionSettingEnabled = spec.dabFunctionSettingSupported ? isBitOn(b, 2) : false;
        status.hdRadioFunctionSettingEnabled = spec.hdRadioFunctionSettingSupported ? isBitOn(b, 1) : false;
        status.tunerFunctionSettingEnabled = spec.tunerFunctionSettingSupported ? isBitOn(b, 0) : false;
    }

    private void v3(byte[] data, CarDeviceStatus status) {
        byte b;
        CarDeviceSpec spec = mStatusHolder.getCarDeviceSpec();
        MixtraxSettingStatus mixtraxSetting = status.mixtraxSettingStatus;
        // D16:設定状態1
        b = data[16];
        status.phoneSettingEnabled = spec.phoneSettingSupported ? isBitOn(b, 7) : false;
        status.ac2AudioSettingEnabled = spec.ac2AudioSettingSupported ? isBitOn(b, 6) : false;
        // D18:有効Function設定
        b = data[18];
        status.btAudioFunctionSettingEnabled = spec.btAudioFunctionSettingSupported ? isBitOn(b, 3) : false;
        // D19:有効Function設定2
        //  (RESERVED)
        // D20:MIXTRAX設定状態
        b = data[20];
        // 個別の対応有無が無いので、CarDeviceSpec#mixtraxSettingSupportedで判定している
        mixtraxSetting.shortPlayback = ShortPlayback.valueOf((byte) getBitsValue(b, 2, 2));
        mixtraxSetting.soundEffectEnabled = spec.mixtraxSettingSupported ? isBitOn(b, 1) : false;
        mixtraxSetting.settingEnabled = spec.mixtraxSettingSupported ? isBitOn(b, 0) : false;
    }

    private void v4(byte[] data, CarDeviceStatus status) {
        byte b;
        CarDeviceSpec spec = mStatusHolder.getCarDeviceSpec();
        // D17:設定状態2
        b = data[17];
        status.soundFxSettingEnabled = spec.soundFxSettingSupported ? isBitOn(b, 3) : false;
        status.initialSettingEnabled = spec.initialSettingSupported ? isBitOn(b, 2) : false;
        status.naviGuideVoiceSettingEnabled = spec.naviGuideVoiceSettingSupported ? isBitOn(b, 1) : false;
        status.parkingSensorSettingEnabled = spec.parkingSensorSettingSupported ? isBitOn(b, 0) : false;
        // D21:サポート機能1
        b = data[21];
        boolean isDisplayParkingSensor =  isBitOn(b, 2);
        // 表示状態が変わった場合はセンサー状態リセット
        if(status.isDisplayParkingSensor != isDisplayParkingSensor){
            mStatusHolder.getParkingSensorStatus().reset();
        }
        status.isDisplayParkingSensor = isDisplayParkingSensor;
        status.reverseStatus = spec.reverseSenseSupported ? ReverseStatus.valueOf((byte) getBitsValue(b, 1, 1)) : ReverseStatus.OFF;
        status.parkingStatus = spec.parkingSenseSupported ? ParkingStatus.valueOf((byte) getBitsValue(b, 0, 1)) : ParkingStatus.OFF;
        // D22:サポート機能2
        //  (RESERVED)
    }

    private void addIfSupported(Set<MediaSourceType> sources, byte b, int bit, MediaSourceType type) {
        if (isBitOn(b, bit)) {
            sources.add(type);
        }
    }

    /**
     * データ長取得.
     * <p>
     * メジャーバージョンからそれに対応したデータ長を取得する
     * アップデートによりデータ長が変更された場合は本メソッドに追加する
     * <p>
     * 対応したバージョンが存在しない場合は、
     * アップデートされたがデータ長は変更されていないと判断し、
     * 最大のデータ長を返す
     *
     * @param version メジャーバージョン
     * @return データ長
     */
    private int getDataLength(int version) {
        final int V2_DATA_LENGTH = 19;
        final int V3_DATA_LENGTH = 21;
        final int V4_DATA_LENGTH = 23;
        final int MAX_DATA_LENGTH = Math.max(Math.max(V2_DATA_LENGTH, V3_DATA_LENGTH), V4_DATA_LENGTH);

        switch(version){
            case 1:
            case 2:
                return V2_DATA_LENGTH;
            case 3:
                return V3_DATA_LENGTH;
            case 4:
                return V4_DATA_LENGTH;
            default:
                return MAX_DATA_LENGTH;
        }
    }
}
