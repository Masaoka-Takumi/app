package com.google.android.exoplayer2.audio;

import com.abaltatech.weblink.core.audioconfig.AudioFormat;

import static com.google.android.exoplayer2.audio.WLAudioStreamEx.PLAY_STATE_NOCHANGE;

public class WLResult {

    public int nextState;
    public int writtenSize;
    public AudioFormat audioformat;
    public int channelID;
    public long playbackPositionUs;

    public WLResult() {
        reset();
    }

    public WLResult reset() {
        this.nextState = PLAY_STATE_NOCHANGE;
        this.writtenSize = 0;
        this.audioformat = null;
        this.channelID = -1;
        this.playbackPositionUs = 0;

        return this;
    }

    public WLResult noChange() {
        this.nextState = PLAY_STATE_NOCHANGE;
        return this;
    }


    public WLResult next(int next) {
        this.nextState = next;
        return this;
    }

    public WLResult setWrittenSize(int writtenSize) {
        this.writtenSize = writtenSize;
        return this;
    }

    public WLResult setAudioFormat(AudioFormat audioformat) {
        this.audioformat = audioformat;
        return this;
    }

    public WLResult setChannelID(int channelID) {
        this.channelID = channelID;
        return this;
    }


    public WLResult setPlaybackPositionUs(long playbackPositionUs) {
        this.playbackPositionUs = playbackPositionUs;
        return this;
    }

}
