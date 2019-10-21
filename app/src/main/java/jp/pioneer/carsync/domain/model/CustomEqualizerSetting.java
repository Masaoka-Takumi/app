package jp.pioneer.carsync.domain.model;

import android.support.annotation.Size;

import com.google.common.base.MoreObjects;

/**
 * カスタムイコライザー設定.
 */
public class CustomEqualizerSetting extends SerialVersion {
    /** CUSTOM EQ種別. */
    public CustomEqType customEqType;
    /** 最小Step値. */
    public int minimumStep;
    /** 最大Step値. */
    public int maximumStep;
    /** BAND1(50Hz). */
    public int band1;
    /** BAND2(80Hz). */
    public int band2;
    /** BAND3(125Hz). */
    public int band3;
    /** BAND4(200Hz.5kHz). */
    public int band4;
    /** BAND5(315Hz). */
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

    /**
     * コンストラクタ.
     */
    public CustomEqualizerSetting() {
        reset();
    }

    /**
     * リセット
     */
    public void reset() {
        customEqType = CustomEqType.CUSTOM1;
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("customEqType", customEqType)
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
                .toString();
    }

    /**
     * 13band配列取得
     *
     * @return 13bandを配列にしたもの
     */
    @Size(13)
    public float[] getBandArray(){
        return new float[]{
                band1,
                band2,
                band3,
                band4,
                band5,
                band6,
                band7,
                band8,
                band9,
                band10,
                band11,
                band12,
                band13
        };
    }
}
