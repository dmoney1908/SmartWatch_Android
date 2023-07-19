package com.lately.tribe.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.lately.tribe.R


/**
 */
class CommonDialog : Dialog {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, themeResId: Int) : super(context!!, themeResId) {}

    class Builder(private val context: Context) {
        private var title: String? = null
        private var message: String? = null
        private var negativeButtonText: String? = null
        private var positiveButtonText: String? = null
        private var cancelable = false
        private var contentView: View? = null
        private var negativeButtonOnClickListener: DialogInterface.OnClickListener? = null
        private var positiveButtonOnClickListener: DialogInterface.OnClickListener? = null
        private var isVertical = false
        private var leftTextColor = -1
        private var rightTextColor = -1
        private var titleTextColor = -1
        private var messageTextColor = -1
        fun setTitle(title: Int): Builder {
            this.title = context.getString(title)
            return this
        }

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setMessage(message: Int): Builder {
            this.message = context.getString(message)
            return this
        }

        fun setMessage(message: String?): Builder {
            this.message = message
            return this
        }

        fun setLeftTextColor(colorRes: Int): Builder {
            leftTextColor = context.resources.getColor(colorRes)
            return this
        }

        fun setRightTextColor(colorRes: Int): Builder {
            rightTextColor = context.resources.getColor(colorRes)
            return this
        }

        fun setTitleTextColor(colorRes: Int): Builder {
            titleTextColor = context.resources.getColor(colorRes)
            return this
        }

        fun setMessageTextColor(colorRes: Int): Builder {
            messageTextColor = context.resources.getColor(colorRes)
            return this
        }

        fun isVertical(isVertical: Boolean): Builder {
            this.isVertical = isVertical
            return this
        }

        /**
         * true:按返回键可dismiss
         *
         * @param cancelable
         * @return
         */
        fun setCancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        /**
         * 确定
         *
         * @param positiveButtonText
         * @param positiveOnClickListener
         * @return
         */
        fun setRightButton(
            positiveButtonText: String?,
            positiveOnClickListener: DialogInterface.OnClickListener?
        ): Builder {
            this.positiveButtonText = positiveButtonText
            positiveButtonOnClickListener = positiveOnClickListener
            return this
        }

        fun setRightButton(
            positiveButtonText: Int,
            positiveOnClickListener: DialogInterface.OnClickListener?
        ): Builder {
            this.positiveButtonText = context.getString(positiveButtonText)
            positiveButtonOnClickListener = positiveOnClickListener
            return this
        }

        fun setRightButton(positiveOnClickListener: DialogInterface.OnClickListener?): Builder {
            positiveButtonOnClickListener = positiveOnClickListener
            return this
        }

        fun setLeftButton(
            negativeButtonText: String?,
            negativeOnClickListener: DialogInterface.OnClickListener?
        ): Builder {
            this.negativeButtonText = negativeButtonText
            negativeButtonOnClickListener = negativeOnClickListener
            return this
        }

        /**
         * set the cancel button  
         *
         * @param negativeButtonText
         * @param negativeOnClickListener
         * @return
         */
        fun setLeftButton(
            negativeButtonText: Int,
            negativeOnClickListener: DialogInterface.OnClickListener?
        ): Builder {
            this.negativeButtonText = context.getString(negativeButtonText)
            negativeButtonOnClickListener = negativeOnClickListener
            return this
        }

        fun setLeftButton(negativeButtonText: Int): Builder {
            this.negativeButtonText = context.getString(negativeButtonText)
            return this
        }

        fun setView(view: View?): Builder {
            contentView = view
            return this
        }

        fun setType(type: Int): Builder {
            this.type = type
            return this
        }

        private var type = -1
        fun create(): CommonDialog {
            val layoutInflater = LayoutInflater.from(context)
            val dialog = CommonDialog(context, R.style.dialog)
            var layout: View? = null
            if (type == -1) {
                layout = layoutInflater.inflate(
                    if (isVertical) R.layout.common_dialog_vertical_layout else R.layout.common_dialog_layout,
                    null
                )
            } else {
                layout = layoutInflater.inflate(R.layout.common_dialog_vertical_layout2, null)
            }
            dialog.addContentView(
                layout!!,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            dialog.setCancelable(cancelable) //true:按返回键可dismiss
            dialog.setCanceledOnTouchOutside(true)
            if (type == -1) {
                if (!isVertical) {
                    dialog.window!!.attributes.width = ((ScreenUtil.getScreenWidth(context) * 0.75f).toInt())
                }
            } else {
                dialog.window!!.attributes.width = ((ScreenUtil.getScreenWidth(context) * 0.9f).toInt())
                dialog.window!!.setGravity(Gravity.BOTTOM)
            }
            if (!TextUtils.isEmpty(title)) {
                (layout.findViewById<View>(R.id.title) as TextView).text = title
                if (titleTextColor != -1) {
                    (layout.findViewById<View>(R.id.title) as TextView).setTextColor(titleTextColor)
                }
            } else {
                layout.findViewById<View>(R.id.title).visibility = View.GONE
            }
            if (!TextUtils.isEmpty(message)) {
                (layout.findViewById<View>(R.id.message) as TextView).text = message
                if (messageTextColor != -1) {
                    (layout.findViewById<View>(R.id.message) as TextView).setTextColor(
                        messageTextColor
                    )
                }
            } else if (contentView != null) {
                (layout.findViewById<View>(R.id.content) as LinearLayout).removeAllViews()
                (layout.findViewById<View>(R.id.content) as LinearLayout).addView(
                    contentView,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }
            if (!TextUtils.isEmpty(positiveButtonText)) {
                (layout.findViewById<View>(R.id.positiveButton) as Button).text =
                    positiveButtonText
                layout.findViewById<View>(R.id.positiveButton).setOnClickListener {
                    dialog.dismiss()
                    if (positiveButtonOnClickListener != null) {
                        positiveButtonOnClickListener!!.onClick(
                            dialog,
                            BUTTON_POSITIVE
                        )
                    }
                }
                if (rightTextColor != -1) {
                    (layout.findViewById<View>(R.id.positiveButton) as Button).setTextColor(
                        rightTextColor
                    )
                }
            } else {
                layout.findViewById<View>(R.id.positiveButton).visibility = View.GONE
                layout.findViewById<View>(R.id.bottom_line).visibility = View.GONE
            }
            if (!TextUtils.isEmpty(negativeButtonText)) {
                (layout.findViewById<View>(R.id.negativeButton) as Button).text =
                    negativeButtonText
                layout.findViewById<View>(R.id.negativeButton).setOnClickListener {
                    dialog.dismiss()
                    if (negativeButtonOnClickListener != null) {
                        negativeButtonOnClickListener!!.onClick(
                            dialog,
                            BUTTON_NEGATIVE
                        )
                    }
                }
                if (leftTextColor != -1) {
                    (layout.findViewById<View>(R.id.negativeButton) as Button).setTextColor(
                        leftTextColor
                    )
                }
            } else {
                layout.findViewById<View>(R.id.negativeButton).visibility = View.GONE
                layout.findViewById<View>(R.id.bottom_line).visibility = View.GONE
            }
            dialog.setContentView(layout)
            return dialog
        }
    }
}