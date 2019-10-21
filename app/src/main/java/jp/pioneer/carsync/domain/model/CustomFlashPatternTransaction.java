package jp.pioneer.carsync.domain.model;

import android.support.annotation.Size;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;

/**
 * CUSTOM発光パターントランザクション.
 * <p>
 * indexは110(最大フレーム件数)ずつ増やしていく
 */
public class CustomFlashPatternTransaction {
    private static final int MAX_TRANSACTION_ID = 255;
    private static final int MAX_FRAME = 110;
    private static int sId;
    /** トランザクションID. */
    public int id;
    /** 件数. */
    public int total;
    /** リストインデックス. */
    public int index;
    private int endIndex;
    /** ゾーン情報. */
    private ArrayList<ZoneFrameInfo> zones;

    /**
     * 初期情報設定.
     *
     * @param zones ゾーン情報
     * @throws NullPointerException {@code listType}がnull
     */
    public CustomFlashPatternTransaction(ArrayList<ZoneFrameInfo> zones) {
        this.total = zones.size();
        this.zones = zones;
        this.index = 0;
        this.endIndex = 0;
    }

    /**
     * さらに項目があるか否か取得.
     * <p>
     * {@code true}となった場合、項目を件数分取得し終えていない。
     *
     * @return {@code true}:項目がある。{@code false}:それ以外。（項目を全て取得した）
     */
    public boolean hasNext() {
        if (total == 0 || endIndex + 1 > total) {
            return false;
        } else {
            id = nextTransactionId();
            index = endIndex + 1;
            endIndex += MAX_FRAME;
            if(total < endIndex){
                endIndex = total;
            }
            return true;
        }
    }

    /**
     * ゾーンフレーム情報取得.
     * <p>
     * 車載機へ通知する情報を取得する
     *
     * @return ゾーンフレーム情報
     */
    @Size(max = 110)
    public ZoneFrameInfo[] getZoneFrameInfo(){
        return zones.subList(index - 1, endIndex).toArray(new ZoneFrameInfo[0]);
    }

    /**
     * インデックス取得.
     *
     * @return インデックス
     */
    public int getIndex(){
        return index;
    }

    private static int nextTransactionId() {
        if (++sId > MAX_TRANSACTION_ID) {
            sId = 1;
        }

        return sId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("id", id)
                .add("total", total)
                .add("index", index)
                .toString();
    }
}
