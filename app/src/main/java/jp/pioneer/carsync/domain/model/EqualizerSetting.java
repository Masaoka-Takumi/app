package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * イコライザー設定.
 * <p>
 * 車載機のイコライザー設定を反映
 * 基本的に本クラスの値は参照しない
 * 各band値を取得する場合は本クラスは使用せず、
 * {@link SoundFxSetting#getEqualizerBandList(SoundFxSettingEqualizerType)}を使用して取得する
 */
public class EqualizerSetting extends SerialVersion {
    /** EQ種別. */
    public AudioSettingEqualizerType audioSettingEqualizerType;
    /** SPECIAL EQ種別. */
    public byte specialEqType;
    /** 最小Step値. */
    public int minimumStep;
    /** 最大Step値. */
    public int maximumStep;
    /** BAND1(50Hz ※JASPERは80Hz). */
    public int band1;
    /** BAND2(80Hz ※JASPERは250Hz). */
    public int band2;
    /** BAND3(125Hz ※JASPERは800Hz). */
    public int band3;
    /** BAND4(200Hz ※JASPERは2.5kHz). */
    public int band4;
    /** BAND5(315Hz ※JASPERは8kHz). */
    public int band5;
    /** BAND6(500Hz). */
    public int band6;
    /** BAND7(800Hz). */
    public int band7;
    /** BAND8(1.25KHz). */
    public int band8;
    /** BAND9(2KHz). */
    public int band9;
    /** BAND10(3.15KHz). */
    public int band10;
    /** BAND11(5KHz). */
    public int band11;
    /** BAND12(8KHz). */
    public int band12;
    /** BAND13(12.5KHz). */
    public int band13;
    /** 最小LEVEL値. */
    public int minimumLevel;
    /** 最大LEVEL値. */
    public int maximumLevel;
    /** LEVEL値. */
    public int currentLevel;

    /**
     * コンストラクタ.
     */
    public EqualizerSetting() {
        reset();
    }

    /**
     * リセット
     */
    public void reset() {
        audioSettingEqualizerType = AudioSettingEqualizerType.FLAT;
        specialEqType = 0x00;
        minimumStep = 0;
        maximumStep = 0;
        band1 = 0;
        band2 = 0;
        band3 = 0;
        band4 = 0;
        band5 = 0;
        band6 = 0;
        band7 = 0;
        band8 = 0;
        band9 = 0;
        band10 = 0;
        band11 = 0;
        band12 = 0;
        band13 = 0;
        minimumLevel = 0;
        maximumLevel = 0;
        currentLevel = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("audioSettingEqualizerType", audioSettingEqualizerType)
                .add("specialEqType", specialEqType)
                .add("minimumStep", minimumStep)
                .add("maximumStep", maximumStep)
                .add("band1", band1)
                .add("band2", band2)
                .add("band3", band3)
                .add("band4", band4)
                .add("band5", band5)
                .add("band6", band6)
                .add("band7", band7)
                .add("band8", band8)
                .add("band9", band9)
                .add("band10", band10)
                .add("band11", band11)
                .add("band12", band12)
                .add("band13", band13)
                .add("minimumLevel", minimumLevel)
                .add("maximumLevel", maximumLevel)
                .add("currentLevel", currentLevel)
                .toString();
    }
}
