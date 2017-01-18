package com.godinsec.privacy.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import com.godinsec.privacy.LauncherApplication;
import com.godinsec.privacy.bean.AppEvent;
import com.godinsec.privacy.bean.AppInfo;
import com.godinsec.privacy.bean.Favorite;
import com.godinsec.privacy.bean.ItemInfo;
import com.godinsec.privacy.db.PrivacyLauncherSQLManger;
import com.godinsec.launcher.R;
import com.godinsec.privacy.utils.AppUtils;
import com.godinsec.privacy.utils.Configure;
import com.godinsec.privacy.utils.LogUtils;
import com.godinsec.privacy.utils.RxJavaHelper;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Seeker on 2016/9/9.
 */

public class LauncherModer extends BroadcastReceiver{

    private static final String TAG = "LauncherModer";

    private Context mContext;

    /**
     * 所有应用的信息
     */
    private static final ArrayList<ArrayList<AppInfo>> screenLists = new ArrayList<>();

    /**
     * 默认应用的信息
     */
    private static final HashMap<String ,Favorite> favorites = new HashMap<>();

    /**
     * 底部应用列表
     */
    private static final ArrayList<AppInfo> hotSetApps = new ArrayList<>();

    /**
     * 可以在手机查出该应用，但是数据库未保存当前应用信息,多数是未收到安装广播
     */
    private static final ArrayList<AppInfo> sqlHasNoData = new ArrayList<>();

    /**
     * 数据库保存了当前信息，但是手机查不出该应用,多数是未收到卸载广播
     */
    private static final HashMap<String,AppInfo> phoneHasNodata = new HashMap<>();

    public LauncherModer(){
        mContext = LauncherApplication.getInstance();
    }

    private static final class SingleFactory {
        private static final LauncherModer instance = new LauncherModer();
    }

    public static LauncherModer getInstance() {
        return SingleFactory.instance;
    }

