package jp.pioneer.carsync.infrastructure.crp.handler.settingcommand;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.LoadSettingsType;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.entity.LoadSettingResponse;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * LOAD SETTING実行応答パケットハンドラ.
 * <p>
 * パケットが不正な場合、{@link #getResult()}は{@code null}となる。
 */
public class LoadSettingNotificationResponsePacketHandler extends ResponsePacketHandler<LoadSettingResponse> {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     */
    public LoadSettingNotificationResponsePacketHandler(@NonNull CarRemoteSession session) {
        mStatusHolder = checkNotNull(session).getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, DATA_LENGTH);

            // D1:LOAD SETTING種別
            LoadSettingsType type = LoadSettingsType.valueOf(data[1]);
            // D2:結果
            // ResponseCodeは「NG」「OK」で、結果は「失敗」「成功」だがResponseCodeで代用する。
            ResponseCode responseCode = ResponseCode.valueOf(data[2]);
            setResult(new LoadSettingResponse(type, responseCode));

            if (responseCode == ResponseCode.OK) {
                mStatusHolder.getAudioSetting().requestStatus = RequestStatus.NOT_SENT;
            }

            Timber.d("handle() Result = " + getResult());
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "handle()");
        }
    }
}
