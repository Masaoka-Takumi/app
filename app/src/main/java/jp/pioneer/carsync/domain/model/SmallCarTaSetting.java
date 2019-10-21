package jp.pioneer.carsync.domain.model;

import android.support.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * Small Car TA設定.
 */
public class SmallCarTaSetting {

    /** Small Car TA設定種別. */
    public SmallCarTaSettingType smallCarTaSettingType;
    /** Small Car TAシート位置 */
    public ListeningPosition listeningPosition;

    /**
     * コンストラクタ.
     */
    public SmallCarTaSetting(){
        reset();
    }

    /**
     * リセット.
     * <p>
     * SmallCarTA設定を初期(OFF設定)状態にする
     */
    public void reset(){
        smallCarTaSettingType = SmallCarTaSettingType.OFF;
        listeningPosition = ListeningPosition.LEFT;
    }

    /**
     * ステップ値取得.
     *
     * @return ステップ値
     */
    @Nullable
    public int[] getStepValue(){
        if(listeningPosition == ListeningPosition.LEFT) {
            return smallCarTaSettingType.leftStepValue;
        } else {
            return smallCarTaSettingType.rightStepValue;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("smallCarTaSetting", smallCarTaSettingType)
                .add("listeningPosition", listeningPosition)
                .toString();
    }
}
