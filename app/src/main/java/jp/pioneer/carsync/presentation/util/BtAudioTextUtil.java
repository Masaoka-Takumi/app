package jp.pioneer.carsync.presentation.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.BtAudioInfo;
import jp.pioneer.carsync.domain.model.PlaybackMode;

/**
 * BtAudioのTextView用Util
 */

public class BtAudioTextUtil {
    private static final String EMPTY = "";
    private static final String STOP = "Stop";

    public static String getPlayerSongTitle(Context context, @NonNull BtAudioInfo info) {
        PlaybackMode playbackMode = info.playbackMode;
        String text;
        if (info.isConnecting() || info.isNoService()) {
            // デバイスA2DP接続中 or デバイスA2DP接続失敗
            text = info.songTitle;
        } else if (playbackMode == PlaybackMode.STOP) {
            // 端末に保存されている曲が存在しない、または端末依存によるStop動作時
            text = EMPTY;
        } else {
            // 正常再生中, 再生中曲のTagにSong Titleの情報あり or 再生中曲のTagにSong Titleの情報無し
            text = TextUtils.isEmpty(info.songTitle) ? context.getResources().getString(R.string.ply_024)
                    : info.songTitle;
        }

        return text;
    }

    public static String getPlayerArtistName(Context context, @NonNull BtAudioInfo info) {
        PlaybackMode playbackMode = info.playbackMode;
        String text;
        if (playbackMode == PlaybackMode.STOP) {
            // 端末に保存されている曲が存在しない、または端末依存によるStop動作時
            // A2DP未接続状態
            text = EMPTY;
        } else if (info.isConnecting() || info.isNoService()) {
            // デバイスA2DP接続中 or デバイスA2DP接続失敗
            text = EMPTY;
        } else {
            // 正常再生中, 再生中曲のTagにArtist情報あり or 再生中曲のTagにArtist情報無し
            text = TextUtils.isEmpty(info.artistName) ? context.getResources().getString(R.string.ply_021)
                    : info.artistName;
        }

        return text;
    }

    public static String getPlayerAlbumName(Context context, @NonNull BtAudioInfo info) {
        PlaybackMode playbackMode = info.playbackMode;
        String text;
        if (playbackMode == PlaybackMode.STOP) {
            // 端末に保存されている曲が存在しない、または端末依存によるStop動作時
            text = EMPTY;
        } else if (info.isConnecting() || info.isNoService()) {
            // デバイスA2DP接続中 or デバイスA2DP接続失敗
            text = EMPTY;
        } else {
            // 正常再生中, 再生中曲のTagにAlbum情報あり or 再生中曲のTagにAlbum情報無し
            text = TextUtils.isEmpty(info.albumName) ? context.getResources().getString(R.string.ply_020)
                    : info.albumName;
        }

        return text;
    }

    public static String getPlayerDeviceName(Context context, @NonNull BtAudioInfo info){
        PlaybackMode playbackMode = info.playbackMode;
        String text;
        if (playbackMode == PlaybackMode.STOP) {
            // 端末依存によるStop動作時
            text = EMPTY;
        } else {
            // 正常再生中, 再生中曲のTagにデバイス名情報あり or 再生中曲のTagにデバイス名情報無し
            text = TextUtils.isEmpty(info.deviceName) ? context.getResources().getString(R.string.ply_023)
                    : info.deviceName;
        }

        return text;
    }
}
