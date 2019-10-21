package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Crossover設定.
 */
public class CrossoverSetting {
    /** Standard Mode:Front. */
    public SpeakerCrossoverSetting front = new SpeakerCrossoverSetting(SpeakerType.FRONT);
    /** Standard Mode:Rear. */
    public SpeakerCrossoverSetting rear = new SpeakerCrossoverSetting(SpeakerType.REAR);
    /** Standard Mode:Subwoofer. */
    public SpeakerCrossoverSetting subwooferStandardMode = new SpeakerCrossoverSetting(SpeakerType.SUBWOOFER_STANDARD_MODE);
    /** 2way Network Mode:High. */
    public SpeakerCrossoverSetting high = new SpeakerCrossoverSetting(SpeakerType.HIGH);
    /** 2way Network Mode:Mid-HPF. */
    public SpeakerCrossoverSetting midHPF = new SpeakerCrossoverSetting(SpeakerType.MID_HPF);
    /** 2way Network Mode:Mid-HPF. */
    public SpeakerCrossoverSetting midLPF = new SpeakerCrossoverSetting(SpeakerType.MID_LPF);
    /** 2way Network Mode:Subwoofer. */
    public SpeakerCrossoverSetting subwoofer2WayNetworkMode = new SpeakerCrossoverSetting(SpeakerType.SUBWOOFER_2WAY_NETWORK_MODE);
    /** [JASPER] HPF. */
    public JasperCrossoverSetting jasperHpf = new JasperCrossoverSetting(HpfLpfFilterType.HPF);
    /** [JASPER] LPF. */
    public JasperCrossoverSetting jasperLpf = new JasperCrossoverSetting(HpfLpfFilterType.LPF);

    /**
     * Crossover設定取得.
     *
     * @param type スピーカー種別
     * @return スピーカーのCrossover設定.
     * @throws NullPointerException {@code type}がnull
     */
    public SpeakerCrossoverSetting findSpeakerCrossoverSetting(@NonNull SpeakerType type) {
        switch (checkNotNull(type)) {
            case FRONT:
                return front;
            case REAR:
                return rear;
            case SUBWOOFER_STANDARD_MODE:
                return subwooferStandardMode;
            case HIGH:
                return high;
            case MID_HPF:
                return midHPF;
            case MID_LPF:
                return midLPF;
            case SUBWOOFER_2WAY_NETWORK_MODE:
                return subwoofer2WayNetworkMode;
            default:
                throw new AssertionError("can't happen.");
        }
    }

    /**
     * [JASPER] Crossover設定取得.
     *
     * @param type FILTER種別
     * @return スピーカーのCrossover設定.
     * @throws NullPointerException {@code type}がnull
     */
    public JasperCrossoverSetting findJasperCrossoverSetting(@NonNull HpfLpfFilterType type) {
        switch (checkNotNull(type)) {
            case HPF:
                return jasperHpf;
            case LPF:
                return jasperLpf;
            default:
                throw new AssertionError("can't happen.");
        }
    }

    /**
     * リセット.
     */
    public void reset() {
        front.reset();
        rear.reset();
        subwooferStandardMode.reset();

        high.reset();
        midHPF.reset();
        midLPF.reset();
        subwoofer2WayNetworkMode.reset();

        jasperHpf.reset();
        jasperLpf.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("front", front)
                .add("rear", rear)
                .add("subwooferStandardMode", subwooferStandardMode)
                .add("high", high)
                .add("midHPF", midHPF)
                .add("midLPF", midLPF)
                .add("subwoofer2WayNetworkMode", subwoofer2WayNetworkMode)
                .add("jasperHpf", jasperHpf)
                .add("jasperLpf", jasperLpf)
                .toString();
    }

    /**
     * スピーカーのCrossover設定.
     */
    public static class SpeakerCrossoverSetting {
        /** スピーカー種別. */
        public final SpeakerType speakerType;
        /** HPF/LPF設定. */
        public HpfLpfSetting hpfLpfSetting;
        /** カットオフ周波数設定値. */
        public CutoffSetting cutoffSetting;
        /** スロープ設定値. */
        public SlopeSetting slopeSetting;

        /**
         * コンストラクタ.
         *
         * @param type スピーカー種別
         */
        public SpeakerCrossoverSetting(SpeakerType type) {
            speakerType = type;
        }

        /**
         * リセット.
         */
        public void reset() {
            hpfLpfSetting = HpfLpfSetting.OFF;

            switch (speakerType) {
                case FRONT:
                case REAR:
                case SUBWOOFER_STANDARD_MODE:
                    cutoffSetting = StandardCutoffSetting._50HZ;
                    slopeSetting = StandardSlopeSetting._6DB;
                    break;
                case HIGH:
                case MID_HPF:
                case MID_LPF:
                case SUBWOOFER_2WAY_NETWORK_MODE:
                    if (speakerType == SpeakerType.SUBWOOFER_2WAY_NETWORK_MODE
                            || speakerType == SpeakerType.MID_HPF) {
                        cutoffSetting = TwoWayNetworkSubwooferLpfMidHpfCutoffSetting._25HZ;
                    } else {
                        cutoffSetting = TwoWayNetworkMidLpfHighHpfCutoffSetting._1_25KHZ;
                    }
                    if (speakerType == SpeakerType.SUBWOOFER_2WAY_NETWORK_MODE) {
                        slopeSetting = TwoWayNetworkSubwooferLpfSlopeSetting._12DB;
                    } else {
                        slopeSetting = TwoWayNetworkMidHfpMidLfpHighHpfSlopeSetting._6DB;
                    }
                    break;
                default:
                    throw new AssertionError("can't happen.");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper("")
                    .add("speakerType", speakerType)
                    .add("hpfLpfSetting", hpfLpfSetting)
                    .add("cutoffSetting", cutoffSetting)
                    .add("slopeSetting", slopeSetting)
                    .toString();
        }
    }

    /**
     * [JASPER] スピーカーのCrossover設定.
     */
    public static class JasperCrossoverSetting {
        /** FILTER種別. */
        public final HpfLpfFilterType hpfLpfFilterType;
        /** HPF/LPF設定. */
        public HpfLpfSetting hpfLpfSetting;
        /** カットオフ周波数設定値. */
        public JasperCutoffSetting cutoffSetting;
        /** スロープ設定値. */
        public JasperSlopeSetting slopeSetting;

        /**
         * コンストラクタ.
         *
         * @param type FILTER種別
         */
        public JasperCrossoverSetting(HpfLpfFilterType type) {
            hpfLpfFilterType = type;
            if (type == HpfLpfFilterType.LPF) {
                hpfLpfSetting = HpfLpfSetting.ON_FIXED;
            }
        }

        /**
         * リセット.
         */
        public void reset() {
            if (hpfLpfFilterType == HpfLpfFilterType.LPF) {
                hpfLpfSetting = HpfLpfSetting.ON_FIXED;
            } else {
                hpfLpfSetting = HpfLpfSetting.OFF;
            }
            cutoffSetting = JasperCutoffSetting._50HZ;
            slopeSetting = JasperSlopeSetting._12DB;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper("")
                    .add("hpfLpfFilterType", hpfLpfFilterType)
                    .add("hpfLpfSetting", hpfLpfSetting)
                    .add("cutoffSetting", cutoffSetting)
                    .add("slopeSetting", slopeSetting)
                    .toString();
        }
    }
}
