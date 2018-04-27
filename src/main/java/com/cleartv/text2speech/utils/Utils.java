package com.cleartv.text2speech.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by Lipengfei on 2017/4/18.
 */

public class Utils {

    public static String TAG = "Utils";
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());


    /***
     * 获取指定目录下的所有的文件夹路径
     *
     * @param obj
     * @return
     */
    public static LinkedList<String> getListDirsPath(Object obj) {
        File directory = null;
        if (obj instanceof File) {
            directory = (File) obj;
        } else {
            directory = new File(obj.toString());
        }
        LinkedList<String> files = new LinkedList<>();
        if (directory.isFile()) {
            files.add(directory.getAbsolutePath());
            return new LinkedList<>();
        } else if (directory.isDirectory()) {
            File[] fileArr = directory.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                if(fileArr[i].isDirectory()){
                    files.add(fileArr[i].getAbsolutePath());
                }
            }
        }
        Collections.sort(files);
        Log.e("LPF",files.toString());
        return files;
    }



    /***
     * 获取指定目录下的所有的文件（不包括文件夹），采用了递归
     *
     * @param directory
     * @return
     */
    public static ArrayList<File> getListFiles(File directory) {
        ArrayList<File> files = new ArrayList<>();
        if (directory.isFile()) {
            files.add(directory);
            return files;
        } else if (directory.isDirectory()) {
            File[] fileArr = directory.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                File fileOne = fileArr[i];
                files.addAll(getListFiles(fileOne));
            }
        }
        return files;
    }

    public static String getValueByKey(String jsonStr, String key) throws JSONException {
        JSONTokener jsonParser = new JSONTokener(jsonStr);
        JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
        if(jsonObj.has(key)){
            return jsonObj.getString(key);
        }
        return null;
    }

    public static <T> T getBeanFromJson(String json,Class<T> cls){
        try {
            return new Gson().fromJson(json,cls);
        }catch (Exception e){
            return null;
        }
    }

    public static <T> ArrayList<T> getBeanListFromJson(String json, Class<T> cls){
        Type type = new TypeToken<ArrayList<JsonObject>>()
        {}.getType();
        ArrayList<JsonObject> jsonObjects = new Gson().fromJson(json, type);

        ArrayList<T> arrayList = new ArrayList<>();
        for (JsonObject jsonObject : jsonObjects){
            arrayList.add(new Gson().fromJson(jsonObject, cls));
        }
        return arrayList;
    }

    public static String getJsonFromBean(Object obj){
        try {
            return new Gson().toJson(obj);
        }catch (Exception e){
            return null;
        }
    }

    public static String getTime() {
        long time = System.currentTimeMillis();
        return timeFormat.format(new Date(time));
    }

    public static String getDay() {
        long time = System.currentTimeMillis();
        return dayFormat.format(new Date(time));
    }

    public static String getLocalIPAddres() {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {

        }

        return "0.0.0.0";
    }

    public static void playAudioTrack(String audioPath) {
        FileInputStream is = null;
        DataInputStream dis = null;
        AudioTrack track = null;
        try {
            int frequence = 16000;
            int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
            int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
            int streamType = AudioManager.STREAM_MUSIC;
            int bufferSize = AudioTrack.getMinBufferSize(frequence, channelConfig, audioEncoding);
            byte[] buffer = new byte[bufferSize / 4];
            File file = new File(audioPath);
            is = new FileInputStream(file);
            dis = new DataInputStream(new BufferedInputStream(is));
            track = new AudioTrack(streamType, frequence, channelConfig, audioEncoding, bufferSize,
                    AudioTrack.MODE_STREAM);
            track.setPlaybackRate(frequence);
        /* start play */
            track.setStereoVolume(1.0f, 1.0f);
            track.play();
            while (dis.available() > 0) {
                int i = 0;
                while (dis.available() > 0 && i < buffer.length) {
                    buffer[i] = dis.readByte();
                    i++;
                }
            /*write data to AudioTrack*/
                track.write(buffer, 0, buffer.length);
            }
        /*stop play*/
            track.stop();
            dis.close();
        }catch (Exception e){

        }finally {
            if(track!=null)
                track.stop();
            try {
                if(dis!=null)
                    dis.close();
                if(is!=null)
                    is.close();
            }catch (Exception e1){

            }
        }
    }

    public static void convertAudioFiles(String src, String target) throws Exception {
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(target);

        //计算长度
        byte[] buf = new byte[1024 * 4];
        int size = fis.read(buf);
        int PCMSize = 0;
        while (size != -1) {
            PCMSize += size;
            size = fis.read(buf);
        }
        fis.close();

        //填入参数，比特率等等。这里用的是16位单声道 16000 hz
        WaveHeader header = new WaveHeader();
        //长度字段 = 内容的大小（PCMSize) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
        header.fileLength = PCMSize + (44 - 8);
        header.FmtHdrLeth = 16;
        header.BitsPerSample = 16;
        header.Channels = 1;
        header.FormatTag = 0x0001;
        header.SamplesPerSec = 16000;
        header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
        header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
        header.DataHdrLeth = PCMSize;

        byte[] h = header.getHeader();

        assert h.length == 44; //WAV标准，头部应该是44字节
        //write header
        fos.write(h, 0, h.length);
        //write data stream
        fis = new FileInputStream(src);
        size = fis.read(buf);
        while (size != -1) {
            fos.write(buf, 0, size);
            size = fis.read(buf);
        }
        fis.close();
        fos.close();
        System.out.println("Convert OK!");
    }

    public static void deleteFile(String s) {
        File file = new File(s);
        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean doReboot() {
        String uri = "http://127.0.0.1:19003/index.html?op=reboot";
        Log.i(TAG, "do reboot ");
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.setRequestProperty("charset", "utf-8");
            conn.setConnectTimeout(10000);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                Log.e(TAG, "response code=" + conn.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "[" + Log.getStackTraceString(e) + "]");
        }
        return false;
    }

    public static void startAppByPackageName(Context ctx, String packageNmae) {
        try {
            Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(packageNmae);
            ctx.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "no such package:" + packageNmae);
        }
    }

    public static void openSystemSetting(Activity activity){
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings","com.android.settings.Settings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult( intent , 0);
    }

    public static void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void copyFromAssetsToSdcard(boolean isCover, String source, String dest , Context context) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = context.getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }


