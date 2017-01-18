package com.godinsec.privacy.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.greenrobot.event.EventBus;

/**
 * Created by Seeker on 2016/9/13.
 */

public abstract class BaseActivity<V extends BaseEmptyView,P extends BasePresenter<V>> extends AppCompatActivity {

    public LayoutInflater mLayoutInflater;

    public Unbinder unbinder;

    public P presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        unbinder = ButterKnife.bind(this);
        mLayoutInflater = LayoutInflater.from(this);
        EventBus.getDefault().register(this);
        presenter = initPresenter();
        presenter.bindView((V) this);
        doWork();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(unbinder != null){
            unbinder.unbind();
        }

        if(presenter != null){
            presenter.unbindView();
        }

        EventBus.getDefault().unregister(this);
    }

    /**
     * 返回布局layoutId
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 一些其它的操作
     */
    protected abstract void doWork();

    /**
     * 实例化presenter
     * @return
     */
    public abstract P initPresenter();
}
