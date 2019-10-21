package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

/**
 * デバイスリスト.
 */
public class DeviceListItem extends SettingListItem {
    /**
     * Audio接続中
     */
    public final boolean audioConnected;

    /**
     * Phone1接続中
     */
    public final boolean phone1Connected;

    /**
     * Phone2接続中
     */
    public final boolean phone2Connected;

    /**
     * オーディオフォーカス状態
     */
    public final boolean audioFocus;

    /**
     * ラストオーディオデバイス状態
     */
    public final boolean lastAudioDevice;

    /**
     * 車載器と連携中
     */
    public final boolean sessionConnected;

    public DeviceListItem(
            @NonNull String bdAddress, @NonNull String deviceName,
            boolean audioSupported, boolean phoneSupported,
            boolean audioConnected, boolean phone1Connected, boolean phone2Connected,
            boolean audioFocus, boolean lastAudioDevice, boolean sessionConnected
    ) {
        super(bdAddress, deviceName, audioSupported, phoneSupported);
        this.audioConnected = audioConnected;
        this.phone1Connected = phone1Connected;
        this.phone2Connected = phone2Connected;
        this.audioFocus = audioFocus;
        this.lastAudioDevice = lastAudioDevice;
        this.sessionConnected = sessionConnected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return super.addToString(helper)
                .add("audioConnected", audioConnected)
                .add("phone1Connected", phone1Connected)
                .add("phone2Connected", phone2Connected)
                .add("lastAudioDevice", lastAudioDevice)
                .add("audioFocus", audioFocus)
                .add("sessionConnected", sessionConnected);
    }

    public static class Builder {
        private String bdAddress;
        private String deviceName;
        private boolean audioSupported;
        private boolean phoneSupported;
        private boolean audioConnected;
        private boolean phone1Connected;
        private boolean phone2Connected;
        private boolean audioFocus;
        private boolean lastAudioDevice;
        private boolean sessionConnected;

        public Builder bdAddress(@NonNull String bdAddress) {
            this.bdAddress = bdAddress;
            return this;
        }

        public Builder deviceName(@NonNull String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public Builder audioSupported(boolean supported) {
            audioSupported = supported;
            return this;
        }

        public Builder phoneSupported(boolean supported) {
            phoneSupported = supported;
            return this;
        }

        public Builder audioConnected(boolean connected) {
            audioConnected = connected;
            return this;
        }

        public Builder phone1Connected(boolean connected) {
            phone1Connected = connected;
            return this;
        }

        public Builder phone2Connected(boolean connected) {
            phone2Connected = connected;
            return this;
        }

        public Builder audioFocus(boolean audioFocus) {
            this.audioFocus = audioFocus;
            return this;
        }

        public Builder lastAudioDevice(boolean lastAudioDevice) {
            this.lastAudioDevice = lastAudioDevice;
            return this;
        }

        public Builder sessionConnected(boolean connected) {
            sessionConnected = connected;
            return this;
        }

        public DeviceListItem build() {
            return new DeviceListItem(
                    bdAddress, deviceName,
                    audioSupported, phoneSupported,
                    audioConnected, phone1Connected, phone2Connected,
                    audioFocus, lastAudioDevice, sessionConnected);
        }
    }
}
