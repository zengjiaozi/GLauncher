package com.godinsec.privacy.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import com.godinsec.privacy.LauncherApplication;
import com.godinsec.privacy.adapter.DragAdapter;
import com.godinsec.privacy.adapter.DragGridBaseAdapter;
import com.godinsec.privacy.bean.AppInfo;
import com.godinsec.privacy.bean.ItemInfo;
import com.godinsec.privacy.bean.Request;
import com.godinsec.privacy.bean.Upload;
import com.godinsec.privacy.controller.CellItemAnimController;
import com.godinsec.privacy.controller.DeleteTargetController;
import com.godinsec.privacy.net.RetrofitManger;
import com.godinsec.privacy.utils.AppUtils;
import com.godinsec.privacy.utils.Configure;
import com.godinsec.privacy.utils.DeviceUtils;
import com.godinsec.privacy.utils.LogUtils;
import com.godinsec.privacy.utils.RxJavaHelper;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class CellLayout extends GridView implements android.widget.AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{

    private static final String TAG = "CellLayout";

    private static final long LONG_PRESS_TIMEOUT = 200;

    /**
     * 是否可以拖拽，默认不可以
     */
    public static boolean isDrag = false;

    private int mDownX;
    private int mDownY;
    private int moveY;
    /**
     * 正在拖拽的position
     */
    private int mDragPosition;

    /**
     * 刚开始拖拽的item对应的View
     */
    private View mStartDragItemView = null;

    /**
     * 用于拖拽的镜像，这里直接用一个ImageView
     */
    private ImageView mDragImageView;

    /**
     * 震动器
     */
    private Vibrator mVibrator;

    private WindowManager mWindowManager;
    /**
     * item镜像的布局参数
     */
    private WindowManager.LayoutParams mWindowLayoutParams;

    /**
     * 按下的点到所在item的上边缘的距离
     */
    private int mPoint2ItemTop ;

    /**
     * 按下的点到所在item的左边缘的距离
     */
    private int mPoint2ItemLeft;

    /**
     * DragGridView距离屏幕顶部的偏移量
     */
    private int mOffset2Top;

    /**
     * DragGridView距离屏幕左边的偏移量
     */
    private int mOffset2Left;

    /**
     * 状态栏的高度
     */
    private int mStatusHeight;

    /**
     * DragGridView自动向下滚动的边界值
     */
    private int mDownScrollBorder;

    /**
     * DragGridView自动向上滚动的边界值
     */
    private int mUpScrollBorder;

    /**
     * DragGridView自动滚动的速度
     */
    private static final int speed = 20;

    private DragAdapter mDragAdapter;
    private int mNumColumns;
    private int mColumnWidth;
    private boolean mNumColumnsSet;
    private int mHorizontalSpacing;

    private Handler mHandler = new Handler();

    private DeleteTargetController mDeleteTargetController;

    private CellItemAnimController mCellAnimController;

    private WorkSpace mWorkSpace;

    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mStatusHeight = AppUtils.getStatusHeight(context); //获取状态栏的高度

        if(!mNumColumnsSet){
            mNumColumns = AUTO_FIT;
        }
        this.setOnItemClickListener(this);
        this.setOnItemLongClickListener(this);
        this.mCellAnimController = new CellItemAnimController(this);
    }

    public void setWorkSpace(WorkSpace mWorkSpace){
        this.mWorkSpace = mWorkSpace;
    }


    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);

        if(adapter instanceof DragAdapter){
            mDragAdapter = (DragAdapter) adapter;
        }else{
            throw new IllegalStateException("the adapter must be implements DragGridAdapter");
        }
    }

    public DragAdapter getDragAdapter(){
        return mDragAdapter;
    }


    @Override
    public void setNumColumns(int numColumns) {
        super.setNumColumns(numColumns);
        mNumColumnsSet = true;
        this.mNumColumns = numColumns;
    }


    @Override
    public void setColumnWidth(int columnWidth) {
        super.setColumnWidth(columnWidth);
        mColumnWidth = columnWidth;
    }


    @Override
    public void setHorizontalSpacing(int horizontalSpacing) {
        super.setHorizontalSpacing(horizontalSpacing);
        this.mHorizontalSpacing = horizontalSpacing;
    }

    /**
     * 若设置为AUTO_FIT，计算有多少列
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mNumColumns == AUTO_FIT) {
            int numFittedColumns;
            if (mColumnWidth > 0) {
                int gridWidth = Math.max(MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()
                        - getPaddingRight(), 0);
                numFittedColumns = gridWidth / mColumnWidth;
                if (numFittedColumns > 0) {
                    while (numFittedColumns != 1) {
                        if (numFittedColumns * mColumnWidth + (numFittedColumns - 1)
                                * mHorizontalSpacing > gridWidth) {
                            numFittedColumns--;
                        } else {
                            break;
                        }
                    }
                } else {
                    numFittedColumns = 1;
                }
            } else {
                numFittedColumns = 2;
            }
            mNumColumns = numFittedColumns;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取DragGridView自动向上滚动的偏移量，小于这个值，DragGridView向下滚动
        mDownScrollBorder = getHeight() / 5;
        //获取DragGridView自动向下滚动的偏移量，大于这个值，DragGridView向上滚动
        mUpScrollBorder = getHeight() * 4/5;


    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setNumColumns(Configure.currentColumn);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        LogUtils.v(TAG, "[dispatchTouchEvent] start!");
        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                LogUtils.v(TAG,"[dispatchTouchEvent] MotionEvent.ACTION_DOWN!");
                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();
                mOffset2Top = (int) (ev.getRawY() - mDownY);
                mOffset2Left = (int) (ev.getRawX() - mDownX);

                /**根据按下的X,Y坐标获取点击的item的position**/
                mDragPosition = pointToPosition(mDownX, mDownY);

                if(mDragPosition == AdapterView.INVALID_POSITION){
                    LogUtils.e(TAG,"[dispatchTouchEvent] mDragPosition == AdapterView.INVALID_POSITION");
                    return super.dispatchTouchEvent(ev);
                }

                LogUtils.v(TAG,"[mLongClickRunnable] mDragPosition = "+mDragPosition);
                //根据position获取该item所对应的View
                mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());

                mHandler.postDelayed(mLongClickRunnable,LONG_PRESS_TIMEOUT);

                break;
            case MotionEvent.ACTION_MOVE:
                LogUtils.v(TAG,"[dispatchTouchEvent] MotionEvent.ACTION_MOVE!");
                int moveX = (int)ev.getX();
                int moveY = (int) ev.getY();
                //如果我们在按下的item上面移动，只要不超过item的边界我们就不移除mRunnable
                if(!isTouchInItem(mStartDragItemView, moveX, moveY)){
                    LogUtils.v(TAG,"[dispatchTouchEvent] remove mLongClickRunnable!");
                    mHandler.removeCallbacks(mLongClickRunnable);
                }
                break;
            case MotionEvent.ACTION_UP:
                LogUtils.v(TAG,"[dispatchTouchEvent] MotionEvent.ACTION_UP!");
                mHandler.removeCallbacks(mLongClickRunnable);
                mHandler.removeCallbacks(mScrollRunnable);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    /**
     * 是否点击在GridView的item上面
     * @param dragView
     * @param x
     * @param y
     * @return
     */
    private boolean isTouchInItem(View dragView, int x, int y){
        if(dragView == null){
            return false;
        }
        int leftOffset = dragView.getLeft();
        int topOffset = dragView.getTop();
        if(x < leftOffset || x > leftOffset + dragView.getWidth()){
            return false;
        }

        if(y < topOffset || y > topOffset + dragView.getHeight()){
            return false;
        }

        return true;
    }



    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean returnValue = super.onTouchEvent(ev);
        int action = ev.getAction();
        LogUtils.d(TAG,"[onTouchEvent] returnValue = "+returnValue+",action = "+action);
        if(CellLayout.isDrag && mDragImageView != null){
            int moveX = (int) ev.getX();
            moveY = (int) ev.getY();
            if(mDeleteTargetController != null){
                mDeleteTargetController.onTouchEvent(ev);
            }
            switch(action){
                case MotionEvent.ACTION_MOVE:
                    //拖动item
                    onDragItem(moveX, moveY);
                    break;
                case MotionEvent.ACTION_UP:
                    mDragAdapter.notifyDataSetChanged();
                    onStopDrag();
                    if(!mDeleteTargetController.isDelete()){
                        pointToPositionOnSwapItem(moveX, moveY);
                    }
                    break;
            }
            returnValue = true;
        }

        if(mStartDragItemView instanceof CellItemView){
            ((CellItemView) mStartDragItemView).handlerOnTouchEvent(ev);
        }

        if(action == MotionEvent.ACTION_UP){
            CellLayout.isDrag = false;
            mStartDragItemView = null;
        }

        return returnValue;
    }


    /**
     * 创建拖动的镜像
     * @param bitmap
     * @param downX
     * 			按下的点相对父控件的X坐标
     * @param downY
     * 			按下的点相对父控件的X坐标
     */
    @SuppressLint("RtlHardcoded")
    private void createDragImage(Bitmap bitmap, int downX , int downY){
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; //图片之外的其他地方透明
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
        mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        mWindowLayoutParams.alpha = 0.55f; //透明度
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE ;

        mDragImageView = new ImageView(getContext());
        mDragImageView.setImageBitmap(bitmap);
        mWindowManager.addView(mDragImageView, mWindowLayoutParams);
    }

    /**
     * 从界面上面移动拖动镜像
     */
    private void removeDragImage(){
        if(mDragImageView != null){
            mWindowManager.removeView(mDragImageView);
            mDragImageView = null;
        }
    }

    /**
     * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
     * @param moveX
     * @param moveY
     */
    private void onDragItem(int moveX, int moveY){
        mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
        mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); //更新镜像的位置
        //GridView自动滚动
        mHandler.post(mScrollRunnable);
    }

    /**
     * 交换item,并且控制item之间的显示与隐藏效果
     * @param moveX
     * @param moveY
     */
    private void pointToPositionOnSwapItem(int moveX, int moveY){
        //获取我们手指移动到的那个item的position
        final int tempPosition = pointToPosition(moveX, moveY);
        onSwapItem(mDragPosition,tempPosition,false);
    }


    public  void onSwapItem(final int oldPosition,final int newPosition,final boolean delete){
        //假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
        if(delete){
            mDragAdapter.setLastDoState(DragGridBaseAdapter.DOSTATE_DELETE);
        }else{
            mDragAdapter.setLastDoState(DragGridBaseAdapter.DOSTATE_SWAP);
        }
        if(delete && oldPosition == newPosition && mDeleteTargetController != null){
            mDeleteTargetController.finishDelete(newPosition);
            return;
        }
        if(oldPosition != newPosition && newPosition != AdapterView.INVALID_POSITION ){
            if(mDragAdapter.reorderItems(oldPosition, newPosition,delete)){
                mDragAdapter.setHideItem(newPosition);
                final ViewTreeObserver observer = getViewTreeObserver();
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        observer.removeOnPreDrawListener(this);
                        mCellAnimController.animateReorder(oldPosition, newPosition,delete);
                        mDragPosition = newPosition;
                        return true;
                    }
                } );
            }
        }
    }


    /**
     * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
     */
    private void onStopDrag(){
        View view = getChildAt(mDragPosition - getFirstVisiblePosition());
        if(view != null && mDeleteTargetController != null && !mDeleteTargetController.isDelete()){
            view.setVisibility(View.VISIBLE);
            LogUtils.i(TAG, "[onStopDrag] show view!");
        }
        removeDragImage();
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppInfo appInfo = (AppInfo) mDragAdapter.getItem(position);
        if (appInfo.getItemType().equals(ItemInfo.Type.EMPTY)) {
            return;
        }

        LauncherApplication.getInstance().startActivity(appInfo.getIntent());

        RxJavaHelper.getInstance().setThread(Observable.just(appInfo))
                .map(new Func1<AppInfo, Upload>() {
                    @Override
                    public Upload call(AppInfo appInfo) {
                        Request request = new Request();

                        request.setHead(new Request.Head("android","4"));
                        request.setBody(new Request.Body(DeviceUtils.getDeviceIMEI(getContext()),
                                appInfo.getPckName(),appInfo.getTitle().toString()));
                        return new Upload(request);
                    }
                })
                .subscribe(new Action1<Upload>() {
                    @Override
                    public void call(Upload upload) {
                        RetrofitManger.getInstance().postClickAppInfo(upload);
                    }
                });

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return true;
    }

    //用来处理是否为长按的Runnable
    private Runnable mLongClickRunnable = new Runnable() {

        @Override
        public void run() {
            LogUtils.d(TAG, "[mLongClickRunnable]long pressed！");
            if(mWorkSpace == null || mWorkSpace.isBeingDragged()){
                LogUtils.v(TAG,"[mLongClickRunnable]workSpace is beingDragged!");
                mHandler.removeCallbacks(this);
                return;
            }

            CellLayout.isDrag = true; //设置可以拖拽

            AppInfo appInfo = (AppInfo) mDragAdapter.getItem(mDragPosition);
            if(appInfo.getItemType().equals(ItemInfo.Type.EMPTY)){
                mHandler.removeCallbacks(this);
                return;
            }
            if(mDeleteTargetController != null){
                mDeleteTargetController.onItemLongClick(appInfo);
            }
            mVibrator.vibrate(50); //震动一下
            mStartDragItemView.setVisibility(View.INVISIBLE);//隐藏该item
            mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
            mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();
            //开启mDragItemView绘图缓存
            mStartDragItemView.setDrawingCacheEnabled(true);
            //获取mDragItemView在缓存中的Bitmap对象
            Bitmap mDragBitmap = Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
            //这一步很关键，释放绘图缓存，避免出现重复的镜像
            mStartDragItemView.destroyDrawingCache();
            //根据我们按下的点显示item镜像
            createDragImage(mDragBitmap, mDownX, mDownY);
            mHandler.removeCallbacks(this);
        }
    };

    /**
     * 当moveY的值大于向上滚动的边界值，触发GridView自动向上滚动
     * 当moveY的值小于向下滚动的边界值，触发GridView自动向下滚动
     * 否则不进行滚动
     */
    private Runnable mScrollRunnable = new Runnable() {

        @Override
        public void run() {
            int scrollY;
            if(getFirstVisiblePosition() == 0 || getLastVisiblePosition() == getCount() - 1){
                mHandler.removeCallbacks(mScrollRunnable);
            }

            if(moveY > mUpScrollBorder){
                scrollY = speed;
                mHandler.postDelayed(mScrollRunnable, 25);
            }else if(moveY < mDownScrollBorder){
                scrollY = -speed;
                mHandler.postDelayed(mScrollRunnable, 25);
            }else{
                scrollY = 0;
                mHandler.removeCallbacks(mScrollRunnable);
            }
            smoothScrollBy(scrollY, 10);
        }
    };

    public void setDeleteTargetController(DeleteTargetController controller){
        this.mDeleteTargetController = controller;
    }

    public DeleteTargetController getDeleteTargetController(){
        return this.mDeleteTargetController;
    }
}

