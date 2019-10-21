package jp.pioneer.carsync.infrastructure.crp;

/**
 * 不良パケット例外.
 * <p>
 * データ部が不正（内容、サイズ）な場合にスローする例外。
 */
public class BadPacketException extends Exception {
    private static final long serialVersionUID = 1840137804069397727L;

    /**
     * コンストラクタ.
     */
    public BadPacketException() {
        super();
    }

    /**
     * コンストラクタ.
     *
     * @param msg 詳細メッセージ
     */
    public BadPacketException(String msg) {
        super(msg);
    }
}
