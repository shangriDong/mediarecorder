package com.vv.shangri.mediarecorder;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.vv.shangri.test.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by shnagri on 2016/3/31.
 */
public class IMMediaRecorder {
    private final static String TAG = "IMMediaRecorder";
    private final static int UPDATE_MIC_STATUS = 1000;
    //音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    private final static int AUDIO_DEFAULT = MediaRecorder.AudioSource.DEFAULT;

    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private final static int AUDIO_SAMPLE_RATE = 16000; //44.1KHz,普遍使用的频率

    public final static int SUCCESS = 0;
    public final static int E_STATE_RECODING = 1;
    public final static int E_UNKOWN = 2;

    private static IMMediaRecorder mInstance;

    private boolean isRecord = false;
    private MediaRecorder mMediaRecorder = null;
    //录音输出文件
    private String mAudioAmrFileName = "WBFinalAudio.amr";

    private MicStatusThread mThread;
    private MicStatusHandler mHandler;
    private int mSampleTime = 200;
    private final static int MAXDB = 35;
    private final static int MAXLEAVEL = 10;
    private final static int LEAVELBASE = MAXDB / MAXLEAVEL;

    private OnMicStatusListener mMicStgatusListener;

    private IMMediaRecorder() {
        mHandler = new MicStatusHandler();
    }

    public void setMicStgatusListener(OnMicStatusListener l) {
        if (l != null) {
            mMicStgatusListener = l;
        }
    }

    public void setSampleTime(int sampleTime) {
        mSampleTime = sampleTime;
    }

    public void setAudioAmrFileName(String vAudioAmrFileName) {
        mAudioAmrFileName = vAudioAmrFileName;
    }

    public String getAudioAmrFileName() {
        return mAudioAmrFileName;
    }

    public synchronized static IMMediaRecorder getInstance() {
        if (null == mInstance)
            mInstance = new IMMediaRecorder();
        return mInstance;
    }

    public static String getErrorInfo(Context vContext, int vType) throws Resources.NotFoundException {
        switch (vType) {
            case SUCCESS:
                return vContext.getResources().getString(R.string.success);
            case E_STATE_RECODING:
                return vContext.getResources().getString(R.string.error_state_record);
            case E_UNKOWN:
            default:
                return vContext.getResources().getString(R.string.error_unknown);
        }
    }

    public int startRecord() {
        if (isRecord) {
            return E_STATE_RECODING;
        } else {
            if (null == mMediaRecorder) {
                createMediaRecord();
            }
            try {

                mMediaRecorder.prepare();
                mMediaRecorder.start();
                startUpdateMicStatus();
                // 让录制状态为true
                Log.i(TAG, "开始录制！！");
                isRecord = true;
                return SUCCESS;
            } catch (IOException ex) {
                ex.printStackTrace();
                return E_UNKOWN;
            }
        }
    }

    public void stopRecord() {
        if (mMediaRecorder != null) {
            Log.d(TAG, "stopRecord");
            isRecord = false;
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (mThread != null) {
            mThread.stopThread();
        }
    }

    public void cancelRecord() {
        stopRecord();
        File file = new File(getAMRFilePath());
        if (file.exists()) {
            file.delete();
        }

    }

    public long getRecordFileSize(String path) {
        File mFile = new File(path);
        if (!mFile.exists()) {
            return -1;
        }
        return mFile.length();
    }

    private void createMediaRecord() {
        if (null == mMediaRecorder) {
            mMediaRecorder = new MediaRecorder();
        }

        try {
            /* setAudioSource/setVedioSource*/
            mMediaRecorder.setAudioSource(AUDIO_INPUT);
        } catch (IllegalStateException e) {
            Log.e(TAG, e.toString());
        }

        try {
            /* 设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default
            * THREE_GPP(3gp格式，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
            */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        } catch (IllegalStateException e) {
            Log.e(TAG, e.toString());
        }

        try {
            /* 设置音频文件的编码：AAC/AMR_NB/AMR_WB/Default */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        } catch (IllegalStateException e) {
            Log.e(TAG, e.toString());
        }

        /* 设置输出文件的路径 */
        File file = new File(getAMRFilePath());
        if (file.exists()) {
            file.delete();
        }
        mMediaRecorder.setOutputFile(getAMRFilePath());
    }

    /**
     * 判断是否有外部存储设备sdcard
     *
     * @return true | false
     */
    private static boolean isSdcardExit() {
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    /**
     * 获取编码后的AMR格式音频文件路径
     *
     * @return
     */
    public String getAMRFilePath() {
        String audioAMRPath = "";
        if (isSdcardExit()) {
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            audioAMRPath = fileBasePath + "/" + mAudioAmrFileName;
        }
        return audioAMRPath;
    }

    public int getMicStatus() {
        int BASE = 600;

        if (mMediaRecorder != null) {
            int ratio = mMediaRecorder.getMaxAmplitude() / BASE;
            int db = 0;// 分贝
            if (ratio > 1) {
                db = (int) (20 * Math.log10(ratio));
                System.out.println("分贝值：" + db + "     " + Math.log10(ratio));
                return db / LEAVELBASE + 1;
            }
            else {
                System.out.println("分贝值：" + db + "     " + Math.log10(ratio));
                return db;
            }

        }
        return -1;
    }

    public void startUpdateMicStatus() {
        if (mMediaRecorder != null) {
            mThread = new MicStatusThread();
            new Thread(mThread).start();
        }
    }

    class MicStatusThread implements Runnable {
        boolean vRun = true;

        public void stopThread() {
            vRun = false;
        }

        public void run() {
            while (vRun) {
                try {
                    Thread.sleep(mSampleTime);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Message msg = new Message();
                Bundle b = new Bundle();// 存放数据
                b.putInt("cmd", UPDATE_MIC_STATUS);
                b.putInt("db", getMicStatus());
                msg.setData(b);
                if (null == msg) {
                    Log.e(TAG, "msg = null!!");
                }
                mHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            }
        }
    }

    class MicStatusHandler extends Handler {
        public MicStatusHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            Bundle b = msg.getData();
            int vCmd = b.getInt("cmd");
            switch (vCmd) {
                case UPDATE_MIC_STATUS:
                    int db = b.getInt("db");
                    //MainActivity.this.txt.setText("正在录音中，已录制：" + vTime + " s" + "音量大小：" + b.getInt("db"));
                    mMicStgatusListener.onMicStatus(db);
                    break;
                default:
                    break;
            }
        }
    }

}