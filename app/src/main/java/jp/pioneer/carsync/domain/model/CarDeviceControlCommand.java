package jp.pioneer.carsync.domain.model;

import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;

import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * 車載機操作コマンド.
 * <p>
 * {@link #code}が{@code 0x40}未満はリモコンキーのコマンドになるので、
 * どういう動作になるかは車載機の機能仕様書を参照。他はコマンド名から推測。
 */
public enum CarDeviceControlCommand {
    /** BAND/ESC. */
    BAND_ESC(0x00),
    /** CENTER PUSH. */
    CENTER_PUSH(0x01),
    /** CROSS UP. */
    CROSS_UP(0x02),
    /** CROSS DOWN. */
    CROSS_DOWN(0x03),
    /** CROSS RIGHT. */
    CROSS_RIGHT(0x04),
    /** CROSS LEFT. */
    CROSS_LEFT(0x05),
    /** VOLUME UP. */
    VOLUME_UP(0x06),
    /** VOLUME DOWN. */
    VOLUME_DOWN(0x07),
    /** SOURCE. */
    SOURCE(0x08),
    /** FUNCTION. */
    FUNCTION(0x09),
    /** AUDIO. */
    AUDIO(0x0A),
    /** OFF HOOK. */
    OFF_HOOK(0x0B),
    /** ON HOOK. */
    ON_HOOK(0x0C),
    /** PHONE. */
    PHONE(0x0D),
    /** MUTE. */
    MUTE(0x0E),
    /** DISP/SCROLL. */
    DISP_SCROLL(0x0F),
    /** ATT. */
    ATT(0x10),
    /** PAUSE ON/OFF. */
    PAUSE(0x11),
    /** 音声認識スタート. */
    VOICE_RECOGNITION_START(0x12),
    /** 音声認識キャンセル. */
    VOICE_RECOGNITION_CANCEL(0x13),
    /** プリセットキー1. */
    PRESET_KEY_1(0x40),
    /** プリセットキー2. */
    PRESET_KEY_2(0x41),
    /** プリセットキー3. */
    PRESET_KEY_3(0x42),
    /** プリセットキー4. */
    PRESET_KEY_4(0x43),
    /** プリセットキー5. */
    PRESET_KEY_5(0x44),
    /** プリセットキー6. */
    PRESET_KEY_6(0x45),
    /** SEEK UP. */
    SEEK_UP(0x80),
    /** LIVE. */
    LIVE(0x82),
    /** PLAY / PAUSE. */
    PLAY_PAUSE(0x83),
    /** TIME SHIFT. */
    TIME_SHIFT(0x84),
    /** CANCEL(LIST UPDATE). */
    CANCEL_LIST_UPDATE(0x85),
    /** TUNE SCAN. */
    TUNE_SCAN(0x86),
    /** TUNE MIX. */
    TUNE_MIX(0x87),
    /** REPLAY MODE. */
    REPLAY_MODE(0x88),
    /** SCAN PLAY. */
    SCAN_PLAY(0x89),
    /** SCAN ESC. */
    SCAN_ESC(0x8A),
    /** START RADIO. */
    START_RADIO(0x8B)
    ;

    /** プロトコルでの定義値. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    CarDeviceControlCommand(int code) {
        this.code = code;
    }

    /**
     * プロトコルでの定義値から取得.
     *
     * @param code プロトコルでの定義値
     * @return プロトコルでの定義値に該当するCarDeviceControlCommand
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static CarDeviceControlCommand valueOf(byte code) {
        for (CarDeviceControlCommand value : values()) {
            if (value.code == PacketUtil.ubyteToInt(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException("invalid value: " + code);
    }

    /**
     * プリセットCH番号 => PRESET_KEY_* のコンビニ.
     *
     * @param presetNo プリセットCH番号
     * @return 指定引数の1:1写像である列挙子, 見つからなければnull.
     */
    public static @Nullable CarDeviceControlCommand presetKey(int presetNo) {
        return PRESET_KEY_x.get(presetNo);
    }
    private static final SparseArrayCompat<CarDeviceControlCommand> PRESET_KEY_x = new SparseArrayCompat<CarDeviceControlCommand>() {{
        put(1, CarDeviceControlCommand.PRESET_KEY_1);
        put(2, CarDeviceControlCommand.PRESET_KEY_2);
        put(3, CarDeviceControlCommand.PRESET_KEY_3);
        put(4, CarDeviceControlCommand.PRESET_KEY_4);
        put(5, CarDeviceControlCommand.PRESET_KEY_5);
        put(6, CarDeviceControlCommand.PRESET_KEY_6);
    }};
}
