package com.godinsec.privacy.contract;

import com.godinsec.privacy.base.BasePresenter;
import com.godinsec.privacy.bean.AppEvent;
import com.godinsec.privacy.bean.Favorite;
import com.godinsec.privacy.controller.DeleteTargetController;
import com.godinsec.privacy.data.LauncherModer;
import com.godinsec.privacy.utils.RxJavaHelper;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Seeker on 2016/9/13.
 */
public class LauncherPresenterImpl extends BasePresenter<LauncherContract.LauncherView>
        implements LauncherContract.LauncherPresenter{

    private RxJavaHelper rxJavaHelper;

    private DeleteTargetController mDeleteTargetController;

    public LauncherPresenterImpl(){
        this.rxJavaHelper = RxJavaHelper.getInstance();

    }

    public void setDeleteTargetController(DeleteTargetController mDeleteTargetController){
        this.mDeleteTargetController = mDeleteTargetController;
    }

    @Override
    public void doLoad() {
        rxJavaHelper.setThread(Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                LauncherModer.getInstance().loadFavorites();
                LauncherModer.getInstance().loadInstalledApps();
                subscriber.onNext(true);
            }
        }))
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if(!checkViewNull()){
                            bindedView.onPreLoad();
                        }
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean && !checkViewNull()){
                            bindedView.setWorkSpaceDatas(LauncherModer.getInstance().obtainApps());
                            bindedView.onFinishLoad();
                        }
                    }
                });
    }

    @Override
    public void doFavorite() {
        LauncherModer.getInstance().obtainFavorites(new Action1<Favorite>() {
            @Override
            public void call(Favorite favorite) {
                if(!checkViewNull()){
                    bindedView.addFavorite(favorite);
                }
            }
        });
    }

    @Override
    public void doDeleta(AppEvent appEvent,boolean direct) {
        if(direct){
            LauncherModer.getInstance().deleteAppinfo(appEvent);
        }else if (mDeleteTargetController.onEventMainThreadDirectDelete(appEvent)) {
            LauncherModer.getInstance().deleteAppinfo(appEvent);
        }
    }
}
