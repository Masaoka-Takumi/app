package jp.pioneer.carsync.infrastructure.crp.handler.controlcommand;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.VoiceRecognitionResponseType;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.entity.VoiceRecognitionResponse;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * 音声認識通知応答パケットハンドラ.
 * <p>
 * パケットが不正な場合、{@link #getResult()}は{@code null}となる。
 */
public class VoiceRecognitionResponsePacketHandler extends ResponsePacketHandler<VoiceRecognitionResponse> {
    private static final int DATA_LENGTH = 3;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     */
    public VoiceRecognitionResponsePacketHandler(@NonNull CarRemoteSession session) {
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

            // D1:音声認識応答種別
            VoiceRecognitionResponseType type = VoiceRecognitionResponseType.valueOf(data[1]);
            // D2:結果
            // ResponseCodeは「NG」「OK」で、結果は「失敗」「成功」だがResponseCodeで代用する。
            ResponseCode responseCode = ResponseCode.valueOf(data[2]);
            setResult(new VoiceRecognitionResponse(type, responseCode));

            Timber.d("handle() Result = " + getResult());
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "handle()");
        }
    }
}