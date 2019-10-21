package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.infrastructure.database.AppMusicPlaylistCursor;

/**
 * AppMusicのSourceController
 */
public interface AppMusicSourceController extends SourceController {

    /**
     * 再生
     *
     * @param params 再生する内容
     */
    void play(AppMusicContract.PlayParams params);

    /**
     * Playトグルボタン処理.
     * <p>
     * PlayとPauseを切り替える.
     */
    void togglePlay();

    /**
     * リピートモードトグルボタン処理
     * <p>
     * リピートモード(ALL,ONE,OFF)を切り替える.
     */
    void toggleRepeatMode();

    /**
     * シャッフルモードトグルボタン処理
     * <p>
     * シャッフルモード(ON,OFF)を切り替える.
     */
    void toggleShuffleMode();

    /**
     * 次の曲へ移動.
     */
    void skipNextTrack();

    /**
     * 前の曲へ移動.
     */
    void skipPreviousTrack();

    /**
     * ボリュームアップ.
     */
    void volumeUp();

    /**
     * ボリュームダウン.
     */
    void volumeDown();

    /**
     * スペアナデータリスナー設定.
     *
     * @param listener スペアナデータリスナー
     * @throws NullPointerException {@code listener}がnull
     */
    void addSpeAnaDataListener(@NonNull OnSpeAnaDataListener listener);

    /**
     * スペアナデータリスナー設定解除.
     *
     * @param listener {@link #addSpeAnaDataListener(OnSpeAnaDataListener)}で設定したスペアナデータリスナー
     * @throws NullPointerException {@code listener}がnull
     */
    void deleteSpeAnaDataListener(@NonNull OnSpeAnaDataListener listener);

    /**
     * トラック選択.
     *
     * @param trackNo トラック番号
     * @throws IllegalArgumentException トラック番号の値が不正
     */
    void selectTrack(int trackNo);

    /**
     * 再生中プレイリスト生成.
     *
     * @return プレイリスト
     */
    AppMusicPlaylistCursor getPlaylistCursor();

    /**
     * 31band->13band変換.
     * <p>
     * プレイヤーが有効ではない場合はnullを返す
     *
     * @param bands 31band値
     * @return 13bandに変換されたバンド値 又は null
     */
    @Size(13)
    @Nullable
    int[] convertBandValue(@Size(31) float[] bands);

    /**
     * 早送り.
     * <p>
     * プレイヤーが準備中の場合は何もしない。
     * 指定されたミリ秒分早送りを実施する。
     * 早送りした結果再生時間を超える場合は再生時間に丸めてシークを実施する。
     *
     * プレイヤーからの早送り動作で使用する。
     *
     * @param time 早送り時間(ミリ秒)
     */
    void fastForwardForPlayer(int time);

    /**
     * 巻き戻し.
     * <p>
     * プレイヤーが準備中の場合は何もしない。
     * 指定されたミリ秒分巻き戻しを実施する。
     * 巻き戻しした結果再生時間を下回る場合は0を指定してシークを実施する。
     *
     * プレイヤーからの巻き戻し動作で使用する。
     *
     * @param time 巻き戻し時間(ミリ秒)
     */
    void rewindForPlayer(int time);

    /**
     * フォーカス放棄.
     */
    void abandonAudioFocus();

    /**
     * 再生時間送信.
     */
    void sendPlaybackTime(int durationInSec, int positionInSec);

    /**
     * 曲情報送信.
     */
    void sendMusicInfo();

    /**
     * スペアナデータリスナー.
     */
    interface OnSpeAnaDataListener {
        /**
         * スペアナデータハンドラ.
         * <p>
         * スペアナのデータを通知する。
         * スペアナデータの供給元と同じスレッド（非Mainスレッド）で呼ばれる。同スレッドでかかった時間だけ次の通知が遅れるため、
         * 時間がかかる場合は別スレッドで処理を行うこと。
         * 通知は再生中のみ行うため、再生中ではなくなったらスペアナの表示を消去する等の処理を行うこと。
         *
         * @param bandData スペアナデータ 。バンド数分のデシベル値(負の値)。
         *                 31Bandの場合、以下の周波数帯（Hz）のデータとなる。（bandData[0] = 20Hz、bandData[30] = 20kHzのデータ）
         *                 20 / 25 / 31.5 / 40 / 50 / 63 / 80 / 100 / 125 / 160 / 200 / 250 / 315 / 400 / 500 / 630 /
         *                 800 / 1k / 1.25k / 1.6k / 2k / 2.5k / 3.15k / 4k / 5k / 6.3k / 8k / 10k / 12.5k / 16k / 20k
         */
        void onSpeAnaData(@NonNull float[] bandData);
    }
}
