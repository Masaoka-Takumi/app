package jp.pioneer.carsync.presentation.view;

import android.net.Uri;
import android.support.annotation.ColorRes;

import java.util.ArrayList;

import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;

/**
 * HOMEの抽象クラス
 */
public interface HomeView {
    void setDistanceUnit(DistanceUnit distanceUnit);
    /**
     * Playerエリアの設定
     *
     * @param type 現在のSource
     */
    void setPlayerView(MediaSourceType type);

    /**
     * Clockエリアの設定
     *
     * @param type 現在のtype
     */
    void setClockView(int type);

    /**
     * SpeedMeterエリアの設定
     *
     * @param enabled 有効/無効
     * @param type 現在のtype
     */
    void setSpeedMeterViewType(boolean enabled, boolean type);

    /**
     * ShortCutKeyの設定
     *
     * @param keys ShortCutKeys
     */
    void setShortcutKeyItems(ArrayList<ShortcutKeyItem> keys);

    /**
     * ShortCutButtonの表示設定
     *
     * @param enabled 表示/非表示
     */
    void setShortCutButtonEnabled(boolean enabled);

    /**
     * 接続デバイス名の設定
     *
     * @param audioDeviceName 接続デバイス名
     */
    void setAudioDeviceName(String audioDeviceName);

    /**
     * 楽曲タイトルの設定
     *
     * @param title 曲のタイトル
     */
    void setMusicTitle(String title);

    /**
     * アルバムアートの設定
     *
     * @param uri アルバムアURI
     */
    void setMusicAlbumArt(Uri uri);

    /**
     * イメージアートの設定
     *
     * @param resource イメージリソース
     */
    void setCenterImage(int resource);

    /**
     * プログレスバーの最大値の設定
     *
     * @param max プログレスバーの最大値
     */
    void setMaxProgress(int max);

    /**
     * プログレスバーの現在値の設定
     *
     * @param curr プログレスバーの現在値
     */
    void setCurrentProgress(int curr);

    /**
     * ラジオ再生情報の設定
     *
     * @param status 再生状態
     * @param info ラジオ情報
     */
    void setRadioInfo(CarDeviceStatus status, RadioInfo info);
    void setHdRadioInfo(CarDeviceStatus status, HdRadioInfo info);
    void setDabInfo(CarDeviceStatus status, DabInfo info);
    /**
     * TI再生情報の設定
     *
     */
    void setTiInfo();

    /**
     * Sxm再生情報の設定
     *
     * @param status 再生状態
     * @param info Sxm情報
     */
    void setSxmInfo(CarDeviceStatus status, SxmMediaInfo info);

    /**
     * プリセットチャンネルの設定
     *
     * @param pch プリセットチャンネル
     */
    void setPch(int pch);

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    void setColor(@ColorRes int color);

    /**
     * Adasアイコンの表示設定
     *
     * @param isEnabled 設定有効/無効
     */
    void setAdasEnabled(boolean isEnabled);

    /**
     * Adasアイコンの表示設定
     *
     * @param status ノーマル/検知中/エラー
     */
    void setAdasIcon(int status);

    void setAdasDetection(boolean pedestrian,boolean car,boolean leftLane,boolean rightLane);
    void setAlexaNotification(boolean notification);
    void displayVoiceMessage(String str);
}
