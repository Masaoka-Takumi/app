package jp.pioneer.carsync.infrastructure.crp;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.CarRemoteSessionLifeCycle;

/**
 * セッションの設定情報.
 */
@CarRemoteSessionLifeCycle
public class SessionConfig {
    /**
     * コンストラクタ.
     */
    @Inject
    public SessionConfig() {
    }

    /**
     * 送信待ち時間取得.
     *
     * @return 連続して送信する際の間に入れる待ち時間(ms)
     */
    public long getSendWaitTime() {
        return 15L;
    }

    /**
     * 送信調整間隔取得.
     *
     * @return 送信間隔の調整が必要なパケットの間隔(ms)
     */
    public long getSendRegulationInterval() {
        return 300L;
    }

    /**
     * 送信リトライ間隔取得.
     *
     * @return 送信のリトライ間隔(ms)
     */
    public long getSendRetryInterval() {
        return 3000L;
    }

    /**
     * 送信リトライ回数取得.
     *
     * @return 送信のリトライ回数
     */
    public int getSendRetryCount() {
        return 3;
    }

    /**
     * 定期通信間隔取得.
     *
     * @return 定期通信間隔(ms)
     */
    public long getPeriodicCommInterval() {
        return 5000L;
    }

    /**
     * 無受信タイムアウト取得.
     *
     * @return 無受信タイムアウト時間(ms)
     */
    public long getIdleReceiveCommTimeout() {
        return 15000L;
    }

    /**
     * 認証エラー切断待ち時間取得.
     *
     * @return 認証エラーで通信を切断する際の待ち時間(ms)
     */
    public long getErrorCloseWaitTime() {
        return 1000L;
    }
}
