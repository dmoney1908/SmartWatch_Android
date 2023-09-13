package com.lately.tribe.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lately.tribe.R

/**
 * 下拉刷新上拉加载控件，目前适用于RecyclerView
 * 来自于开源中国项目代码
 */
class RecyclerRefreshLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) :
    SwipeRefreshLayout(context!!, attrs), SwipeRefreshLayout.OnRefreshListener {
    private var mRecycleView: RecyclerView? = null
    private val mTouchSlop: Int
    private var listener: SuperRefreshLayoutListener? =
        null
    private var mIsOnLoading: Boolean = false
    private var mCanLoadMore: Boolean = true
    private var mHasMore: Boolean = true
    private var mYDown: Int = 0
    private var mLastY: Int = 0

    init {
        mTouchSlop = context?.let { ViewConfiguration.get(it).scaledTouchSlop }!!
        setOnRefreshListener(this)
    }

    override fun onRefresh() {
        if (listener != null && !mIsOnLoading) {
            listener!!.onRefreshing()
        } else setRefreshing(false)
    }

    protected override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // 初始化ListView对象
        if (mRecycleView == null) {
            recycleView
        }
    }//找到Recyclerview

    /**
     * 获取RecyclerView，后续支持AbsListView
     */
    private val recycleView: Unit
        private get() {
            if (getChildCount() > 0) {
                var childView: View? = getChildAt(0)
                if (!(childView is RecyclerView)) {
                    //找到Recyclerview
                    childView = findViewById(R.id.refresh_recyclerView)
                }
                if (childView != null && childView is RecyclerView) {
                    mRecycleView = childView
                    mRecycleView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(
                            recyclerView: RecyclerView,
                            newState: Int
                        ) {
                        }

                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            if (canLoad() && mCanLoadMore) {
                                loadData()
                            }
                        }
                    })
                }
            }
        }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val action: Int = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> mYDown = event.rawY.toInt()
            MotionEvent.ACTION_MOVE -> mLastY = event.rawY.toInt()
            else -> {}
        }
        return super.dispatchTouchEvent(event)
    }

    /**
     * 是否可以加载更多, 条件是到了最底部
     *
     * @return isCanLoad
     */
    private fun canLoad(): Boolean {
        return isScrollBottom && !mIsOnLoading && isPullUp && mHasMore
    }

    /**
     * 如果到了最底部,而且是上拉操作.那么执行onLoad方法
     */
    private fun loadData() {
        if (listener != null) {
            setOnLoading(true)
            listener!!.onLoadMore()
        }
    }

    /**
     * 是否是上拉操作
     *
     * @return isPullUp
     */
    private val isPullUp: Boolean
        private get() = (mYDown - mLastY) >= mTouchSlop

    /**
     * 设置正在加载
     *
     * @param loading 设置正在加载
     */
    fun setOnLoading(loading: Boolean) {
        if (!mIsOnLoading) {
            mYDown = 0
            mLastY = 0
        }
        mIsOnLoading = loading
    }

    /**
     * 判断是否到了最底部
     */
    private val isScrollBottom: Boolean
        private get() = ((mRecycleView != null && mRecycleView!!.adapter != null)
                && lastVisiblePosition == (mRecycleView!!.adapter!!.itemCount - 1))

    /**
     * 加载结束记得调用
     */
    fun onComplete() {
        setOnLoading(false)
        setRefreshing(false)
        mHasMore = true
    }

    /**
     * 是否可加载更多
     *
     * @param mCanLoadMore 是否可加载更多
     */
    fun setCanLoadMore(mCanLoadMore: Boolean) {
        this.mCanLoadMore = mCanLoadMore
    }

    /**
     * 获取RecyclerView可见的最后一项
     *
     * @return 可见的最后一项position
     */
    val lastVisiblePosition: Int
        get() {
            val position: Int
            if (mRecycleView!!.layoutManager is LinearLayoutManager) {
                position =
                    (mRecycleView!!.layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()
            } else if (mRecycleView!!.layoutManager is GridLayoutManager) {
                position =
                    (mRecycleView!!.layoutManager as GridLayoutManager?)!!.findLastVisibleItemPosition()
            } else if (mRecycleView!!.layoutManager is StaggeredGridLayoutManager) {
                val layoutManager: StaggeredGridLayoutManager? =
                    mRecycleView!!.layoutManager as StaggeredGridLayoutManager?
                val lastPositions: IntArray = layoutManager!!.findLastVisibleItemPositions(
                    IntArray(
                        layoutManager.spanCount
                    )
                )
                position = getMaxPosition(lastPositions)
            } else {
                position = mRecycleView!!.layoutManager!!.itemCount - 1
            }
            return position
        }

    /**
     * 获得最大的位置
     *
     * @param positions 获得最大的位置
     * @return 获得最大的位置
     */
    private fun getMaxPosition(positions: IntArray): Int {
        var maxPosition: Int = Int.MIN_VALUE
        for (position: Int in positions) {
            maxPosition = Math.max(maxPosition, position)
        }
        return maxPosition
    }

    /**
     * 添加加载和刷新
     *
     * @param listener add the listener for SuperRefreshLayout
     */
    fun setSuperRefreshLayoutListener(listener: SuperRefreshLayoutListener?) {
        this.listener = listener
    }

    interface SuperRefreshLayoutListener {
        fun onRefreshing()
        fun onLoadMore()
    }
}