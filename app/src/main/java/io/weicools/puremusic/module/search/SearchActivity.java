package io.weicools.puremusic.module.search;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.weicools.puremusic.R;
import io.weicools.puremusic.interfaze.OnMoreClickListener;
import io.weicools.puremusic.executor.DownloadSearchedMusic;
import io.weicools.puremusic.executor.PlaySearchedMusic;
import io.weicools.puremusic.executor.ShareOnlineMusic;
import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.data.Music;
import io.weicools.puremusic.data.SearchMusic;
import io.weicools.puremusic.enums.LoadStateEnum;
import io.weicools.puremusic.service.AudioPlayer;
import io.weicools.puremusic.module.base.BaseActivity;
import io.weicools.puremusic.util.FileUtil;
import io.weicools.puremusic.util.ToastUtil;
import io.weicools.puremusic.util.ViewUtil;

public class SearchActivity extends BaseActivity implements SearchView.OnQueryTextListener,
        AdapterView.OnItemClickListener, OnMoreClickListener {

    private ListView mLvSearchList;
    private LinearLayout mLlLoading;
    private LinearLayout mLlLoadFail;

    private List<SearchMusic.Song> searchMusicList = new ArrayList<>();
    private SearchMusicAdapter mAdapter = new SearchMusicAdapter(searchMusicList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mLvSearchList = findViewById(R.id.lv_search_music_list);
        mLlLoading = findViewById(R.id.ll_loading);
        mLlLoadFail = findViewById(R.id.ll_load_fail);
    }

    @Override
    public boolean canBack() {
        return true;
    }

    @Override
    protected int getDarkTheme() {
        return R.style.AppThemeDark_Search;
    }

    @Override
    protected void onServiceBound() {
        mLvSearchList.setAdapter(mAdapter);
        TextView tvLoadFail = mLlLoadFail.findViewById(R.id.tv_load_fail_text);
        tvLoadFail.setText(R.string.search_empty);

        mLvSearchList.setOnItemClickListener(this);
        mAdapter.setOnMoreClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_music, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.onActionViewExpanded();
        searchView.setQueryHint(getString(R.string.search_tips));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        try {
            Field field = searchView.getClass().getDeclaredField("mGoButton");
            field.setAccessible(true);
            ImageView mGoButton = (ImageView) field.get(searchView);
            mGoButton.setImageResource(R.drawable.ic_menu_search);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        ViewUtil.changeViewState(mLvSearchList, mLlLoading, mLlLoadFail, LoadStateEnum.LOADING);
        searchMusic(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        new PlaySearchedMusic(this, searchMusicList.get(position)) {
            @Override
            public void onPrepare() {
                showProgress();
            }

            @Override
            public void onExecuteSuccess(Music music) {
                cancelProgress();
                AudioPlayer.getInstance().addAndPlay(music);
                ToastUtil.showShort("已添加到播放列表");
            }

            @Override
            public void onExecuteFail(Exception e) {
                cancelProgress();
                ToastUtil.showShort(R.string.unable_to_play);
            }
        }.execute();
    }

    @Override
    public void onMoreClick(int position) {
        final SearchMusic.Song song = searchMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(song.getSongname());
        String path = FileUtil.getMusicDir() + FileUtil.getMp3FileName(song.getArtistname(), song.getSongname());
        File file = new File(path);
        int itemsId = file.exists() ? R.array.search_music_dialog_no_download : R.array.search_music_dialog;
        dialog.setItems(itemsId, (dialog1, which) -> {
            switch (which) {
                case 0:// 分享
                    share(song);
                    break;
                case 1:// 下载
                    download(song);
                    break;
            }
        });
        dialog.show();
    }

    private void searchMusic(String keyword) {
        HttpClient.searchMusic(keyword, new HttpCallback<SearchMusic>() {
            @Override
            public void onSuccess(SearchMusic response) {
                if (response == null || response.getSong() == null) {
                    ViewUtil.changeViewState(mLvSearchList, mLlLoading, mLlLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                }
                ViewUtil.changeViewState(mLvSearchList, mLlLoading, mLlLoadFail, LoadStateEnum.LOAD_SUCCESS);
                searchMusicList.clear();
                searchMusicList.addAll(response.getSong());
                mAdapter.notifyDataSetChanged();
                mLvSearchList.requestFocus();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLvSearchList.setSelection(0);
                    }
                });
            }

            @Override
            public void onFail(Exception e) {
                ViewUtil.changeViewState(mLvSearchList, mLlLoading, mLlLoadFail, LoadStateEnum.LOAD_FAIL);
            }
        });
    }

    private void share(SearchMusic.Song song) {
        new ShareOnlineMusic(this, song.getSongname(), song.getSongid()) {
            @Override
            public void onPrepare() {
                showProgress();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                cancelProgress();
            }

            @Override
            public void onExecuteFail(Exception e) {
                cancelProgress();
            }
        }.execute();
    }

    private void download(SearchMusic.Song song) {
        new DownloadSearchedMusic(this, song) {
            @Override
            public void onPrepare() {
                showProgress();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                cancelProgress();
                ToastUtil.showShort(getString(R.string.now_download, song.getSongname()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                cancelProgress();
                ToastUtil.showShort(R.string.unable_to_download);
            }
        }.execute();
    }
}
