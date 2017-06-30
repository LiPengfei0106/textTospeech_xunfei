package com.cleartv.text2speech.server;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.cleartv.text2speech.Action;
import com.cleartv.text2speech.MainActivity;
import com.cleartv.text2speech.MyService;
import com.cleartv.text2speech.SpeechManager;
import com.cleartv.text2speech.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VodServer extends NanoHTTPD {
    public final static String TAG = "VodServer";
    public static int port = 19000;
    public static String ipAddress = "http://" + Utils.getLocalIPAddres() + ":" + port;
    private Context context;

    private static VodServer mLocalServer = null;

    private Handler mHandler = new Handler();
    private Runnable startRunnable = new Runnable() {

        @Override
        public void run() {
            port++;
            if(port>19100)
                port =19000;
            mLocalServer = new VodServer(port);
            mLocalServer.startWork(context);
        }
    };

    private VodServer(int port) {
        super(port);
    }

    public static VodServer instance() {
        if (mLocalServer == null) {
            mLocalServer = new VodServer(port);
        }
        return mLocalServer;
    }

    public void startWork(Context context) {
        this.context = context;
        try {
            if (!isAlive())
                start();
        } catch (Exception e) {
            sendMsg(Action.ACTION_SERVER_ERROR, e.getLocalizedMessage());
        }
        if (wasStarted()) {
            sendMsg(Action.ACTION_SERVER_ON, "本地服务已启动！");
            ipAddress = "http://" + Utils.getLocalIPAddres() + ":" + port;
        }else{
            mHandler.postDelayed(startRunnable, 3000);
            sendMsg(Action.ACTION_SERVER_ERROR,"启动失败，3秒后重启...");
        }
    }

    private void sendMsg(String action, String msg) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("msg", msg);
        context.sendBroadcast(intent);
    }

    public void stopWork() {
        mLocalServer.stop();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> files = new HashMap<String, String>();
        String body = null;
        String mimeType = "text/html";
        Response.Status status = Response.Status.OK;
        String responseStr = "";
        /*获取header信息，NanoHttp的header不仅仅是HTTP的header，还包括其他信息。*/
        Map<String, String> headers = session.getHeaders();
        if (Method.POST.equals(session.getMethod())) {
            synchronized (this) {
                try {
                /*这句尤为重要就是将将body的数据写入files中，大家可以看看parseBody具体实现，倒现在我也不明白为啥这样写。*/
                    session.parseBody(files);
                /*看就是这里，POST请教的body数据可以完整读出*/
                    body = session.getQueryParameterString();
                    sendMsg(Action.ACTION_SERVER_REQUEST, "HTTP POST\nREQUEST BODY:" + body);
                    if (session.getUri().equals("/server")){
                        MyService.changeServerInfo(context,Utils.getValueByKey(body,"serverUrl"),Utils.getValueByKey(body,"stationID"));
                        responseStr = "设置成功";
                    }else{
                        if (SpeechManager.getInstance().addMsgByJson(body)) {
                            responseStr = SpeechManager.getInstance().getMsgListInfo();
                        } else {
                            status = Response.Status.BAD_REQUEST;
                            responseStr = body;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    status = Response.Status.INTERNAL_ERROR;
                    responseStr = "500\n" + e + "\nbody:\n" + body;
                }
            }
        } else {
            try {
                sendMsg(Action.ACTION_SERVER_REQUEST, "HTTP \nREQUEST URI:" + session.getUri());
                if (session.getUri().equals("/startactivity")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else if (session.getUri().equals("/stop")) {
                    SpeechManager.getInstance().stopSpeaking();
                } else if (session.getUri().equals("/resume")) {
                    SpeechManager.getInstance().resumeSpeaking();
                } else if (session.getUri().equals("/pause")) {
                    SpeechManager.getInstance().pauseSpeaking();
                } else if (session.getUri().equals("/clear")) {
                    SpeechManager.getInstance().clearSpeaking();
                } else if (session.getUri().equals("/init")) {
                    SpeechManager.getInstance().init(context);
                } else if (session.getUri().startsWith("/setting")) {
                    String[] strs = session.getUri().split("/");
                    String[] atrs = {"100", "50", "50"};
                    for (int i = 2; i < strs.length; i++) {
                        if (i < 5 ){
                            try{
                                int val = Integer.parseInt(strs[i]);
                                if(val<=100 && val >=0){
                                    atrs[i - 2] = strs[i];
                                }
                            }catch (Exception e){

                            }
                        }
                    }
                    SpeechManager.getInstance().setSpeaker(atrs);
                    responseStr = "volume :" + atrs[0] + "\n"
                            + "pitch :" + atrs[1] + "\n"
                            + "speed :" + atrs[2] + "\n";
                } else if (session.getUri().equals("/speechlist")) {
                    responseStr = SpeechManager.getInstance().getMsgListInfo();
                } else if (session.getUri().equals("/speechfailedlist")) {
                    responseStr = SpeechManager.getInstance().getFailedListInfo();
                } else if (session.getUri().equals("/clearfailedlist")) {
                    SpeechManager.getInstance().clearFailedList();
                } else {
                    String[] msgs = session.getUri().split("/");
                    int index = 0;
                    try {
                        index = msgs.length > 3 ? Integer.parseInt(msgs[3]) : 0;
                    } catch (Exception e) {
                        index = 0;
                    }

                    SpeechManager.getInstance().speakSingleMsg(msgs[1], msgs.length > 2 ? msgs[2] : "插入语音", index);
                    responseStr = SpeechManager.getInstance().getMsgListInfo();
                }
            } catch (Exception e) {
                e.printStackTrace();
                status = Response.Status.INTERNAL_ERROR;
                responseStr = "500\n" + e + "\nuri:\n" + session.getUri();
            }

        }
        sendMsg(Action.ACTION_SERVER_RESPONSE, "Response status:" + status.name() + "\nResponse body:" + responseStr);
        return new Response(status, mimeType, responseStr);
    }

}