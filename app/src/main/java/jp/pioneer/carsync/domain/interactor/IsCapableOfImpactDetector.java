package jp.pioneer.carsync.domain.interactor;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.ImpactDetector;

import static com.google.common.base.Preconditions.checkArgument;
import static jp.pioneer.carsync.domain.interactor.ControlImpactDetector.IMPACT_THRESHOLD;

/**
 * 衝突検知の能力があるか否か取得.
 * <p>
 * 端末が衝突検知を行えるだけの能力がある加速度センサーを備えているかを確認する際に使用する。
 * 能力がない場合、衝突検知を行えない旨の表示を行うこと。
 */
public class IsCapableOfImpactDetector {
    private static final float MAXIMUM_RANGE_THRESHOLD = 30.0f;
    @Inject ImpactDetector mImpactDetector;

    /**
     * コンストラクタ.
     */
    @Inject
    public IsCapableOfImpactDetector() {
    }

    /**
     * 実行.
     *
     * @return {@code true}:能力がある。{@code false}:能力がない。
     * @throws IllegalArgumentException 衝突として扱う値に満たない閾値を使用している。
     *          本例外は本来コンパイルエラーレベルのものであるが、C++におけるstatic_assertに
     *          相当するものがないので実行時に検出している。呼び出し側の責任ではない。
     */
    public boolean execute() {
        checkArgument(IMPACT_THRESHOLD >= Math.sqrt((MAXIMUM_RANGE_THRESHOLD * MAXIMUM_RANGE_THRESHOLD) * 3));

        return MAXIMUM_RANGE_THRESHOLD <= mImpactDetector.getMaximumRange();
    }
}
