package com.vv.shangri.test;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.vv.shangri.mediarecorder.IMMediaRecorder;
import com.vv.shangri.mediarecorder.MyPlayer;
import com.vv.shangri.mediarecorder.OnMicStatusListener;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "shangri";
    private TextView text = null;
    private final static int FLAG_WAV = 0;
    private final static int FLAG_AMR = 1;
    private int mState = -1;    //-1:没再录制，0：录制wav，1：录制amr
    //private Button btn_record_wav;
    private Button btn_record_amr;
    private Button btn_stop;
    private Button btn_player;
    private TextView txt;
    private UIHandler uiHandler;
    private UIThread uiThread;
    private String mPath = "";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //text = (TextView) findViewById(R.id.text);

        //text.setText("");
        init();
        findViewByIds();
        setListeners();
        initDate();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void findViewByIds() {
        //btn_record_wav = (Button)this.findViewById(R.id.btn_record_wav);
        btn_record_amr = (Button) this.findViewById(R.id.btn_record_amr);
        btn_stop = (Button) this.findViewById(R.id.btn_stop);
        txt = (TextView) this.findViewById(R.id.text);
        btn_player = (Button) this.findViewById(R.id.btn_play);
    }

    private void initDate() {
        IMMediaRecorder.getInstance().setMicStgatusListener(new OnMicStatusListener() {
            @Override
            public void onMicStatus(int db) {
                Log.i("shangri", "db: " + db);
            }
        });
    }

    private void setListeners() {
        //btn_record_wav.setOnClickListener(btn_record_wav_clickListener);
        btn_record_amr.setOnClickListener(btn_record_amr_clickListener);
        btn_stop.setOnClickListener(btn_stop_clickListener);
        btn_player.setOnClickListener(btn_player_clickListener);
    }

    private void init() {
        uiHandler = new UIHandler();
    }

    private Button.OnClickListener btn_player_clickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            player();
        }
    };
    private Button.OnClickListener btn_record_amr_clickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            record(FLAG_AMR);
        }
    };
    private Button.OnClickListener btn_stop_clickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            stop();
        }
    };

    /**
     *
     */
    private void player() {
        Log.i(TAG, "player");
        if (null == mPath) {
            Log.e(TAG, "mPath = null!!!");
            return;
        }

        MyPlayer play = new MyPlayer(mPath);

        play.start();


    }

    /**
     * 开始录音
     *
     * @param mFlag，0：录制wav格式，1：录音amr格式
     */
    private void record(int mFlag) {
        if (mState != -1) {
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd", CMD_RECORDFAIL);
            b.putInt("msg", IMMediaRecorder.getInstance().E_STATE_RECODING);
            msg.setData(b);
            Log.i(TAG, "mState != -1");
            uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            return;
        }
        int mResult = -1;
        switch (mFlag) {
            case FLAG_WAV:
                //AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                //mResult = mRecord_1.startRecordAndFile();
                break;
            case FLAG_AMR:
                IMMediaRecorder mRecord_2 = IMMediaRecorder.getInstance();
                mResult = mRecord_2.startRecord();

                break;
        }
        if (mResult == IMMediaRecorder.getInstance().SUCCESS) {
            uiThread = new UIThread();
            new Thread(uiThread).start();
            mState = mFlag;
        } else {
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd", CMD_RECORDFAIL);
            b.putInt("msg", mResult);
            msg.setData(b);

            uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
        }
    }

    /**
     * 停止录音
     */
    private void stop() {
        if (mState != -1) {
            switch (mState) {
                case FLAG_WAV:
                    //AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                    //mRecord_1.stopRecordAndFile();
                    break;
                case FLAG_AMR:
                    IMMediaRecorder mRecord_2 = IMMediaRecorder.getInstance();
                    mRecord_2.stopRecord();
                    break;
            }
            if (uiThread != null) {
                uiThread.stopThread();
            }
            if (uiHandler != null)
                uiHandler.removeCallbacks(uiThread);
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd", CMD_STOP);
            b.putInt("msg", mState);
            msg.setData(b);
            uiHandler.sendMessageDelayed(msg, 1000); // 向Handler发送消息,更新UI
            mState = -1;
        }
    }

    private final static int CMD_RECORDING_TIME = 2000;
    private final static int CMD_RECORDFAIL = 2001;
    private final static int CMD_STOP = 2002;

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.vv.shangri.test/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.vv.shangri.test/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    class UIHandler extends Handler {
        public UIHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.d(TAG, "handleMessage......");
            super.handleMessage(msg);
            Bundle b = msg.getData();
            int vCmd = b.getInt("cmd");
            switch (vCmd) {
                case CMD_RECORDING_TIME:
                    int vTime = b.getInt("msg");
                    MainActivity.this.txt.setText("正在录音中，已录制：" + vTime + " s" + "音量大小：" + b.getInt("db"));
                    break;
                case CMD_RECORDFAIL:
                    int vErrorCode = b.getInt("msg");
                    String vMsg = IMMediaRecorder.getInstance().getErrorInfo(MainActivity.this, vErrorCode);
                    MainActivity.this.txt.setText("录音失败：" + vMsg);
                    break;
                case CMD_STOP:
                    int vFileType = b.getInt("msg");
                    switch (vFileType) {
                        case FLAG_WAV:
                            //AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                            //long mSize = mRecord_1.getRecordFileSize();
                            //MainActivity.this.txt.setText("录音已停止.录音文件:" + AudioFileFunc.getWavFilePath() + "\n文件大小：" + mSize);
                            break;
                        case FLAG_AMR:
                            IMMediaRecorder mRecord_2 = IMMediaRecorder.getInstance();
                            long mSize = mRecord_2.getRecordFileSize(mRecord_2.getAMRFilePath());
                            MainActivity.this.txt.setText("录音已停止.录音文件:"
                                    + mRecord_2.getAMRFilePath()
                                    + "\n文件大小：" + mSize);
                            mPath = mRecord_2.getAMRFilePath();
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    class UIThread implements Runnable {
        int mTimeMill = 0;
        boolean vRun = true;

        public void stopThread() {
            vRun = false;
        }

        public void run() {
            while (vRun) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mTimeMill++;
                Log.d(TAG, "mThread........" + mTimeMill);
                Message msg = new Message();
                Bundle b = new Bundle();// 存放数据
                IMMediaRecorder mRecord_2 = IMMediaRecorder.getInstance();

                b.putInt("cmd", CMD_RECORDING_TIME);
                b.putInt("msg", mTimeMill);
                b.putInt("db", 1);
                msg.setData(b);
                if (null == msg) {
                    Log.e(TAG, "msg = null!!");
                }
                MainActivity.this.uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            }

        }
    }


}
