package jp.pioneer.carsync.domain.component;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.SxmBandType;

/**
 * Sirius XMのSourceController.
 */
public interface SiriusXmSourceController extends SourceController {

    /**
     * バンド切り替え.
     */
    void toggleBand();

    /**
     * プリセットCH呼び出し.
     * <p>
     * 引数のプリセットCH番号に変更する。
     *
     * @param presetNo プリセット番号.
     * @throws IllegalArgumentException {@code presetNo} の値が不正.
     */
    void callPreset(@IntRange(from = 1, to = 6) int presetNo);

    /**
     * お気に入り選択.
     *
     * @param channelNo チャンネルNo.
     * @param bandType  バンド種別.
     * @param sId       SID.
     * @throws NullPointerException {@code bandType} がnull.
     */
    void selectFavorite(int channelNo, @NonNull SxmBandType bandType, int sId);

    /**
     * チャンネルアップ.
     */
    void channelUp();

    /**
     * チャンネルダウン.
     */
    void channelDown();

    /**
     * Scanアップ.
     */
    void scanUp();

    /**
     * Scanダウン.
     */
    void scanDown();

    /**
     * トグルPlay.
     * <p>
     * PlayとPauseを切り替える。
     */
    void togglePlay();

    /**
     * P.CHアップ.
     * <p>
     * 以下状態で実行してはいけない
     * ・BSM中
     * ・購読更新ポップアップ表示中
     * ・リプレイモード
     * ・ポーズ中
     */
    void presetUp();

    /**
     * P.CHダウン.
     * <p>
     * 以下状態で実行してはいけない
     * ・BSM中
     * ・購読更新ポップアップ表示中
     * ・リプレイモード
     * ・ポーズ中
     */
    void presetDown();

    /**
     * ライブモード切り替え.
     * <p>
     * スキャン中は実行することができない
     */
    void toggleLiveMode();

    /**
     * チャンネルモード又はリプレイモード切り替え.
     * <p>
     * チャンネルモード切り替えとリプレイモード切り替えは同じコマンドを送信するため、
     * 1つにまとめる。
     * チャンネルモード切り替えの場合スキャン中は実行することができない。
     */
    void toggleChannelModeOrReplayMode();

    /**
     * チューンミックス切り替え.
     */
    void toggleTuneMix();

    /**
     * SubscriptionUpdate解除.
     */
    void releaseSubscriptionUpdating();

    /**
     * ボリュームアップ.
     */
    void volumeUp();

    /**
     * ボリュームダウン.
     */
    void volumeDown();
}