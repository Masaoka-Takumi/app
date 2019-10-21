package jp.pioneer.carsync.domain.interactor;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionSensitivity;
import jp.pioneer.carsync.domain.model.AdasFunctionSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionType;
import jp.pioneer.carsync.domain.model.AppStatus;

/**
 * ADAS設定
 */
public class PreferAdas {
    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mStatusCase;
    /**
     * コンストラクタ.
     */
    @Inject
    public PreferAdas(){
    }

    /**
     * ADAS設定済.
     */
    public void setAdasSettingConfigured(boolean setting){
        mPreference.setAdasSettingConfigured(setting);
    }

    /**
     * ADAS設定済か否か.
     *
     * @return ADAS設定済か否か
     */
    public boolean isAdasSettingConfigured(){
        return mPreference.isAdasSettingConfigured();
    }

    /**
     * ADAS有効設定.
     */
    public void setAdasEnabled(boolean setting){
        mPreference.setAdasEnabled(setting);
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        appStatus.homeViewAdas = setting;
    }

    /**
     * ADASが有効か否か取得.
     *
     * @return ADASが有効か否か
     */
    public boolean getAdasEnabled(){
        return mPreference.isAdasEnabled();
    }

    /**
     * ADAS Alarm有効設定.
     */
    public void setAdasAlarmEnabled(boolean setting){
        mPreference.setAdasAlarmEnabled(setting);
    }

    /**
     * ADAS Alarm設定が有効か否か取得.
     *
     * @return ADASが有効か否か
     */
    public boolean getAdasAlarmEnabled(){
        return mPreference.isAdasAlarmEnabled();
    }

    /**
     * ADAS キャリブレーション設定.
     *
     * @param setting 画面内に見える車体の先端の高さ[px]
     */
    public void setAdasCalibrationSetting(int setting){
        mPreference.setAdasCalibrationSetting(setting);
    }

    /**
     * ADAS キャリブレーション設定取得.
     *
     * @return 画面内に見える車体の先端の高さ[px]
     */
    public int getAdasCalibrationSetting(){
        return mPreference.getAdasCalibrationSetting();
    }

    /**
     * ADAS カメラ設定.
     *
     * @param setting 設定内容
     */
    public void setAdasCameraSetting(AdasCameraSetting setting){
        mPreference.setAdasCameraSetting(setting);
    }

    /**
     * ADAS カメラ設定取得.
     *
     * @return ADASカメラ設定
     */
    public AdasCameraSetting getAdasCameraSetting(){
        return mPreference.getAdasCameraSetting();
    }

    /**
     * 各機能のON/OFF設定.
     *
     * @param type 設定する対象の機能種別
     */
    public void setFunctionEnabled(AdasFunctionType type, boolean enabled){
        AdasFunctionSetting setting = getFunctionSetting(type);
        setting.settingEnabled = enabled;
        setFunctionSetting(setting);
    }

    /**
     * 各機能の感度設定.
     *
     * @param type 設定する対象の機能種別
     * @param sensitivity 感度
     */
    public void setFunctionSensitivity(AdasFunctionType type, AdasFunctionSensitivity sensitivity){
        AdasFunctionSetting setting = getFunctionSetting(type);
        setting.functionSensitivity = sensitivity;
        setFunctionSetting(setting);
    }

    /**
     * 各機能設定取得.
     *
     * @param type 取得する対象の機能種別
     * @return 機能設定
     */
    public AdasFunctionSetting getFunctionSetting(AdasFunctionType type){
        AdasFunctionSetting setting;
        switch(type){
            case LDW:
                setting = mPreference.getAdasLdwSetting();
                break;
            case PCW:
                setting = mPreference.getAdasPcwSetting();
                break;
            case FCW:
                setting = mPreference.getAdasFcwSetting();
                break;
            case LKW:
                setting = mPreference.getAdasLkwSetting();
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        if(setting.functionSensitivity == null || setting.functionSensitivity == AdasFunctionSensitivity.OFF){
            setting.functionSensitivity = AdasFunctionSensitivity.MIDDLE;
        }

        return setting;
    }

    private void setFunctionSetting(AdasFunctionSetting setting){
        AdasFunctionType type = setting.functionType;
        switch(type){
            case LDW:
                mPreference.setAdasLdwSetting(setting);
                break;
            case PCW:
                mPreference.setAdasPcwSetting(setting);
                break;
            case FCW:
                mPreference.setAdasFcwSetting(setting);
                break;
            case LKW:
                mPreference.setAdasLkwSetting(setting);
                break;
        }
    }
}