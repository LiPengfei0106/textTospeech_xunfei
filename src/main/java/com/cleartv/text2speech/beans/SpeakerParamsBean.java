package com.cleartv.text2speech.beans;

/**
 * Created by Lee on 2017/6/14.
 */

public class SpeakerParamsBean {
    private String volume;
    private String pitch;
    private String speed;

    public SpeakerParamsBean(String volume, String pitch, String speed) {
        this.volume = volume;
        this.pitch = pitch;
        this.speed = speed;
    }
}
