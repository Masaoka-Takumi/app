package jp.pioneer.carsync.infrastructure.crp;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import java.nio.ByteBuffer;
import java.util.Arrays;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.toHex;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * 受信パケット.
 */
public class IncomingPacket {
    private static final int DLE = 0x9F;
    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int ESCAPE = 0x9F;

    enum Phase {
        /** 1バイト目の DLE 待ち. */
        STX_DLE,
        /** 2バイト目の STX 待ち. */
        STX_STX,
        /** ID待ち. */
        ID,
        /** TYPE待ち. */
        TYPE,
        /** 残り. */
        DATA
    }

    private boolean mIsComplete;
    private int mId;
    private int mType;
    private IncomingPacketIdType mPacketIdType;
    private byte[] mPacket = new byte[1024];
    private ByteBuffer mPacketStream = ByteBuffer.wrap(mPacket);
    private byte[] mData = new byte[1024];
    private ByteBuffer mDataStream = ByteBuffer.wrap(mData);
    private Phase mPhase = Phase.STX_DLE;
    private boolean mIsEscaped;

    /**
     * コンストラクタ.
     */
    public IncomingPacket() {
    }

    /**
     * 生データ取得.
     *
     * @return 受信データ（未加工）
     */
    @NonNull
    public byte[] getRawPacket() {
        return Arrays.copyOf(mPacket, mPacketStream.position());
    }

    /**
     * データ部取得（アンエスケープ済）.
     *
     * @return データ部取得（アンエスケープ済）
     */
    @NonNull
    public byte[] getData() {
        return Arrays.copyOf(mData, mDataStream.position());
    }

    /**
     * 受信パケットIDタイプ取得.
     *
     * @return 受信パケットIDタイプ
     */
    public IncomingPacketIdType getPacketIdType() {
        return mPacketIdType;
    }

    /**
     * 受信データ追加.
     *
     * @param b 受信データ（1バイト分）
     * @throws IllegalStateException パケットの終了（ETX）を既に受け取っている
     * @throws IllegalArgumentException パケットフォーマット不正、チェックサム不一致、不明な受信パケットIDタイプ
     */
    public void appendByte(int b) {
        if (mIsComplete) {
            throw new IllegalStateException("This packet is completed.");
        }

        mPacketStream.put((byte) b);
        if (mPhase == Phase.STX_DLE) {
            if (b != DLE) {
                throw new IllegalArgumentException("must be DLE:" + toHex((byte) b));
            }
            mPhase = Phase.STX_STX;
            return;
        }
        if (mPhase == Phase.STX_STX) {
            if (b != STX) {
                throw new IllegalArgumentException("must be STX:" + toHex((byte) b));
            }
            mPhase = Phase.ID;
            return;
        }

        // ID, TYPE, DATA以降のPhase

        // 1バイト目の0x9Fは無視
        if (b == ESCAPE) {
            if (!mIsEscaped) {
                mIsEscaped = true;
                return;
            }
        }

        if (mIsEscaped) {
            mIsEscaped = false;

            if (b == ETX) {
                // パケットの終了 (DLE, ETX)

                // checksumを取得
                int checksum = ubyteToInt(mDataStream.get(mDataStream.position() - 1));

                // CSはdataではないのでpositionを戻す
                mDataStream.position(mDataStream.position() - 1);

                // checksumが正しいか確認
                int expectedChecksum = PacketUtil.createChecksum(mId, mType, getData());
                if (expectedChecksum != checksum) {
                    throw new IllegalArgumentException(String.format("invalid checksum. (Expected, Actual) = %s, %s",
                            toHex((byte) expectedChecksum), toHex((byte) checksum)));
                }

                // IncomingPacketIdTypeを取得
                int d0 = getD0();
                mPacketIdType = IncomingPacketIdType.valueOf(mId, mType, d0);
                mIsComplete = true;
                return;
            } else if (b != ESCAPE) {
                throw new IllegalArgumentException("must be either ETX or ESCAPE:" + toHex((byte) b));
            }
        }

        if (mPhase == Phase.ID) {
            mId = b;
            mPhase = Phase.TYPE;
        } else if (mPhase == Phase.TYPE) {
            mType = b;
            mPhase = Phase.DATA;
        } else if (mPhase == Phase.DATA) {
            mDataStream.put((byte) b);
        } else {
            throw new IllegalStateException("Unexpected phase:" + mPhase);
        }
    }

    /**
     * 受信パケットを正しく読み込めたか否か取得.
     *
     * @return {@code true}:読み込めた。{@code false}:それ以外。
     */
    public boolean isCompleted() {
        return mIsComplete;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("phase", mPhase)
                .add("packetIdType", mPacketIdType)
                .add("id", toHex((byte) mId))
                .add("type", toHex((byte) mType))
                .add("data", "[" + toHex(getData()) + "]")
                .toString();
    }

    private int getD0() {
        byte[] data = getData();
        if (data.length > 0) {
            return ubyteToInt(data[0]);
        } else {
            throw new IllegalStateException("D0 is not defined.");
        }
    }
}
