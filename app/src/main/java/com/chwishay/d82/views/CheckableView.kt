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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chwishay.d82.R
import com.chwishay.d82.databinding.LayoutCheckableViewBinding
import com.chwishay.d82.tools.showShortToast
import com.tencent.mmkv.MMKV

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
    var index: Int = 0
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
            checkedLiveData.value = field
        }

    val checkedLiveData = MutableLiveData(false)

    var clickStrategy: (() -> Boolean) = { true }

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
            index = it.getInt(R.styleable.CheckableView_index, 0)
            name = it.getString(R.styleable.CheckableView_name)
            value = it.getString(R.styleable.CheckableView_value)
            it.recycle()
        }

        setOnClickListener {
            if (clickStrategy()) {
                isChecked = !isChecked
            }
        }
        setOnLongClickListener {
            ChangeNameDialog(context) { dialog, newName ->
                name = "$newName"
                MMKV.defaultMMKV()?.encode("$index", newName)
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

class MaxCheckableConstraintLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        const val MAX_CHECKABLE_NUM = 5
    }

    private val checkedList = arrayListOf<CheckableView>()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return when {
            checkedList.size < MAX_CHECKABLE_NUM -> super.dispatchTouchEvent(ev)
            checkedList.size == MAX_CHECKABLE_NUM -> {
                false
            }
            else -> {
                true
            }
        }
    }
}