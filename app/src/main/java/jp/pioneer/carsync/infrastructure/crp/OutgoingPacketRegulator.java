package jp.pioneer.carsync.infrastructure.crp;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.CarRemoteSessionLifeCycle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 送信パケット調整.
 * <p>
 * 同じ種類のAudio設定を行うパケットは最低300msの間隔を空けて送信する必要がある。
 * 本クラスはAudio設定系のパケット送信を記録し、送信可否の判断を行う。
 */
@CarRemoteSessionLifeCycle
public class OutgoingPacketRegulator {
    @Inject SessionConfig mSessionConfig;
    // ARCのコメント曰く:
    // 「最大でも 300ms/15ms で 20 packet 程度を保持しておけばよいので、性能的に ArrayList でも問題ないはず」
    private List<Record> mRecords = new ArrayList<>();

    @Inject
    public OutgoingPacketRegulator() {
    }

    /**
     * パケットを送信可能か否か.
     * <p>
     * 送信パケットがAudio設定系かに関係なく本メソッドを呼び出すこと。
     *
     * @param packet 送信パケット
     * @return {@code true}:送信可。{@code false}:送信不可。
     */
    public boolean canSend(@NonNull OutgoingPacket packet) {
        checkNotNull(packet);

        if (!shouldRegulate(packet)) {
            return true;
        }

        long now = SystemClock.elapsedRealtime();
        long threshold = expiryThreshold(now);
        boolean exists = Stream.of(mRecords)
                .map(r -> r.expire(threshold))
                .anyMatch(r -> !r.expired && r.matches(packet));

        removeExpiredRecords();
        if (!exists) {
            record(packet);
        }

        return !exists;
    }

    private boolean shouldRegulate(@NonNull OutgoingPacket packet) {
        return (packet.packetIdType.id == 0x22) && (packet.packetIdType.type == 0x01);
    }

    private void record(OutgoingPacket packet) {
        long now = SystemClock.elapsedRealtime();
        long threshold = expiryThreshold(now);
        Stream.of(mRecords)
                .forEach(record -> record.expire(threshold));
        removeExpiredRecords();
        Record record = new Record(packet, now);
        mRecords.add(record);
    }

    private long expiryThreshold(long now) {
        return now - mSessionConfig.getSendRegulationInterval();
    }

    private void removeExpiredRecords() {
        List<Record> expired = Stream.of(mRecords)
                .filter(record -> record.expired)
                .collect(Collectors.toList());
        mRecords.removeAll(expired);
    }

    static class Record {
        private int mId;
        private int mType;
        private byte mD0;
        private long mTimestamp;
        boolean expired;

        Record(OutgoingPacket packet, long timestamp) {
            mId = packet.packetIdType.id;
            mType = packet.packetIdType.type;
            mD0 = packet.data[0];
            mTimestamp = timestamp;
        }

        Record expire(long threshold) {
            if (mTimestamp <= threshold) {
                expired = true;
            }

            return this;
        }

        boolean matches(OutgoingPacket packet) {
            return (mId == packet.packetIdType.id)
                    && (mType == packet.packetIdType.type)
                    && (mD0 == packet.data[0]);
        }
    }
}
