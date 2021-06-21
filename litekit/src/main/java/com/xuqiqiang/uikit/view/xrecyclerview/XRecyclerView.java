package com.xuqiqiang.uikit.view.xrecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import java.util.ArrayList;
import java.util.List;

import static com.xuqiqiang.uikit.view.xrecyclerview.BaseRefreshHeader.STATE_DONE;

public class XRecyclerView extends RecyclerView {
    private boolean isLoadingData = false;
    private boolean isNoMore = false;
    private ArrayList<View> mHeaderViews = new ArrayList<>();
    private WrapAdapter mWrapAdapter;
    private float mLastY = -1;
    private float dragRate = 3;
    private CustomFooterViewCallBack footerViewCallBack;
    private LoadingListener mLoadingListener;
    private BaseRefreshHeader mRefreshHeader;
    private boolean pullRefreshEnabled = true;
    private boolean loadingMoreEnabled = true;
    //下面的ItemViewType是保留值(ReservedItemViewType),如果用户的adapter与它们重复将会强制抛出异常。不过为了简化,我们检测到重复时对用户的提示是ItemViewType必须小于10000
    private static final int TYPE_REFRESH_HEADER = 10000;//设置一个很大的数字,尽可能避免和用户的adapter冲突
    private static final int TYPE_FOOTER = 10001;
    private static final int HEADER_INIT_INDEX = 10002;
    private static final List<Integer> sHeaderTypes = new ArrayList<>();//每个header必须有不同的type,不然滚动的时候顺序会变化

    //adapter没有数据的时候显示,类似于listView的emptyView
    private View mEmptyView;
    private View mFootView;
    private final RecyclerView.AdapterDataObserver mDataObserver = new DataObserver();
    // limit number to call load more
    // 控制多出多少条的时候调用 onLoadMore
    private int limitNumberToCallLoadMore = 1;
    private AppBarStateListener mAppBarStateListener;

    public XRecyclerView(Context context) {
        this(context, null);
    }

    public XRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * call it when you finish the activity,
     * when you call this,better don't call some kind of functions like
     * RefreshHeader,because the reference of mHeaderViews is NULL.
     */
    public void destroy(){
        if(mHeaderViews != null){
            mHeaderViews.clear();
            mHeaderViews = null;
        }
        if(mFootView instanceof BaseLoadingFooter){
            ((BaseLoadingFooter) mFootView).destroy();
            mFootView = null;
        }
        if(mRefreshHeader != null){
            mRefreshHeader.destroy();
            mRefreshHeader = null;
        }
    }

    public BaseRefreshHeader getDefaultRefreshHeaderView(){
        if(mRefreshHeader == null){
            return null;
        }
        return mRefreshHeader;
    }

    public BaseLoadingFooter getDefaultFootView(){
        if(mFootView == null){
            return null;
        }
        if(mFootView instanceof BaseLoadingFooter){
            return ((BaseLoadingFooter) mFootView);
        }
        return null;
    }

    // set the number to control call load more,see the demo on linearActivity
    public void setLimitNumberToCallLoadMore(int limitNumberToCallLoadMore) {
        this.limitNumberToCallLoadMore = limitNumberToCallLoadMore;
    }

    public View getFootView(){
        return mFootView;
    }

    public void addHeaderView(View view) {
        if(mHeaderViews == null || sHeaderTypes == null)
            return;
        sHeaderTypes.add(HEADER_INIT_INDEX + mHeaderViews.size());
        mHeaderViews.add(view);
        if (mWrapAdapter != null) {
            mWrapAdapter.notifyDataSetChanged();
        }
    }

    public void removeHeaderView(@NonNull View v){
        if(mHeaderViews == null || sHeaderTypes == null || v == null)
            return;
        for (View view : mHeaderViews) {
            if (view == v) {
                mHeaderViews.remove(view);
                break;
            }
        }
        if (mWrapAdapter != null) {
            mWrapAdapter.notifyDataSetChanged();
        }
    }

    public void removeAllHeaderView(){
        if(mHeaderViews == null || sHeaderTypes == null)
            return;
        mHeaderViews.clear();
        if (mWrapAdapter != null) {
            mWrapAdapter.notifyDataSetChanged();
        }
    }

    //根据header的ViewType判断是哪个header
    private View getHeaderViewByType(int itemType) {
        if(!isHeaderType(itemType)) {
            return null;
        }
        if(mHeaderViews == null)
            return null;
        return mHeaderViews.get(itemType - HEADER_INIT_INDEX);
    }

    //判断一个type是否为HeaderType
    private boolean isHeaderType(int itemViewType) {
        if(mHeaderViews == null || sHeaderTypes == null)
            return false;
        return mHeaderViews.size() > 0 &&  sHeaderTypes.contains(itemViewType);
    }

