package jp.pioneer.carsync.infrastructure.crp.handler.devicestatus;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * オーディオ設定ステータス情報パケットプロセッサ.
 * <p>
 * オーディオ設定ステータス情報応答と通知で使用する。
 */
public class AudioSettingStatusPacketProcessor {
    private static final int DATA_LENGTH = 6;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public AudioSettingStatusPacketProcessor(@NonNull StatusHolder statusHolder) {
        mStatusHolder = checkNotNull(statusHolder);
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
            byte[] data = checkNotNull(packet).getData();
            checkPacketDataLength(data, DATA_LENGTH);

            int majorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().major;
            AudioSettingSpec spec = mStatusHolder.getCarDeviceSpec().audioSettingSpec;
            AudioSettingStatus status = mStatusHolder.getAudioSettingStatus();
            byte b;
            // D1:有効オーディオ設定1
            b = data[1];
            status.listeningPositionSettingEnabled = spec.listeningPositionSettingSupported ? isBitOn(b, 7) : false;
            status.crossoverSettingEnabled = spec.crossoverSettingSupported ? isBitOn(b, 6) : false;
            status.speakerLevelSettingEnabled = spec.speakerLevelSettingSupported ? isBitOn(b, 5) : false;
            status.subwooferPhaseSettingEnabled = spec.subwooferPhaseSettingSupported ? isBitOn(b, 4) : false;
            status.subwooferSettingEnabled = spec.subwooferSettingSupported ? isBitOn(b, 3) : false;
            status.balanceSettingEnabled = spec.balanceSettingSupported ? isBitOn(b, 2) : false;
            status.faderSettingEnabled = spec.faderSettingSupported ? isBitOn(b, 1) : false;
            status.equalizerSettingEnabled = spec.equalizerSettingSupported ? isBitOn(b, 0) : false;
            // D2:有効オーディオ設定2
            b = data[2];
            status.slaSettingEnabled = spec.slaSettingSupported ? isBitOn(b, 7) : false;
            status.alcSettingEnabled = spec.alcSettingSupported ? isBitOn(b, 6) : false;
            status.loudnessSettingEnabled = spec.loudnessSettingSupported ? isBitOn(b, 5) : false;
            status.bassBoosterSettingEnabled = spec.bassBoosterSettingSupported ? isBitOn(b, 4) : false;
            status.loadSettingEnabled = spec.loadSettingSupported ? isBitOn(b, 3) : false;
            status.saveSettingEnabled = spec.saveSettingSupported ? isBitOn(b, 2) : false;
            status.aeqSettingEnabled = spec.aeqSettingSupported ? isBitOn(b, 1) : false;
            status.timeAlignmentSettingEnabled = spec.timeAlignmentSettingSupported ? isBitOn(b, 0) : false;
            // D3:有効オーディオ設定3
            b = data[3];
            status.audioDataBulkSettingEnabled = spec.audioDataBulkUpdateSupported ? isBitOn(b, 0) : false;
            // D4:有効オーディオ設定4
            //  (RESERVED)
            // D5:オーディオ設定 その他
            b = data[5];
            status.rearSpeakerEnabled = isBitOn(b, 5);
            status.subwooferSpeakerEnabled = isBitOn(b, 4);
            status.timeAlignmentPresetAtaEnabled = spec.timeAlignmentPresetAtaSupported ? isBitOn(b, 3) : false;
            status.saveSettingEnabled2 = isBitOn(b, 2);
            status.loadAeqSettingEnabled = spec.loadAeqSettingSupported ? isBitOn(b, 1) : false;
            status.loadSoundSettingEnabled = spec.loadSoundSettingSupported ? isBitOn(b, 0) : false;

            if (majorVer >= 3) {
                v3(data, status);
            }

            if (majorVer >= 4) {
                v4(data, status);
            }

            status.updateVersion();
            Timber.d("process() AudioSettingStatus = " + status);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }

    private void v3(byte[] data, AudioSettingStatus status) {
        byte b;

        AudioSettingSpec spec = mStatusHolder.getCarDeviceSpec().audioSettingSpec;
        // D3:有効オーディオ設定3
        b = data[3];
        status.levelSettingEnabled = spec.levelSettingSupported ? isBitOn(b, 2) : false;
        status.beatBlasterSettingEnabled = spec.beatBlasterSettingSupported ? isBitOn(b, 1) : false;
    }

    private void v4(byte[] data, AudioSettingStatus status) {
        byte b;

        AudioSettingSpec spec = mStatusHolder.getCarDeviceSpec().audioSettingSpec;
        // D3:有効オーディオ設定3
        b = data[3];
        status.soundRetrieverSettingEnabled = spec.soundRetrieverSettingSupported ? isBitOn(b, 3) : false;
    }
}
