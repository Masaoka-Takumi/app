package jp.pioneer.carsync.domain.model;

import java.util.Locale;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;

/**
 * SmartPhone操作コマンド.
 * <p>
 * SmartPhone操作コマンドは操作コマンドとコマンドの状態で定義する
 */
public enum SmartPhoneControlCommand {
    /** Back. */
    BACK(0x00, 0x00, R.string.control_command_back),
    BACK_LONG(0x00, 0x01, R.string.control_command_back),
    /** Music. */
    MUSIC(0x01, 0x00, R.string.control_command_music),
    MUSIC_LONG(0x01, 0x01, R.string.control_command_music),
    /** Navi. */
    NAVI(0x02, 0x00, R.string.control_command_navi),
    NAVI_LONG(0x02, 0x01, R.string.control_command_navi),
    /** Phone. */
    PHONE(0x03 , 0x00, R.string.control_command_phone),
    /** DirectCall. */
    DIRECT_CALL(0x03, 0x01, R.string.control_command_phone),
    /** Mail. */
    MAIL(0x05, 0x00, R.string.control_command_mail),
    MAIL_LONG(0x05, 0x01, R.string.control_command_mail),
    /** VR. */
    VR(0x06, 0x00, R.string.control_command_vr),
    VR_LONG(0x06, 0x01, R.string.control_command_vr),
    /** App. */
    APP(0x07, 0x00, R.string.control_command_app),
    /** AV. */
    AV(0x07, 0x01, R.string.control_command_app),

    ;

    /** 操作コマンドのプロトコルでの定義値. */
    public final int controlCommandCode;

    /** コマンド状態のプロトコルでの定義値. */
    public final int commandStatusCode;

    /** 操作コマンドの表示文字列. */
    public final int label;
    /**
     * コンストラクタ.
     *
     * @param controlCommandCode 操作コマンドのプロトコルでの定義値
     * @param commandStatusCode コマンド状態のプロトコルでの定義値
     */
    SmartPhoneControlCommand(int controlCommandCode, int commandStatusCode, int label) {
        this.controlCommandCode = controlCommandCode;
        this.commandStatusCode = commandStatusCode;
        this.label = label;
    }

    public boolean isLongPress(){
        return commandStatusCode == 0x01;
    }
    /**
     * プロトコルでの定義値から取得.
     *
     * @param controlCommandCode 操作コマンドのプロトコルでの定義値
     * @param commandStatusCode コマンド状態のプロトコルでの定義値
     * @return プロトコルでの定義値に該当するSmartPhoneControlCommand
     * @throws IllegalArgumentException プロトコルでの定義値に該当するものがない
     */
    public static SmartPhoneControlCommand valueOf(byte controlCommandCode, byte commandStatusCode) {
        for (SmartPhoneControlCommand value : values()) {
            if (value.controlCommandCode == PacketUtil.ubyteToInt(controlCommandCode)
                    && value.commandStatusCode == PacketUtil.ubyteToInt(commandStatusCode)) {
                return value;
            }
        }

        throw new IllegalArgumentException(String.format(Locale.US, "invalid controlCommandCode: %d, commandStatusCode: %d",
                controlCommandCode, commandStatusCode));
    }

}
