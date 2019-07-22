package com.honeywell.scan_demo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    // Intent定义，不同厂家定义不同值
    private static final String HONEYWELL_INTENT_ACTION = "com.honeywell.action.BARCODE_DATA";
    private static final String ZEBRA_INTENT_ACTION = "com.zebra.action.BARCODE_DATA";

    private static final String INTENT_ACTION = "com.intent.action.BARCODE";

    private static final String ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER";
    private static final String ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER";
    private static final String EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER";
    private static final String EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE";
    private static final String EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES";

    private static final String EXTRA_CONTROL = "com.honeywell.aidc.action.ACTION_CONTROL_SCANNER";
    private static final String EXTRA_SCAN = "com.honeywell.aidc.extra.EXTRA_SCAN";

    // Zebra和Honeywell 扫描数据标识
    private static final String HONEYWELL_DATA_STRING_TAG = "data";
    private static final String ZEBRA_DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    // 设备厂家名称定义
    private static final String Honeywell = "Honeywell";
    private static final String Zebra = "Zebra";

    private TextView textview;
    private TextView tvResult;
    private Button button;
    private String BRAND="";

    // SOCKET相关设置，服务器IP，端口等信息
    private static final String HOST = "192.168.1.153";
    private static final int PORT = 8899;
    private Socket client = null;
    private BufferedReader bufferedReader = null;
    private OutputStream outputStream = null;
    private PrintWriter printWriter = null;
    private String content = "";
    private Boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initSocket();
        Receive();
        textview = findViewById(R.id.textview);
        tvResult = findViewById(R.id.tvResult);
        button = findViewById(R.id.button);
        button.setText("Start Scan");
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //sendBroadcast(new Intent(EXTRA_CONTROL).putExtra(EXTRA_SCAN, true));
            }
        });
        BRAND = Build.BRAND;
        textview.setText(BRAND);
    }

    @Override
    protected void onResume(){
        super.onResume();
        /*if (BRAND.equals(Zebra)) {
            registerReceiver(broadcastReceiver, new IntentFilter(ZEBRA_INTENT_ACTION));

        }else if(BRAND.equals(Honeywell)) {
            registerReceiver(broadcastReceiver, new IntentFilter(HONEYWELL_INTENT_ACTION));
        }*/

        // 通用设置
        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION));
        //claimScanner();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        //releaseScanner();
    }

    private void claimScanner(){
        Bundle properties = new Bundle();
        properties.putBoolean("DPR_DATA_INTENT", true);
        properties.putString("DPR_DATA_INTENT_ACTION",HONEYWELL_INTENT_ACTION);
        sendBroadcast(new Intent(ACTION_CLAIM_SCANNER)
                    .putExtra(EXTRA_SCANNER, "dcs.scanner.imager")
                    .putExtra(EXTRA_PROFILE,"DEFAULT")
                    .putExtra(EXTRA_PROPERTIES, properties));
    }

    private void releaseScanner(){
        sendBroadcast(new Intent(ACTION_RELEASE_SCANNER));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        private String data = "";
        @Override
        public void onReceive(Context context, Intent intent) {

            if (BRAND.equals(Zebra)){
                data = intent.getStringExtra(ZEBRA_DATA_STRING_TAG);
            }else if (BRAND.equals(Honeywell)){
                data = intent.getStringExtra(HONEYWELL_DATA_STRING_TAG);
            }
            setText(data);
            sendMsg(data);

            /*if (client.isConnected()){
                if (!client.isOutputShutdown()){
                    Toast.makeText(MainActivity.this, "正在发送，请稍等......", Toast.LENGTH_LONG).show();
                    Receive();
                }
            }*/
            /*if (ACTION_BARCODE_DATA.equals(intent.getAction())){
                int version = intent.getIntExtra("version",0);
                if (version>=1){
                    String aimId = intent.getStringExtra("aimId");
                    String charset = intent.getStringExtra("charset");
                    String codeId = intent.getStringExtra("codeId");
                    String data = intent.getStringExtra("data");
                    byte[] dataBytes = intent.getByteArrayExtra("dataBytes");
                    String dataBytesStr="";
                    if(dataBytes!=null && dataBytes.length>0)
                        dataBytesStr = bytesToHexString(dataBytes);
                    String timestamp = intent.getStringExtra("timestamp");
                    String text = String.format(
                                    "Data:%s\n" +
                                    "Charset:%s\n" +
                                    "Bytes:%s\n" +
                                    "AimId:%s\n" +
                                    "CodeId:%s\n" +
                                    "Timestamp:%s\n",
                            data, charset, dataBytesStr, aimId, codeId, timestamp);
                    setText(text);
                }
            }*/
        }
    };

    private void setMsgText(final String text){
        if (textview!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textview.setText(text);
                }
            });
        }
    }

    private void setText(final String text){
        if (tvResult!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvResult.setText(text);
                }
            });
        }
    }

    private String bytesToHexString(byte[] arr){
        String s = "[]";
        if (arr!=null){
            for (int i=0;i<arr.length;i++){
                s += "0x" + Integer.toHexString(arr[i]) + ",";
            }
            s = s.substring(0, s.length()-1) + "]";
        }
        return s;
    }

    // SOCKET 通讯函数
    public void initSocket(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    client = new Socket(HOST, PORT);
                    bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMsg(final String data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.write(data.getBytes());
                }catch (Exception e){
                    setMsgText(e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void Receive(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while (true){
                        if ((client==null) || (!client.isConnected())){
                            client = new Socket(HOST, PORT);
                        }
                        if ((!client.isOutputShutdown() && !isConnected)){
                            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            outputStream = client.getOutputStream();
                            String data = "BEGIN";
                            outputStream.write(data.getBytes());
                            //printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                            //printWriter.println("BEGIN");
                        }
                        String temp = bufferedReader.readLine();
                        if (temp.equals("START")){
                            isConnected = true;
                            setMsgText("服务端连接成功。");

                        }else if(temp.equals("OK")){
                            isConnected = true;
                            setMsgText("数据发送成功！");

                        } else{
                            isConnected = false;
                            setMsgText("服务端已关闭，无法上传数据!");

                        }
                    }
                } catch (IOException e) {
                    isConnected = false;
                    setMsgText("服务端没有开启，请开启服务端程序。");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        try{
            outputStream.close();
            client.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    public Handler mhandler = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            textview.setText(content);
        }
    };
}
