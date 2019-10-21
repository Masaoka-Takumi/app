package jp.pioneer.carsync.infrastructure.crp;

/**
 * 想定していないパケット例外.
 * <p>
 * リストのトランザクションIDが期待しているものと異なるといった
 * 期待していない応答パケットの場合にスローする例外。
 * 応答パケットの受信を継続したい場合に使用する。
 */
public class UnexpectedPacketException extends Exception {
    private static final long serialVersionUID = -669036745391282938L;

    /**
     * コンストラクタ.
     */
    public UnexpectedPacketException() {
        super();
    }

    /**
     * コンストラクタ.
     *
     * @param msg 詳細メッセージ
     */
    public UnexpectedPacketException(String msg) {
        super(msg);
    }
}
