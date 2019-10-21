package com.google.android.exoplayer2.audio;

import android.support.annotation.NonNull;

import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_CLOSED;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_ERROR;

public class WLAudioStreamExProcessorError extends WLAudioStreamExProcessor {

    @Override
    public int getPlayState() {
        return PLAY_STATE_ERROR;
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
        return wlresult.reset().noChange();
    }

    @Override
    public WLResult close() {
        return super.close();
    }

    @Override
    public WLResult flush() throws IllegalStateException {
        return wlresult.reset().noChange();
    }

    @Override
    public WLResult getAudioFormat() throws IllegalStateException {
        return wlresult.reset().noChange();
    }

    @Override
    public WLResult getChannelID() throws IllegalStateException {
        return wlresult.reset().noChange();
    }

    @Override
    public WLResult getPlaybackPositionUs() throws IllegalStateException {
        return wlresult.reset().noChange();
    }

}
