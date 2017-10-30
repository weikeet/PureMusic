package io.weicools.puremusic.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created on 2017/10/28 by weicools.
 */

public class ArtistInfo {
    @SerializedName("constellation")
    private String constellation;   // 星座
    @SerializedName("weight")
    private float weight;           // 体重
    @SerializedName("stature")
    private float stature;          // 身高
    @SerializedName("country")
    private String country;         // 国籍
    @SerializedName("url")
    private String url;             // 歌手链接
    @SerializedName("intro")
    private String intro;           // 歌手简介
    @SerializedName("avatar_s1000")
    private String avatar_s1000;    // 头像
    @SerializedName("name")
    private String name;            // 姓名
    @SerializedName("birth")
    private String birth;           // 生日

    public String getConstellation() {
        return constellation;
    }

    public float getWeight() {
        return weight;
    }

    public float getStature() {
        return stature;
    }

    public String getCountry() {
        return country;
    }

    public String getUrl() {
        return url;
    }

    public String getIntro() {
        return intro;
    }

    public String getAvatar_s1000() {
        return avatar_s1000;
    }

    public String getName() {
        return name;
    }

    public String getBirth() {
        return birth;
    }

    public void setConstellation(String constellation) {
        this.constellation = constellation;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setStature(float stature) {
        this.stature = stature;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public void setAvatar_s1000(String avatar_s1000) {
        this.avatar_s1000 = avatar_s1000;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }
}
