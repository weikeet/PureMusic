package io.weicools.puremusic.module.musicinfo;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import io.weicools.puremusic.R;
import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.data.ArtistInfo;
import io.weicools.puremusic.enums.LoadStateEnum;
import io.weicools.puremusic.module.base.BaseActivity;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.ViewUtil;

public class ArtistInfoActivity extends BaseActivity {
    private ScrollView svArtistInfo;
    private LinearLayout llArtistInfoContainer;
    private LinearLayout llLoading;
    private LinearLayout llLoadFail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_info);

        svArtistInfo = findViewById(R.id.sv_artist_info);
        llArtistInfoContainer = findViewById(R.id.ll_artist_info_container);
        llLoading = findViewById(R.id.ll_loading);
        llLoadFail = findViewById(R.id.ll_load_fail);

        String tingUid = getIntent().getStringExtra(ConstantUtil.TING_UID);
        getArtistInfo(tingUid);
        ViewUtil.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOADING);
    }

    private void getArtistInfo(String tingUid) {
        HttpClient.getArtistInfo(tingUid, new HttpCallback<ArtistInfo>() {
            @Override
            public void onSuccess(ArtistInfo artistInfo) {
                if (artistInfo == null) {
                    onFail(null);
                    return;
                }
                ViewUtil.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                setData(artistInfo);
            }

            @Override
            public void onFail(Exception e) {
                ViewUtil.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            }
        });
    }

    private void setData(ArtistInfo artistInfo) {
        String name = artistInfo.getName();
        String avatarUri = artistInfo.getAvatar_s1000();
        String country = artistInfo.getCountry();
        String constellation = artistInfo.getConstellation();

        float stature = artistInfo.getStature();
        float weight = artistInfo.getWeight();

        String birth = artistInfo.getBirth();
        String intro = artistInfo.getIntro();
        String url = artistInfo.getUrl();

        if (!TextUtils.isEmpty(avatarUri)) {
            ImageView ivAvatar = new ImageView(this);
            ivAvatar.setScaleType(ImageView.ScaleType.FIT_START);
            Glide.with(this)
                    .load(avatarUri)
                    .placeholder(R.drawable.default_artist)
                    .error(R.drawable.default_artist)
                    .into(ivAvatar);
            llArtistInfoContainer.addView(ivAvatar);
        }

        if (!TextUtils.isEmpty(name)) {
            setTitle(name);
            TextView tvName = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvName.setText(getString(R.string.artist_info_name, name));
            llArtistInfoContainer.addView(tvName);
        }

        if (!TextUtils.isEmpty(country)) {
            TextView tvCountry = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvCountry.setText(getString(R.string.artist_info_country, country));
            llArtistInfoContainer.addView(tvCountry);
        }

        if (!TextUtils.isEmpty(constellation) && !TextUtils.equals(constellation, "未知")) {
            TextView tvConstellation = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvConstellation.setText(getString(R.string.artist_info_constellation, constellation));
            llArtistInfoContainer.addView(tvConstellation);
        }

        if (stature != 0f) {
            TextView tvStature = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvStature.setText(getString(R.string.artist_info_stature, String.valueOf(stature)));
            llArtistInfoContainer.addView(tvStature);
        }

        if (weight != 0f) {
            TextView tvWeight = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvWeight.setText(getString(R.string.artist_info_weight, String.valueOf(weight)));
            llArtistInfoContainer.addView(tvWeight);
        }

        if (!TextUtils.isEmpty(birth) && !TextUtils.equals(birth, "0000-00-00")) {
            TextView tvBirth = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvBirth.setText(getString(R.string.artist_info_birth, birth));
            llArtistInfoContainer.addView(tvBirth);
        }

        if (!TextUtils.isEmpty(intro)) {
            TextView tvIntro = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvIntro.setText(getString(R.string.artist_info_intro, intro));
            llArtistInfoContainer.addView(tvIntro);
        }

        if (!TextUtils.isEmpty(url)) {
            TextView tvUrl = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvUrl.setLinkTextColor(ContextCompat.getColor(this, R.color.blue));
            tvUrl.setMovementMethod(LinkMovementMethod.getInstance());
            SpannableString spannableString = new SpannableString("查看更多信息");
            spannableString.setSpan(new URLSpan(url), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvUrl.setText(spannableString);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            tvUrl.setLayoutParams(layoutParams);
            llArtistInfoContainer.addView(tvUrl);
        }

        if (llArtistInfoContainer.getChildCount() == 0) {
            ViewUtil.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            ((TextView) llLoadFail.findViewById(R.id.tv_load_fail_text)).setText(R.string.artist_info_empty);
        }
    }
}
