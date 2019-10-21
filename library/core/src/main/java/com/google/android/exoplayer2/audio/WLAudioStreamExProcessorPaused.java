package com.google.android.exoplayer2.audio;

import android.support.annotation.NonNull;

import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_NOCHANGE;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_PAUSED;

public class WLAudioStreamExProcessorPaused extends WLAudioStreamExProcessor {

    @Override
    public int getPlayState() {
        return PLAY_STATE_PAUSED;
    }

    @Override
    public WLResult writeData(@NonNull byte[] buffer, int offset, int size, long presentationTimeUs) throws IllegalStateException, IllegalArgumentException, InterruptedException {
        return wlresult.reset().noChange();
    }

    @Override
    public WLResult pause() throws IllegalStateException {
        return super.pause();
    }

    @Override
    public WLResult play() throws IllegalStateException {
        return super.play();
    }

    @Override
    public WLResult close() {
        return super.close();
    }

    @Override
    public WLResult flush() throws IllegalStateException {
        return super.flush();
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
