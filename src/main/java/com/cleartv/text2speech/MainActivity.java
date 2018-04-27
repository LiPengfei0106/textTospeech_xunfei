package com.cleartv.text2speech;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cleartv.text2speech.server.VodServer;
import com.cleartv.text2speech.utils.SPUtil;
import com.cleartv.text2speech.utils.Utils;

import java.util.Calendar;

public class MainActivity extends Activity {

    MyReceiver broadcastreciver;
    private EditText et_ip_1;
    private EditText et_ip_2;
    private EditText et_ip_3;
    private EditText et_ip_4;
    private EditText et_ip_port,et_delay_time;
    private LinearLayout msg_content;
    private EditText et_address;
    private EditText et_stationId;
    private TextView ip_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this,MyService.class));
        setContentView(R.layout.main_activity);

        String ip_1 = (String) SPUtil.get(this,"ip_1","");
        String ip_2 = (String) SPUtil.get(this,"ip_2","");
        String ip_3 = (String) SPUtil.get(this,"ip_3","");
        String ip_4 = (String) SPUtil.get(this,"ip_4","");
        String ip_port = (String) SPUtil.get(this,"ip_port","80");
        String address = (String) SPUtil.get(this,"address","hqueue/mediaBox/heartBeat");
        String stationId = (String) SPUtil.get(this,"et_stationId","");
        SpeechManager.intervalTime = (int) SPUtil.get(this,"intervalTime",0);

        msg_content = (LinearLayout) findViewById(R.id.msg_content);

        et_ip_1 = (EditText) findViewById(R.id.ip_1);
        et_ip_2 = (EditText) findViewById(R.id.ip_2);
        et_ip_3 = (EditText) findViewById(R.id.ip_3);
        et_ip_4 = (EditText) findViewById(R.id.ip_4);
        et_ip_port = (EditText) findViewById(R.id.ip_port);
        et_address = (EditText) findViewById(R.id.address);
        et_stationId = (EditText) findViewById(R.id.et_station_id);
        et_delay_time = (EditText) findViewById(R.id.et_delay_time);

        et_ip_1.setText(ip_1);
        et_ip_2.setText(ip_2);
        et_ip_3.setText(ip_3);
        et_ip_4.setText(ip_4);
        et_ip_port.setText(ip_port);
        et_address.setText(address);
        et_stationId.setText(stationId);

        findViewById(R.id.heart_beat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeHost();
            }
        });

        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Utils.startAppByPackageName(MainActivity.this, "com.android.settings");
                Utils.openSystemSetting(MainActivity.this);
            }
        });

        ip_address = (TextView) findViewById(R.id.ip_address);

        et_delay_time.setText("" + SpeechManager.intervalTime);
        et_delay_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    SpeechManager.intervalTime = Integer.valueOf(s.toString());
                    SPUtil.putAndApply(MainActivity.this,"intervalTime",(int)SpeechManager.intervalTime);
                }catch (Exception e){

                }
            }
        });
    }

    private void changeHost() {
        String ip_1 = et_ip_1.getText().toString();
        String ip_2 = et_ip_2.getText().toString();
        String ip_3 = et_ip_3.getText().toString();
        String ip_4 = et_ip_4.getText().toString();
        if(TextUtils.isEmpty(ip_1) || TextUtils.isEmpty(ip_2) || TextUtils.isEmpty(ip_3) || TextUtils.isEmpty(ip_4) ){
            Toast.makeText(this,"不能为空！",Toast.LENGTH_LONG).show();
            return;
        }

        String ip_port = et_ip_port.getText().toString();
        String address = et_address.getText().toString();
        String stationId = et_stationId.getText().toString();
        String host_address = new StringBuffer().append("http://")
                .append(ip_1).append(".")
                .append(ip_2).append(".")
                .append(ip_3).append(".")
                .append(ip_4).append(":")
                .append(ip_port).append("/")
                .append(address).toString();

        MyService.changeServerInfo(this,host_address,stationId);

        SPUtil.putAndApply(this,"ip_1",ip_1);
        SPUtil.putAndApply(this,"ip_2",ip_2);
        SPUtil.putAndApply(this,"ip_3",ip_3);
        SPUtil.putAndApply(this,"ip_4",ip_4);
        SPUtil.putAndApply(this,"ip_port",ip_port);
        SPUtil.putAndApply(this,"address",address);
//        SPUtil.putAndApply(this,"host_address",host_address);
//        SPUtil.putAndApply(this,"et_stationId",stationId);
//
//        MyService.hostaddress = host_address;
//        MyService.stationId = stationId;


        Intent intent = new Intent();
        intent.setAction(Action.ACTION_SERVICE_HEARTBEAT);
        intent.putExtra("msg","更改HeartBeat HostAddress :" + host_address);
        sendBroadcast(intent);

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        // 动态注册广播
        broadcastreciver = new MyReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Action.ACTION_SERVER_ON);
        intentfilter.addAction(Action.ACTION_SERVER_ERROR);
        intentfilter.addAction(Action.ACTION_SERVER_OFF);
        intentfilter.addAction(Action.ACTION_SERVER_REQUEST);
        intentfilter.addAction(Action.ACTION_SERVER_RESPONSE);

        intentfilter.addAction(Action.ACTION_SPEAKER_SUCCESS);
        intentfilter.addAction(Action.ACTION_SPEAKER_FAILED);
        intentfilter.addAction(Action.ACTION_SPEAKING_STARTE);
        intentfilter.addAction(Action.ACTION_SPEAKING_END);
        intentfilter.addAction(Action.ACTION_SPEAKING_ERROR);

        intentfilter.addAction(Action.ACTION_SERVICE_HEARTBEAT);
        intentfilter.addAction(Action.ACTION_SERVICE_DESTORY);
        registerReceiver(broadcastreciver, intentfilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        unregisterReceiver(broadcastreciver);
        super.onStop();
    }

    public void addMsg(int color, String msg){
        TextView view = new TextView(this);
        view.setTextColor(color);
        view.setTextSize(20);
        view.setText(msg);
        msg_content.addView(view,0);
        if(msg_content.getChildCount() > 300){
            msg_content.removeViewAt(300);
        }
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String id = intent.getStringExtra("id");
            String msg = intent.getStringExtra("msg");
            String info = Calendar.getInstance().getTime().toLocaleString() + "：" +msg;
            Log.e("MyReceiver",action+"\nid:"+id+"\nmsg:"+msg);
            switch (action){
                case Action.ACTION_SERVER_ON:
                    addMsg(Color.argb(255,119,195,79),info); // light green
                    ip_address.setText(VodServer.ipAddress);
                    break;
                case Action.ACTION_SERVER_ERROR:
                    addMsg(Color.argb(255,235,63,47),info); //red
                    break;
                case Action.ACTION_SERVER_OFF:
                    addMsg(Color.argb(255,86,163,108),info); //dark green
                    break;
                case Action.ACTION_SERVER_REQUEST:
                    addMsg(Color.argb(255,94,133,121),info); //gray green
                    break;
                case Action.ACTION_SERVER_RESPONSE:
                    addMsg(Color.argb(255,94,133,121),info); //gray green
                    break;


                case Action.ACTION_SPEAKER_SUCCESS:
                    addMsg(Color.argb(255,119,195,79),info); // light green
                    break;
                case Action.ACTION_SPEAKER_FAILED:
                    addMsg(Color.argb(255,235,63,47),info); //red
                    break;
                case Action.ACTION_SPEAKING_STARTE:
                    addMsg(Color.argb(255,86,163,108),info);
                    break;
                case Action.ACTION_SPEAKING_END:
                    addMsg(Color.argb(255,119,195,79),info);
                    break;
                case Action.ACTION_SPEAKING_ERROR:
                    addMsg(Color.argb(255,242,195,63),info); //orange
                    break;

                case Action.ACTION_SERVICE_HEARTBEAT:
                    addMsg(Color.RED,info);
                    break;
                case Action.ACTION_SERVICE_DESTORY:
                    break;
            }

        }
    }
}
