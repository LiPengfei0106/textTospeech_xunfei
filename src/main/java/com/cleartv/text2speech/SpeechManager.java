package com.cleartv.text2speech;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.cleartv.text2speech.beans.ErrorMsgBean;
import com.cleartv.text2speech.beans.MsgBean;
import com.cleartv.text2speech.beans.SpeakerParamsBean;
import com.cleartv.text2speech.beans.SpeechListBean;
import com.cleartv.text2speech.utils.SPUtil;
import com.cleartv.text2speech.utils.Utils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SynthesizerListener;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Lipengfei on 2017/4/20.
 */

public class SpeechManager {

    private static final String TAG = "SpeechManager";
    private static SpeechManager manager;

    public boolean isSpeechInit = false;

    private Context context;

    // 语音合成对象
    private static com.iflytek.cloud.SpeechSynthesizer mTts;

    // 提示音间隔时间
    public static long intervalTime = 15000;
    private long lastSpeechTime = 0;

    private SoundPool mSoundPool;

    /**
     * 发音人
     */
//    public final static String[] COLOUD_VOICERS_ENTRIES = {"小燕", "小宇", "凯瑟琳", "亨利", "玛丽", "小研", "小琪", "小峰", "小梅", "小莉", "小蓉", "小芸", "小坤", "小强 ", "小莹",
//            "小新", "楠楠", "老孙","Mariane","Allabent","Gabriela","Abha","XiaoYun"};
//    public final static String[] COLOUD_VOICERS_VALUE = {"xiaoyan", "xiaoyu", "catherine", "henry", "vimary", "vixy", "xiaoqi", "vixf", "xiaomei",
//            "xiaolin", "xiaorong", "xiaoqian", "xiaokun", "xiaoqiang", "vixying", "xiaoxin", "nannan", "vils","Mariane","Allabent","Gabriela","Abha","XiaoYun"};

    private SpeechListBean speechListBean;
    private LinkedList<ErrorMsgBean> failedMsg = new LinkedList<>();

