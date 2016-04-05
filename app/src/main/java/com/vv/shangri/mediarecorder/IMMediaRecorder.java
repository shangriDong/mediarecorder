package com.vv.shangri.mediarecorder;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.vv.shangri.test.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by shnagri on 2016/3/31.
 */
public class IMMediaRecorder {
    private final static String TAG = "IMMediaRecorder";

    //音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    private final static int AUDIO_DEFAULT = MediaRecorder.AudioSource.DEFAULT;

    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private final static int AUDIO_SAMPLE_RATE = 44100; //44.1KHz,普遍使用的频率

    private final static int SUCCESS = 0;
    private final static int E_STATE_RECODING = 1;
    private final static int E_UNKOWN = 2;

    private static IMMediaRecorder mInstance;

    private boolean isRecord = false;
    private MediaRecorder mMediaRecorder = null;
    //录音输出文件
    private String mAudioAmrFileName = "FinalAudio.amr";

    private IMMediaRecorder() {

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

    private static String getErrorInfo(Context vContext, int vType) throws Resources.NotFoundException {
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

    /**
     * Begin record
     *
     * @return
     */
    public int startRecordAndFile() {
        if (isRecord) {
            return E_STATE_RECODING;
        } else {
            if (null == mMediaRecorder) {
                createMediaRecord();
            }
            try {
                mMediaRecorder.prepare();
                mMediaRecorder.start();
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

    public void stopRecordAndFile() {
        close();
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
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        } catch (IllegalStateException e) {
            Log.e(TAG, e.toString());
        }

        try {
            /* 设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
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

    private void close() {
        if (mMediaRecorder != null) {
            Log.i(TAG, "stopRecord");
            isRecord = false;
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
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
    private String getAMRFilePath() {
        String audioAMRPath = "";
        if (isSdcardExit()) {
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            audioAMRPath = fileBasePath + "/" + mAudioAmrFileName;
        }
        return audioAMRPath;
    }
}