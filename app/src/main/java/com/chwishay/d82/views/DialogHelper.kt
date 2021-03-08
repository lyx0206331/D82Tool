package com.chwishay.d82.views

import android.app.Dialog
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.chwishay.d82.BR.viewModel
import com.chwishay.d82.R
import com.chwishay.d82.adapters.DevListAdapter
import com.chwishay.d82.databinding.DialogDeviceListBinding
import com.chwishay.d82.entity.BleDeviceInfo
import com.chwishay.d82.viewmodels.BleListViewModel
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import kotlinx.android.synthetic.main.dialog_change_name.*
import kotlinx.android.synthetic.main.dialog_device_list.*

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

class DeviceListDialog(context: Context, val connectedDev: BleDevice?, private val connListener: IConnListener): Dialog(context, R.style.DialogTheme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.inflate<DialogDeviceListBinding>(LayoutInflater.from(context), R.layout.dialog_device_list, null, false)
        setContentView(binding.root)
        val viewModel = BleListViewModel()
        binding.viewModel = viewModel

        binding.rvDevices.addItemDecoration(DividerItemDecoration(context, LinearLayout.HORIZONTAL))
        val adapter = DevListAdapter(object : BleGattCallback(){
            override fun onStartConnect() {
                pbLoading.isVisible = true
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                pbLoading.isVisible = false
            }

            override fun onConnectSuccess(
                bleDevice: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                if (bleDevice != null && gatt != null) {
                    connListener.onConnSuccess(bleDevice, gatt)
                    dismiss()
                }
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                if (device != null && gatt != null) {
                    connListener.onDisconnDev(device, gatt)
                }
            }
        })
        binding.rvDevices.adapter = adapter

        binding.viewModel.devLiveData.observe(binding.lifecycleOwner!!) {
            adapter.submitList(it)
        }

        setOnShowListener {
            BleManager.getInstance().disconnect(connectedDev)
            startScanBle(binding, viewModel)
        }
    }

    private fun startScanBle(
        binding: DialogDeviceListBinding,
        viewModel: BleListViewModel
    ) {
        BleManager.getInstance().initScanRule(
            BleScanRuleConfig.Builder().setServiceUuids(null)
                .setDeviceName(true, null)
                .setDeviceMac("").setAutoConnect(false).setScanTimeOut(10000).build()
        )
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                binding.pbLoading.isVisible = true
            }

            override fun onScanning(bleDevice: BleDevice?) {
                bleDevice?.also {
                    viewModel.devLiveData.value?.add(BleDeviceInfo(bleDevice))
                }
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                binding.pbLoading.isVisible = false
            }
        })
    }

    interface IConnListener {
        fun onConnSuccess(bleDevice: BleDevice, gatt: BluetoothGatt)
        fun onDisconnDev(bleDevice: BleDevice, gatt: BluetoothGatt)
    }
}