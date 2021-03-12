package com.chwishay.d82.tools

import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
 * date:2021/3/11 0011 10:19
 * description:
 */
suspend fun TextView.appendDataAutoScroll(data: String?, maxLength: Int = 4000) {
    if (data == null) return
    else {
        withContext(Dispatchers.Main) {
            if (text.length > maxLength) {
                text = ""
            }
            append("$data\n")
            val contentHeight = lineCount * lineHeight
            if (contentHeight > height) {
                scrollTo(0, contentHeight - height)
            }
        }
    }
}