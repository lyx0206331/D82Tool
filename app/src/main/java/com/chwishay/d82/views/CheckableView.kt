package com.chwishay.d82.views

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Checkable
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.chwishay.d82.R
import com.chwishay.d82.databinding.LayoutCheckableViewBinding
import com.chwishay.d82.tools.showShortToast

//                       _ooOoo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                       O\ = /O
//                   ____/`---'\____
//                 .   ' \\| |// `.
//                  / \\||| : |||// \
//                / _||||| -:- |||||- \
//                  | | \\\ - /// | |
//                | \_| ''\---/'' | |
//                 \ .-\__ `-` ___/-. /
//              ______`. .' /--.--\ `. . __
//           ."" '< `.___\_<|>_/___.' >'"".
//          | | : `- \`.;`\ _ /`;.`/ - ` : | |
//            \ \ `-. \_ __\ /__ _/ .-` / /
//    ======`-.____`-.___\_____/___.-`____.-'======
//                       `=---='
//
//    .............................................
//             佛祖保佑             永无BUG
/**
 * author:RanQing
 * date:2021/3/4 0004 13:54
 * description:
 */
class CheckableView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): LinearLayout(context, attrs, defStyleAttr) {

    private val tvIndex: AppCompatTextView
    private val tvName: AppCompatTextView
    private val tvValue: AppCompatTextView
    private var index: String? = null
        set(value) {
            field = value
            tvIndex.text = "$field."
        }
    var name: String? = null
        set(value) {
            field = value
            tvName.text = "$field:"
        }
    var value: String? = null
        set(value) {
            field = value
            tvValue.text = field
        }

    private val checkedDrawable: Drawable
    private val uncheckedDrawable: Drawable

    var isChecked = false
        set(value) {
            field = value
            background = if (field) checkedDrawable else uncheckedDrawable
        }

    init {
        LayoutCheckableViewBinding.inflate(LayoutInflater.from(context), this, true).also {
            tvIndex = it.tvIndex
            tvName = it.tvName
            tvValue = it.tvValue
        }
        tvIndex.text = "1."
        tvName.text = "name:"
        tvValue.text = "value"

        checkedDrawable = resources.getDrawable(R.drawable.shape_checked, context.theme)
        uncheckedDrawable = resources.getDrawable(R.drawable.shape_unchecked, context.theme)

        context.obtainStyledAttributes(attrs, R.styleable.CheckableView).also {
            index = it.getString(R.styleable.CheckableView_index)
            name = it.getString(R.styleable.CheckableView_name)
            value = it.getString(R.styleable.CheckableView_value)
            it.recycle()
        }

        setOnClickListener {
            isChecked = !isChecked
        }
        setOnLongClickListener {
            ChangeNameDialog(context) { dialog, newName ->
                name = "$newName"
                dialog.dismiss()
            }.show()
            true
        }

        isChecked = false
        isClickable = true
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null) false
        else {
            val o = other as CheckableView
            o.index == index
        }
    }
}