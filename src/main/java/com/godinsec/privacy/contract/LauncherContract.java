package com.godinsec.privacy.contract;

import com.godinsec.privacy.base.BaseView;
import com.godinsec.privacy.bean.AppEvent;
import com.godinsec.privacy.bean.AppInfo;
import com.godinsec.privacy.bean.Favorite;
import java.util.ArrayList;

/**
 * Created by Seeker on 2016/9/13.
 */

public interface LauncherContract {

    interface LauncherView extends BaseView{

        void setWorkSpaceDatas(ArrayList<ArrayList<AppInfo>> screenAppDatas);

        void addFavorite(Favorite favorite);

    }

    interface LauncherPresenter{

        void doLoad();

        void doFavorite();

        void doDeleta(AppEvent appEvent,boolean direct);
    }
}
