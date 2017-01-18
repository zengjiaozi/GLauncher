package com.godinsec.privacy.launcher;

import android.graphics.drawable.Animatable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.godinsec.launcher.R;
import com.godinsec.privacy.adapter.DragAdapter;
import com.godinsec.privacy.adapter.WorkSpaceAdapter;
import com.godinsec.privacy.base.BaseActivity;
import com.godinsec.privacy.bean.AppEvent;
import com.godinsec.privacy.bean.AppInfo;
import com.godinsec.privacy.bean.Favorite;
import com.godinsec.privacy.contract.LauncherContract;
import com.godinsec.privacy.contract.LauncherPresenterImpl;
import com.godinsec.privacy.controller.AppDeleteCallback;
import com.godinsec.privacy.controller.DeleteTargetController;
import com.godinsec.privacy.controller.ScreenChangeListener;
import com.godinsec.privacy.utils.LogUtils;
import com.godinsec.privacy.widget.CellLayout;
import com.godinsec.privacy.widget.HotSetView;
import com.godinsec.privacy.widget.PageIndicator;
import com.godinsec.privacy.widget.WorkSpace;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;

public class LauncherActivity extends BaseActivity<LauncherContract.LauncherView,LauncherPresenterImpl>
        implements AppDeleteCallback,LauncherContract.LauncherView {

    private static final String TAG = "LauncherActivity";

    @BindView(R.id.workSpace)
    WorkSpace workSpace;

    @BindView(R.id.main_contanetView)
    LinearLayout mainContentView;

    @BindView(R.id.loading)
    ImageView loading;

    @BindView(R.id.pageIndicator)
    PageIndicator mPageIndicator;

    @BindView(R.id.deleteContainer)
    RelativeLayout deleteContainer;

    @BindView(R.id.hotSetContainer)
    LinearLayout hotSetContainer;

    private ArrayList<ArrayList<AppInfo>> screenAppDatas;

    private int screenCount;

    private List<View> cellLayouts = new ArrayList<>();

    private WorkSpaceAdapter mWorkSpaceAdapter;

    private boolean isLoadingApp = false;

    private DeleteTargetController mDeleteTargetController;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_launcher;
    }

    @Override
    public LauncherPresenterImpl initPresenter() {
        return new LauncherPresenterImpl();
    }

    @Override
    protected void doWork() {
        mDeleteTargetController = new DeleteTargetController(this);
        mDeleteTargetController.setAppDoCallback(this);
        mDeleteTargetController.setDeleteContainer(deleteContainer);
        presenter.setDeleteTargetController(mDeleteTargetController);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(screenCount <= 0 && !isLoadingApp){
            presenter.doLoad();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDeleteTargetController != null) {
            mDeleteTargetController.destroy();
            mDeleteTargetController = null;
        }
    }

    /**
     * 接收安装或卸载之后的消息刷新界面,eventBus 发送
     *
     */
    public void onEventMainThread(AppEvent appEvent) {
        switch (appEvent.getAction()){
            case AppEvent.ACTION_ADD:
                break;
            case AppEvent.ACTION_REMOVE:
                presenter.doDeleta(appEvent,false);
                break;
            case AppEvent.ACTION_REFRESH:
                refreshGUI();
                break;
        }

    }

    @Override
    public void startDelete(int deletePosition) {
        CellLayout current = (CellLayout) cellLayouts.get(workSpace.getCurrentItem()).findViewById(R.id.dragGridView);
        current.onSwapItem(deletePosition, screenAppDatas.get(workSpace.getCurrentItem()).size() - 1, true);
    }

    @Override
    public void finishDelete(int deletePosition) {
        List<AppInfo> infos = screenAppDatas.get(workSpace.getCurrentItem());
        AppEvent event = new AppEvent();
        event.setPckName(infos.get(deletePosition).getPckName());
        presenter.doDeleta(event,true);
    }

    @Override
    public void onPreLoad() {
        isLoadingApp = true;
        loading.setVisibility(View.VISIBLE);
        mainContentView.setVisibility(View.GONE);
        ((Animatable)loading.getDrawable()).start();
    }

    @Override
    public void onFinishLoad() {
        ((Animatable)loading.getDrawable()).stop();
        loading.setVisibility(View.GONE);
        isLoadingApp = false;
        mainContentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setWorkSpaceDatas(ArrayList<ArrayList<AppInfo>> datas) {
        this.screenAppDatas = datas;
        screenCount = screenAppDatas.size();
        mPageIndicator.setCount(screenCount);

        bindDataToWorkSpace();

        presenter.doFavorite();
    }

    @Override
    public void addFavorite(Favorite favorite) {
        LogUtils.i(TAG,"[bindDataToWorkSpace.Action1] pckName = "+favorite.getPckName()
                +",position = "+favorite.getPosition()+",isHotSetView = "+favorite.isHotSetView());
        if(favorite.isHotSetView()){
            mLayoutInflater.inflate(R.layout.hosetview,hotSetContainer);
            final int childCount = hotSetContainer.getChildCount();
            HotSetView hotSetView = (HotSetView) hotSetContainer.getChildAt(childCount-1);
            hotSetView.setFavorite(favorite);
        }
    }

    /**
     * 刷新界面
     */
    private void refreshGUI() {
        LogUtils.d(TAG, "[refreshGUI] application changed,refreshGUI:screenCount = "+screenCount
                +",screenAppDatas.size = "+screenAppDatas.size());
        if (screenCount < screenAppDatas.size()) {
            /**增加新的一屏**/
            createEveryScreen(screenAppDatas.get(screenAppDatas.size() -1));
            mPageIndicator.setCount(screenAppDatas.size());
        } else if (screenCount > screenAppDatas.size()){
            cellLayouts.remove(screenAppDatas.size());
            mWorkSpaceAdapter.notifyDataSetChanged();
            mPageIndicator.setCount(screenAppDatas.size());
        }
        screenCount = screenAppDatas.size();
        mWorkSpaceAdapter.notifyDataSetChanged();
        ((CellLayout) cellLayouts.get(workSpace.getCurrentItem())
                .findViewById(R.id.dragGridView)).getDragAdapter().setHideItem(-1);
    }

    /**
     * 绑定数据到界面显示
     */
    private void bindDataToWorkSpace(){

        for(int i = 0; i< screenCount; i++){
            createEveryScreen(screenAppDatas.get(i));
        }
        mWorkSpaceAdapter = new WorkSpaceAdapter(cellLayouts);
        workSpace.setAdapter(mWorkSpaceAdapter);
        workSpace.addOnPageChangeListener(new ScreenChangeListener(mPageIndicator));
    }

    /**
     * 生成每一屏
     *
     * @param list
     */
    private void createEveryScreen(List<AppInfo> list) {
        View container = mLayoutInflater.inflate(R.layout.screen_layout, null);
        CellLayout cellLayout = (CellLayout) container.findViewById(R.id.dragGridView);
        final DragAdapter mDragAdapter = new DragAdapter(this,list);
        cellLayout.setAdapter(mDragAdapter);
        cellLayout.setDeleteTargetController(mDeleteTargetController);
        cellLayout.setWorkSpace(workSpace);
        cellLayouts.add(container);
    }

}
