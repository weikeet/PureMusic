package io.weicools.puremusic.module;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import io.weicools.puremusic.R;
import io.weicools.puremusic.executor.ControlPanel;
import io.weicools.puremusic.module.base.BaseActivity;
import io.weicools.puremusic.module.local.LocalMusicFragment;
import io.weicools.puremusic.module.online.SongSheetFragment;
import io.weicools.puremusic.module.playing.PlayFragment;
import io.weicools.puremusic.service.AudioPlayer;
import io.weicools.puremusic.service.QuitTimer;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.SystemUtil;

public class MainActivity extends BaseActivity implements View.OnClickListener, QuitTimer.OnTimerListener {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FrameLayout mBottomPlayBar;

    private LocalMusicFragment mLocalMusicFragment;
    private SongSheetFragment mSongSheetFragment;
    private MySheetFragment mMySheetFragment;
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
        mTabLayout = findViewById(R.id.tab_layout_main);
        mViewPager = findViewById(R.id.view_pager);
        mBottomPlayBar = findViewById(R.id.bottom_play_bar);

        mBottomPlayBar.setOnClickListener(this);
        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.tab_title_main_1));
        titles.add(getString(R.string.tab_title_main_2));
        titles.add(getString(R.string.tab_title_main_3));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(2)));

        List<Fragment> fragments = new ArrayList<>();
        mLocalMusicFragment = new LocalMusicFragment();
        mSongSheetFragment = new SongSheetFragment();
        mMySheetFragment = MySheetFragment.newInstance();
        fragments.add(mLocalMusicFragment);
        fragments.add(mMySheetFragment);
        fragments.add(mSongSheetFragment);

        mViewPager.setOffscreenPageLimit(2);

        FragmentAdapter mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(mFragmentAdapter);
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
            // FIXME: 2018/3/27
//            timerItem = mNavigationView.getMenu().findItem(R.id.action_timer);
        }
        String title = getString(R.string.menu_timer);
        timerItem.setTitle(remain == 0 ? title : SystemUtil.formatTime(title + "(mm:ss)", remain));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.iv_search:
//                startActivity(new Intent(this, SearchActivity.class));
//                break;
//            case R.id.tv_local_music:
//                mViewPager.setCurrentItem(0);
//                break;
//            case R.id.tv_online_music:
//                mViewPager.setCurrentItem(1);
//                break;
            case R.id.bottom_play_bar:
                showPlayingFragment();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && mIsPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }

        super.onBackPressed();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public static class FragmentAdapter extends FragmentStatePagerAdapter {
        private List<String> mTitles;
        private List<Fragment> mFragments;

        FragmentAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
            super(fm);
            mTitles = titles;
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }
    }
}