//    WavHeader辅助类。用于生成头部信息。

    public static class WaveHeader {
        public final char fileID[] = {'R', 'I', 'F', 'F'};
        public int fileLength;
        public char wavTag[] = {'W', 'A', 'V', 'E'};
        ;
        public char FmtHdrID[] = {'f', 'm', 't', ' '};
        public int FmtHdrLeth;
        public short FormatTag;
        public short Channels;
        public int SamplesPerSec;
        public int AvgBytesPerSec;
        public short BlockAlign;
        public short BitsPerSample;
        public char DataHdrID[] = {'d', 'a', 't', 'a'};
        public int DataHdrLeth;

        public byte[] getHeader() throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            WriteChar(bos, fileID);
            WriteInt(bos, fileLength);
            WriteChar(bos, wavTag);
            WriteChar(bos, FmtHdrID);
            WriteInt(bos, FmtHdrLeth);
            WriteShort(bos, FormatTag);
            WriteShort(bos, Channels);
            WriteInt(bos, SamplesPerSec);
            WriteInt(bos, AvgBytesPerSec);
            WriteShort(bos, BlockAlign);
            WriteShort(bos, BitsPerSample);
            WriteChar(bos, DataHdrID);
            WriteInt(bos, DataHdrLeth);
            bos.flush();
            byte[] r = bos.toByteArray();
            bos.close();
            return r;
        }

        private void WriteShort(ByteArrayOutputStream bos, int s) throws IOException {
            byte[] mybyte = new byte[2];
            mybyte[1] = (byte) ((s << 16) >> 24);
            mybyte[0] = (byte) ((s << 24) >> 24);
            bos.write(mybyte);
        }


        private void WriteInt(ByteArrayOutputStream bos, int n) throws IOException {
            byte[] buf = new byte[4];
            buf[3] = (byte) (n >> 24);
            buf[2] = (byte) ((n << 8) >> 24);
            buf[1] = (byte) ((n << 16) >> 24);
            buf[0] = (byte) ((n << 24) >> 24);
            bos.write(buf);
        }

        private void WriteChar(ByteArrayOutputStream bos, char[] id) {
            for (int i = 0; i < id.length; i++) {
                char c = id[i];
                bos.write(c);
            }
        }
    }
}