    //判断是否是XRecyclerView保留的itemViewType
    private boolean isReservedItemViewType(int itemViewType) {
        if(itemViewType == TYPE_REFRESH_HEADER || itemViewType == TYPE_FOOTER || sHeaderTypes.contains(itemViewType)) {
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("all")
    public void setFootView(@NonNull final View view,@NonNull CustomFooterViewCallBack footerViewCallBack) {
        if(view == null || footerViewCallBack == null){
            return;
        }
        mFootView = view;
        this.footerViewCallBack = footerViewCallBack;
    }

    public void loadMoreComplete() {
        isLoadingData = false;
        if (mFootView instanceof BaseLoadingFooter) {
            ((BaseLoadingFooter) mFootView).setState(BaseLoadingFooter.STATE_COMPLETE);
        } else {
            if(footerViewCallBack != null){
                footerViewCallBack.onLoadMoreComplete(mFootView);
            }
        }
    }

    public void setNoMore(boolean noMore){
        isLoadingData = false;
        isNoMore = noMore;
        if (mFootView instanceof BaseLoadingFooter) {
            ((BaseLoadingFooter) mFootView).setState(isNoMore ? BaseLoadingFooter.STATE_NOMORE:BaseLoadingFooter.STATE_COMPLETE);
        } else {
            if(footerViewCallBack != null){
                footerViewCallBack.onSetNoMore(mFootView,noMore);
            }
        }
    }
    public void refresh() {
        if (pullRefreshEnabled && mLoadingListener != null) {
            mRefreshHeader.setState(BaseRefreshHeader.STATE_REFRESHING);
            mLoadingListener.onRefresh();
        }
    }
    public void reset(){
        setNoMore(false);
        loadMoreComplete();
        refreshComplete();
    }

    public void refreshComplete() {
        if(mRefreshHeader != null)
            mRefreshHeader.refreshComplete();
        setNoMore(false);
    }

    public void setRefreshHeader(BaseRefreshHeader refreshHeader) {
        if (!(refreshHeader instanceof View)) {
            throw new IllegalArgumentException("refreshHeader must be view");
        }
        mRefreshHeader = refreshHeader;
    }

    public void setPullRefreshEnabled(boolean enabled) {
        pullRefreshEnabled = enabled;
    }

    public void setLoadingMoreEnabled(boolean enabled) {
        loadingMoreEnabled = enabled;
        if (!enabled) {
            if (mFootView instanceof BaseLoadingFooter) {
                ((BaseLoadingFooter)mFootView).setState(BaseLoadingFooter.STATE_COMPLETE);
            }
        }
    }

    /**
      * issues/303
      * 下拉刷新的触发距离过大，导致用户下拉刷新操作经常达不到触发点，无法刷新，必须用力向下拉一段距离才可以。
      * */
    // 设置下拉时候的偏移计量因子。y = deltaY/dragRate
    // dragRate 越大，意味着，用户要下拉滑动更久来触发下拉刷新。相反越小，就越短距离
    public void setDragRate(float rate){
        if(rate <= 0.5){
            return;
        }
        dragRate = rate;
    }

    // if you can't sure that you are 100% going to
    // have no data load back from server anymore,do not use this
    @Deprecated
    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        mDataObserver.onChanged();
    }

    @Deprecated
    public View getEmptyView() {
        return mEmptyView;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mWrapAdapter = new WrapAdapter(adapter);
        super.setAdapter(mWrapAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
    }

    //避免用户自己调用getAdapter() 引起的ClassCastException
    @Override
    public Adapter getAdapter() {
        if(mWrapAdapter != null)
            return mWrapAdapter.getOriginalAdapter();
        else
            return null;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if(mWrapAdapter != null){
            if (layout instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) layout);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (mWrapAdapter.isHeader(position) || mWrapAdapter.isFooter(position) || mWrapAdapter.isRefreshHeader(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
        }
    }

    /**
     * 解决和CollapsingToolbarLayout冲突的问题
     */
    public void setAppBarStateListener(AppBarStateListener appBarStateListener) {
        mAppBarStateListener = appBarStateListener;
    }

    /** ===================== try to adjust the position for XR when you call those functions below ====================== */
    // which cause "Called attach on a child which is not detached" exception info.
    // {reason analyze @link:http://www.cnblogs.com/linguanh/p/5348510.html}
    // by lgh on 2017-11-13 23:55

    // example: listData.remove(position); You can also see a demo on LinearActivity
    public<T> void notifyItemRemoved(List<T> listData,int position) {
        if(mWrapAdapter.adapter == null)
            return;
        int headerSize = getHeaders_includingRefreshCount();
        int adjPos = position + headerSize;
        mWrapAdapter.adapter.notifyItemRemoved(adjPos);
        mWrapAdapter.adapter.notifyItemRangeChanged(headerSize, listData.size(),new Object());
    }

    public<T> void notifyItemInserted(int position) {
        if(mWrapAdapter.adapter == null)
            return;
        int headerSize = getHeaders_includingRefreshCount();
        int adjPos = position + headerSize;
        mWrapAdapter.adapter.notifyItemInserted(adjPos);
    }

    public<T> void notifyItemInserted(List<T> listData,int position) {
        if(mWrapAdapter.adapter == null)
            return;
        int headerSize = getHeaders_includingRefreshCount();
        int adjPos = position + headerSize;
        mWrapAdapter.adapter.notifyItemInserted(adjPos);
        mWrapAdapter.adapter.notifyItemRangeChanged(headerSize, listData.size(),new Object());
    }

    public void notifyItemChanged(int position) {
        if(mWrapAdapter.adapter == null)
            return;
        int adjPos = position + getHeaders_includingRefreshCount();
        mWrapAdapter.adapter.notifyItemChanged(adjPos);
    }

    public void notifyItemChanged(int position,Object o) {
        if(mWrapAdapter.adapter == null)
            return;
        int adjPos = position + getHeaders_includingRefreshCount();
        mWrapAdapter.adapter.notifyItemChanged(adjPos,o);
    }

    private int getHeaders_includingRefreshCount(){
        if(mWrapAdapter == null)
            return 0;
        return mWrapAdapter.getHeadersCount()+1;
    }

    /** ======================================================= end ======================================================= */

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadingListener != null && !isLoadingData && loadingMoreEnabled) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            int adjAdapterItemCount = layoutManager.getItemCount()+getHeaders_includingRefreshCount();
            //Log.e("aaaaa","adjAdapterItemCount "+adjAdapterItemCount +" getItemCount "+layoutManager.getItemCount());

            int status = STATE_DONE;

            if(mRefreshHeader != null)
                status = mRefreshHeader.getState();
            if (
                    layoutManager.getChildCount() > 0
                    && lastVisibleItemPosition >= adjAdapterItemCount - limitNumberToCallLoadMore
                    && adjAdapterItemCount >= layoutManager.getChildCount()
                    && !isNoMore
                    && status < BaseRefreshHeader.STATE_REFRESHING
            )
            {
                isLoadingData = true;
                if (mFootView instanceof BaseLoadingFooter) {
                    ((BaseLoadingFooter) mFootView).setState(BaseLoadingFooter.STATE_LOADING);
                } else {
                    if(footerViewCallBack != null){
                        footerViewCallBack.onLoadingMore(mFootView);
                    }
                }
                mLoadingListener.onLoadMore();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (isOnTop() && pullRefreshEnabled) {
                    if (mAppBarStateListener != null && !mAppBarStateListener.isExpanded())
                        return super.onTouchEvent(ev);
                    if(mRefreshHeader == null)
                        break;
                    mRefreshHeader.onMove(deltaY / dragRate);
                    if (mRefreshHeader.getVisibleHeight() > 0 && mRefreshHeader.getState() < BaseRefreshHeader.STATE_REFRESHING) {
                        return false;
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && pullRefreshEnabled) {
                    if (mAppBarStateListener != null && !mAppBarStateListener.isExpanded())
                        return super.onTouchEvent(ev);
                    if (mRefreshHeader != null && mRefreshHeader.releaseAction()) {
                        if (mLoadingListener != null) {
                            mLoadingListener.onRefresh();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private boolean isOnTop() {
        if(mRefreshHeader == null)
            return false;
        if (((View) mRefreshHeader).getParent() != null) {
            return true;
        } else {
            return false;
        }
    }

    private class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyDataSetChanged();
            }
            if (mWrapAdapter != null && mEmptyView != null) {
                int emptyCount = 1 + mWrapAdapter.getHeadersCount();
                if (loadingMoreEnabled) {
                    emptyCount++;
                }
                if (mWrapAdapter.getItemCount() == emptyCount) {
                    mEmptyView.setVisibility(View.VISIBLE);
                    XRecyclerView.this.setVisibility(View.GONE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                    XRecyclerView.this.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    };

    private class WrapAdapter extends RecyclerView.Adapter<ViewHolder> {

        private RecyclerView.Adapter adapter;

        public WrapAdapter(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        public RecyclerView.Adapter getOriginalAdapter(){
            return this.adapter;
        }

        public boolean isHeader(int position) {
            if(mHeaderViews == null)
                return false;
            return position >= 1 && position < mHeaderViews.size() + 1;
        }

        public boolean isFooter(int position) {
            if(loadingMoreEnabled) {
                return position == getItemCount() - 1;
            }else {
                return false;
            }
        }

        public boolean isRefreshHeader(int position) {
            return position == 0;
        }

        public int getHeadersCount() {
            if(mHeaderViews == null)
                return 0;
            return mHeaderViews.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_REFRESH_HEADER) {
                return new SimpleViewHolder((View) mRefreshHeader);
            } else if (isHeaderType(viewType)) {
                return new SimpleViewHolder(getHeaderViewByType(viewType));
            } else if (viewType == TYPE_FOOTER) {
                return new SimpleViewHolder(mFootView);
            }
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (isHeader(position) || isRefreshHeader(position)) {
                return;
            }
            int adjPosition = position - (getHeadersCount() + 1);
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    adapter.onBindViewHolder(holder, adjPosition);
                }
            }
        }

        // some times we need to override this
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position,List<Object> payloads) {
            if (isHeader(position) || isRefreshHeader(position)) {
                return;
            }

            int adjPosition = position - (getHeadersCount() + 1);
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    if(payloads.isEmpty()){
                        adapter.onBindViewHolder(holder, adjPosition);
                    }
                    else{
                        adapter.onBindViewHolder(holder, adjPosition,payloads);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            int adjLen = (loadingMoreEnabled?2:1);
            if (adapter != null) {
                return getHeadersCount() + adapter.getItemCount() + adjLen;
            } else {
                return getHeadersCount() + adjLen;
            }
        }

        @Override
        public int getItemViewType(int position) {
            int adjPosition = position - (getHeadersCount() + 1);
            if (isRefreshHeader(position)) {
                return TYPE_REFRESH_HEADER;
            }
            if (isHeader(position)) {
                position = position - 1;
                return sHeaderTypes.get(position);
            }
            if (isFooter(position)) {
                return TYPE_FOOTER;
            }
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (adjPosition < adapterCount) {
                    int type =  adapter.getItemViewType(adjPosition);
                    if(isReservedItemViewType(type)) {
                        throw new IllegalStateException("XRecyclerView require itemViewType in adapter should be less than 10000 " );
                    }
                    return type;
                }
            }
            return 0;
        }

        @Override
        public long getItemId(int position) {
            if (adapter != null && position >= getHeadersCount() + 1) {
                int adjPosition = position - (getHeadersCount() + 1);
                if (adjPosition < adapter.getItemCount()) {
                    return adapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (isHeader(position) || isFooter(position) || isRefreshHeader(position))
                                ? gridManager.getSpanCount() : 1;
                    }
                });
            }
            adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            adapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null
                    && lp instanceof StaggeredGridLayoutManager.LayoutParams
                    && (isHeader(holder.getLayoutPosition()) ||isRefreshHeader(holder.getLayoutPosition()) || isFooter(holder.getLayoutPosition()))) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
            adapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            adapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
            return adapter.onFailedToRecycleView(holder);
        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            adapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            adapter.registerAdapterDataObserver(observer);
        }

        private class SimpleViewHolder extends RecyclerView.ViewHolder {
            public SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    public interface LoadingListener {

        void onRefresh();

        void onLoadMore();
    }

    /** add by LinGuanHong below */
    private int scrollDyCounter = 0;

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);
        /** if we scroll to position 0, the scrollDyCounter should be reset */
        if(position == 0){
            scrollDyCounter = 0;
        }
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        if(scrollAlphaChangeListener == null){
            return;
        }
        int height = scrollAlphaChangeListener.setLimitHeight();
        scrollDyCounter = scrollDyCounter + dy;
        if (scrollDyCounter <= 0) {
            scrollAlphaChangeListener.onAlphaChange(0);
        }else if(scrollDyCounter <= height && scrollDyCounter > 0){
            float scale = (float) scrollDyCounter / height; /** 255/height = x/255 */
            float alpha = (255 * scale);
            scrollAlphaChangeListener.onAlphaChange((int) alpha);
        }else {
            scrollAlphaChangeListener.onAlphaChange(255);
        }
    }

    private ScrollAlphaChangeListener scrollAlphaChangeListener;
    public void setScrollAlphaChangeListener(
            ScrollAlphaChangeListener scrollAlphaChangeListener){
        this.scrollAlphaChangeListener = scrollAlphaChangeListener;
    }
    public interface ScrollAlphaChangeListener{
        void onAlphaChange(int alpha);  /** you can handle the alpha insert it */
        int setLimitHeight(); /** set a height for the begging of the alpha start to change */
    }
}