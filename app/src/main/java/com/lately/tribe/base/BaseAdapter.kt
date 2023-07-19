package com.lately.tribe.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lately.tribe.R

/**
 */


abstract class BaseAdapter<T, MVH : RecyclerView.ViewHolder?>(
    mContext: Context?,
    protected var mList: MutableList<T>?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    protected var mContext: Context?
    protected var inflater: LayoutInflater
    protected var mOnItemClickListener: OnItemClickListener? =
        null
    private var mRetryClickListener: OnRetryClickListener? =
        null
    private var customClickListener: OnCustomClickListener? =
        null

    init {
        if (mList != null && mList!!.size > 0) {
            showNormalData()
        } else {
            showNoData()
        }
        this.mContext = mContext
        inflater = LayoutInflater.from(mContext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        when (viewType) {
            Companion.TYPE_NORMAL -> viewHolder =
                onCreateViewHolder(parent)
            Companion.TYPE_NO_DATA -> viewHolder =
                NoDataViewHolder(
                    inflater.inflate(
                        R.layout.refresh_load_no_date,
                        parent,
                        false
                    )
                )
            Companion.TYPE_NET_ERROR -> viewHolder =
                NetErrorViewHolder(
                    inflater.inflate(
                        R.layout.refresh_load_net_error,
                        parent,
                        false
                    )
                )
            else -> {
                val holder: RecyclerView.ViewHolder = onCreateViewHolder(parent, viewType) as RecyclerView.ViewHolder
                if (holder != null) {
                    holder.itemView.tag = holder
                    holder.itemView.setOnClickListener { view: View? ->
                        mOnItemClickListener?.onItemClick(
                            view,
                            viewType
                        )
                    }
                }
            }
        }
        return viewHolder as RecyclerView.ViewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NoDataViewHolder) {
        } else if (holder is NetErrorViewHolder) {
//            ((NetErrorViewHolder) holder).tvNetRetry.setOnClickListener(v -> {
//                if (mRetryClickListener != null) {
//                    mRetryClickListener.onRetryClick(v);
//                }
//            });
        } else {
            val mvh = holder as MVH?
            onNormalBindViewHolder(mvh, mList!![position], position)
        }
    }

    /**
     * 正常情况下的 BindViewHolder
     *
     * @param holder
     * @param position
     */
    protected abstract fun onNormalBindViewHolder(holder: MVH?, itemBean: T, position: Int)

    /**
     * 返回 正常情况下的ViewHolder
     *
     * @param parent
     * @return
     */
    protected abstract fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder?
    override fun getItemCount(): Int {
        return if (Companion.currentType == 0) {
            mList!!.size
        } else {
            1
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    interface OnCustomClickListener {
        fun onCustomClick(view: View?, position: Int)
    }

    @JvmName("setCustomClickListener1")
    fun setCustomClickListener(customClickListener: OnCustomClickListener?) {
        this.customClickListener = customClickListener
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = onItemClickListener
    }

    interface OnRetryClickListener {
        fun onRetryClick(view: View?)
    }

    fun setRetryClickListener(mRetryClickListener: OnRetryClickListener?) {
        this.mRetryClickListener = mRetryClickListener
    }

    override fun getItemViewType(position: Int): Int {
        return Companion.currentType
    }

    //用于上拉加载
    fun addMoreItem(list: List<T>?) {
        mList!!.addAll(list!!)
        notifyDataSetChanged()
    }

    //用于普通更新数据
    fun setList(list: MutableList<T>?) {
        if (list == null) {
            currentType = 1
            mList = ArrayList()
        } else {
            currentType = 0
            mList = list
        }
        notifyDataSetChanged()
    }

    //得到集合
    val list: List<T>?
        get() = mList

    //添加数据
    fun addItem(position: Int, data: T) {
        mList!!.add(position, data)
        notifyItemInserted(position) //通知演示插入动画
        notifyItemRangeChanged(position, mList!!.size - position) //通知数据与界面重新绑定
    }

    fun getItem(position: Int): T {
        return mList!![position]
    }

    fun showNormalData() {
        currentType = 0
        notifyDataSetChanged()
    }

    fun showNoData() {
        currentType = 1
        notifyDataSetChanged()
    }

    fun showNetError() {
        currentType = 2
        notifyDataSetChanged()
    }

    //暂无数据的 的ViewHolder
    class NoDataViewHolder(itemView: View?) : BaseViewHolder(itemView) {

        var moduleBaseIdEmptyImg: ImageView? = null

        var moduleBaseEmptyText: TextView? = null

        var llLoadNoDate: LinearLayout? = null

        init {

            moduleBaseIdEmptyImg = itemView?.findViewById(R.id.module_base_id_empty_img)
            moduleBaseEmptyText = itemView?.findViewById(R.id.module_base_empty_text)
            llLoadNoDate = itemView?.findViewById(R.id.ll_load_no_date)
            llLoadNoDate!!.visibility = View.VISIBLE
        }
    }

    //网络请求失败的 的ViewHolder
    class NetErrorViewHolder(itemView: View?) : BaseViewHolder(itemView) {
        var llNetError: LinearLayout? = null

        init {
            llNetError = itemView?.findViewById(R.id.ll_net_error)
            llNetError!!.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val TYPE_NORMAL = 0
        private const val TYPE_NO_DATA = 1 //暂无数据
        private const val TYPE_NET_ERROR = 2 //网络错误
        private var currentType = 0 //当前类别
    }
}