package jp.pioneer.carsync.domain.model;

import javax.inject.Inject;

/**
 * WeChatの通知.
 */
public class WeChatNotification extends DefaultNotificationImpl {
    /**
     * コンストラクタ
     */
    @Inject
    public WeChatNotification() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadTarget() {
        if (getNotification().tickerText == null ||
                getNotification().tickerText.toString().matches(".*: \\Q[\\E.*\\Q]\\E")) {
            /*
             *  ログインの期限が切れた場合、tickerTextにnullが入る。
             *  nullであればログイン期限が切れたと判断し、除外する。
             *  日本語の場合、"[送信元]:[音声呼び出し]"のような文章が取得できる。
             * "*: [*]"の形式であれば不在通知と暫定的に認識して除外する。
             */
            return false;
        }

        return super.isReadTarget();
    }
}
