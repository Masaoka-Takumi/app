package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

/**
 * サーチリスト.
 */
public class SearchListItem extends SettingListItem {
    public SearchListItem(
            @NonNull String bdAddress, @NonNull String deviceName,
            boolean audioSupported, boolean phoneSupported
    ) {
        super(bdAddress, deviceName, audioSupported, phoneSupported);
    }

    public static class Builder {
        private String bdAddress;
        private String deviceName;
        private boolean audioSupported;
        private boolean phoneSupported;

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

        public SearchListItem build() {
            return new SearchListItem(
                    bdAddress, deviceName,
                    audioSupported, phoneSupported
            );
        }
    }
}
