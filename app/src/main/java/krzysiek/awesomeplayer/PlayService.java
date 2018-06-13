package krzysiek.awesomeplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class PlayService extends Service {
    static PlayService instance;
    static MediaPlayer player;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static PlayService getInstance(){
        return instance;
    }

    public static MediaPlayer getPlayer(){
        return player;
    }

    public static void setPlayer(MediaPlayer player){
        PlayService.player = player;
    }
}
