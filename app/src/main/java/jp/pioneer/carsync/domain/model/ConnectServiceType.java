package jp.pioneer.carsync.domain.model;

/**
 * 接続サービス種別.
 */
public enum ConnectServiceType {
    /** Phoneサービス. */
    PHONE(0x01),
    /** Audioサービス. */
    AUDIO(0x02)
    ;

    /** プロトコルでの定義値（サービスコネクトコマンド通知）. */
    public final int code;

    /**
     * コンストラクタ.
     *
     * @param code プロトコルでの定義値
     */
    ConnectServiceType(int code) {
        this.code = code;
    }

    /** 全ての場合のプロトコルでの定義値（サービスコネクトコマンド通知）. */
    public static final int ALL_CODE = 0x00;
}
