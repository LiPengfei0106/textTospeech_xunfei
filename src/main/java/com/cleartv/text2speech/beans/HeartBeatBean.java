package com.cleartv.text2speech.beans;

import java.util.LinkedList;

/**
 * Created by Lee on 2017/6/14.
 */

public class HeartBeatBean {
    private MsgBean speakingMsg;
    private LinkedList<MsgBean> speechList;
    private SpeakerParamsBean speakerParams;
    private LinkedList<ErrorMsgBean> failedSpeechMsgList;
    private boolean isHttpServerAlive;
    private boolean isSpeechManagerInit;
    private String serverAddress;
    private String stationId;

    public HeartBeatBean(boolean isHttpServerAlive, boolean isSpeechManagerInit, MsgBean speakingMsg,LinkedList<MsgBean> speechList, SpeakerParamsBean speakerParams,String serverAddress,String stationId,LinkedList<ErrorMsgBean> failedSpeechMsgList) {
        this.speakingMsg = speakingMsg;
        this.speechList = speechList;
        this.speakerParams = speakerParams;
        this.isHttpServerAlive = isHttpServerAlive;
        this.isSpeechManagerInit = isSpeechManagerInit;
        this.serverAddress = serverAddress;
        this.stationId = stationId;
        this.failedSpeechMsgList = failedSpeechMsgList;
    }
}
