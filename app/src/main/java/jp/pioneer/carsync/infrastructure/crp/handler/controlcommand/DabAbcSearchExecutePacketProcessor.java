package jp.pioneer.carsync.infrastructure.crp.handler.controlcommand;

import android.support.annotation.NonNull;

import com.google.common.base.Objects;

import jp.pioneer.carsync.domain.event.CrpDabAbcSearchResultEvent;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

public class DabAbcSearchExecutePacketProcessor {
    private static final int DATA_LENGTH = 2;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param statusHolder StatusHolder
     * @throws NullPointerException {@code statusHolder}がnull
     */
    public DabAbcSearchExecutePacketProcessor(@NonNull StatusHolder statusHolder) {
        mStatusHolder = checkNotNull(statusHolder);
    }

    /**
     * 処理.
     *
     * @param packet 受信パケット
     * @return {@link Boolean#TRUE}:成功。{@link Boolean#FALSE}:それ以外。
     * @throws NullPointerException {@code packet}がnull
     */
    public Boolean process(@NonNull IncomingPacket packet, CarRemoteSession session) {
        try {
            byte[] data = checkNotNull(packet).getData();
            checkPacketDataLength(data, DATA_LENGTH);

            // D1:結果
            Timber.d("process() abcSearchResult = " + (ubyteToInt(data[1]) == 0x01));
            session.publishEvent(new CrpDabAbcSearchResultEvent((ubyteToInt(data[1]) == 0x01)));
            return Boolean.TRUE;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "process()");
            return Boolean.FALSE;
        }
    }
}
