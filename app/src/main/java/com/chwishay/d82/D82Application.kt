package com.chwishay.d82

import android.app.Application
import android.graphics.Typeface
import com.chwishay.d82.tools.logE
import com.clj.fastble.BleManager
import com.clj.fastble.BuildConfig
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
 * date:2021/3/3 0003 16:21
 * description:
 */
class D82Application: Application() {

    override fun onCreate() {
        super.onCreate()
//        Typeface.createFromAsset(assets, "font/fontawesome-webfont.ttf")

        val rootDir = MMKV.initialize(this)

        "MMKV_ROOTDIR".logE("mmkv root dir:$rootDir")

        initBle()
    }

    private fun initBle() {
        BleManager.getInstance().init(this)
        BleManager.getInstance().enableLog(BuildConfig.DEBUG).setReConnectCount(1, 5000)
            .setConnectOverTime(20000).setOperateTimeout(5000)
    }
}