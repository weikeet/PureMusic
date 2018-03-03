package io.weicools.puremusic.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.weicools.puremusic.R;
import io.weicools.puremusic.adapter.OnMoreClickListener;
import io.weicools.puremusic.adapter.OnlineMusicAdapter;
import io.weicools.puremusic.executor.DownloadOnlineMusic;
import io.weicools.puremusic.executor.PlayOnlineMusic;
import io.weicools.puremusic.executor.ShareOnlineMusic;
import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.model.OnlineMusic;
import io.weicools.puremusic.model.OnlineMusicList;
import io.weicools.puremusic.model.SongListInfo;
import io.weicools.puremusic.model.enums.LoadStateEnum;
import io.weicools.puremusic.ui.base.BaseActivity;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.FileUtil;
import io.weicools.puremusic.util.ScreenUtil;
import io.weicools.puremusic.util.ToastUtil;
import io.weicools.puremusic.util.ViewUtil;
import io.weicools.puremusic.widget.AutoLoadListView;

public class OnlineMusicActivity extends BaseActivity implements AutoLoadListView.OnLoadListener {

    private static final int MUSIC_LIST_SIZE = 20;

    private Context mContext;
    private AutoLoadListView lvOnlineMusic;
    private LinearLayout llLoading;
    private LinearLayout llLoadFail;
    private View vHeader;
    private SongListInfo mListInfo;
    private OnlineMusicList mOnlineMusicList;
    private List<OnlineMusic> mMusicList = new ArrayList<>();
    private OnlineMusicAdapter mAdapter = new OnlineMusicAdapter(mMusicList);
    private ProgressDialog mProgressDialog;
    private int mOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_music);
        mContext = this;
        if (!checkServiceAlive()) {
            return;
        }

        mListInfo = (SongListInfo) getIntent().getSerializableExtra(ConstantUtil.MUSIC_LIST_TYPE);
        setTitle(mListInfo.getTitle());

        initView();
        initListener();
        onLoad();
    }

    private void initView() {
        lvOnlineMusic = findViewById(R.id.lv_online_music_list);
        llLoading = findViewById(R.id.ll_loading);
        llLoadFail = findViewById(R.id.ll_load_fail);

        vHeader = LayoutInflater.from(this).inflate(R.layout.activity_online_music_list_header, null);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.dp2px(150));
        vHeader.setLayoutParams(params);
        lvOnlineMusic.addHeaderView(vHeader, null, false);
        lvOnlineMusic.setAdapter(mAdapter);
        lvOnlineMusic.setOnLoadListener(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        ViewUtil.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);
    }

    private void initListener() {
        lvOnlineMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                play((OnlineMusic) parent.getAdapter().getItem(position));
            }
        });
        mAdapter.setOnMoreClickListener(new OnMoreClickListener() {
            @Override
            public void onMoreClick(int position) {
                final OnlineMusic onlineMusic = mMusicList.get(position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle(mMusicList.get(position).getTitle());
                String path = FileUtil.getMusicDir() + FileUtil.getMp3FileName(onlineMusic.getArtist_name(), onlineMusic.getTitle());
                File file = new File(path);
                int itemsId = file.exists() ? R.array.online_music_dialog_without_download : R.array.online_music_dialog;
                dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:// 分享
                                share(onlineMusic);
                                break;
                            case 1:// 查看歌手信息
                                artistInfo(onlineMusic);
                                break;
                            case 2:// 下载
                                download(onlineMusic);
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public void onLoad() {
        getMusic(mOffset);
    }

    private void getMusic(final int offset) {
        HttpClient.getSongListInfo(mListInfo.getType(), MUSIC_LIST_SIZE, offset, new HttpCallback<OnlineMusicList>() {
            @Override
            public void onSuccess(OnlineMusicList response) {
                lvOnlineMusic.onLoadComplete();
                mOnlineMusicList = response;
                if (offset == 0 && response == null) {
                    ViewUtil.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                } else if (offset == 0) {
                    initHeader();
                    ViewUtil.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                }
                if (response == null || response.getSong_list() == null || response.getSong_list().size() == 0) {
                    lvOnlineMusic.setEnable(false);
                    return;
                }
                mOffset += MUSIC_LIST_SIZE;
                mMusicList.addAll(response.getSong_list());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(Exception e) {
                lvOnlineMusic.onLoadComplete();
                if (e instanceof RuntimeException) {
                    // 歌曲全部加载完成
                    lvOnlineMusic.setEnable(false);
                    return;
                }
                if (offset == 0) {
                    ViewUtil.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                } else {
                    ToastUtil.showShort(mContext, getString(R.string.load_fail));
                }
            }
        });
    }

    private void initHeader() {
        final ImageView ivHeaderBg = vHeader.findViewById(R.id.iv_header_bg);
        final ImageView ivCover = vHeader.findViewById(R.id.iv_cover);
        TextView tvTitle = vHeader.findViewById(R.id.tv_title);
        TextView tvUpdateDate = vHeader.findViewById(R.id.tv_update_date);
        TextView tvComment = vHeader.findViewById(R.id.tv_comment);
        tvTitle.setText(mOnlineMusicList.getBillboard().getName());
        tvUpdateDate.setText(getString(R.string.recent_update, mOnlineMusicList.getBillboard().getUpdate_date()));
        tvComment.setText(mOnlineMusicList.getBillboard().getComment());
        // TODO: 2017/11/27 Glide 4
//        Glide.with(this)
//                .load(mOnlineMusicList.getBillboard().getPic_s640())
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                        ivCover.setImageBitmap(resource);
//                        ivHeaderBg.setImageBitmap(ImageUtil.blur(resource));
//                    }
//                });
//                .asBitmap()
//                .placeholder(R.drawable.default_cover)
//                .error(R.drawable.default_cover)
//                .override(200, 200)
    }

    private void play(OnlineMusic onlineMusic) {
        new PlayOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Music music) {
                mProgressDialog.cancel();
                getMusicService().play(music);
                ToastUtil.showShort(mContext, getString(R.string.now_play, music.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
                ToastUtil.showShort(mContext, getString(R.string.unable_to_play));
            }
        }.execute();
    }

    private void share(final OnlineMusic onlineMusic) {
        new ShareOnlineMusic(this, onlineMusic.getTitle(), onlineMusic.getSong_id()) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                mProgressDialog.cancel();
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
            }
        }.execute();
    }

    private void artistInfo(OnlineMusic onlineMusic) {
        ArtistInfoActivity.start(this, onlineMusic.getTing_uid());
    }

    private void download(final OnlineMusic onlineMusic) {
        new DownloadOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                mProgressDialog.cancel();
                ToastUtil.showShort(mContext, getString(R.string.now_download, onlineMusic.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
                ToastUtil.showShort(mContext, getString(R.string.unable_to_download));
            }
        }.execute();
    }
}