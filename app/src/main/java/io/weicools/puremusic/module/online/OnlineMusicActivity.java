package io.weicools.puremusic.module.online;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.weicools.puremusic.R;
import io.weicools.puremusic.interfaze.OnMoreClickListener;
import io.weicools.puremusic.executor.DownloadOnlineMusic;
import io.weicools.puremusic.executor.PlayOnlineMusic;
import io.weicools.puremusic.executor.ShareOnlineMusic;
import io.weicools.puremusic.http.HttpCallback;
import io.weicools.puremusic.http.HttpClient;
import io.weicools.puremusic.data.Music;
import io.weicools.puremusic.data.OnlineMusic;
import io.weicools.puremusic.data.OnlineMusicList;
import io.weicools.puremusic.data.SongSheetInfo;
import io.weicools.puremusic.enums.LoadStateEnum;
import io.weicools.puremusic.service.AudioPlayer;
import io.weicools.puremusic.module.musicinfo.ArtistInfoActivity;
import io.weicools.puremusic.module.base.BaseActivity;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.FileUtil;
import io.weicools.puremusic.util.ImageUtil;
import io.weicools.puremusic.util.ScreenUtil;
import io.weicools.puremusic.util.ToastUtil;
import io.weicools.puremusic.util.ViewUtil;
import io.weicools.puremusic.widget.AutoLoadListView;

public class OnlineMusicActivity extends BaseActivity implements AutoLoadListView.OnLoadListener {

    private static final int MUSIC_LIST_SIZE = 20;

    private Context mContext;
    private LinearLayout llLoading;
    private LinearLayout llLoadFail;
    private AutoLoadListView lvOnlineMusic;

    private View vHeader;
    private SongSheetInfo mListInfo;
    private OnlineMusicList mOnlineMusicList;
    private List<OnlineMusic> mMusicList = new ArrayList<>();
    private OnlineMusicAdapter mAdapter = new OnlineMusicAdapter(mMusicList);

    private int mOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_music);
        mContext = this;

        initViews();
    }

    private void initViews() {
        llLoading = findViewById(R.id.ll_loading);
        llLoadFail = findViewById(R.id.ll_load_fail);
        lvOnlineMusic = findViewById(R.id.lv_online_music_list);

        vHeader = LayoutInflater.from(this).inflate(R.layout.activity_online_music_list_header, null);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.dp2px(150));
        vHeader.setLayoutParams(params);
        lvOnlineMusic.addHeaderView(vHeader, null, false);
        lvOnlineMusic.setAdapter(mAdapter);
        lvOnlineMusic.setOnLoadListener(this);
        ViewUtil.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);

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
                            default:
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public boolean canBack() {
        return true;
    }

    @Override
    protected void onServiceBound() {
        mListInfo = (SongSheetInfo) getIntent().getSerializableExtra(ConstantUtil.MUSIC_LIST_TYPE);
        setTitle(mListInfo.getTitle());

        onLoad();
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
        Glide.with(this)
                .load(mOnlineMusicList.getBillboard().getPic_s640())
                .asBitmap()
                .placeholder(R.drawable.default_cover)
                .error(R.drawable.default_cover)
                .override(200, 200)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        ivCover.setImageBitmap(resource);
                        ivHeaderBg.setImageBitmap(ImageUtil.blur(resource));
                    }
                });
    }

    private void play(OnlineMusic onlineMusic) {
        new PlayOnlineMusic(this, onlineMusic) {
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

    private void share(final OnlineMusic onlineMusic) {
        new ShareOnlineMusic(this, onlineMusic.getTitle(), onlineMusic.getSong_id()) {
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

    private void artistInfo(OnlineMusic onlineMusic) {
        Intent intent = new Intent(this, ArtistInfoActivity.class);
        intent.putExtra(ConstantUtil.TING_UID, onlineMusic.getTing_uid());
        startActivity(intent);
    }

    private void download(final OnlineMusic onlineMusic) {
        new DownloadOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                showProgress();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                cancelProgress();
                ToastUtil.showShort(mContext, getString(R.string.now_download, onlineMusic.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                cancelProgress();
                ToastUtil.showShort(mContext, getString(R.string.unable_to_download));
            }
        }.execute();
    }
}
