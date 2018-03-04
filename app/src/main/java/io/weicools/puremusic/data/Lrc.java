package io.weicools.puremusic.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created on 2017/10/28 by weicools.
 */

public class Lrc {
    @SerializedName("lrcContent")
    private String lrcContent;

    public String getLrcContent() {
        return lrcContent;
    }

    public void setLrcContent(String lrcContent) {
        this.lrcContent = lrcContent;
    }
}
