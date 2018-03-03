package io.weicools.puremusic.data.database;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

/**
 * Create by weicools on 2018/3/4.
 */

public class DBManager {
    private static final String DB_NAME = "database";
    private MusicDao musicDao;

    private DBManager() {
    }

    private static class DBManagerHolder {
        private static DBManager INSTANCE = new DBManager();
    }

    public static DBManager getInstance() {
        return DBManagerHolder.INSTANCE;
    }

    public void init(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        musicDao = daoSession.getMusicDao();
    }

    public MusicDao getMusicDao() {
        return musicDao;
    }
}
