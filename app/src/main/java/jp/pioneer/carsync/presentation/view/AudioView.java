package jp.pioneer.carsync.presentation.view;

import android.support.annotation.Nullable;

import jp.pioneer.carsync.domain.model.BeatBlasterSetting;
import jp.pioneer.carsync.domain.model.LoudnessSetting;
import jp.pioneer.carsync.domain.model.SoundRetrieverSetting;

/**
 * Audio設定画面の抽象クラス
 */

public interface AudioView {

    /**
     * BeatBlaster設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容
     */
    void setBeatBlasterSetting(boolean isSupported,
                               boolean isEnabled,
                               @Nullable BeatBlasterSetting setting);

    /**
     * Loudness設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容
     */
    void setLoudnessSetting(boolean isSupported,
                            boolean isEnabled,
                            @Nullable LoudnessSetting setting);

    /**
     * SLA設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param min         設定可能最小値
     * @param max         設定可能最大値
     * @param curr        現在設定値
     */
    void setSourceLevelAdjuster(boolean isSupported,
                                boolean isEnabled,
                                int min,
                                int max,
                                int curr);

    /**
     * Fader/Balance設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param faderSettingEnabled Fader設定可能か否か
     */
    void setFaderBalanceSetting(boolean isSupported,
                                boolean isEnabled,
                                boolean faderSettingEnabled);

    /**
     * Advanced設定.
     *
     * @param isEnabled   設定可能か否か
     */
    void setAdvancedSetting(boolean isEnabled);

    /**
     * Sound Retriever設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     * @param setting     設定内容
     */
    void setSoundRetrieverSetting(boolean isSupported,
                                  boolean isEnabled,
                                  @Nullable SoundRetrieverSetting setting);

    /**
     * Load設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     */
    void setLoadSetting(boolean isSupported,
                        boolean isEnabled);

    /**
     * Save設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled   設定可能か否か
     */
    void setSaveSetting(boolean isSupported,
                        boolean isEnabled);

    /**
     * トースト表示
     *
     * @param text テキスト
     */
    void showToast(String text);
}
