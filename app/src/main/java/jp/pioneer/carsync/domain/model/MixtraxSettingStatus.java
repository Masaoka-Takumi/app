package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * MIXTRAX設定状態.
 */
@SuppressFBWarnings("UWF_NULL_FIELD")
public class MixtraxSettingStatus {
    /** SHORT PLAYBACK設定. */
    public ShortPlayback shortPlayback;
    /** 効果音設定有効. */
    public boolean soundEffectEnabled;
    /** 設定状態有効. */
    public boolean settingEnabled;

    /**
     * コンストラクタ.
     */
    public MixtraxSettingStatus() {
        reset();
    }

    /**
     * コピーコンストラクタ.
     *
     * @param mixtraxSettingStatus コピー元のオブジェクト
     */
    public MixtraxSettingStatus(MixtraxSettingStatus mixtraxSettingStatus) {
        shortPlayback = mixtraxSettingStatus.shortPlayback;
        soundEffectEnabled = mixtraxSettingStatus.soundEffectEnabled;
        settingEnabled = mixtraxSettingStatus.settingEnabled;
    }

    /**
     * リセット.
     */
    public void reset() {
        shortPlayback = null;
        soundEffectEnabled = false;
        settingEnabled = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof MixtraxSettingStatus)) {
            return false;
        }

        MixtraxSettingStatus other = (MixtraxSettingStatus) obj;
        return Objects.equal(shortPlayback, other.shortPlayback)
                && Objects.equal(soundEffectEnabled, other.soundEffectEnabled)
                && Objects.equal(settingEnabled, other.settingEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(shortPlayback,
                soundEffectEnabled,
                settingEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("shortPlayback", shortPlayback)
                .add("soundEffectEnabled", soundEffectEnabled)
                .add("settingEnabled", settingEnabled)
                .toString();
    }
}