    private SynthesizerListener synthesizerListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            Log.i(TAG, "开始播放");
            sendMsg(Action.ACTION_SPEAKING_STARTE,"onSpeakBegin:"+speechListBean.speakingMsg.getText(),speechListBean.speakingMsg.getId());
        }

        @Override
        public void onSpeakPaused() {
            Log.i(TAG, "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            Log.i(TAG, "继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // TODO 缓冲的进度
            Log.i(TAG, "缓冲 : " + percent);
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // TODO 说话的进度
            Log.i(TAG, "合成 : " + percent);
        }

        @Override
        public void onCompleted(com.iflytek.cloud.SpeechError error) {
            if (error == null) {
                Log.i(TAG, "播放完成");
                try {
                    sendMsg(Action.ACTION_SPEAKING_END,"onCompleted:"+speechListBean.speakingMsg.getText(),speechListBean.speakingMsg.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, error.getPlainDescription(true));
                    sendMsg(Action.ACTION_SPEAKING_ERROR,e.getMessage(),speechListBean.speakingMsg.getId());
                }
            } else if (error != null) {
                Log.i(TAG, error.getPlainDescription(true));
                if(failedMsg.size()>300)
                    failedMsg.removeFirst();
                failedMsg.addLast(new ErrorMsgBean(speechListBean.speakingMsg,System.currentTimeMillis(),error.getErrorCode(),error.getErrorDescription()));
                sendMsg(Action.ACTION_SPEAKING_ERROR,"error code : "+ error.getErrorCode()+error.getPlainDescription(true),speechListBean.speakingMsg.getId());
            }
            lastSpeechTime = System.currentTimeMillis();
            startSpeak();

        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };


    public static SpeechManager getInstance() {
        if(manager == null){
            manager = new SpeechManager();
        }
        return manager;
    }

    public void init(final Context context) {
        this.context = context;
        if(speechListBean == null){
            speechListBean = new SpeechListBean();
            speechListBean.speechList = new LinkedList<>();
        }
        mSoundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM,5);
        mSoundPool.load(context,R.raw.dingdong,1);
        // 初始化合成对象
        if(mTts == null || !isSpeechInit){
            mTts = com.iflytek.cloud.SpeechSynthesizer.createSynthesizer(context, new InitListener() {
                @Override
                public void onInit(int code) {
                    if (code != ErrorCode.SUCCESS) {
                        sendMsg(Action.ACTION_SPEAKER_FAILED,"初始化语音合成失败 code:"+code);
                        isSpeechInit = false;
                    }else{
                        sendMsg(Action.ACTION_SPEAKER_SUCCESS,"初始化语音合成成功 code:"+code);
                        isSpeechInit = true;
                        // 设置音量
                        mTts.setParameter(SpeechConstant.VOLUME, (String) SPUtil.get(context,SpeechConstant.VOLUME, "100"));
                        // 设置音调
                        mTts.setParameter(SpeechConstant.PITCH, (String) SPUtil.get(context,SpeechConstant.PITCH, "50"));
                        // 设置语速
                        mTts.setParameter(SpeechConstant.SPEED, (String) SPUtil.get(context,SpeechConstant.SPEED, "50"));
                    }
                }
            });
        }else{
            sendMsg(Action.ACTION_SPEAKER_SUCCESS,"语音合成已初始化");
        }
    }

    public SpeakerParamsBean getSpeakerParams(){
        return new SpeakerParamsBean(mTts.getParameter(SpeechConstant.VOLUME),mTts.getParameter(SpeechConstant.PITCH),mTts.getParameter(SpeechConstant.SPEED));
    }

    public void setSpeaker(String[] atrs){
        // 设置语言
//        mTts.setParameter(SpeechConstant.LANGUAGE, "");
//        // 设置方言
//        mTts.setParameter(SpeechConstant.ACCENT, "");
//        // 设置发音人
//        mTts.setParameter(SpeechConstant.VOICE_NAME, requestBean.getSpeaker());
        // 设置音量
        SPUtil.putAndApply(context,SpeechConstant.VOLUME, atrs[0]);
        mTts.setParameter(SpeechConstant.VOLUME, atrs[0]);
        // 设置音调
        mTts.setParameter(SpeechConstant.PITCH, atrs[1]);
        SPUtil.putAndApply(context,SpeechConstant.PITCH, atrs[1]);
        // 设置语速
        mTts.setParameter(SpeechConstant.SPEED, atrs[2]);
        SPUtil.putAndApply(context,SpeechConstant.SPEED, atrs[2]);

    }

    public void speakSingleMsg(String msg,String id,int index){
        if(index>speechListBean.speechList.size())
            index = speechListBean.speechList.size();
        speechListBean.speechList.add(index , new MsgBean(id,msg));
        if(!mTts.isSpeaking())
            startSpeak();
    }

    public boolean addMsgByJson(String jsonMsg) {
        ArrayList<MsgBean> beans = Utils.getBeanListFromJson(jsonMsg,MsgBean.class);

        if(beans == null || beans.size()<1){
            sendMsg(Action.ACTION_SPEAKING_ERROR,"Probably JsonError:\n"+jsonMsg);
            return false;
        }
        for(MsgBean msg : beans){
            if(msg!=null && !TextUtils.isEmpty(msg.getText())){
                speechListBean.speechList.addLast(msg);
            }
        }
        if(!mTts.isSpeaking())
            startSpeak();
        return true;

    }

    private void startSpeak() {

        if(speechListBean.speechList.isEmpty()){
            // 转换完毕
            speechListBean.speakingMsg = null;
        }else {
            if(intervalTime > 0 && System.currentTimeMillis() - lastSpeechTime > intervalTime){
                // TODO

                mSoundPool.play(1,1, 1, 0, 0, 1);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            speechListBean.speakingMsg = speechListBean.speechList.removeFirst();
            int code = mTts.startSpeaking(speechListBean.speakingMsg.getText(), synthesizerListener);
            if (code != ErrorCode.SUCCESS) {
                if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                    sendMsg(Action.ACTION_SPEAKING_ERROR, "没有安装语音,错误码: " + code);
                } else {
                    sendMsg(Action.ACTION_SPEAKING_ERROR, "语音合成失败,错误码: " + code);
                }
            }

        }
    }


    public void stopSpeaking() {
        mTts.stopSpeaking();
    }

    public void pauseSpeaking() {
        mTts.pauseSpeaking();
    }

    public void resumeSpeaking() {
        mTts.resumeSpeaking();
    }

    public void clearSpeaking() {
        speechListBean.speechList.clear();
    }

    public void clearFailedList() {
        failedMsg.clear();
    }

    // 发送广播
    private void sendMsg(String action,String msg){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("msg",msg);
        context.sendBroadcast(intent);
    }

    private void sendMsg(String action,String msg,String id){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("msg",msg);
        intent.putExtra("id",id);
        context.sendBroadcast(intent);
    }

    public String getMsgListInfo(){
        return Utils.getJsonFromBean(speechListBean);
    }

    public SpeechListBean getSpeechListBean(){
        return  speechListBean;
    }

    public LinkedList<ErrorMsgBean> getFailedMsg(){
        return failedMsg;
    }

    public String getFailedListInfo(){
        return Utils.getJsonFromBean(failedMsg);
    }

}
