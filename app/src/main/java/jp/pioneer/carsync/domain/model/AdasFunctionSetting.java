package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * ADASの各機能設定
 * <p>
 * ADASの機能であるLDW,PCW,FCW,LKWの設定パラメータ。
 */
public class AdasFunctionSetting {

    /** 種別 */
    public AdasFunctionType functionType;

    /** 設定が有効か否か */
    public boolean settingEnabled;

    /** 感度 */
    public AdasFunctionSensitivity functionSensitivity;

    /**
     * コンストラクタ.
     *
     * @param functionType 種別
     */
    public AdasFunctionSetting(AdasFunctionType functionType){
        this.functionType = functionType;
        reset();
    }

    /**
     * リセット.
     */
    public void reset(){
        settingEnabled = true;
        functionSensitivity = AdasFunctionSensitivity.MIDDLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("functionType", functionType)
                .add("settingEnabled", settingEnabled)
                .add("functionSensitivity", functionSensitivity)
                .toString();
    }

    /**
     * 感度取得.
     * <p>
     * 設定が無効の場合は{@link AdasFunctionSensitivity#OFF}を返す
     *
     * @return 感度
     */
    public AdasFunctionSensitivity getSensitivity(){
        if(!settingEnabled){
            return AdasFunctionSensitivity.OFF;
        } else {
            return functionSensitivity;
        }
    }
}
