package jp.pioneer.carsync.presentation.view;

/**
 * Created by NSW00_008316 on 2017/03/24.
 */

public interface FxView {

    /**
     * イコライザー設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     */
    void setEqualizerSetting(boolean isSupported,
                             boolean isEnabled);

    /**
     * Live Simulation設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     */
    void setLiveSimulationSetting(boolean isSupported,
                                  boolean isEnabled);

    /**
     * Super轟設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     */
    void setSuperTodorokiSetting(boolean isSupported,
                                 boolean isEnabled);

    /**
     * Small Car TA設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     */
    void setSmallCarTaSetting(boolean isSupported,
                              boolean isEnabled);

    /**
     * カラオケ設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     */
    void setKaraokeSetting(boolean isSupported,
                           boolean isEnabled);
}
