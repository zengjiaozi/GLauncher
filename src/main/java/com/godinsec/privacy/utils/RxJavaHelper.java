package com.godinsec.privacy.utils;

import com.godinsec.privacy.bean.AppInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Seeker on 2016/9/9.
 */

public final class RxJavaHelper {

    private ExecutorService exector = null;

    private RxJavaHelper(){
        exector = Executors.newFixedThreadPool(5,new ExecutorThreadFactory());
    }

    private static final class Factory{
        private static final RxJavaHelper instance = new RxJavaHelper();
    }

    public static RxJavaHelper getInstance(){
        return Factory.instance;
    }

    /**
     * 设置线程
     * @param observable
     * @return
     */
    public <T>Observable<T> setThread(Observable<T> observable){
        observable.subscribeOn(Schedulers.from(exector))
                  .observeOn(AndroidSchedulers.mainThread());
        return observable;
    }

    private static class ExecutorThreadFactory implements ThreadFactory {

        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r,"PrivacyLauncherThread_id:"+mCount.getAndIncrement());
            thread.setPriority(Thread.NORM_PRIORITY -1);
            return thread;
        }
    }

}
