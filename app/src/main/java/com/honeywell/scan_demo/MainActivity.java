package com.honeywell.scan_demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "IntentApiSample";
    private static final String ACTION_BARCODE_DATA = "com.honeywell.sample.action.BARCODE_DATA";
    private static final String ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER";
    private static final String ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER";
    private static final String EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER";
    private static final String EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE";
    private static final String EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES";

    private static final String EXTRA_CONTROL = "com.honeywell.aidc.action.ACTION_CONTROL_SCANNER";
    private static final String EXTRA_SCAN = "com.honeywell.aidc.extra.EXTRA_SCAN";

    private TextView textview;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textview = findViewById(R.id.textview);
        button = findViewById(R.id.button);
        button.setText("Start Scan");
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendBroadcast(new Intent(EXTRA_CONTROL).putExtra(EXTRA_SCAN, true));
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_BARCODE_DATA));
        claimScanner();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        releaseScanner();
    }

    private void claimScanner(){
        Bundle properties = new Bundle();
        properties.putBoolean("DPR_DATA_INTENT", true);
        properties.putString("DPR_DATA_INTENT_ACTION",ACTION_BARCODE_DATA);
        sendBroadcast(new Intent(ACTION_CLAIM_SCANNER)
                    .putExtra(EXTRA_SCANNER, "dcs.scanner.imager")
                    .putExtra(EXTRA_PROFILE,"DEFAULT")
                    .putExtra(EXTRA_PROPERTIES, properties));
    }

    private void releaseScanner(){
        sendBroadcast(new Intent(ACTION_RELEASE_SCANNER));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_BARCODE_DATA.equals(intent.getAction())){
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
            }
        }
    };

    private void setText(final String text){
        if (textview!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textview.setText(text);
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
}
