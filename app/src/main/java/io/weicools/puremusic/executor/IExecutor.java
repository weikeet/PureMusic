package io.weicools.puremusic.executor;

/**
 * Author: weicools
 * Time: 2017/11/22 上午10:45
 */

public interface IExecutor<T> {
    void execute();

    void onPrepare();

    void onExecuteSuccess(T t);

    void onExecuteFail(Exception e);
}
