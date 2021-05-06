package com.chwishay.d82tool.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chwishay.d82tool.entity.BleDeviceInfo
import com.chwishay.d82tool.views.CheckableView

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
 * date:2021/3/8 0008 15:10
 * description:
 */
class D82ViewModel: ViewModel() {
    val connectedDev = MutableLiveData<BleDeviceInfo?>()

    val devicesLiveData = MutableLiveData<ArrayList<BleDeviceInfo>>(arrayListOf())

    val filterViewsLiveData = MutableLiveData<ArrayList<CheckableView>>(arrayListOf())

    val bleData = MutableLiveData<ByteArray?>()
}