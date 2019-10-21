package com.google.android.exoplayer2.audio;

import android.support.annotation.NonNull;

import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_NOCHANGE;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_PLAYING;

public class WLAudioStreamExProcessorPlaying extends WLAudioStreamExProcessor {

    @Override
    public int getPlayState() {
        return PLAY_STATE_PLAYING;
    }

    @Override
    public WLResult writeData(@NonNull byte[] buffer, int offset, int size, long presentationTimeUs) throws IllegalStateException, IllegalArgumentException, InterruptedException {
        return super.writeData(buffer, offset, size, presentationTimeUs);
    }

    @Override
    public WLResult pause() throws IllegalStateException {
        return super.pause();
    }

    @Override
    public WLResult play() throws IllegalStateException {
        // play中のplay要求は処理なしとする
        return wlresult.reset().noChange();
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
