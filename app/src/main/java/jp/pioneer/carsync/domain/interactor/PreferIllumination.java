package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.IlluminationSettingUpdater;
import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.CarDeviceScreen;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.DimmerTimeType;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.IncomingMessageColorSetting;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TransitionDirection;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * イルミネーションの設定
 */
public class PreferIllumination {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject IlluminationSettingUpdater mUpdater;

    /**
     * コンストラクタ
     */
    @Inject
    public PreferIllumination() {
    }

    /**
     * イルミネーションカラー設定.
     * <p>
     * 対象がキーの場合に、キーカラー設定が無効な場合は何もしない。
     * 対象がディスプレイの場合に、ディスプレイカラー設定が無効な場合は何もしない。
     *
     * @param target イルミネーション設定対象種別
     * @param color  イルミネーションカラー
     * @throws NullPointerException {@code target}がnull
     * @throws NullPointerException {@code target}がnull
     */
    public void setColor(@NonNull IlluminationTarget target, @NonNull IlluminationColor color) {
        checkNotNull(target);
        checkNotNull(color);

        mHandler.post(() -> {
            IlluminationSettingStatus status = mStatusHolder.getIlluminationSettingStatus();
            switch (target) {
                case DISP: {
                    if (!status.dispColorSettingEnabled) {
                        Timber.w("setColor() Display color setting disabled.");
                        return;
                    }
                    break;
                }
                case KEY: {
                    if (!status.keyColorSettingEnabled) {
                        Timber.w("setColor() Key color setting disabled.");
                        return;
                    }
                    break;
                }
                default:
                    throw new AssertionError("can't happen.");
            }

            mUpdater.setColor(target, color);
        });
    }

    /**
     * イルミネーションカスタムカラー設定.
     * <p>
     * 対象がキーの場合に、キーカスタムカラー設定が無効な場合は何もしない。
     * 対象がディスプレイの場合に、ディスプレイカスタムカラー設定が無効な場合は何もしない。
     * {@code red}、{@code green}、{@code blue}のどれか1つが10以上であること。
     *
     * @param target イルミネーション設定対象種別
     * @param red    RGB値 RED
     * @param green  RGB値 GREEN
     * @param blue   RGB値 BLUE
     * @throws NullPointerException     {@code target}がnull
     * @throws IllegalArgumentException {@code red}、{@code green}、{@code blue}が不正
     */
    public void setCustomColor(@NonNull IlluminationTarget target,
                               @IntRange(from = 0, to = 60) int red,
                               @IntRange(from = 0, to = 60) int green,
                               @IntRange(from = 0, to = 60) int blue) {
        checkNotNull(target);
        checkArgument(red >= 0 && red <= 60);
        checkArgument(green >= 0 && green <= 60);
        checkArgument(blue >= 0 && blue <= 60);
        checkArgument(Math.max(red, Math.max(green, blue)) >= 10);

        mHandler.post(() -> {
            IlluminationSettingStatus status = mStatusHolder.getIlluminationSettingStatus();
            switch (target) {
                case DISP: {
                    if (!status.colorCustomDispSettingEnabled) {
                        Timber.w("setCustomColor() Display custom color setting disabled.");
                        return;
                    }
                    break;
                }
                case KEY: {
                    if (!status.colorCustomKeySettingEnabled) {
                        Timber.w("setCustomColor() Key custom color setting disabled.");
                        return;
                    }
                    break;
                }
                default:
                    throw new AssertionError("can't happen.");
            }

            mUpdater.setCustomColor(target, red, green, blue);
        });
    }

