package com.chwishay.d82.views

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import com.chwishay.d82.R
import kotlinx.android.synthetic.main.dialog_change_name.*

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
 * date:2021/3/4 0004 18:22
 * description:
 */
class ChangeNameDialog(context: Context, val listener: (dialog: ChangeNameDialog, newName: String) -> Unit) : Dialog(context, R.style.DialogTheme) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_change_name)
        btnConfirm.setOnClickListener {
            if (TextUtils.isEmpty(etInputNewName.text?.trim())) {
                etInputNewName.error = context.getString(R.string.change_name_prompt)
            } else {
                listener(this, etInputNewName.text.toString())
            }
        }
    }
}