package io.weicools.puremusic.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;

import java.io.File;
import java.util.Locale;

import io.weicools.puremusic.R;
import io.weicools.puremusic.model.Music;
import io.weicools.puremusic.ui.base.BaseActivity;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.CoverLoader;
import io.weicools.puremusic.util.FileUtil;
import io.weicools.puremusic.util.ImageUtil;
import io.weicools.puremusic.util.PermissionUtil;
import io.weicools.puremusic.util.SystemUtil;
import io.weicools.puremusic.util.ToastUtil;
import io.weicools.puremusic.util.id3.ID3TagUtils;
import io.weicools.puremusic.util.id3.ID3Tags;

public class MusicInfoActivity extends BaseActivity implements View.OnClickListener  {
    private ImageView mIvCover;
    private EditText mEtTitle;
    private EditText mEtArtist;
    private EditText mEtAlbum;
    private TextView mTvDuration;
    private TextView mTvFileName;
    private TextView mTvFileSize;
    private TextView mTvFilePath;

    private File mMusicFile;
    private Bitmap mCoverBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_info);

        initViews();
    }

    private void initViews() {
        mIvCover = findViewById(R.id.iv_music_info_cover);
        mEtTitle = findViewById(R.id.et_music_info_title);
        mEtArtist = findViewById(R.id.et_music_info_artist);
        mEtAlbum = findViewById(R.id.et_music_info_album);
        mTvDuration = findViewById(R.id.tv_music_info_duration);
        mTvFileName = findViewById(R.id.tv_music_info_file_name);
        mTvFileSize = findViewById(R.id.tv_music_info_file_size);
        mTvFilePath = findViewById(R.id.tv_music_info_file_path);
    }

    @Override
    protected void onServiceBound() {
        Music music = (Music) getIntent().getSerializableExtra(ConstantUtil.MUSIC);
        if (music == null || music.getType() != Music.Type.LOCAL) {
            finish();
        }
        mMusicFile = new File(music.getPath());
        mCoverBitmap = CoverLoader.getInstance().loadThumbnail(music);

        mIvCover.setImageBitmap(mCoverBitmap);
        mIvCover.setOnClickListener(this);

        mEtTitle.setText(music.getTitle());
        mEtTitle.setSelection(mEtTitle.length());
        mEtArtist.setText(music.getArtist());
        mEtArtist.setSelection(mEtArtist.length());
        mEtAlbum.setText(music.getAlbum());
        mEtAlbum.setSelection(mEtAlbum.length());

        mTvDuration.setText(SystemUtil.formatTime("mm:ss", music.getDuration()));

        mTvFileName.setText(music.getFileName());
        mTvFileSize.setText(String.format(Locale.getDefault(), "%.2fMB", FileUtil.b2mb((int) music.getFileSize())));
        mTvFilePath.setText(mMusicFile.getParent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_music_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            save();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        PermissionUtil.with(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .result(new PermissionUtil.Result() {
                    @Override
                    public void onGranted() {
                        ImageUtil.startAlbum(MusicInfoActivity.this);
                    }

                    @Override
                    public void onDenied() {
                        ToastUtil.showShort(R.string.no_permission_select_image);
                    }
                })
                .request();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == ConstantUtil.REQUEST_ALBUM && data != null) {
            ImageUtil.startCorp(this, data.getData());
        } else if (requestCode == ConstantUtil.REQUEST_CORP) {
            File corpFile = new File(FileUtil.getCorpImagePath(this));
            if (!corpFile.exists()) {
                ToastUtil.showShort("图片保存失败");
                return;
            }

            mCoverBitmap = BitmapFactory.decodeFile(corpFile.getPath());
            mIvCover.setImageBitmap(mCoverBitmap);
            //noinspection ResultOfMethodCallIgnored
            corpFile.delete();
        }
    }

    private void save() {
        if (!mMusicFile.exists()) {
            ToastUtil.showShort("歌曲文件不存在");
            return;
        }

        ID3Tags id3Tags = new ID3Tags.Builder()
                .setCoverBitmap(mCoverBitmap)
                .setTitle(mEtTitle.getText().toString())
                .setArtist(mEtArtist.getText().toString())
                .setAlbum(mEtAlbum.getText().toString())
                .build();
        ID3TagUtils.setID3Tags(mMusicFile, id3Tags, false);

        // 刷新媒体库
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mMusicFile));
        sendBroadcast(intent);

        mHandler.postDelayed(() -> RxBus.get().post(ConstantUtil.SCAN_MUSIC, new Object()), 1000);

        ToastUtil.showShort("保存成功");
    }
}
