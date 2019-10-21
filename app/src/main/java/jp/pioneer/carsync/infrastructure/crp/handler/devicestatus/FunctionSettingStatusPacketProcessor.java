package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.BtAudioFunctionSettingStatus;
import jp.pioneer.carsync.domain.model.BtAudioSettingSpec;
import jp.pioneer.carsync.domain.model.DabFunctionSettingSpec;
import jp.pioneer.carsync.domain.model.DabFunctionSettingStatus;
import jp.pioneer.carsync.domain.model.HdRadioFunctionSettingSpec;
import jp.pioneer.carsync.domain.model.HdRadioFunctionSettingStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingSpec;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingStatus;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * Function設定ステータス情報パケットプロセッサ.
 * <p>
 * Function設定ステータス情報応答と通知で使用する。
 */
public class FunctionSettingStatusPacketProcessor {
    private static final int DATA_LENGTH = 4;
    private StatusHolder mStatusHolder;
    private int mMajorVer;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public FunctionSettingStatusPacketProcessor(@NonNull StatusHolder statusHolder) {
        mStatusHolder = checkNotNull(statusHolder);
    }

    /**
     * 処理.
     *
     * @param packet 受信パケット
     * @return {@link Boolean#TRUE}:成功。{@link Boolean#FALSE}:それ以外。
     * @throws NullPointerException {@code packet}がnull
     */
    public Boolean process(@NonNull IncomingPacket packet) {
        try {
            byte[] data = checkNotNull(packet).getData();
            checkPacketDataLength(data, DATA_LENGTH);

            mMajorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().major;

            // D1:ソース情報
            MediaSourceType sourceType = MediaSourceType.valueOf(data[1]);
            // D2-:ソース毎の情報
            switch (sourceType) {
                case RADIO:
                    processRadio(data);
                    break;
                case DAB:
                    processDab(data);
                    break;
                case HD_RADIO:
                    processHdRadio(data);
                    break;
                case BT_AUDIO:
                    processBtAudio(data);
                    break;
                default:
                    throw new AssertionError("can't happen.");
            }

            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }

    private void processRadio(byte[] data) {
        byte b;
        TunerFunctionSettingSpec spec = mStatusHolder.getCarDeviceSpec().tunerFunctionSettingSpec;
        TunerFunctionSettingStatus status = mStatusHolder.getTunerFunctionSettingStatus();
        // D2:有効Function設定情報1
        b = data[2];
        status.alarmSettingEnabled = spec.alarmSettingSupported ? isBitOn(b, 7) : false;
        status.newsSettingEnabled = spec.newsSettingSupported ? isBitOn(b, 6) : false;
        status.afSettingEnabled = spec.afSettingSupported ? isBitOn(b, 5) : false;
        status.taSettingEnabled = spec.taSettingSupported ? isBitOn(b, 4) : false;
        status.regSettingEnabled = spec.regSettingSupported ? isBitOn(b, 3) : false;
        status.fmSettingEnabled = spec.fmSettingSupported ? isBitOn(b, 2) : false;
        status.localSettingEnabled = spec.localSettingSupported ? isBitOn(b, 1) : false;
        status.bsmSettingEnabled = spec.bsmSettingSupported ? isBitOn(b, 0) : false;
        // D3:有効Function設定情報2
        b = data[3];
        status.pchManualEnabled = spec.pchManualSupported ? isBitOn(b, 0) : false;

        if (mMajorVer >= 4) {
            v4Radio(data, status);
        }

        status.updateVersion();
        Timber.d("processRadio() TunerFunctionSettingStatus = " + status);
    }

    private void v4Radio(byte[] data, TunerFunctionSettingStatus status) {
        byte b;
        TunerFunctionSettingSpec spec = mStatusHolder.getCarDeviceSpec().tunerFunctionSettingSpec;
        // D3:有効Function設定情報2
        b = data[3];
        status.ptySearchSettingEnabled = spec.ptySearchSettingSupported ? isBitOn(b, 1) : false;
    }

    private void processDab(byte[] data) {
        byte b;
        DabFunctionSettingSpec spec = mStatusHolder.getCarDeviceSpec().dabFunctionSettingSpec;
        DabFunctionSettingStatus status = mStatusHolder.getDabFunctionSettingStatus();
        // D2:有効Function設定情報1
        b = data[2];
        status.taSettingEnabled = spec.taSettingSupported ? isBitOn(b, 2) : false;
        status.serviceFollowSettingEnabled = spec.serviceFollowSettingSupported ? isBitOn(b, 1) : false;
        status.softlinkSettingEnabled = spec.softlinkSettingSupported ? isBitOn(b, 0) : false;
        // D3:有効Function設定情報2
        //  (RESERVED)

        status.updateVersion();
    }

    private void processHdRadio(byte[] data) {
        byte b;
        HdRadioFunctionSettingSpec spec = mStatusHolder.getCarDeviceSpec().hdRadioFunctionSettingSpec;
        HdRadioFunctionSettingStatus status = mStatusHolder.getHdRadioFunctionSettingStatus();
        // D2:有効Function設定情報1
        b = data[2];
        status.activeRadioSettingEnabled = spec.activeRadioSettingSupported ? isBitOn(b, 4) : false;
        status.blendingSettingEnabled = spec.blendingSettingSupported ? isBitOn(b, 3) : false;
        status.hdSeekSettingEnabled = spec.hdSeekSettingSupported ? isBitOn(b, 2) : false;
        status.localSettingEnabled = spec.localSettingSupported ? isBitOn(b, 1) : false;
        status.bsmSettingEnabled = spec.bsmSettingSupported ? isBitOn(b, 0) : false;
        // D3:有効Function設定情報2
        //  (RESERVED)

        status.updateVersion();
    }

    private void processBtAudio(byte[] data) {
        byte b;
        BtAudioSettingSpec spec = mStatusHolder.getCarDeviceSpec().btAudioSettingSpec;
        BtAudioFunctionSettingStatus status = mStatusHolder.getBtAudioFunctionSettingStatus();
        // D2:有効Function設定情報1
        b = data[2];
        status.audioDeviceSelectEnabled = spec.audioDeviceSelectSupported ? isBitOn(b, 0) : false;
        // D3:有効Function設定情報2
        //  (RESERVED)

        status.updateVersion();
    }
}
