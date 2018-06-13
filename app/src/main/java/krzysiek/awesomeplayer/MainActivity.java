package krzysiek.awesomeplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static krzysiek.awesomeplayer.PlayService.player;

public class MainActivity extends AppCompatActivity implements SongFragment.OnListFragmentInteractionListener , AccelerometerListener{

    private static View layout;

    private static Button playButton;
    private static Button backwardButton;
    private static Button forwardButton;
    private static SeekBar progressBar;

    private static TextView titleView;
    private static TextView timeView;
    private static TextView durationView;
    private static Song currentSong;

    private static boolean playNext;
    private static boolean shakeToRandom;
    private static boolean sensorNightNode;

    private static LightSensorManager lightManager = new LightSensorManager();

    private static final Song DEFAULT_SONG = Song.SONGS.get(0);

    private boolean isInit = false;

    private void init(){

        playButton = findViewById(R.id.play_btn);
        backwardButton = findViewById(R.id.bck_btn);
        forwardButton = findViewById(R.id.fwd_btn);
        progressBar = findViewById(R.id.progress_bar);

        titleView = findViewById(R.id.title);
        durationView = findViewById(R.id.song_duration);
        timeView = findViewById(R.id.song_time);

        layout = findViewById(R.id.layout);

        isInit = true;


    }

    private void refreshSharedPref(){
        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("Player", 0);
        playNext = sharedPref.getBoolean(getString(R.string.play_next_setting), false);
        shakeToRandom = sharedPref.getBoolean(getString(R.string.shake_switch_setting), true);
        sensorNightNode = sharedPref.getBoolean(getString(R.string.night_switch_setting), true);
    }

    private void setOnCompletionListener(){
        PlayService.getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stop();
                mp.seekTo(0);
                progressBar.setProgress(0);
                if(playNext) playAnotherSong();
                mp.setOnCompletionListener(this);
            }
        });
    }

    private void setListeners(){

        playButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PlayService.getPlayer().isPlaying()){
                    stop();
                }else{
                    start();
                }
            }
        });


        backwardButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayService.getPlayer().seekTo(PlayService.getPlayer().getCurrentPosition() - 10000);
                refreshProgressbar();
            }
        });

        forwardButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayService.getPlayer().seekTo(PlayService.getPlayer().getCurrentPosition() + 10000);
                refreshProgressbar();
                refreshTime();
            }
        });

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    PlayService.getPlayer().seekTo(seekBar.getProgress());
                }
                refreshTime();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        setOnCompletionListener();


        lightManager.init(this, lightSensorListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshSharedPref();
        init();

        if(currentSong == null){
            loadSong(DEFAULT_SONG);
        }else{
            refreshSongInfo();
        }

        if(PlayService.getInstance() == null){
            startService(new Intent(this, PlayService.class));
        }

        setListeners();

        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                refreshProgressbar();
                refreshSharedPref();
            }
        }, 1, 1, TimeUnit.SECONDS);


    }

    private void refreshProgressbar(){
        progressBar.setProgress(PlayService.getPlayer().getCurrentPosition());
    }

    private void refreshTime(){
        int maxSeconds = currentSong.getDuration();
        double progress = (double)progressBar.getProgress() / progressBar.getMax();
        int seconds = (int)Math.round(progress * maxSeconds);

        timeView.setText(Song.getFormatTimeFromSeconds(seconds));
    }

    @Override
    public void onListFragmentInteraction(Song song) {
        loadSong(song);
    }

    public void loadSong(Song song){
        currentSong = song;
        titleView.setText(song.getHeader());
        durationView.setText(Song.getFormatTimeFromSeconds(song.getDuration()));
        stop();
        PlayService.setPlayer(MediaPlayer.create(this, song.getFileId()));
        setOnCompletionListener();
        progressBar.setMax(PlayService.getPlayer().getDuration());
        refreshProgressbar();
        refreshTime();
    }

    public void refreshSongInfo(){
        titleView.setText(currentSong.getHeader());
        durationView.setText(Song.getFormatTimeFromSeconds(currentSong.getDuration()));
        progressBar.setMax(PlayService.getPlayer().getDuration());
        refreshProgressbar();
        refreshTime();
        refreshPlay();
    }

    public void playAnotherSong(){
        // if another song is next in ordered list
        loadSong(getNextSong());
        start();

    }

    public Song getNextSong(){
        int index = Song.SONGS.indexOf(currentSong);
        return Song.SONGS.get((index+1) % Song.SONGS.size());
    }

    public void start(){
        if(PlayService.getPlayer() != null) PlayService.getPlayer().start();
        refreshPlay();
    }

    public void stop(){
        if(player != null && player.isPlaying()) player.pause();
        refreshPlay();
    }

    public void refreshPlay(){
        if(player != null && player.isPlaying()){
            playButton.setText("STOP");
        }else{
            playButton.setText("PLAY");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.author_option){
            Intent intent = new Intent(this,AuthorActivity.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.settings){
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AccelerometerManager.isSupported(this)) {
            AccelerometerManager.startListening(this);
        }
    }

    @Override
    public void onAccelerationChanged(float x, float y, float z) {

    }

    @Override
    public void onShake(float force) {
        if(shakeToRandom) {
            loadSong(getNextSong());
            PlayService.getPlayer().setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    PlayService.getPlayer().start();
                    refreshPlay();
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (AccelerometerManager.isListening()) {

            AccelerometerManager.stopListening();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
        }
    }

    private final SensorEventListener lightSensorListener = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT && sensorNightNode && isInit){
                if(event.values[0] < 50){
                    nightModeOn();
                }else{
                    nightModeOff();
                }
            }
        }

        public void nightModeOn(){
            layout.setBackgroundColor(Color.BLACK);
            titleView.setTextColor(getResources().getColor(R.color.night_text_color));
            timeView.setTextColor(getResources().getColor(R.color.night_text_color));
            durationView.setTextColor(getResources().getColor(R.color.night_text_color));
            backwardButton.getBackground().setColorFilter(getResources().getColor(R.color.night_button_color), PorterDuff.Mode.MULTIPLY);
            playButton.getBackground().setColorFilter(getResources().getColor(R.color.night_button_color), PorterDuff.Mode.MULTIPLY);
            forwardButton.getBackground().setColorFilter(getResources().getColor(R.color.night_button_color), PorterDuff.Mode.MULTIPLY);
        }

        public void nightModeOff(){
            layout.setBackgroundColor(getResources().getColor(R.color.layout_color));
            titleView.setTextColor(getResources().getColor(R.color.day_text_color));
            timeView.setTextColor(getResources().getColor(R.color.day_text_color));
            durationView.setTextColor(getResources().getColor(R.color.day_text_color));
            backwardButton.getBackground().setColorFilter(getResources().getColor(R.color.day_button_color), PorterDuff.Mode.MULTIPLY);
            playButton.getBackground().setColorFilter(getResources().getColor(R.color.day_button_color), PorterDuff.Mode.MULTIPLY);
            forwardButton.getBackground().setColorFilter(getResources().getColor(R.color.day_button_color), PorterDuff.Mode.MULTIPLY);
        }

    };

}
