package jp.pioneer.carsync.domain.interactor;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.ImpactDetector;
import jp.pioneer.carsync.domain.event.ImpactEvent;

/**
 * 衝突検知制御.
 * <p>
 * 衝突検知の開始/停止を制御する。
 * {@link IsCapableOfImpactDetector#execute()}が{@code false}となる端末では使用しないこと。
 * 本クラスでは能力の有無、{@link AppSharedPreference#isImpactDetectionEnabled()}の設定値を
 * 参照しない。呼び出し側の責務とする。
 */
public class ControlImpactDetector {
    private static final float FILTER_CONSTANT = 0.9f;
    static final float IMPACT_THRESHOLD = 6.0f * 9.8f;
    static final float DEBUG_MODE_IMPACT_THRESHOLD = 6.0f * 9.8f / 2f;
    @Inject ImpactDetector mImpactDetector;
    @Inject AppSharedPreference mPreference;

    /**
     * コンストラクタ.
     */
    @Inject
    public ControlImpactDetector() {
    }

    /**
     * 衝突検知開始.
     * <p>
     * 衝突が発生した場合、{@link ImpactEvent}が発生する。
     *
     * @return {@code true}:成功。{@code false}:失敗。
     * @throws IllegalStateException 既に開始している
     */
    public boolean startDetection() {
        float threshold = mPreference.isImpactDetectionDebugModeEnabled() ? DEBUG_MODE_IMPACT_THRESHOLD : IMPACT_THRESHOLD;
        return mImpactDetector.startDetection(FILTER_CONSTANT, threshold);
    }

    /**
     * 衝突検知停止.
     * <p>
     * 既に停止している（開始していない）場合は無視する。
     */
    public void stopDetection() {
        mImpactDetector.stopDetection();
    }
}
