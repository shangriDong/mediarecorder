package com.vv.shangri.mediarecorder;

import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by admin on 2016/3/31.
 */
public class MyPlayer {

    private final String TAG = MyPlayer.class.getName();
    private String path = "";

    private MediaPlayer mPlayer = null;
    public MyPlayer(String path){
        this.path = path;
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(player_completionLis);
    }

    private MediaPlayer.OnCompletionListener player_completionLis = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, "播放完成！");
            stop();
        }

    };

    public boolean start() {
        try {
            //设置要播放的文件
            mPlayer.setDataSource(path);
            mPlayer.prepare();
            //播放
            mPlayer.start();
        }catch(Exception e){
            Log.e(TAG, "prepare() failed");
        }

        return false;
    }

    public boolean stop() {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        return false;
    }
}