    private void beginDocument(XmlPullParser parser, String firstElementName)
            throws IOException, XmlPullParserException {
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
        }
        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }
        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException(
                    "Unexpected start tag: found " + parser.getName() + ", expected " + firstElementName);
        }
    }

    /**
     * 获取预置应用信息
     */
    public void loadFavorites(){
        try {
            XmlResourceParser parser = mContext.getResources().getXml(R.xml.default_workspace);
            if(parser == null) return;
            beginDocument(parser,"favorites");
            AttributeSet attrs = Xml.asAttributeSet(parser);
            if (attrs == null) return;
            final int depth = parser.getDepth();
            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT){
                if(type != XmlPullParser.START_TAG){
                    continue;
                }

                TypedArray ta = mContext.obtainStyledAttributes(attrs,R.styleable.Favorite);

                Favorite favorite = new Favorite();
                if(!ta.hasValue(R.styleable.Favorite_packageName)){
                    throw new NullPointerException("miss packageName.");
                }
                favorite.setPckName(ta.getString(R.styleable.Favorite_packageName));
                if(!ta.hasValue(R.styleable.Favorite_position)){
                    throw new NullPointerException("miss position.");
                }
                favorite.setPosition(ta.getInt(R.styleable.Favorite_position,-1));
                if(!ta.hasValue(R.styleable.Favorite_isHotSetView)){
                    throw new NullPointerException("miss isHotSetView");
                }
                favorite.setHotSetView(ta.getBoolean(R.styleable.Favorite_isHotSetView,true));
                LogUtils.i(TAG,favorite.toString());

                favorites.put(favorite.getPckName(),favorite);
            }
        }catch (XmlPullParserException | IOException e){
            LogUtils.e(TAG, "Got exception parsing favorites:" + e);
        }
    }

    /**
     * 生成每屏的数据源数组
     *
     * @param screenIndex
     */
    private ArrayList<AppInfo> createSource(int screenIndex) {
        ArrayList<AppInfo> appInfos = new ArrayList<>();
        int size = Configure.currentColumn * Configure.currentRow;
        for (int i = 0; i < size; i++) {
            appInfos.add(createEmptyAppInfo());
        }
        return appInfos;
    }

    private AppInfo createEmptyAppInfo() {
        AppInfo appInfo = new AppInfo();
        appInfo.setItemType(ItemInfo.Type.EMPTY);
        return appInfo;
    }

    /**
     * 获取所有数据，并进行分屏处理
     */
    public void loadInstalledApps(){

        /**
         * 初始化数据库保存的信息，默认手机中全不存在
         */
        phoneHasNodata.putAll(PrivacyLauncherSQLManger.getInstance().queryApplicationInfoList());

        ArrayList<AppInfo> infos = loadApplications();

        if(infos.size() > 0 ){

            if(phoneHasNodata.size() > 0){//数据库中有数据，既是应用不是第一次启动
                locatedApplicationBySqlite(infos);
            }else {
                locatedApplicationByDefault(infos);
            }

            //重新以安装的名义给未在数据库sql查到的信息设置新位置
            LogUtils.i(TAG,"sqlHasNodata.size = "+sqlHasNoData.size());
            for(AppInfo appInfo:sqlHasNoData){
                LogUtils.d(TAG,"install（add） " +appInfo.getTitle()+" ,has not find in sql.");
                addAppWork(createAppEvent(appInfo.getPckName(),AppEvent.ACTION_ADD));
            }

            //重新以卸载的名义给未在手机中查到的信息在sql中做删除操作
            Set<String> pcks = phoneHasNodata.keySet();
            LogUtils.i(TAG,"phoneHasNodata.size = "+phoneHasNodata.size());
            for(String pck:pcks){
                LogUtils.e(TAG,"remove pck = "+pck+"[title="+phoneHasNodata.get(pck).getTitle()+"]"+",has not find in phone. ");
                PrivacyLauncherSQLManger.getInstance().deleteApplicationInfo(pck);
            }

            sqlHasNoData.clear();
            phoneHasNodata.clear();
        }

    }

    /**
     * 获取手机内部安装的所有应用信息
     * @return
     */
    private ArrayList<AppInfo> loadApplications(){
        ArrayList<AppInfo> appInfos = new ArrayList<>();

        final PackageManager packageManager = mContext.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent,0);

        if(resolveInfos == null || resolveInfos.size() == 0){
            LogUtils.e(TAG,"[loadApplications] you has not install any app!");
            return appInfos;
        }

        for(ResolveInfo resolveInfo:resolveInfos){

            final String packageName = resolveInfo.activityInfo.applicationInfo.packageName;

            try {
                if((packageManager.getApplicationInfo(packageName, 0).flags & ApplicationInfo.FLAG_SYSTEM) <= 0){
                    /**
                     * 一些预置应用的信息处理
                     */
                    if (favorites.containsKey(packageName)){
                        Favorite favorite = favorites.get(packageName);
                        AppInfo appInfo = new AppInfo();
                        appInfo.setPosition(favorite.getPosition());
                        hotSetApps.add(AppUtils.getAppInfoByPck(resolveInfo.activityInfo.applicationInfo,appInfo));
                    }else {
                        AppInfo appInfo = AppUtils.getAppInfoByPck(resolveInfo.activityInfo.applicationInfo);
                        appInfos.add(appInfo);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        resolveInfos.clear();

        return appInfos;
    }

    /**
     * 根据保存的信息做分屏处理
     *
     * @param infos
     */
    private void locatedApplicationBySqlite(ArrayList<AppInfo> infos) {
        LogUtils.v(TAG,"enter fuc:[locatedApplicationBySqlite]");
        final int size = infos.size();

        for (int i = 0; i < size; i++) {
            /**
             * 从手机获取的应用基本信息
             */
            AppInfo appInfo = infos.get(i);
            /**
             * 数据库中保存的当前应用的位置信息
             */
            AppInfo sqliteInfo = PrivacyLauncherSQLManger.getInstance().queryApplicationInfo(appInfo.getPckName());
            if (sqliteInfo == null) {
                LogUtils.e(TAG,"["+appInfo.getTitle() +"] has not find in sql!");
                sqlHasNoData.add(appInfo);
                continue;
            }

            /**
             * 移除在手机和sql都存在的信息，保留sql存在的信息而手机不存在的信息
             */
            phoneHasNodata.remove(appInfo.getPckName());


            int screenIndex = sqliteInfo.getScreen();
            /**
             * 说明需要新加一屏
             */
            if (screenIndex >= screenLists.size()) {
                LogUtils.i(TAG, "[locatedApplicationBySqlite] need add new screenIndex!");
                for (int j = screenLists.size(); j <= screenIndex; j++) {
                    screenLists.add(createSource(j));
                }
            }

            /**
             * 添加位置信息到appInfo
             */
            appInfo.setScreen(screenIndex);
            appInfo.setPosition(sqliteInfo.getPosition());
            appInfo.setItemType(sqliteInfo.getItemType());
            screenLists.get(screenIndex).set(sqliteInfo.getPosition(), appInfo);
        }
        LogUtils.v(TAG,"leave fuc:[locatedApplicationBySqlite]");
    }

    /**
     * 根据加载的顺序做分屏处理
     *
     * @param infos
     */
    private void locatedApplicationByDefault(ArrayList<AppInfo> infos) {
        LogUtils.v(TAG,"enter fuc:[locatedApplicationByDefault]");
        final int size = infos.size();

        final int screenApps = Configure.currentColumn * Configure.currentRow;

        final int remainder = size % screenApps;

        final int screenCount = size / screenApps +(remainder > 0 ?1:0);

        for(int i = 0;i <screenCount;i++){
            screenLists.add(createSource(i));
        }

        for(int i = 0;i <size;i++){
            /**
             * 从手机获取的应用基本信息
             */
            AppInfo appInfo = infos.get(i);
            appInfo.setScreen(i / screenApps);
            appInfo.setPosition(i % screenApps);
            appInfo.setItemType(ItemInfo.Type.APPLICATION);
            screenLists.get(appInfo.getScreen()).set(appInfo.getPosition(), appInfo);
            PrivacyLauncherSQLManger.getInstance().updateApplicationInfo(appInfo);
        }
        LogUtils.v(TAG,"leave fuc:[locatedApplicationByDefault]");
    }

    /**
     * 获取处理好的应用数据
     *
     * @return
     */

    public ArrayList<ArrayList<AppInfo>> obtainApps() {
        return screenLists;
    }

    public void obtainFavorites(Action1 action){
        RxJavaHelper helper = RxJavaHelper.getInstance();
        helper.setThread(Observable.just(favorites))
                .flatMapIterable(new Func1<HashMap<String, Favorite>, Iterable<?>>() {
                    @Override
                    public Iterable<?> call(HashMap<String, Favorite> maps) {
                        Collection<Favorite> fc = maps.values();
                        List<Favorite> fl = new ArrayList<>(fc);
                        Collections.sort(fl,new FavoriteComparator());
                        return fl;
                    }
                })
                .subscribe(action);

    }

    private AppEvent createAppEvent(String pck,int action){
        AppEvent appEvent = new AppEvent();
        appEvent.setPckName(pck);
        appEvent.setAction(action);
        return appEvent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null || intent.getData() == null || TextUtils.isEmpty(intent.getData().getSchemeSpecificPart())){
            LogUtils.e(TAG,"receivered a unvalidate broadcast.");
            return;
        }
        final String action = intent.getAction();
        LogUtils.v(TAG,"[onReceive] action = "+action+",packageName = "+intent.getData().getSchemeSpecificPart());
        if(action.equals(Intent.ACTION_PACKAGE_ADDED)){
            String packageName = intent.getData().getSchemeSpecificPart();
            addAppWork(createAppEvent(packageName,AppEvent.ACTION_ADD));
        }else if(action.equals(Intent.ACTION_PACKAGE_REMOVED)){
            String packageName = intent.getData().getSchemeSpecificPart();
            EventBus.getDefault().post(createAppEvent(packageName,AppEvent.ACTION_REMOVE));
        }
    }

    /**
     * 添加应用
     * @param iaEvent
     */
    private void addAppWork(final AppEvent iaEvent){
        RxJavaHelper helper = RxJavaHelper.getInstance();
        helper.setThread(Observable.create(new Observable.OnSubscribe<AppEvent>() {
            @Override
            public void call(Subscriber<? super AppEvent> subscriber) {
                try {
                    ApplicationInfo applicationInfo = mContext.getPackageManager()
                            .getApplicationInfo(iaEvent.getPckName(),0);
                    if(applicationInfo == null){
                        subscriber.onError(new Throwable("according to [packagename=" + iaEvent.getPckName()
                                + "] get appinfo is null."));
                        return;
                    }
                    AppInfo appInfo = AppUtils.getAppInfoByPck(applicationInfo);

                    ArrayList<AppInfo> lastScreenApps = screenLists.get(screenLists.size() - 1);
                    final int size = lastScreenApps.size();
                    boolean hasSpace = false;
                    for(int i = 0;i < size;i++){
                        if(lastScreenApps.get(i).getItemType().equals(ItemInfo.Type.EMPTY)){
                            hasSpace = true;
                            appInfo.setPosition(i);
                            lastScreenApps.set(i,appInfo);
                            break;
                        }
                    }
                    if(!hasSpace){
                        LogUtils.v(TAG,"[addAppWork] lastScreen has not space,should add new screen!");
                        ArrayList<AppInfo> newScreen = createSource(screenLists.size());
                        appInfo.setPosition(0);
                        newScreen.set(0,appInfo);
                        screenLists.add(newScreen);
                    }

                    //保存数据到数据库
                    appInfo.setScreen(screenLists.size() -1);
                    PrivacyLauncherSQLManger.getInstance().updateApplicationInfo(appInfo);

                    iaEvent.setScreen(screenLists.size() -1);
                    subscriber.onNext(iaEvent);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }))
                .subscribe(new Action1<AppEvent>() {
                    @Override
                    public void call(AppEvent event) {
                        event.setAction(AppEvent.ACTION_REFRESH);
                        event.setRefreshIndex(screenLists.size()-1);
                        EventBus.getDefault().post(event);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogUtils.e(TAG,"[addAppWork] : "+throwable.getMessage());
                    }
                });
    }


    /**
     * 判断当前应用是否是预置
     *
     * @param appInfo
     * @return boolean
     */
    public boolean isDefault(AppInfo appInfo) {
        if (appInfo == null) {
            return false;
        }
        String pckName = appInfo.getPckName();
        if (TextUtils.isEmpty(pckName)) {
            throw new NullPointerException("packageName is null!");
        }
        if (favorites.containsKey(pckName)) {
            return true;
        }
        return false;
    }

    /**
     * 删除卸载应用的数据
     *
     * @param mUnInstallAppEvent
     */
    public void deleteAppinfo(final AppEvent mUnInstallAppEvent) {
        LogUtils.i(TAG, "[deleteAppinfo] deleteAppinfo.PCK = " + mUnInstallAppEvent.getPckName());
        RxJavaHelper helper = RxJavaHelper.getInstance();
        helper.setThread(Observable.just(mUnInstallAppEvent))
                .map(new Func1<AppEvent, Integer>() {
                    @Override
                    public Integer call(AppEvent unInstallAppEvent) {
                        String removePck = mUnInstallAppEvent.getPckName();
                        int screens = screenLists.size();
                        int refreshIndex = -1;
                        for (int i = 0; i < screens; i++) {
                            boolean needbreak = false;
                            ArrayList<AppInfo> appInfos = screenLists.get(i);
                            int size = appInfos.size();
                            for (int j = 0; j < size; j++) {
                                AppInfo appInfo = appInfos.get(j);
                                LogUtils.i(TAG, "removePck = " + removePck+",appInfo.packageName = "+appInfo.getPckName());
                                if (removePck.equals(appInfo.getPckName())) {
                                    appInfos.remove(j);
                                    appInfos.add(createEmptyAppInfo());
                                    /**
                                     * 删除数据库中数据
                                     */
                                    PrivacyLauncherSQLManger.getInstance().deleteApplicationInfo(removePck);
                                    /**
                                     * 判断当前页面的应用个数，若为0，则删除当前页面
                                     */
                                    recursiveMove(i);
                                    needbreak = true;
                                    refreshIndex = i;
                                    break;
                                }
                            }
                            if (needbreak) {
                                break;
                            }
                        }
                        return refreshIndex;
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer result) {
                        LogUtils.d(TAG,"[onNext] Boolean  = "+result);
                        if(screenLists.size() > 1){
                            ArrayList<AppInfo> infos = screenLists.get(screenLists.size() - 1);
                            AppInfo firstInfo = infos.get(0);
                            if(firstInfo.getItemType().equals(ItemInfo.Type.EMPTY)){
                                screenLists.remove(screenLists.size() - 1);
                            }
                        }

                        mUnInstallAppEvent.setRefreshIndex(result);
                        mUnInstallAppEvent.setAction(AppEvent.ACTION_REFRESH);

                        EventBus.getDefault().post(mUnInstallAppEvent);
                    }
                });
    }

    /**
     * 下一页的应用依次往前瞬移一位，若存在下一页，说明上页肯定是满的，所以只需要把下一页的第一个app
     * 移到上一页的最后一个就可以了
     * 更新数据库中其它应用的位置信息
     *
     * @param currentPager
     */
    private void recursiveMove(int currentPager) {
        int screens = screenLists.size();
        if (currentPager == screens - 1) {//说明是在最后一页做的删除
            reSaveApp(screenLists.get(currentPager), currentPager);
        } else if (currentPager < screens - 1) {
            ArrayList<AppInfo> currentInfos = screenLists.get(currentPager);
            ArrayList<AppInfo> afterInfos = screenLists.get(currentPager + 1);
            /**
             * 下一页的第一个应用瞬移到上一页最后一位
             */
            currentInfos.set(currentInfos.size() - 1, afterInfos.get(0));
            reSaveApp(currentInfos, currentPager);
            /**
             * 下一页删除第一个应用，并在最后添加一个空的应用
             */
            afterInfos.remove(0);
            afterInfos.add(createEmptyAppInfo());

            /**
             * 判断currentPager+2是否存在，若不存在，则保存afterInfos信息，若存在，则暂时不执行保存操作
             */
            if (currentPager + 2 <= screens - 1) {
                recursiveMove(currentPager + 1);
            } else {
                reSaveApp(afterInfos, currentPager + 1);
            }
        }
    }

    /**
     * 重新保存应用的位置
     *
     * @param appInfos
     * @param currentPager
     */
    private void reSaveApp(ArrayList<AppInfo> appInfos, int currentPager) {
        for (int i = 0; i < appInfos.size(); i++) {
            AppInfo sqliteinfo = appInfos.get(i);
            sqliteinfo.setPosition(i);
            sqliteinfo.setScreen(currentPager);
            PrivacyLauncherSQLManger.getInstance().updateApplicationInfo(sqliteinfo);
        }
    }

}
