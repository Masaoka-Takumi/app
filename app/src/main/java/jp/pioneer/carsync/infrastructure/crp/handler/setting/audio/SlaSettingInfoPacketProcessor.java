package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.SlaGroup;
import jp.pioneer.carsync.domain.model.SlaSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * SLA設定情報パケットプロセッサ.
 * <p>
 * SLA設定情報応答と通知で使用する。
 */
public class SlaSettingInfoPacketProcessor {
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public SlaSettingInfoPacketProcessor(@NonNull StatusHolder statusHolder) {
        mStatusHolder = checkNotNull(statusHolder);
    }

    /**
     * 処理.
     *
     * @param packet 受信パケット
     * @return {@link Boolean#TRUE}:成功。{@link Boolean#FALSE}:それ以外。
     */
    public Boolean process(@NonNull IncomingPacket packet) {
        try {
            byte[] data = checkNotNull(packet).getData();

            int majorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().major;
            checkPacketDataLength(data, getDataLength(majorVer));
            AudioSetting audioSetting = mStatusHolder.getAudioSetting();
            SlaSetting setting = audioSetting.slaSetting;
            // D1:最大ステップ値
            setting.maximumStep = data[1];
            // D2:最小ステップ値
            setting.minimumStep = data[2];
            // D3:設定値
            setting.currentStep = data[3];

            if (majorVer >= 3) {
                v3(data, setting);
            }

            audioSetting.updateVersion();
            Timber.d("process() SlaSetting = " + setting);
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }

    private void v3(byte[] data, SlaSetting setting) {
        // D4:グループ情報
        setting.group = SlaGroup.valueOf(data[4]);
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
        final int V2_DATA_LENGTH = 4;
        final int V3_DATA_LENGTH = 5;
        final int MAX_DATA_LENGTH = Math.max(V2_DATA_LENGTH, V3_DATA_LENGTH);

        switch(version){
            case 1:
            case 2:
                return V2_DATA_LENGTH;
            case 3:
                return V3_DATA_LENGTH;
            default:
                return MAX_DATA_LENGTH;
        }
    }
}
