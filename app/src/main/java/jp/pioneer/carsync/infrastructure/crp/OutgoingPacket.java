package jp.pioneer.carsync.infrastructure.crp;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import java.io.ByteArrayOutputStream;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.toHex;

/**
 * 送信パケット.
 */
public class OutgoingPacket {
    private static final int DLE = 0x9F;
    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int ESCAPE = 0x9F;

    /** 送信パケットIDタイプ. */
    public final OutgoingPacketIdType packetIdType;
    /** データ部（D0-n）*/
    public final byte[] data;
    /** チェックサム. */
    public final int checksum;
    /** パケットのシーケンス番号. */
    public int seqNumber;

    /**
     * コンストラクタ.
     *
     * @param packetIdType パケットIDタイプ
     * @param data データ
     * @throws NullPointerException {@code packetIdType}、または、{@code data}がnull
     */
    public OutgoingPacket(@NonNull OutgoingPacketIdType packetIdType, @NonNull byte[] data) {
        this.packetIdType = checkNotNull(packetIdType);
        this.data = checkNotNull(data);
        this.checksum = PacketUtil.createChecksum(packetIdType.id, packetIdType.type, data);
    }

    /**
     * バイト列化.
     * <p>
     * CarRemote Protocolの１パケットの通信フォーマットに適合する。
     *
     * @return バイト列
     */
    @NonNull
    public byte[] toByteArray() {
        // id, type, Dn, CSをdataStreamに書き込む
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream(1024);
        dataStream.write(packetIdType.id);
        dataStream.write(packetIdType.type);
        dataStream.write(data, 0, data.length);
        dataStream.write(checksum);

        // id, type, Dn, CSに0x9Fが含まれる場合はエスケープさせる
        byte[] escaped = PacketUtil.escape(dataStream.toByteArray());

        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        stream.write(DLE);
        stream.write(STX);
        stream.write(escaped, 0, escaped.length);
        stream.write(DLE);
        stream.write(ETX);
        return stream.toByteArray();
    }

    public boolean shouldWaitForResponse() {
        return packetIdType.responsePacketIdType != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("packetIdType", packetIdType)
                .add("data", "[" + toHex(data) + "]")
                .add("checksum", toHex((byte) checksum))
                .add("seqNumber", seqNumber)
                .toString();
    }
}
