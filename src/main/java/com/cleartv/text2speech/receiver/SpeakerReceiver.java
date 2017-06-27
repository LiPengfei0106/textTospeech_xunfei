package com.cleartv.text2speech.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cleartv.text2speech.Action;
import com.cleartv.text2speech.MyService;
import com.cleartv.text2speech.SpeechManager;
import com.cleartv.text2speech.server.VodServer;

/**
 * Created by Lee on 2017/5/31.
 */

public class SpeakerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String id = intent.getStringExtra("id");
        String msg = intent.getStringExtra("msg");
        Log.e("SpeakerReceiver",action+"\nid:"+id+"\nmsg:"+msg);
        switch (action){
            case Action.ACTION_SERVER_ON:
                break;
            case Action.ACTION_SERVER_ERROR:
                VodServer.instance().startWork(context);
                break;
            case Action.ACTION_SERVER_OFF:
                VodServer.instance().startWork(context);
                break;
            case Action.ACTION_SERVER_REQUEST:
                break;
            case Action.ACTION_SERVER_RESPONSE:
                break;


            case Action.ACTION_SPEAKER_SUCCESS:
                break;
            case Action.ACTION_SPEAKER_FAILED:
                SpeechManager.getInstance().init(context);
                break;
            case Action.ACTION_SPEAKING_STARTE:
                break;
            case Action.ACTION_SPEAKING_END:
                break;
            case Action.ACTION_SPEAKING_ERROR:
                break;

            case Intent.ACTION_BOOT_COMPLETED:
            case Action.ACTION_SERVICE_DESTORY:
                context.startService(new Intent(context,MyService.class));
                break;
        }

    }
}
