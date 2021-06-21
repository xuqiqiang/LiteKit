package com.xuqiqiang.uikit.view.adapter;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.view.xrecyclerview.XRecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuqiqiang on 2021/06/10.
 */
public abstract class AnimatorAdapter<E>
    extends RecyclerView.Adapter<AnimatorAdapter.ViewHolder<E>> {

    private static final long ANIM_ADD_DURING = 400;
    private static final long ANIM_MOVE_DURING = 400;
    private final RecyclerView mRecyclerView;
    @NonNull
    protected final List<E> mList = new ArrayList<>();
    private OnItemClickListener<E> mOnItemClickListener;
    private long mAddTime;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    public AnimatorAdapter(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setAdapter(this);
        RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
        if (itemAnimator != null) {
            itemAnimator.setAddDuration(animAddDuring());
            itemAnimator.setChangeDuration(animMoveDuring());
            itemAnimator.setMoveDuration(animMoveDuring());
            itemAnimator.setRemoveDuration(animAddDuring());
        }
    }

    public void setOnItemClickListener(OnItemClickListener<E> listener) {
        this.mOnItemClickListener = listener;
    }

    protected long animAddDuring() {
        return ANIM_ADD_DURING;
    }

    protected long animMoveDuring() {
        return ANIM_MOVE_DURING;
    }

    @NonNull
    @Override
    public AnimatorAdapter.ViewHolder<E> onCreateViewHolder(@NonNull ViewGroup parent,
        int viewType) {
        int layoutId = viewHolderId(parent, viewType);//R.layout.list_item_device;
        View view = LayoutInflater.from(mRecyclerView.getContext())
            .inflate(layoutId, parent, false);
        return new AnimatorAdapter.ViewHolder<E>(view).setAdapter(this);
    }

    protected abstract int viewHolderId(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull AnimatorAdapter.ViewHolder<E> holder, int position) {
        E e = mList.get(position);
        convert(holder, position, e);
        holder.itemView.setTag(e);
    }

    /**
     * Implement this method and use the holder to adapt the view to the given item.
     *
     * @param holder A fully initialized holder.
     * @param item   The item that needs to be displayed.
     */
    protected abstract void convert(@NonNull AnimatorAdapter.ViewHolder<E> holder, int position,
        E item);

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setItems(List<E> list) {
        mList.clear();
        if (!ArrayUtils.isEmpty(list)) {
            mList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void addItem(final int i, final E e) {
        long now = System.currentTimeMillis();
        long during = animAddDuring() + 30;
        if (now - mAddTime < during) {
            mAddTime += during;
            mMainHandler.postDelayed(new Runnable() {
                @Override public void run() {
                    _addItem(i, e);
                }
            }, mAddTime - now);
            return;
        }

        if (mAddTime < now) {
            mAddTime = now;
        }

        _addItem(i, e);
    }

    private void _addItem(int i, E e) {
        if (i < 0) i = 0;
        if (i > mList.size()) i = mList.size();
        mList.add(i, e);
        if (mRecyclerView instanceof XRecyclerView) {
            ((XRecyclerView) mRecyclerView).notifyItemInserted(i);
        } else {
            notifyItemInserted(i);
        }
    }

    public interface OnItemClickListener<E> {
        void onItemClick(E e, int position);
    }

    public static class ViewHolder<E> extends RecyclerView.ViewHolder {
        /**
         * Views indexed with their IDs
         */
        private final SparseArray<View> views;
        private AnimatorAdapter<E> adapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.views = new SparseArray<>();
            itemView.setTag(-1);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onItemClick();
                }
            });
        }

        @SuppressWarnings("unchecked")
        protected void onItemClick() {
            int position = getItemPosition();
            if (adapter.mOnItemClickListener != null) {
                E e = (E) itemView.getTag();
                if (e == null) e = adapter.mList.get(position);
                adapter.mOnItemClickListener.onItemClick(e, position);
            }
        }

        public int getItemPosition() {
            int position = getAdapterPosition();
            if (adapter.mRecyclerView instanceof XRecyclerView) position -= 1;
            if (position < 0) position = 0;
            if (position >= adapter.getItemCount()) position = adapter.getItemCount() - 1;
            return position;
        }

        @SuppressWarnings("unchecked")
        public <T extends View> T getView(@IdRes int viewId) {
            View view = views.get(viewId);
            if (view == null) {
                view = itemView.findViewById(viewId);
                views.put(viewId, view);
            }
            return (T) view;
        }

        public void setText(@IdRes int viewId, CharSequence text) {
            TextView textView = getView(viewId);
            if (textView != null) {
                textView.setText(text);
            }
        }

        public void setImageResource(@IdRes int viewId, @DrawableRes int resId) {
            ImageView textView = getView(viewId);
            if (textView != null) {
                textView.setImageResource(resId);
            }
        }

        protected ViewHolder<E> setAdapter(AnimatorAdapter<E> adapter) {
            this.adapter = adapter;
            return this;
        }
    }
}