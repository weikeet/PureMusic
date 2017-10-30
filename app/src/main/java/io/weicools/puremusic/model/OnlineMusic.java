package io.weicools.puremusic.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created on 2017/10/28 by weicools.
 */

public class OnlineMusic {
    @SerializedName("pic_big")
    private String pic_big;
    @SerializedName("pic_small")
    private String pic_small;
    @SerializedName("lrc_link")
    private String lrc_link;
    @SerializedName("song_id")
    private String song_id;
    @SerializedName("title")
    private String title;
    @SerializedName("title_uid")
    private String title_uid;
    @SerializedName("album_title")
    private String album_title;
    @SerializedName("artist_name")
    private String artist_name;

    public String getPic_big() {
        return pic_big;
    }

    public String getPic_small() {
        return pic_small;
    }

    public String getLrc_link() {
        return lrc_link;
    }

    public String getSong_id() {
        return song_id;
    }

    public String getTitle() {
        return title;
    }

    public String getTitle_uid() {
        return title_uid;
    }

    public String getAlbum_title() {
        return album_title;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setPic_big(String pic_big) {
        this.pic_big = pic_big;
    }

    public void setPic_small(String pic_small) {
        this.pic_small = pic_small;
    }

    public void setLrc_link(String lrc_link) {
        this.lrc_link = lrc_link;
    }

    public void setSong_id(String song_id) {
        this.song_id = song_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle_uid(String title_uid) {
        this.title_uid = title_uid;
    }

    public void setAlbum_title(String album_title) {
        this.album_title = album_title;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }
}
