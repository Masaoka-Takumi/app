package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * パーキングセンサー設定.
 */
public class ParkingSensorSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** パーキングセンサー設定ON. */
    public boolean parkingSensorSetting;
    /** 警告音出力先設定. */
    public AlarmOutputDestinationSetting alarmOutputDestinationSetting;
    /** 警告音量設定. */
    public AlarmVolumeSetting alarmVolumeSetting = new AlarmVolumeSetting();
    /** バック信号極性設定. */
    public BackPolarity backPolarity;

    /**
     * コンストラクタ.
     */
    public ParkingSensorSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        parkingSensorSetting = false;
        alarmOutputDestinationSetting = null;
        alarmVolumeSetting.reset();
        backPolarity = null;
        clear();
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("requestStatus", requestStatus)
                .add("parkingSensorSetting", parkingSensorSetting)
                .add("alarmOutputDestinationSetting", alarmOutputDestinationSetting)
                .add("alarmVolumeSetting", alarmVolumeSetting)
                .add("backPolarity", backPolarity)
                .toString();
    }
    /**
     * タグ.
     */
    public static class Tag {
        public static final String PARKING_SENSOR = "parking_sensor";
        public static final String ALARM_OUTPUT_DESTINATION = "alarm_output_destination";
        public static final String ALARM_VOLUME = "alarm_volume";
        public static final String BACK_POLARITY = "back_polarity";

        /**
         * 全てのタグを取得.
         *
         * @return 全てのタグ名
         */
        public static List<String> getAllTags(){
            return new ArrayList<String>(){{
                add(PARKING_SENSOR);
                add(ALARM_OUTPUT_DESTINATION);
                add(ALARM_VOLUME);
                add(BACK_POLARITY);
            }};
        }
    }

}
