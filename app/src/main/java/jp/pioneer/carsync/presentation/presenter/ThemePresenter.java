package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationColorSpec;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSettingSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.ThemeView;
import jp.pioneer.carsync.presentation.view.argument.IlluminationColorParams;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * テーマ設定Top画面のpresenter
 */
@PresenterLifeCycle
public class ThemePresenter extends Presenter<ThemeView> {

    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject PreferIllumination mPreferIllumination;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    private IlluminationColor[] mDualIllumiColor;

    /**
     * コンストラクタ
     */
    @Inject
    public ThemePresenter() {
    }

    @Override
    void onTakeView() {
        updateView();
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingChangeEvent(IlluminationSettingChangeEvent event) {
        updateView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingStatusChangeEvent(IlluminationSettingStatusChangeEvent event) {
        updateView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        updateView();
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            StatusHolder holder = mGetStatusHolder.execute();
            boolean isIllumiSettingEnabled = holder.getCarDeviceStatus().illuminationSettingEnabled &&
                    holder.getCarDeviceSpec().illuminationSettingSupported;
            IlluminationSettingSpec spec = holder.getCarDeviceSpec().illuminationSettingSpec;
            IlluminationSettingStatus status = holder.getIlluminationSettingStatus();
            IlluminationSetting setting = holder.getIlluminationSetting();
            createDualIllumiColor(setting);

            boolean isCommonThemeSetting = spec.commonColorSettingSupported && status.commonColorSettingEnabled;
            boolean isSeparateThemeSetting = (spec.dispColorSettingSupported && spec.keyColorSettingSupported) &&
                    (status.dispColorSettingEnabled && status.keyColorSettingEnabled);
            view.setThemeSetting(
                    isIllumiSettingEnabled
            );
            view.setUiColorSetting(
                    true
            );
            view.setIlluminationSetting(
                    spec.commonColorSettingSupported,
                    isIllumiSettingEnabled && status.commonColorSettingEnabled
            );
            view.setDisplayIlluminationSetting(
                    spec.dispColorSettingSupported && spec.keyColorSettingSupported,
                    isIllumiSettingEnabled && status.dispColorSettingEnabled && status.keyColorSettingEnabled
            );
            view.setKeyIlluminationSetting(
                    spec.dispColorSettingSupported && spec.keyColorSettingSupported,
                    isIllumiSettingEnabled && status.dispColorSettingEnabled && status.keyColorSettingEnabled
            );

            view.setDualIlluminationSetting(
                    !spec.dispColorSettingSupported && spec.keyColorSettingSupported && mDualIllumiColor != null,
                    isIllumiSettingEnabled && !status.dispColorSettingEnabled && status.keyColorSettingEnabled && mDualIllumiColor != null,
                    setting.keyColor
            );
            view.setDimmerSetting(
                    spec.dimmerSettingSupported,
                    isIllumiSettingEnabled && status.dimmerSettingEnabled
            );
            view.setBrightnessSetting(
                    spec.brightnessSettingSupported,
                    isIllumiSettingEnabled && status.brightnessSettingEnabled,
                    setting.brightnessSetting.min,
                    setting.brightnessSetting.max,
                    setting.brightnessSetting.current
            );
            view.setDisplayBrightnessSetting(
                    spec.dispBrightnessSettingSupported,
                    isIllumiSettingEnabled && status.dispBrightnessSettingEnabled,
                    setting.dispBrightnessSetting.min,
                    setting.dispBrightnessSetting.max,
                    setting.dispBrightnessSetting.current
            );
            view.setKeyBrightnessSetting(
                    spec.keyBrightnessSettingSupported,
                    isIllumiSettingEnabled && status.keyBrightnessSettingEnabled,
                    setting.keyBrightnessSetting.min,
                    setting.keyBrightnessSetting.max,
                    setting.keyBrightnessSetting.current
            );
            view.setIlluminationEffectSetting(
                    spec.hotaruNoHikariLikeSettingSupported,
                    isIllumiSettingEnabled && status.hotaruNoHikariLikeSettingEnabled,
                    setting.illuminationEffect
            );
            view.setBgvLinkedSetting(
                    holder.getProtocolSpec().isSphCarDevice(),
                    isIllumiSettingEnabled,
                    mPreference.isLightingEffectEnabled()
            );
        });
    }

