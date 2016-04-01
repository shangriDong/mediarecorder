package com.vv.shangri.mediarecorder;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by shnagri on 2016/3/31.
 */
public class MyMediaRecorder {
    private final static String TAG = "MyMediaRecorder";
    private boolean isRecord = false;

    private MediaRecorder mMediaRecorder = null;

    private MyMediaRecorder() {

    }

    private static MyMediaRecorder mInstance;

    public synchronized static MyMediaRecorder getInstance() {
        if (mInstance == null)
            mInstance = new MyMediaRecorder();
        return mInstance;
    }

    public int startRecordAndFile() {
        if (isRecord) {
            return ErrorCode.E_STATE_RECODING;
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
                return ErrorCode.SUCCESS;
            } catch (IOException ex) {
                ex.printStackTrace();
                return ErrorCode.E_UNKOWN;
            }
        }
    }

    public void stopRecordAndFile() {
        close();
    }

    public long getRecordFileSize() {
        return AudioFileFunc.getFileSize(AudioFileFunc.getAMRFilePath());
    }

    private void createMediaRecord() {
        /* ①Initial：实例化MediaRecorder对象 */
        if (null == mMediaRecorder) {
            mMediaRecorder = new MediaRecorder();
        }

        try {
            /* setAudioSource/setVedioSource*/
            mMediaRecorder.setAudioSource(AudioFileFunc.AUDIO_INPUT);//设置麦克风
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
        File file = new File(AudioFileFunc.getAMRFilePath());
        if (file.exists()) {
            file.delete();
            }
        mMediaRecorder.setOutputFile(AudioFileFunc.getAMRFilePath());
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
}