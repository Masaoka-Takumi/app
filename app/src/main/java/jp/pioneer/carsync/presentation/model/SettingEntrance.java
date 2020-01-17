package jp.pioneer.carsync.presentation.model;

import jp.pioneer.carsync.R;

/**
 * Created by NSW00_007906 on 2017/10/02.
 */

public enum SettingEntrance {
    SYSTEM(R.string.hom_006),
    VOICE(R.string.hom_016),
    NAVIGATION(R.string.hom_017),
    MESSAGE(R.string.hom_018),
    PHONE(R.string.hom_019),
    CAR_SAFETY(R.string.hom_020),
    APPEARANCE(R.string.hom_021),
    SOUND_FX(R.string.hom_022),
    AUDIO(R.string.hom_008),
    DAB(R.string.src_014),
    RADIO(R.string.hom_023),
    HD_RADIO(R.string.src_015),
    FUNCTION(R.string.hom_024),
    INFORMATION(R.string.hom_012),
    AMAZON_ALEXA(R.string.hom_037),
    YOUTUBE_LINK(R.string.hom_039),
    ;

    private int mStrRes;

    SettingEntrance(int strRes) {
        mStrRes = strRes;
    }

    public int getResource() {
        return mStrRes;
    }
}
