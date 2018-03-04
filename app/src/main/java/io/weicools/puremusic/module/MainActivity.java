package io.weicools.puremusic.module;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import io.weicools.puremusic.R;
import io.weicools.puremusic.executor.ControlPanel;
import io.weicools.puremusic.service.AudioPlayer;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.service.QuitTimer;
import io.weicools.puremusic.module.search.SearchActivity;
import io.weicools.puremusic.module.base.BaseActivity;
import io.weicools.puremusic.module.local.LocalMusicFragment;
import io.weicools.puremusic.module.playing.PlayFragment;
import io.weicools.puremusic.module.online.SongSheetFragment;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.Preferences;
import io.weicools.puremusic.util.SystemUtil;
import io.weicools.puremusic.util.ToastUtil;

public class MainActivity extends BaseActivity implements View.OnClickListener, QuitTimer.OnTimerListener,
        NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private TextView mTvLocalMusic;
    private TextView mTvOnlineMusic;
    private ViewPager mViewPager;
    private FrameLayout mBottomPlayBar;

    private LocalMusicFragment mLocalMusicFragment;
    private SongSheetFragment mSongSheetFragment;
    private PlayFragment mPlayFragment;
    private ControlPanel mControlPanel;

    private boolean mIsPlayFragmentShow = false;
    private MenuItem timerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        mDrawerLayout = findViewById(R.id.draw_layout);
        mNavigationView = findViewById(R.id.nav_view);

        ImageView ivMenu = findViewById(R.id.iv_menu);
        ImageView ivSearch = findViewById(R.id.iv_search);
        mViewPager = findViewById(R.id.view_pager);
        mTvLocalMusic = findViewById(R.id.tv_local_music);
        mTvOnlineMusic = findViewById(R.id.tv_online_music);
        mBottomPlayBar = findViewById(R.id.bottom_play_bar);

        ivMenu.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        mTvLocalMusic.setOnClickListener(this);
        mTvOnlineMusic.setOnClickListener(this);
        mViewPager.addOnPageChangeListener(this);
        mBottomPlayBar.setOnClickListener(this);
        mNavigationView.setNavigationItemSelectedListener(this);

        // add navigation header
        View navigationHeader = LayoutInflater.from(this).inflate(R.layout.navigation_header, mNavigationView, false);
        mNavigationView.addHeaderView(navigationHeader);

        mLocalMusicFragment = new LocalMusicFragment();
        mSongSheetFragment = new SongSheetFragment();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        fragmentAdapter.addFragment(mLocalMusicFragment);
        fragmentAdapter.addFragment(mSongSheetFragment);
        mViewPager.setAdapter(fragmentAdapter);
        mTvLocalMusic.setSelected(true);
    }

    @Override
    protected void onServiceBound() {
        mControlPanel = new ControlPanel(mBottomPlayBar);
        AudioPlayer.getInstance().addOnPlayEventListener(mControlPanel);
        QuitTimer.getInstance().setOnTimerListener(this);
        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(ConstantUtil.EXTRA_NOTIFICATION)) {
            showPlayingFragment();
            setIntent(new Intent());
        }
    }

    private void showPlayingFragment() {
        if (mIsPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();
        mIsPlayFragmentShow = true;
    }

    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commitAllowingStateLoss();
        mIsPlayFragmentShow = false;
    }

    @Override
    public void onTimer(long remain) {
        if (timerItem == null) {
            timerItem = mNavigationView.getMenu().findItem(R.id.action_timer);
        }
        String title = getString(R.string.menu_timer);
        timerItem.setTitle(remain == 0 ? title : SystemUtil.formatTime(title + "(mm:ss)", remain));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            mTvLocalMusic.setSelected(true);
            mTvOnlineMusic.setSelected(false);
        } else {
            mTvLocalMusic.setSelected(false);
            mTvOnlineMusic.setSelected(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_menu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.iv_search:
                startActivity(new Intent(this, SearchActivity.class));
                break;
            case R.id.tv_local_music:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.tv_online_music:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.bottom_play_bar:
                showPlayingFragment();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawers();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);
            }
        }, 500);

        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_about:
                // TODO: 2018/3/4 start about
                //startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_night:
                Preferences.saveNightMode(!Preferences.isNightMode());
                this.recreate();
                break;
            case R.id.action_timer:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_timer)
                        .setItems(this.getResources().getStringArray(R.array.timer_text), (dialog, which) -> {
                            int[] times = this.getResources().getIntArray(R.array.timer_int);
                            QuitTimer.getInstance().start(times[which] * 60 * 1000);
                            if (times[which] > 0) {
                                ToastUtil.showShort(this.getString(R.string.timer_set, String.valueOf(times[which])));
                            } else {
                                ToastUtil.showShort(R.string.timer_cancel);
                            }
                        })
                        .show();
                return true;
            case R.id.action_exit:
                finish();
                MusicService.startCommand(this, ConstantUtil.ACTION_STOP);
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && mIsPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ConstantUtil.VIEW_PAGER_INDEX, mViewPager.getCurrentItem());
        mLocalMusicFragment.onSaveInstanceState(outState);
        mSongSheetFragment.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(savedInstanceState.getInt(ConstantUtil.VIEW_PAGER_INDEX), false);
                mLocalMusicFragment.onRestoreInstanceState(savedInstanceState);
                mSongSheetFragment.onRestoreInstanceState(savedInstanceState);
            }
        });
    }

    @Override
    protected void onDestroy() {
        AudioPlayer.getInstance().removeOnPlayEventListener(mControlPanel);
        QuitTimer.getInstance().setOnTimerListener(null);
        super.onDestroy();
    }
}
