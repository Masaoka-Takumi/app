package com.google.android.exoplayer2.audio;

import android.support.annotation.NonNull;

import com.abaltatech.wlmediamanager.EAudioFocusState;
import com.abaltatech.wlmediamanager.WLAudioManager;

import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_CREATED;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_NOCHANGE;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_PLAYING;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_WAITFOCUS;

public class WLAudioStreamExProcessorCreated extends WLAudioStreamExProcessor {

    @Override
    public int getPlayState() {
        return PLAY_STATE_CREATED;
    }

    @Override
    public WLResult writeData(@NonNull byte[] buffer, int offset, int size, long presentationTimeUs) throws IllegalStateException, IllegalArgumentException, InterruptedException {
        return wlresult.reset().noChange();
    }

    @Override
    public WLResult pause() throws IllegalStateException {
        return wlresult.reset().noChange();
    }

    @Override
    public WLResult play() throws IllegalStateException {
        // Focusの取得要求
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

    @Override
    public WLResult close() {
        return wlresult.reset().noChange();
    }

    @Override
    public WLResult flush() throws IllegalStateException {
        return wlresult.reset().noChange();
    }

    @Override
    public WLResult getAudioFormat() throws IllegalStateException {
        return super.getAudioFormat();
    }

    @Override
    public WLResult getChannelID() throws IllegalStateException {
        return super.getChannelID();
    }

    @Override
    public WLResult getPlaybackPositionUs() throws IllegalStateException {
        return super.getPlaybackPositionUs();
    }

}
