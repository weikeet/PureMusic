package io.weicools.puremusic.model;

import java.io.Serializable;

/**
 * Created on 2017/10/28 by weicools.
 */

public class Music implements Serializable {
    private long id;            // [本地歌曲]歌曲id
    private Type type;          // 歌曲类型:本地/网络
    private String title;       // 音乐标题
    private String artist;      // 艺术家
    private String album;       // 专辑
    private long albumId;       // [本地歌曲]专辑ID
    private String coverPath;   // [在线歌曲]专辑封面路径
    private long duration;      // 持续时间
    private String path;        // 音乐路径
    private String fileName;    // 文件名
    private long fileSize;      // 文件大小

    public enum Type {
        LOCAL,
        ONLINE
    }

    /**
     * 对比本地歌曲是否相同
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Music && this.getId() == ((Music) o).getId();
    }

    public long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public long getDuration() {
        return duration;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
