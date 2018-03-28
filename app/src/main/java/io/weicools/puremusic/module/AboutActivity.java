package io.weicools.puremusic.module;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.weicools.puremusic.R;
import io.weicools.puremusic.util.AppUtil;
import io.weicools.puremusic.util.ConstantUtil;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.toolbar_about)
    Toolbar mToolbarAbout;
    @BindView(R.id.img_card_about_1)
    ImageView mImgCardAbout1;
    @BindView(R.id.tv_card_about_1_1)
    TextView mTvCardAbout11;
    @BindView(R.id.tv_about_version)
    TextView mTvAboutVersion;
    @BindView(R.id.tv_card_about_2_1)
    TextView mTvCardAbout21;
    @BindView(R.id.view_card_about_2_line)
    View mViewCardAbout2Line;
    @BindView(R.id.ll_card_about_2_shop)
    LinearLayout mLlCardAbout2Shop;
    @BindView(R.id.ll_card_about_2_email)
    LinearLayout mLlCardAbout2Email;
    @BindView(R.id.ll_card_about_2_git_hub)
    LinearLayout mLlCardAbout2GitHub;
    @BindView(R.id.ll_card_about_2_location)
    LinearLayout mLlCardAbout2Location;
    @BindView(R.id.card_about_2)
    CardView mCardAbout2;
    @BindView(R.id.ll_card_about_source_licenses)
    LinearLayout mLlCardAboutSourceLicenses;
    @BindView(R.id.card_about_source_licenses)
    CardView mCardAboutSourceLicenses;
    @BindView(R.id.scroll_about)
    ScrollView mScrollAbout;
    @BindView(R.id.fab_about_share)
    FloatingActionButton mFabAboutShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbarAbout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimary));

        initView();
    }

    private void initView() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_about_card_show);
        mScrollAbout.startAnimation(animation);

        mLlCardAbout2Shop.setOnClickListener(this);
        mLlCardAbout2Email.setOnClickListener(this);
        mLlCardAbout2GitHub.setOnClickListener(this);
        mLlCardAboutSourceLicenses.setOnClickListener(this);

        mFabAboutShare.setOnClickListener(this);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setStartOffset(600);

        TextView tv_about_version = findViewById(R.id.tv_about_version);
        tv_about_version.setText(AppUtil.getVersionName(this));
        tv_about_version.startAnimation(alphaAnimation);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();

        switch (view.getId()) {
            case R.id.ll_card_about_2_shop:
                intent.setData(Uri.parse(ConstantUtil.APP_URL));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                break;

            case R.id.ll_card_about_2_email:
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(ConstantUtil.EMAIL));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_email_intent));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(AboutActivity.this, getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.ll_card_about_source_licenses:
                final Dialog dialog = new Dialog(this, R.style.DialogFullscreenWithTitle);
                dialog.setTitle(getString(R.string.about_source_licenses));
                dialog.setContentView(R.layout.dialog_source_licenses);

                final WebView webView = dialog.findViewById(R.id.web_source_licenses);
                webView.loadUrl("file:///android_asset/source_licenses.html");

                Button btnSourceLicensesClose = dialog.findViewById(R.id.btn_source_licenses_close);
                btnSourceLicensesClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;

            case R.id.ll_card_about_2_git_hub:
                intent.setData(Uri.parse(ConstantUtil.GIT_HUB));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                break;

            case R.id.fab_about_share:
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, ConstantUtil.SHARE_CONTENT);
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getString(R.string.share_with)));
                break;
            default:
                break;
        }
    }
}
