package io.weicools.puremusic.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.weicools.puremusic.AppCache;
import io.weicools.puremusic.R;
import io.weicools.puremusic.adapter.FragmentAdapter;
import io.weicools.puremusic.executor.NavMenuExecutor;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.service.OnPlayerEventListener;
import io.weicools.puremusic.ui.base.BaseActivity;
import io.weicools.puremusic.ui.fragment.LocalMusicFragment;
import io.weicools.puremusic.ui.fragment.PlayFragment;
import io.weicools.puremusic.ui.fragment.PlayListFragment;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.CoverLoader;
import io.weicools.puremusic.util.SystemUtil;

public class MainActivity extends BaseActivity implements View.OnClickListener, OnPlayerEventListener,
        NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ImageView mIvMenu;
    private ImageView mIvSearch;
    private TextView mTvLocalMusic;
    private TextView mTvOnlineMusic;
    private ViewPager mViewPager;
    private FrameLayout mBottomPlayBar;
    private ImageView mIvPlayBarCover;
    private TextView mTvPlayBarTitle;
    private TextView mTvPlayBarArtist;
    private ImageView mIvPlayBarPlay;
    private ImageView mIvPlayBarNext;
    private ProgressBar mProgressBar;

    private LocalMusicFragment mLocalMusicFragment;
    private PlayListFragment mPlayListFragment;
    private PlayFragment mPlayFragment;
    private boolean mIsPlayFragmentShow = false;
    private MenuItem timerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkServiceAlive()) {
            return;
        }

        getMusicService().setOnPlayEventListener(this);

        initView();
        initListener();
        onChangeImpl(getMusicService().getPlayingMusic());
        parseIntent();
    }

    private void initView() {
        mDrawerLayout = findViewById(R.id.draw_layout);
        mNavigationView = findViewById(R.id.nav_view);

        mIvMenu = findViewById(R.id.iv_menu);
        mIvSearch = findViewById(R.id.iv_search);
        mTvLocalMusic = findViewById(R.id.tv_local_music);
        mTvOnlineMusic = findViewById(R.id.tv_online_music);
        mViewPager = findViewById(R.id.view_pager);

        mBottomPlayBar = findViewById(R.id.bottom_play_bar);
        mIvPlayBarCover = findViewById(R.id.iv_play_bar_cover);
        mTvPlayBarTitle = findViewById(R.id.tv_play_bar_title);
        mTvPlayBarArtist = findViewById(R.id.tv_play_bar_artist);
        mIvPlayBarPlay = findViewById(R.id.iv_play_bar_play);
        mIvPlayBarNext = findViewById(R.id.iv_play_bar_next);
        mProgressBar = findViewById(R.id.play_progressbar);

        // add navigation header
        View navigationHeader = LayoutInflater.from(this).inflate(R.layout.navigation_header, mNavigationView, false);
        mNavigationView.addHeaderView(navigationHeader);

        mLocalMusicFragment = new LocalMusicFragment();
        mPlayListFragment = new PlayListFragment();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        fragmentAdapter.addFragment(mLocalMusicFragment);
        fragmentAdapter.addFragment(mPlayListFragment);
        mViewPager.setAdapter(fragmentAdapter);
        mTvLocalMusic.setSelected(true);
    }

    protected void initListener() {
        mIvMenu.setOnClickListener(this);
        mIvSearch.setOnClickListener(this);
        mTvLocalMusic.setOnClickListener(this);
        mTvOnlineMusic.setOnClickListener(this);
        mViewPager.addOnPageChangeListener(this);
        mBottomPlayBar.setOnClickListener(this);
        mIvPlayBarPlay.setOnClickListener(this);
        mIvPlayBarNext.setOnClickListener(this);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void onChangeImpl(Music music) {
        if (music == null) {
            return;
        }

        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        mIvPlayBarCover.setImageBitmap(cover);
        mTvPlayBarTitle.setText(music.getTitle());
        mTvPlayBarArtist.setText(music.getArtist());
        mIvPlayBarPlay.setSelected(getMusicService().isPlaying() || getMusicService().isPreparing());
        mProgressBar.setMax((int) music.getDuration());
        mProgressBar.setProgress((int) getMusicService().getCurrentPosition());

        if (mLocalMusicFragment != null && mLocalMusicFragment.isAdded()) {
            mLocalMusicFragment.onItemPlay();
        }
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(ConstantUtil.EXTRA_NOTIFICATION)) {
            showPlayingFragment();
            setIntent(new Intent());
        }
    }

    private void play() {
        getMusicService().playPause();
    }

    private void next() {
        getMusicService().next();
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
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        mDrawerLayout.closeDrawers();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);
            }
        }, 500);

        return NavMenuExecutor.onNavigationItemSelected(item, this);
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
                // startActivity(new Intent(this, ));
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
            case R.id.iv_play_bar_play:
                play();
                break;
            case R.id.iv_play_bar_next:
                next();
                break;
        }
    }

    @Override
    public void onChange(Music music) {
        onChangeImpl(music);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            // mPlayFragment.onChange(music);
        }
    }

    @Override
    public void onPlayerStart() {
        mIvPlayBarPlay.setSelected(true);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            // mPlayFragment.onPlayerStart();
        }
    }

    @Override
    public void onPlayerPause() {
        mIvPlayBarPlay.setSelected(false);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            // mPlayFragment.onPlayerPause();
        }
    }

    @Override
    public void onPublish(int progress) {
        mProgressBar.setProgress(progress);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            // mPlayFragment.onPublish(progress);
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            // mPlayFragment.onBufferingUpdate(percent);
        }
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
    public void onMusicListUpdate() {
        if (mLocalMusicFragment != null && mLocalMusicFragment.isAdded()) {
            mLocalMusicFragment.onMusicListUpdate();
        }
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
        mPlayListFragment.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(savedInstanceState.getInt(ConstantUtil.VIEW_PAGER_INDEX), false);
                mLocalMusicFragment.onRestoreInstanceState(savedInstanceState);
                // mPlayListFragment.onRestoreInstanceState(savedInstanceState);
            }
        });
    }

    @Override
    protected void onDestroy() {
        MusicService service = AppCache.getPlayService();
        if (service != null) {
            service.setOnPlayEventListener(null);
        }
        super.onDestroy();
    }
}
