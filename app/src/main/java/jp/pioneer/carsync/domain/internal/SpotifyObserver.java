package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.SpotifyInfoChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * Spotifyソース監視.
 * <p>
 * Spotifyソース関連の情報が更新された場合にイベントを発行する。
 *
 * @see SpotifyInfoChangeEvent
 */
public class SpotifyObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mInfoVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public SpotifyObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mInfoVersion = statusHolder.getCarDeviceMediaInfoHolder().spotifyMediaInfo.getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long infoVersion = statusHolder.getCarDeviceMediaInfoHolder().spotifyMediaInfo.getSerialVersion();
        if (infoVersion != mInfoVersion) {
            mInfoVersion = infoVersion;
            mEventBus.post(new SpotifyInfoChangeEvent());
        }
    }
}
