package jp.pioneer.carsync.domain.component;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.DimmerTimeType;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.IncomingMessageColorSetting;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;

/**
 * イルミネーション設定更新.
 */
public interface IlluminationSettingUpdater {

    /**
     * イルミネーションカラー設定.
     *
     * @param target イルミネーション設定対象種別
     * @param color  イルミネーションカラー
     * @throws NullPointerException {@code target} 又は {@code color}がnull
     */
    void setColor(@NonNull IlluminationTarget target, @NonNull IlluminationColor color);

    /**
     * イルミネーションカスタムカラー設定.
     * <p>
     * {@code red}、{@code green}、{@code blue}のどれか1つが10以上であること。
     *
     * @param target イルミネーション設定対象種別
     * @param red    RGB値 RED
     * @param green  RGB値 GREEN
     * @param blue   RGB値 BLUE
     * @throws NullPointerException     {@code target}がnull
     * @throws IllegalArgumentException {@code red}、{@code green}、{@code blue}が不正
     */
    void setCustomColor(@NonNull IlluminationTarget target,
                        @IntRange(from = 0, to = 60) int red,
                        @IntRange(from = 0, to = 60) int green,
                        @IntRange(from = 0, to = 60) int blue);

    /**
     * BT着信カラー設定.
     *
     * @param color BT着信時のカラー設定種別
     * @throws NullPointerException {@code target}がnull
     */
    void setBtPhoneColor(@NonNull BtPhoneColor color);

    /**
     * ディマー設定.
     *
     * @param dimmer ディマー設定値
     * @throws NullPointerException {@code target}がnull
     */
    void setDimmer(@NonNull DimmerSetting.Dimmer dimmer);

    /**
     * ディマー時刻設定.
     *
     * @param type   ディマー時刻種別
     * @param hour   時
     * @param minute 分
     * @throws NullPointerException {@code target}がnull
     */
    void setDimmerTime(@NonNull DimmerTimeType type, int hour, int minute);

    /**
     * 輝度設定(個別設定).
     *
     * @param target     イルミネーション設定対象種別
     * @param brightness 輝度
     * @throws NullPointerException {@code target}がnull
     */
    void setBrightness(@NonNull IlluminationTarget target, int brightness);

    /**
     * 輝度設定(共通設定).
     * <p>
     * 輝度設定が個別に設定できない場合に使用するメソッド。
     *
     * @param brightness 輝度
     */
    void setCommonBrightness(int brightness);

    /**
     * 蛍の光設定.
     *
     * @param enabled 有効か否か {@code true}:蛍の光設定有効 {@code false}:蛍の光設定無効
     */
    void setIlluminationEffect(boolean enabled);

    /**
     * オーディオレベルメーター連動設定.
     *
     * @param enabled 有効か否か {@code true}:オーディオレベルメーター連動設定有効 {@code false}:オーディオレベルメーター連動設定無効
     */
    void setAudioLevelMeterLinked(boolean enabled);

    /**
     * [SPH] BT Phone color設定.
     *
     * @param setting [SPH] BT Phone color設定
     * @throws NullPointerException {@code setting}がnull
     */
    void setSphBtPhoneColor(@NonNull SphBtPhoneColorSetting setting);

    /**
     * イルミネーションカラー設定(共通設定).
     * <p>
     * 個別に設定できないモデルの場合に使用するメソッド
     *
     * @param color イルミネーションカラー
     * @throws NullPointerException {@code setting}がnull
     */
    void setCommonColor(@NonNull IlluminationColor color);

    /**
     * イルミネーションカスタムカラー設定(共通設定).
     * <p>
     * 個別に設定できないモデルの場合に使用するメソッド
     * {@code red}、{@code green}、{@code blue}のどれか1つが10以上であること。
     *
     * @param red RGB値 RED
     * @param green RGB値 GREEN
     * @param blue RGB値 BLUE
     * @throws IllegalArgumentException {@code red}、{@code green}、{@code blue}が不正
     */
    void setCommonCustomColor(@IntRange(from = 0, to = 60) int red,
                                     @IntRange(from = 0, to = 60) int green,
                                     @IntRange(from = 0, to = 60) int blue);

    /**
     * メッセージ受信COLOR設定.
     *
     * @param setting メッセージ受信COLOR
     * @throws NullPointerException {@code setting}がnull
     */
    void setIncomingMessageColor(@NonNull IncomingMessageColorSetting setting);
}