    /**
     * BT着信カラー設定.
     * <p>
     * BT着信カラー設定が無効な場合は何もしない。
     * <p>
     * BT着信カラー設定時は以下を実施することで車載機でプレビューすることができる。
     * <pre>
     *  設定画面起動時：
     *   {@link ChangeScreen#execute(CarDeviceScreen screen, TransitionDirection direction)}
     *   {@code screen}：{@link CarDeviceScreen#ILLUMI_PREVIEW_PHONE_COLOR}
     *   {@code direction}：{@link TransitionDirection#ENTER}
     * </pre>
     * <pre>
     *  設定画面終了時：
     *   {@link ChangeScreen#execute(CarDeviceScreen screen, TransitionDirection direction)}
     *   {@code screen}：{@link CarDeviceScreen#ILLUMI_PREVIEW_PHONE_COLOR}
     *   {@code direction}：{@link TransitionDirection#EXIT}
     * </pre>
     *
     * @param color BT着信時のカラー設定種別
     * @throws NullPointerException {@code target}がnull
     */
    public void setBtPhoneColor(@NonNull BtPhoneColor color) {
        checkNotNull(color);

        mHandler.post(() -> {
            if (!mStatusHolder.getIlluminationSettingStatus().btPhoneColorSettingEnabled) {
                Timber.w("setBtPhoneColor()  Disabled.");
                return;
            }

            mUpdater.setBtPhoneColor(color);
        });
    }

    /**
     * ディマー設定.
     * <p>
     * ディマー設定が無効な場合は何もしない。
     *
     * @param dimmer ディマー設定値
     * @throws NullPointerException {@code target}がnull
     */
    public void setDimmer(@NonNull DimmerSetting.Dimmer dimmer) {
        checkNotNull(dimmer);

        mHandler.post(() -> {
            if (!mStatusHolder.getIlluminationSettingStatus().dimmerSettingEnabled) {
                Timber.w("setDimmer()  Disabled.");
                return;
            }

            mUpdater.setDimmer(dimmer);
        });
    }

    /**
     * ディマー時刻設定.
     * <p>
     * ディマー設定が無効な場合は何もしない。
     *
     * @param type   ディマー時刻種別
     * @param hour   時
     * @param minute 分
     * @throws NullPointerException {@code target}がnull
     */
    public void setDimmerTime(@NonNull DimmerTimeType type, int hour, int minute) {
        checkNotNull(type);

        mHandler.post(() -> {
            if (!mStatusHolder.getIlluminationSettingStatus().dimmerSettingEnabled) {
                Timber.w("setDimmerTime()  Disabled.");
                return;
            }

            mUpdater.setDimmerTime(type, hour, minute);
        });
    }

    /**
     * 輝度設定(個別設定).
     * <p>
     * 対象がキーの場合に、キー輝度設定が無効な場合は何もしない。
     * 対象がディスプレイの場合に、ディスプレイ輝度設定が無効な場合は何もしない。
     *
     * @param target     イルミネーション設定対象種別
     * @param brightness 輝度
     * @throws NullPointerException {@code target}がnull
     */
    public void setBrightness(@NonNull IlluminationTarget target, int brightness) {
        checkNotNull(target);

        mHandler.post(() -> {
            IlluminationSettingStatus status = mStatusHolder.getIlluminationSettingStatus();
            switch (target) {
                case DISP: {
                    if (!status.dispBrightnessSettingEnabled) {
                        Timber.w("setBrightness() Display brightness setting disabled.");
                        return;
                    }
                    break;
                }
                case KEY: {
                    if (!status.keyBrightnessSettingEnabled) {
                        Timber.w("setBrightness() Key brightness setting disabled.");
                        return;
                    }
                    break;
                }
                default:
                    throw new AssertionError("can't happen.");
            }

            mUpdater.setBrightness(target, brightness);
        });
    }

    /**
     * 輝度設定(共通設定).
     * <p>
     * 輝度設定が無効な場合、何もしない。
     * 輝度設定が個別に設定できない場合に使用するメソッド。
     *
     * @param brightness 輝度
     */
    public void setCommonBrightness(int brightness) {

        mHandler.post(() -> {
            if (!mStatusHolder.getIlluminationSettingStatus().brightnessSettingEnabled) {
                Timber.w("setCommonBrightness()  Disabled.");
                return;
            }

            mUpdater.setCommonBrightness(brightness);
        });
    }

    /**
     * 蛍の光設定.
     * <p>
     * 蛍の光設定が無効な場合は何もしない。
     *
     * @param enabled 有効か否か {@code true}:蛍の光設定有効 {@code false}:蛍の光設定無効
     */
    public void setIlluminationEffect(boolean enabled) {

        mHandler.post(() -> {
            if (!mStatusHolder.getIlluminationSettingStatus().hotaruNoHikariLikeSettingEnabled) {
                Timber.w("setIlluminationEffect()  Disabled.");
                return;
            }

            mUpdater.setIlluminationEffect(enabled);
        });
    }