    private void createDualIllumiColor(IlluminationSetting setting) {
        int colorIndex = 0;
        mDualIllumiColor = new IlluminationColor[2];
        for (IlluminationColor color : IlluminationColor.values()) {
            if (color == IlluminationColor.FOR_MY_CAR || color == IlluminationColor.CUSTOM) {
                continue;
            } else if (colorIndex > 1) {
                break;
            }

            IlluminationColorSpec colorSpec = setting.keyColorSpec.get(color);
            if (colorSpec.isValid()) {
                mDualIllumiColor[colorIndex] = color;
                colorIndex++;
            }
        }

        if(colorIndex <= 1){
            mDualIllumiColor = null;
        }
    }

    /**
     * テーマ設定
     */
    public void onThemeSetAction() {
        mEventBus.post(new NavigateEvent(ScreenId.THEME_SET_SETTING, createSettingsParams(mContext.getString(R.string.set_228))));
    }

    /**
     * イルミネーションカラー設定（共通設定）
     */
    public void onIlluminationColorAction() {
        IlluminationColorParams params = new IlluminationColorParams();
        params.pass = mContext.getString(R.string.set_093);
        params.type = IlluminationColorParams.IlluminationType.COMMON;
        mEventBus.post(new NavigateEvent(ScreenId.ILLUMINATION_COLOR_SETTING, params.toBundle()));
    }

    /**
     * イルミネーションカラー設定
     */
    public void onIlluminationDispColorAction() {
        IlluminationColorParams params = new IlluminationColorParams();
        params.pass = mContext.getString(R.string.set_061);
        params.type = IlluminationColorParams.IlluminationType.DISP;
        mEventBus.post(new NavigateEvent(ScreenId.ILLUMINATION_COLOR_SETTING, params.toBundle()));
    }

    /**
     * イルミネーションカラー設定
     */
    public void onIlluminationKeyColorAction() {
        IlluminationColorParams params = new IlluminationColorParams();
        params.pass = mContext.getString(R.string.set_108);
        params.type = IlluminationColorParams.IlluminationType.KEY;
        mEventBus.post(new NavigateEvent(ScreenId.ILLUMINATION_COLOR_SETTING, params.toBundle()));
    }

    /**
     * デュアルイルミネーションカラー設定
     */
    public void onIlluminationColorDualAction() {
        IlluminationColor currentColor = mGetStatusHolder.execute().getIlluminationSetting().keyColor;
        IlluminationColor nextColor = mDualIllumiColor[0] == currentColor ? mDualIllumiColor[1] : mDualIllumiColor[0];
        mPreferIllumination.setColor(IlluminationTarget.KEY, nextColor);
    }

    /**
     * UIカラー設定
     */
    public void onUiColorAction() {
        mEventBus.post(new NavigateEvent(ScreenId.UI_COLOR_SETTING, createSettingsParams(mContext.getString(R.string.set_230))));
    }

    /**
     * ディマー設定
     */
    public void onIlluminationDimmerAction() {
        mEventBus.post(new NavigateEvent(ScreenId.ILLUMINATION_DIMMER_SETTING, createSettingsParams(mContext.getString(R.string.set_058))));
    }

    /**
     * 輝度設定(共通設定)
     *
     * @param value 輝度レベル
     */
    public void onBrightnessAction(int value) {
        mPreferIllumination.setCommonBrightness(value);
    }

    /**
     * ディスプレイ輝度設定
     *
     * @param value 輝度レベル
     */
    public void onDisplayBrightnessAction(int value) {
        mPreferIllumination.setBrightness(IlluminationTarget.DISP, value);
    }

    /**
     * キー輝度設定
     *
     * @param value 輝度レベル
     */
    public void onKeyBrightnessAction(int value) {
        mPreferIllumination.setBrightness(IlluminationTarget.KEY, value);
    }

    /**
     * 蛍の光風設定
     *
     * @param value 有効無効
     */
    public void onIllumiFxChange(boolean value) {
        mPreferIllumination.setIlluminationEffect(value);
    }


    /**
     * BGV連動演出設定
     *
     * @param value 有効無効
     */
    public void onIllumiFxWithBgvChange(boolean value) {
        mPreference.setLightingEffectEnabled(value);
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}
