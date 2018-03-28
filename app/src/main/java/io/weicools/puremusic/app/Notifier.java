package io.weicools.puremusic.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import io.weicools.puremusic.R;
import io.weicools.puremusic.data.Music;
import io.weicools.puremusic.module.MainActivity;
import io.weicools.puremusic.receiver.StatusBarReceiver;
import io.weicools.puremusic.service.MusicService;
import io.weicools.puremusic.util.ConstantUtil;
import io.weicools.puremusic.util.CoverLoader;
import io.weicools.puremusic.util.FileUtil;

/**
 * Author: weicools
 * Time: 2017/10/30 下午5:32
 */

public class Notifier {
    private static final int NOTIFICATION_ID = 0x111;
    private static MusicService sMusicService;
    private static NotificationManager notificationManager;

    private Notifier() {
    }

    private static class NotifierHolder {
        private static Notifier INSTANCE = new Notifier();
    }

    public static Notifier getInstance() {
        return NotifierHolder.INSTANCE;
    }

    public void init(MusicService playService) {
        Notifier.sMusicService = playService;
        notificationManager = (NotificationManager) playService.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void showPlay(Music music) {
        if (music == null) {
            return;
        }
        sMusicService.startForeground(NOTIFICATION_ID, buildNotification(sMusicService, music, true));
    }

    public void showPause(Music music) {
        if (music == null) {
            return;
        }
        sMusicService.stopForeground(false);
        notificationManager.notify(NOTIFICATION_ID, buildNotification(sMusicService, music, false));
    }

    public void cancelAll() {
        notificationManager.cancelAll();
    }

    private Notification buildNotification(Context context, Music music, boolean isPlaying) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ConstantUtil.EXTRA_NOTIFICATION, true);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setCustomContentView(getRemoteViews(context, music, isPlaying));
        return builder.build();
    }

    private RemoteViews getRemoteViews(Context context, Music music, boolean isPlaying) {
        String title = music.getTitle();
        String subtitle = FileUtil.getArtistAndAlbum(music.getArtist(), music.getAlbum());
        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
        if (cover != null) {
            remoteViews.setImageViewBitmap(R.id.iv_icon, cover);
        } else {
            remoteViews.setImageViewResource(R.id.iv_icon, R.mipmap.ic_launcher);
        }
        remoteViews.setTextViewText(R.id.tv_title, title);
        remoteViews.setTextViewText(R.id.tv_subtitle, subtitle);

        Intent playIntent = new Intent(StatusBarReceiver.ACTION_STATUS_BAR);
        playIntent.putExtra(StatusBarReceiver.EXTRA, StatusBarReceiver.EXTRA_PLAY_PAUSE);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_play_pause, playPendingIntent);

        Intent nextIntent = new Intent(StatusBarReceiver.ACTION_STATUS_BAR);
        nextIntent.putExtra(StatusBarReceiver.EXTRA, StatusBarReceiver.EXTRA_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.iv_next, nextPendingIntent);

        return remoteViews;
    }
}
