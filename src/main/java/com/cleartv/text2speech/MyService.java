package com.cleartv.text2speech;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.cleartv.text2speech.beans.HeartBeatBean;
import com.cleartv.text2speech.server.VodServer;
import com.cleartv.text2speech.utils.SPUtil;
import com.cleartv.text2speech.utils.Utils;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyService extends Service {

    Notification notification;
    private MyBinder binder = new MyBinder();
    private boolean isHeartBeatRun = false;
    public static String hostaddress;
    public static String stationId;
    OkHttpClient client = new OkHttpClient();

    public MyService() {
    }

    @Override
    public void onCreate() {
        hostaddress = (String) SPUtil.get(this,"host_address","");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SpeechManager.getInstance().init(this);
        VodServer.instance().startWork(this);

        notification = new Notification.Builder(this.getApplicationContext())
                .setContentText("文本转语音服务")
//                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(110, notification);
        startHeartBeat();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        isHeartBeatRun = false;
        sendMsg(Action.ACTION_SERVICE_DESTORY,"");
        super.onDestroy();
    }

    public class MyBinder extends Binder {

        public MyService getService() {
            return MyService.this;
        }
    }

    public void startHeartBeat() {
        if(isHeartBeatRun)
            return;
        isHeartBeatRun = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isHeartBeatRun) {
                    try {
                        if(!TextUtils.isEmpty(hostaddress)){
                            String json = Utils.getJsonFromBean(
                                    new HeartBeatBean(
                                            VodServer.instance().isAlive(),
                                            SpeechManager.getInstance().isSpeechInit,
                                            SpeechManager.getInstance().getSpeechListBean().speakingMsg,
                                            SpeechManager.getInstance().getSpeechListBean().speechList,
                                            SpeechManager.getInstance().getSpeakerParams(),
                                            VodServer.ipAddress,stationId,
                                            SpeechManager.getInstance().getFailedMsg())
                            );

                            Request request = new Request.Builder().url(hostaddress).post(RequestBody.create(MediaType.parse("application/json"),json)).build();
                            try {
                                Response response = client.newCall(request).execute();
                                if(response.isSuccessful()){
                                    sendMsg(Action.ACTION_SERVICE_HEARTBEAT,"HeartBeat send success :" + response.body().toString());
                                }else{
                                    sendMsg(Action.ACTION_SERVICE_HEARTBEAT,"HeartBeat send failed :" + response);
                                }
                            }catch (IOException e) {
                                sendMsg(Action.ACTION_SERVICE_HEARTBEAT,"HeartBeat send failed :" + e.getLocalizedMessage());
                                e.printStackTrace();
                            }
                        }else{
                            sendMsg(Action.ACTION_SERVICE_HEARTBEAT,"HostAddress is null");
                        }
                        Thread.sleep(1000 * 5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void sendMsg(String action,String msg){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("msg",msg);
        sendBroadcast(intent);
    }

}
