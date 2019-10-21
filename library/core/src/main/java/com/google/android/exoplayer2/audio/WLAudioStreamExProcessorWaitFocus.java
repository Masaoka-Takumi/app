package com.google.android.exoplayer2.audio;

import android.support.annotation.NonNull;

import com.abaltatech.wlmediamanager.WLAudioStream;

import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_NOCHANGE;
import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_WAITFOCUS;

public class WLAudioStreamExProcessorWaitFocus extends WLAudioStreamExProcessor {

    private long mPrevTryTime;

    @Override
    public int getPlayState() {
        return PLAY_STATE_WAITFOCUS;
    }

    @Override
    public void onEnter(WLAudioStream stream) {
        super.onEnter(stream);
        mPrevTryTime = 0;
    }

    @Override
    public void onLeave() {
        super.onEnter(stream);
        mPrevTryTime = 0;
    }


    @Override
    public WLResult writeData(@NonNull byte[] buffer, int offset, int size, long presentationTimeUs) throws IllegalStateException, IllegalArgumentException, InterruptedException {
        return retryPlay();
    }

    @Override
    public WLResult pause() throws IllegalStateException {
        return super.pause();
    }

    @Override
    public WLResult play() throws IllegalStateException {
        return retryPlay();
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
        return wlresult.reset().noChange();
    }


    ////////////////////////////////////////////////////////
    // private
    ////////////////////////////////////////////////////////

    private WLResult retryPlay() throws IllegalStateException {
        // 前回retryから1秒経ってない場合はSkip
        if (System.currentTimeMillis() - mPrevTryTime < 1000) {
            return wlresult.reset().noChange();
        }

        mPrevTryTime = System.currentTimeMillis();
        return super.play();
    }

}
