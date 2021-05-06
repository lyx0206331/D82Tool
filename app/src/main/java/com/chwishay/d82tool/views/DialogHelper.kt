package com.chwishay.d82tool.views

import android.app.Dialog
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chwishay.d82tool.R
import com.chwishay.d82tool.adapters.DevListAdapter
import com.chwishay.d82tool.databinding.DialogDeviceListBinding
import com.chwishay.d82tool.entity.BleDeviceInfo
import com.chwishay.d82tool.tools.showShortToast
import com.chwishay.d82tool.viewmodels.D82ViewModel
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.data.BleScanState
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import kotlinx.android.synthetic.main.dialog_change_name.*
import kotlinx.android.synthetic.main.dialog_device_list.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

class DeviceListDialog(context: Context,
                       private val lifecycleOwner: LifecycleOwner,
                       private val viewModel: D82ViewModel,
                       private val connListener: IConnListener): Dialog(context, R.style.DialogTheme) {

    private lateinit var binding : DialogDeviceListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_device_list, null, false)
        setContentView(binding.root)
        binding.viewModel = viewModel

        binding.rvDevices.layoutManager = LinearLayoutManager(context)
        binding.rvDevices.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {setDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.black_232323)))})
        val adapter = DevListAdapter(object : BleGattCallback(){
            override fun onStartConnect() {
                pbLoading.isVisible = true
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                pbLoading.isVisible = false
                context.showShortToast(R.string.connect_fail)
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
                    connListener.onDisconnDev(isActiveDisConnected, device, gatt, status)
                }
            }
        })
        binding.rvDevices.adapter = adapter

        binding.viewModel?.devicesLiveData?.observe(lifecycleOwner) {
//            "bleObserve".logE("observe:${it.size}")
            adapter.submitList(it.toMutableList())
        }

        binding.tvScan.setOnClickListener {
            if (BleManager.getInstance().scanSate == BleScanState.STATE_SCANNING) {
                BleManager.getInstance().cancelScan()
            } else {
                startScanBle()
            }
        }

        setOnShowListener {
            lifecycleOwner.lifecycleScope.launch {
                delay(500)
                startScanBle()
            }
        }

        setOnDismissListener {
            BleManager.getInstance().cancelScan()
        }
    }

    private fun startScanBle() {
        BleManager.getInstance().initScanRule(
            BleScanRuleConfig.Builder().setServiceUuids(null)
                .setDeviceName(true, "E104")
                .setDeviceMac("").setAutoConnect(false).setScanTimeOut(10000).build()
        )
        val devList = arrayListOf<BleDeviceInfo>()
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
//                "bleDevice".logE("start scan")
                binding.pbLoading.isVisible = true
                binding.tvScan.setText(R.string.stop_scan)
            }

            override fun onScanning(bleDevice: BleDevice?) {
                bleDevice?.also {
//                    "bleDevice".logE("ble mac:${it.mac}")
                    viewModel.devicesLiveData.also { list ->
                        devList.add(BleDeviceInfo(it))
                        list.value = devList
                    }
                }
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
//                "bleDevice".logE("stop scan")
                binding.pbLoading.isVisible = false
                binding.tvScan.setText(R.string.start_scan)
            }
        })
    }

    interface IConnListener {
        fun onConnSuccess(bleDevice: BleDevice, gatt: BluetoothGatt)
        fun onDisconnDev(isActiveDisConnected: Boolean,
                         device: BleDevice?,
                         gatt: BluetoothGatt?,
                         status: Int)
    }
}