package com.google.android.exoplayer2.audio;

import android.support.annotation.NonNull;

import com.abaltatech.weblink.core.audioconfig.AudioFormat;
import com.abaltatech.wlmediamanager.EAudioFocusState;
import com.abaltatech.wlmediamanager.WLAudioManager;
import com.abaltatech.wlmediamanager.WLAudioStream;

import jp.pioneer.mbg.logmanager.TAGS;
import jp.pioneer.mbg.logmanager.TagManager;
import jp.pioneer.mobile.logger.api.Logger;

import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_CLOSED;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_NOCHANGE;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_PAUSED;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_PLAYING;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_WAITFOCUS;

public abstract class WLAudioStreamExProcessor {
    protected Logger logger = TagManager.getInstance().getLogger(TAGS.ExoPlayer);
    protected WLAudioStream stream = null;
    protected WLResult wlresult = new WLResult();

    abstract public int getPlayState();

    public void onEnter(WLAudioStream stream) {
        this.stream = stream;
    }

    public void onLeave() {
        this.stream = null;
    }

    public WLResult writeData(@NonNull byte[] buffer, int offset, int size, long presentationTimeUs) throws IllegalStateException, IllegalArgumentException, InterruptedException {
        stream.writeData(buffer, offset, size, presentationTimeUs);
        return wlresult.reset().setWrittenSize(size).noChange();
    }

    public WLResult pause() throws IllegalStateException {
        stream.pauseStream(true);
        WLAudioManager.getInstance().abandonAudioFocus(stream);
        logger.v("success");
        return wlresult.reset().next(PLAY_STATE_PAUSED);
    }

    public WLResult play() throws IllegalStateException {
        // Focusの再取得要求 + 再生開始要求を行う
        EAudioFocusState focus = WLAudioManager.getInstance().requestAudioFocus(stream);
        if (focus != EAudioFocusState.AF_Blocked_Permission) {
            // 再生開始要求
            stream.pauseStream(false);
            logger.v("success");
            return wlresult.reset().next(PLAY_STATE_PLAYING);
        } else {
            return wlresult.reset().next(PLAY_STATE_WAITFOCUS);
        }
    }

    public WLResult close() {
        // closeによる例外は握り潰す
        try {
            stream.closeStream();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            logger.e();
        }

        return wlresult.reset().next(PLAY_STATE_CLOSED);
    }

    public WLResult flush() throws IllegalStateException {
        stream.flush();
        return wlresult.reset().next(PLAY_STATE_NOCHANGE);
    }

    public WLResult getAudioFormat() throws IllegalStateException {
        AudioFormat audioformat = stream.getAudioFormat();
        return wlresult.reset().setAudioFormat(audioformat).next(PLAY_STATE_NOCHANGE);
    }

    public WLResult getChannelID() throws IllegalStateException {
        int channelID = stream.getChannelID();
        return wlresult.reset().setChannelID(channelID).next(PLAY_STATE_NOCHANGE);
   }

    public WLResult getPlaybackPositionUs() throws IllegalStateException {
        long playbackPositionUs = stream.getPlaybackPositionUs();
        return wlresult.reset().setPlaybackPositionUs(playbackPositionUs).next(PLAY_STATE_NOCHANGE);
    }
}
