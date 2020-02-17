package jp.pioneer.carsync.infrastructure.crp.handler.setting.function;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.DabFunctionSetting;
import jp.pioneer.carsync.domain.model.FMTunerSetting;
import jp.pioneer.carsync.domain.model.HdRadioFunctionSetting;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TASetting;
import jp.pioneer.carsync.domain.model.TunerFunctionSetting;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * Function設定情報パケットプロセッサ.
 * <p>
 * Function設定情報応答と通知で使用する。
 */
public class FunctionSettingInfoPacketProcessor {
    private static final int TUNER_DATA_LENGTH = 11;
    private static final int DAB_DATA_LENGTH = 5;
    private static final int HD_RADIO_DATA_LENGTH = 7;
    private static final int MIN_DATA_LENGTH = Math.min(Math.min(TUNER_DATA_LENGTH, DAB_DATA_LENGTH), HD_RADIO_DATA_LENGTH);
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public FunctionSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
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
            checkPacketDataLength(data, MIN_DATA_LENGTH);

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
                default:
                    throw new AssertionError("can't happen.");
            }

            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }

    private void processRadio(byte[] data) throws BadPacketException {
        checkPacketDataLength(data, TUNER_DATA_LENGTH);

        TunerFunctionSetting setting = mStatusHolder.getTunerFunctionSetting();
        // D2:BSM設定
        setting.bsmSetting = (ubyteToInt(data[2]) == 0x01);
        // D3:LOCAL設定
        setting.localSetting = LocalSetting.valueOf(data[3]);
        // D4:FM Tuner設定
        setting.fmTunerSetting = FMTunerSetting.valueOf(data[4]);
        // D5:REG広域設定
        setting.regSetting = (ubyteToInt(data[5]) == 0x01);
        // D6:TA設定
        setting.taSetting = (ubyteToInt(data[6]) == 0x01);
        if(mStatusHolder.getCarDeviceSpec().dabFunctionSettingSpec.taSettingSupported) {
            setting.taDabSetting = TASetting.valueOf(data[6]);
            Timber.d("taDabSetting= "+setting.taDabSetting);
        }
        // D7:AF設定
        setting.afSetting = (ubyteToInt(data[7]) == 0x01);
        // D8:NEWS設定
        setting.newsSetting = (ubyteToInt(data[8]) == 0x01);
        // D9:ALARM設定
        setting.alarmSetting = (ubyteToInt(data[9]) == 0x01);
        // D10:P.CH/MANUAL設定
        setting.pchManualSetting = PCHManualSetting.valueOf(data[10]);

        setting.updateVersion();
        Timber.d("processRadio() TunerFunctionSetting = " + setting);
    }

    private void processDab(byte[] data) throws BadPacketException {
        checkPacketDataLength(data, DAB_DATA_LENGTH);

        DabFunctionSetting setting = mStatusHolder.getDabFunctionSetting();
        // D2:TA設定
        setting.taSetting = TASetting.valueOf(data[2]);
        // D3:SERVICE FOLLOW ON/OFF設定
        setting.serviceFollowSetting = (ubyteToInt(data[3]) == 0x01);
        // D4:SOFTLINK設定
        setting.softlinkSetting = (ubyteToInt(data[4]) == 0x01);

        setting.updateVersion();
        Timber.d("processDab() DabFunctionSetting = " + setting);
    }

    private void processHdRadio(byte[] data) throws BadPacketException {
        checkPacketDataLength(data, HD_RADIO_DATA_LENGTH);

        HdRadioFunctionSetting setting = mStatusHolder.getHdRadioFunctionSetting();
        // D2:BSM設定
        setting.bsmSetting = (ubyteToInt(data[2]) == 0x01);
        // D3:LOCAL設定
        setting.localSetting = LocalSetting.valueOf(data[3]);
        // D4:HD SEEK設定
        setting.hdSeekSetting = (ubyteToInt(data[4]) == 0x01);
        // D5:BLENDING設定
        setting.blendingSetting = (ubyteToInt(data[5]) == 0x01);
        // D6:ACTIVE RADIO設定
        setting.activeRadioSetting = (ubyteToInt(data[6]) == 0x01);

        setting.updateVersion();
        Timber.d("processHdRadio() HdRadioFunctionSetting = " + setting);
    }
}