    /**
     * オーディオレベルメーター連動設定.
     * <p>
     * オーディオレベルメーター連動設定が無効な場合は何もしない
     *
     * @param enabled 有効か否か {@code true}:オーディオレベルメーター連動設定有効 {@code false}:オーディオレベルメーター連動設定無効
     */
    public void setAudioLevelMeterLinked(boolean enabled){

        mHandler.post(() -> {
            if (!mStatusHolder.getIlluminationSettingStatus().audioLevelMeterLinkedSettingEnabled) {
                Timber.w("setAudioLevelMeterLinked()  Disabled.");
                return;
            }

            mUpdater.setAudioLevelMeterLinked(enabled);
        });
    }

    /**
     * [SPH] BT Phone color設定.
     * <p>
     * [SPH] BT Phone color設定が無効な場合は何もしない
     *
     * @param setting [SPH] BT Phone color設定
     * @throws NullPointerException {@code setting}がnull
     */
    public void setSphBtPhoneColor(@NonNull SphBtPhoneColorSetting setting){
        checkNotNull(setting);

        mHandler.post(() -> {
            if (!mStatusHolder.getIlluminationSettingStatus().sphBtPhoneColorSettingEnabled) {
                Timber.w("setSphBtPhoneColor()  Disabled.");
                return;
            }

            mUpdater.setSphBtPhoneColor(setting);
        });
    }

    /**
     * イルミネーションカラー設定(共通設定).
     * <p>
     * イルミネーションカラー設定(共通設定)が無効な場合は何もしない
     * 個別に設定できないモデルの場合に使用するメソッド
     *
     * @param color イルミネーションカラー
     * @throws NullPointerException {@code setting}がnull
     */
    public void setCommonColor(@NonNull IlluminationColor color){
        checkNotNull(color);

        mHandler.post(() -> {
            if (!mStatusHolder.getIlluminationSettingStatus().commonColorSettingEnabled) {
                Timber.w("setCommonColor()  Disabled.");
                return;
            }

            mUpdater.setCommonColor(color);
        });
    }

    /**
     * イルミネーションカスタムカラー設定(共通設定).
     * <p>
     * イルミネーションカスタムカラー設定(共通設定)が無効な場合は何もしない
     * 個別に設定できないモデルの場合に使用するメソッド
     * {@code red}、{@code green}、{@code blue}のどれか1つが10以上であること。
     *
     * @param red RGB値 RED
     * @param green RGB値 GREEN
     * @param blue RGB値 BLUE
     * @throws IllegalArgumentException {@code red}、{@code green}、{@code blue}が不正
     */
    public void setCommonCustomColor(@IntRange(from = 0, to = 60) int red,
                                     @IntRange(from = 0, to = 60) int green,
                                     @IntRange(from = 0, to = 60) int blue){
        checkArgument(red >= 0 && red <= 60);
        checkArgument(green >= 0 && green <= 60);
        checkArgument(blue >= 0 && blue <= 60);
        checkArgument(Math.max(red, Math.max(green, blue)) >= 10);

        mHandler.post(() -> {
            if (!mStatusHolder.getIlluminationSettingStatus().commonColorCustomSettingEnabled) {
                Timber.w("setCommonCustomColor()  Disabled.");
                return;
            }

            mUpdater.setCommonCustomColor(red,green,blue);
        });
    }

    /**
     * メッセージ受信COLOR設定.
     * <p>
     * メッセージ受信COLOR設定が無効な場合は何もしない
     *
     * @param setting メッセージ受信COLOR
     * @throws NullPointerException {@code setting}がnull
     */
    public void setIncomingMessageColor(@NonNull IncomingMessageColorSetting setting){
        checkNotNull(setting);

        mHandler.post(() -> {
            if (!mStatusHolder.getIlluminationSettingStatus().incomingMessageColorSettingEnabled) {
                Timber.w("setIncomingMessageColor()  Disabled.");
                return;
            }

            mUpdater.setIncomingMessageColor(setting);
        });
    }
}
