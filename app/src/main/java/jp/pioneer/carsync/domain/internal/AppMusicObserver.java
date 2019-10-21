package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.AppMusicPlayPositionChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicPlaybackModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicRepeatModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicShuffleModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicTrackChangeEvent;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * AppMusicソース監視.
 * <p>
 * AppMusicソース関連の情報が更新された場合にイベントを発行する。
 *
 * @see AppMusicPlaybackModeChangeEvent
 * @see AppMusicPlayPositionChangeEvent
 * @see AppMusicTrackChangeEvent
 * @see AppMusicRepeatModeChangeEvent
 * @see AppMusicShuffleModeChangeEvent
 */
public class AppMusicObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private PlaybackMode mPlaybackMode;
    private int mPositionInSec;
    private long mMediaId;
    private SmartPhoneRepeatMode mRepeatMode;
    private ShuffleMode mShuffleMode;

    /**
     * コンストラクタ
     */
    @Inject
    public AppMusicObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mPlaybackMode = statusHolder.getSmartPhoneStatus().playbackMode;
        mPositionInSec = statusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo.positionInSec;
        mMediaId = statusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo.mediaId;
        mRepeatMode = statusHolder.getSmartPhoneStatus().repeatMode;
        mShuffleMode = statusHolder.getSmartPhoneStatus().shuffleMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        PlaybackMode playbackMode = statusHolder.getSmartPhoneStatus().playbackMode;
        if (playbackMode != mPlaybackMode) {
            mPlaybackMode = playbackMode;
            mEventBus.post(new AppMusicPlaybackModeChangeEvent());
        }

        int positionInSec = statusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo.positionInSec;
        if (positionInSec != mPositionInSec) {
            mPositionInSec = positionInSec;
            mEventBus.post(new AppMusicPlayPositionChangeEvent());
        }

        long mediaId = statusHolder.getCarDeviceMediaInfoHolder().androidMusicMediaInfo.mediaId;
        if (mediaId != mMediaId) {
            mMediaId = mediaId;
            mEventBus.post(new AppMusicTrackChangeEvent());
        }

        SmartPhoneRepeatMode repeatMode = statusHolder.getSmartPhoneStatus().repeatMode;
        if (repeatMode != mRepeatMode) {
            mRepeatMode = repeatMode;
            mEventBus.post(new AppMusicRepeatModeChangeEvent());
        }

        ShuffleMode shuffleMode = statusHolder.getSmartPhoneStatus().shuffleMode;
        if (shuffleMode != mShuffleMode) {
            mShuffleMode = shuffleMode;
            mEventBus.post(new AppMusicShuffleModeChangeEvent());
        }
    }
}
