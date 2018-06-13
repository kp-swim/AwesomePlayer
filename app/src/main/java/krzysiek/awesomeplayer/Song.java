package krzysiek.awesomeplayer;

import java.util.ArrayList;

public class Song {
    public static ArrayList<Song> SONGS = new ArrayList<>();

    static{
        SONGS.add(new Song( "Alligatoah","Willst du", R.raw.alligatoah_willst_du, 217));
        SONGS.add(new Song( "Borys LBD feat. Bado","Jessica", R.raw.borys_lbd_bado_jessica, 285));
        SONGS.add(new Song( "Bearson","Go to sleep", R.raw.bearson_go_to_sleep, 237));
        SONGS.add(new Song( "Vigiland","Shots & Squats", R.raw.vigiland_shots_squats,171));
        SONGS.add(new Song( "Alligatoah","Willst du", R.raw.alligatoah_willst_du, 217));
        SONGS.add(new Song( "Borys LBD feat. Bado","Jessica", R.raw.borys_lbd_bado_jessica, 285));
        SONGS.add(new Song( "Bearson","Go to sleep", R.raw.bearson_go_to_sleep, 237));
        SONGS.add(new Song( "Vigiland","Shots & Squats", R.raw.vigiland_shots_squats,171));
    }

    private int fileId;
    private String band;
    private String title;
    private int duration;

    public Song(String band, String title, int fileId, int duration){
        this.band = band;
        this.title = title;
        this.fileId = fileId;
        this.duration = duration;
    }

    public int getFileId() {
        return fileId;
    }

    public String getBand() {
        return band;
    }

    public String getTitle() {
        return title;
    }

    public String getHeader() { return band + " - " + title; }

    public int getDuration() {
        return duration;
    }

    public static String getFormatTimeFromSeconds(int secondsDuration){
        int minutes = secondsDuration / 60;
        int seconds = secondsDuration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
