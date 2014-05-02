package de.lukeslog.alarmclock.support;

import java.util.HashMap;

/**
 * Created by lukas on 29.04.14.
 */
public class Radiostations
{
    public static HashMap<String, String> stations = new HashMap<String, String>();

    public static void setUp()
    {
        stations.put("DLF", "http://stream.dradio.de/7/249/142684/v1/gnl.akacast.akamaistream.net/dradio_mp3_dlf_m");
        stations.put("Club Sounds", "http://revolutionradio.ru/live.ogg");
        stations.put("Top 100", "");
        stations.put("Indie Rock", "http://sc2.3wk.com/3wk-u-ogg-lo");
        stations.put("Energy Pop", "http://95.81.146.25/1U1_J5AEJdeFBnW9FowU92ucrMeXULh7A0lo=/8823/nrj_162895.mp3");
        stations.put("Energy Hip Hop", "http://95.81.146.24/1U1_KBwEJYqLA9d6XjuMV1ucZUO3gFwzZGTM=/8478/nrj_178159.mp3");
        stations.put("80s", "http://81.95.4.70:8081/best_of_80s");
        stations.put("Radio GFM RockPop", "http://radio-gfm.net:8000/rockpop.ogg");
    }
}
