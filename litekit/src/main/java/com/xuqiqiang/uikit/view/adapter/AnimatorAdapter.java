package com.xuqiqiang.uikit.view.adapter;

import android.os.Handler;
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

public abstract class AnimatorAdapter<E>
    extends RecyclerView.Adapter<AnimatorAdapter.ViewHolder<E>> {

    private static final int ANIM_ADD_DURING = 400;
    private static final int ANIM_MOVE_DURING = 400;
    private final RecyclerView mRecyclerView;
    @NonNull
    protected final List<E> mList = new ArrayList<>();
    private OnItemClickListener<E> mOnItemClickListener;
    private long mAddTime;
    private final Handler mMainHandler = new Handler();

    public AnimatorAdapter(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setAdapter(this);
        RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
        if (itemAnimator != null) {
            itemAnimator.setAddDuration(ANIM_ADD_DURING);
            itemAnimator.setChangeDuration(ANIM_MOVE_DURING);
            itemAnimator.setMoveDuration(ANIM_MOVE_DURING);
            itemAnimator.setRemoveDuration(ANIM_ADD_DURING);
        }
    }

    public void setOnItemClickListener(OnItemClickListener<E> listener) {
        this.mOnItemClickListener = listener;
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
        convert(holder, position, mList.get(position));
        holder.itemView.setTag(position);
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
        synchronized (mList) {
            return mList.size();
        }
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
        if (now - mAddTime < ANIM_ADD_DURING) {
            mAddTime += ANIM_ADD_DURING;
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
        mList.add(i, e);
        if (mRecyclerView instanceof XRecyclerView) {
            ((XRecyclerView) mRecyclerView).notifyItemInserted(mList, i);
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

        protected void onItemClick() {
            int position = getItemPosition();
            if (adapter.mOnItemClickListener != null) {
                adapter.mOnItemClickListener.onItemClick(adapter.mList.get(position), position);
            }
        }

        public int getItemPosition() {
            int position = (Integer) itemView.getTag();
            if (position >= 0) return position;
            return adapter.mRecyclerView instanceof XRecyclerView ?
                getAdapterPosition() - 1 : getAdapterPosition();
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