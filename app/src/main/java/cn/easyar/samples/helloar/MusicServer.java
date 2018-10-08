package cn.easyar.samples.helloar;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by HPA on 2017/7/18.
 */

public class MusicServer extends Service {
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.pikaqiu);
            //mediaPlayer.setLooping(true);//设置为循环播放
            mediaPlayer.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }
}
