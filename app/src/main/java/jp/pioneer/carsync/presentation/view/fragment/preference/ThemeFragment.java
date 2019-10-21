package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.presentation.presenter.ThemePresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.ThemeView;

/**
 * テーマ設定Top画面
 */

public class ThemeFragment extends AbstractPreferenceFragment<ThemePresenter, ThemeView> implements ThemeView {

    @Inject ThemePresenter mPresenter;
    private Preference mThemeSet;
    private Preference mIlluminationColor;
    private Preference mIlluminationDispColor;
    private Preference mIlluminationKeyColor;
    private Preference mIlluminationDualColor;
    private Preference mUiColor;
    private Preference mDimmer;
    private SeekBarPreference mBrightness;
    private SeekBarPreference mDispBrightness;
    private SeekBarPreference mKeyBrightness;
    private SwitchPreferenceCompat mIllumiFx;
    private SwitchPreferenceCompat mIllumiFxBgv;

    /**
     * コンストラクタ
     */
    public ThemeFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return ThemeFragment
     */
    public static ThemeFragment newInstance(Bundle args) {
        ThemeFragment fragment = new ThemeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings_theme, rootKey);

        mThemeSet = findPreference(getString(R.string.key_theme_theme_set));
        mThemeSet.setOnPreferenceClickListener((preference) -> {
            getPresenter().onThemeSetAction();
            return true;
        });
        mIlluminationColor = findPreference(getString(R.string.key_theme_illumination_color_common));
        mIlluminationColor.setOnPreferenceClickListener((preference) -> {
            getPresenter().onIlluminationColorAction();
            return true;
        });
        mIlluminationDispColor = findPreference(getString(R.string.key_theme_illumination_color_disp));
        mIlluminationDispColor.setOnPreferenceClickListener((preference) -> {
            getPresenter().onIlluminationDispColorAction();
            return true;
        });
        mIlluminationKeyColor = findPreference(getString(R.string.key_theme_illumination_color_key));
        mIlluminationKeyColor.setOnPreferenceClickListener((preference) -> {
            getPresenter().onIlluminationKeyColorAction();
            return true;
        });
        mIlluminationDualColor = findPreference(getString(R.string.key_theme_illumination_color_dual));
        mIlluminationDualColor.setOnPreferenceClickListener((preference) -> {
            getPresenter().onIlluminationColorDualAction();
            return true;
        });
        mUiColor = findPreference(getString(R.string.key_theme_ui_color));
        mUiColor.setOnPreferenceClickListener((preference) -> {
            getPresenter().onUiColorAction();
            return true;
        });
        mDimmer = findPreference(getString(R.string.key_theme_illumination_dimmer));
        mDimmer.setOnPreferenceClickListener((preference) -> {
            getPresenter().onIlluminationDimmerAction();
            return true;
        });

        mBrightness = (SeekBarPreference) findPreference(getString(R.string.key_theme_illumination_brightness_common));
        mBrightness.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onBrightnessAction((int) newValue);
            return true;
        });

        mDispBrightness = (SeekBarPreference) findPreference(getString(R.string.key_theme_illumination_brightness_disp));
        mDispBrightness.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onDisplayBrightnessAction((int) newValue);
            return true;
        });

        mKeyBrightness = (SeekBarPreference) findPreference(getString(R.string.key_theme_illumination_brightness_key));
        mKeyBrightness.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onKeyBrightnessAction((int) newValue);
            return true;
        });

        mIllumiFx = (SwitchPreferenceCompat) findPreference(getString(R.string.key_theme_illumi_fx));
        mIllumiFx.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onIllumiFxChange((boolean) newValue);
            return true;
        });

        mIllumiFxBgv = (SwitchPreferenceCompat) findPreference(getString(R.string.key_theme_illumi_fx_with_bgv));
        mIllumiFxBgv.setOnPreferenceChangeListener((preference, newValue) -> {
            getPresenter().onIllumiFxWithBgvChange((boolean) newValue);
            return true;
        });
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected ThemePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_THEME;
    }

    @Override
    public void setThemeSetting(boolean isEnabled) {
        mThemeSet.setEnabled(isEnabled);
    }

    @Override
    public void setUiColorSetting(boolean isEnabled) {
        mUiColor.setEnabled(isEnabled);
    }

    @Override
    public void setIlluminationSetting(boolean isSupported, boolean isEnabled) {
        mIlluminationColor.setVisible(isSupported);
        mIlluminationColor.setEnabled(isEnabled);
    }

    @Override
    public void setDisplayIlluminationSetting(boolean isSupported, boolean isEnabled) {
        mIlluminationDispColor.setVisible(isSupported);
        mIlluminationDispColor.setEnabled(isEnabled);
    }

    @Override
    public void setKeyIlluminationSetting(boolean isSupported, boolean isEnabled) {
        mIlluminationKeyColor.setVisible(isSupported);
        mIlluminationKeyColor.setEnabled(isEnabled);
    }

    @Override
    public void setDualIlluminationSetting(boolean isSupported, boolean isEnabled, IlluminationColor setting) {
        mIlluminationDualColor.setVisible(isSupported);
        mIlluminationDualColor.setEnabled(isEnabled);
        Optional.ofNullable(setting).
                ifPresent(color -> mIlluminationDualColor.setSummary(color.label));
    }

    @Override
    public void setDimmerSetting(boolean isSupported, boolean isEnabled) {
        mDimmer.setVisible(isSupported);
        mDimmer.setEnabled(isEnabled);
    }

    @Override
    public void setBrightnessSetting(boolean isSupported, boolean isEnabled, int min, int max, int curr) {
        mBrightness.setVisible(isSupported);
        mBrightness.setEnabled(isEnabled);
        mBrightness.setMin(min);
        mBrightness.setMax(max);
        mBrightness.setValue(curr);
    }

    @Override
    public void setDisplayBrightnessSetting(boolean isSupported, boolean isEnabled, int min, int max, int curr) {
        mDispBrightness.setVisible(isSupported);
        mDispBrightness.setEnabled(isEnabled);
        mDispBrightness.setMin(min);
        mDispBrightness.setMax(max);
        mDispBrightness.setValue(curr);
    }

    @Override
    public void setKeyBrightnessSetting(boolean isSupported, boolean isEnabled, int min, int max, int curr) {
        mKeyBrightness.setVisible(isSupported);
        mKeyBrightness.setEnabled(isEnabled);
        mKeyBrightness.setMin(min);
        mKeyBrightness.setMax(max);
        mKeyBrightness.setValue(curr);
    }

    @Override
    public void setIlluminationEffectSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mIllumiFx.setVisible(isSupported);
        mIllumiFx.setEnabled(isEnabled);
        mIllumiFx.setChecked(setting);
    }

    @Override
    public void setBgvLinkedSetting(boolean isSupported, boolean isEnabled, boolean setting) {
        mIllumiFxBgv.setVisible(isSupported);
        mIllumiFxBgv.setEnabled(isEnabled);
        mIllumiFxBgv.setChecked(setting);
    }
}
