package jp.pioneer.carsync.presentation.model;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.R;
import timber.log.Timber;

public enum YouTubeLinkSearchItem{
    ARTIST(R.string.set_398),
    MUSIC_TITLE(R.string.set_399),
    INFORMATION(R.string.set_400),
    DYNAMIC_LABEL(R.string.set_401),
    ;
    public final int textResource;

    YouTubeLinkSearchItem(int textResource) {
        this.textResource = textResource;
    }

    @NonNull
    @Override
    public String toString() {
        return "YoutubeLinkSearchItem_" + name();
    }
}