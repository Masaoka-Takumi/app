package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.EnumSet;
import java.util.Set;

/**
 * スマートフォンステータス情報.
 */
public class SmartPhoneStatus {
    /** シャッフルモード. */
    public ShuffleMode shuffleMode;
    /** リピートモード. */
    public SmartPhoneRepeatMode repeatMode;
    /** 再生状態. */
    public PlaybackMode playbackMode;
    /** サーチリスト表示中. */
    public boolean showingSearchList;
    /** デバイスリスト表示中. */
    public boolean showingDeviceList;
    /** 発生しているADASの警告イベント. */
    public Set<AdasWarningEvent> adasWarningEvents;
    /** 99App Service(BLE). */
    public AppServicePublicationStatus bleAppServicePublicationStatus;

    /**
     * コンストラクタ.
     */
    public SmartPhoneStatus() {
        shuffleMode = ShuffleMode.OFF;
        repeatMode = SmartPhoneRepeatMode.ALL;
        playbackMode = PlaybackMode.STOP;
        showingSearchList = false;
        showingDeviceList = false;
        adasWarningEvents = EnumSet.noneOf(AdasWarningEvent.class);
        bleAppServicePublicationStatus = AppServicePublicationStatus.OFF;
    }

    /**
     * リセット.
     */
    public void reset() {
        showingSearchList = false;
        showingDeviceList = false;
    }

    /**
     * ADAS警告状態取得.
     * <p>
     * 現在発生しているADASの警告イベントから警告状態を取得する
     * 車線逸脱の場合の警告状態は連続
     * 歩行者衝突予測、前方衝突予測の警告状態は単発
     * 発生していない場合は警告なし
     *
     * @return ADAS警告状態
     */
    public AdasWarningStatus getAdasWarningStatus(){
        int warningQuantity = adasWarningEvents.size();

        if (warningQuantity > 0) {
            if (adasWarningEvents.contains(AdasWarningEvent.PEDESTRIAN_WARNING_EVENT) || adasWarningEvents.contains(AdasWarningEvent.PEDESTRIAN_CAREFUL_EVENT)
                    || adasWarningEvents.contains(AdasWarningEvent.FORWARD_TTC_COLLISION_EVENT) || adasWarningEvents.contains(AdasWarningEvent.FORWARD_HEADWAY_COLLISION_EVENT)) {
                //歩行者衝突予測、前方衝突予測
                return AdasWarningStatus.SINGLE;
            } else if (adasWarningEvents.contains(AdasWarningEvent.OFF_ROAD_LEFT_SOLID_EVENT) || adasWarningEvents.contains(AdasWarningEvent.OFF_ROAD_LEFT_DASH_EVENT)
                    || adasWarningEvents.contains(AdasWarningEvent.OFF_ROAD_RIGHT_SOLID_EVENT) || adasWarningEvents.contains(AdasWarningEvent.OFF_ROAD_RIGHT_DASH_EVENT)) {
                //車線逸脱
                return AdasWarningStatus.CONTINUOUS;
            }
            return AdasWarningStatus.SINGLE;
        } else {
            return AdasWarningStatus.NONE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("shuffleMode", shuffleMode)
                .add("repeatMode", repeatMode)
                .add("playbackMode", playbackMode)
                .add("showingSearchList", showingSearchList)
                .add("showingDeviceList", showingDeviceList)
                .add("adasWarningEvents", adasWarningEvents)
                .add("bleAppServicePublicationStatus", bleAppServicePublicationStatus)
                .toString();
    }
}
