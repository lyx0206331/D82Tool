package com.chwishay.d82tool.ui

import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chwishay.d82tool.R
import com.chwishay.d82tool.databinding.FragmentDataBinding
import com.chwishay.d82tool.entity.BleDeviceInfo
import com.chwishay.d82tool.tools.*
import com.chwishay.d82tool.tools.D82ProtocolUtil.getExoParams
import com.chwishay.d82tool.tools.D82ProtocolUtil.getParamByIndex
import com.chwishay.d82tool.tools.D82ProtocolUtil.parseImuData
import com.chwishay.d82tool.viewmodels.D82ViewModel
import com.chwishay.d82tool.views.CheckableView
import com.chwishay.d82tool.views.DeviceListDialog
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleRssiCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.fragment_data.*
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Use the [DataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DataFragment : Fragment() {
    private var param1: Int? = null

    private val safeArgs: DataFragmentArgs by navArgs()

    val filterVM : D82ViewModel by viewModels()
    val filters by lazy { filterVM.filterViewsLiveData }

    private lateinit var binding: FragmentDataBinding

    private var scheduledExecutorService = Executors.newScheduledThreadPool(1)

    private val colors by lazy { arrayOf(ContextCompat.getColor(requireContext(), R.color.black),
        ContextCompat.getColor(requireContext(), R.color.teal_200),
        ContextCompat.getColor(requireContext(), R.color.titleText),
        ContextCompat.getColor(requireContext(), R.color.purple_200),
        ContextCompat.getColor(requireContext(), R.color.yellow_ffff00)) }

    private val mmkv by lazy { MMKV.defaultMMKV() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        BleUUID.instance.createExample()
        binding = FragmentDataBinding.inflate(inflater, container, false).also { b ->
            b.toolbar.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.settingDest -> {
                        findNavController().navigate(R.id.actionDataFragment2SettingFragment)
                    } else -> {}
                }
                true
            }
            b.btnChooseDev.setOnClickListener {
                (requireActivity() as MainActivity).checkPermissions()
            }
            b.btnSave.setOnClickListener {
                filterVM.connectedDev.value?.let {  dev ->
                    val fileName = b.etFileName.text
                    if (fileName.isNullOrEmpty() || fileName.trim()
                            .isNullOrEmpty()
                    ) {
                        context?.showShortToast(getString(R.string.input_file_name))
                    } else {
                        dev.needSave = !dev.needSave
                        if (dev.needSave) {
                            dev.fileName = fileName.toString()
                            context?.showShortToast("开始保存数据")
                            b.btnSave.text = getString(R.string.stop_save)
                        } else {
                            context?.showShortToast("停止保存数据")
                            b.btnSave.text = getString(R.string.start_save)
                        }
                    }
                }
            }
            initNames(b)
            b.tvOriginalData.setScrollable()
            b.chart.init()

            filterVM.connectedDev.observe(viewLifecycleOwner) {
                btnChooseDev.text = if (it == null) {
//                    binding.tvOriginalData.text = ""
                    resources.getString(R.string.choose_dev)
                } else {
                    "${it.getShowName()}\n${it.dev.mac}"
                }
            }
            filters.observe(viewLifecycleOwner) {
                binding.chart.lineData.clearValues()
            }
            val attrs = arrayListOf<ChartAttr>()
            filterVM.bleData.observe(viewLifecycleOwner) {
                it?.also {
//                    binding.chart.addEntry(it)
                    appendData(
                        binding.tvOriginalData,
                        HexUtil.formatHexString(it, true)
                    )
                    it.parseImuData()?.resultList.takeIf {  entities ->
                        !entities.isNullOrEmpty() }?.run { this[0] }
                        ?.let { entity ->
                            val params = getExoParams(entity)
//                            "params".logE("params:$params")
//                            binding.chart.drawSet(arrayListOf(ChartAttr(0, getParamByIndex(params, 0).toFloat(), colors[0], "exoMode"), ChartAttr(1, getParamByIndex(params, 1).toFloat(), colors[1], "gradeSitStand")))
//                            filters.value?.mapIndexed { index, checkableView -> ChartAttr(checkableView.index, getParamByIndex(params, checkableView.index-1).toFloat(), colors[index], "${checkableView.name}") }?.also { attrs ->
////                                "checkedView".logE("checkedSize:${attrs.size}")
//                                binding.chart.drawSet(attrs)
//                            }
                            updateValues(params)
                            attrs.clear()
                            filters.value?.forEachIndexed { index, checkableView ->
                                attrs.add(ChartAttr(checkableView.index, getParamByIndex(params, checkableView.index-1).toFloat(), colors[index], "${checkableView.name}"))
                            }
                            if (attrs.size > 0) {
                                binding.chart.drawSet(attrs)
                            }
                        }
                }
            }
            b.cv1.updateUI()
            b.cv2.updateUI()
            b.cv3.updateUI()
            b.cv4.updateUI()
            b.cv5.updateUI()
            b.cv6.updateUI()
            b.cv7.updateUI()
            b.cv8.updateUI()
            b.cv9.updateUI()
            b.cv10.updateUI()
            b.cv11.updateUI()
            b.cv12.updateUI()
            b.cv13.updateUI()
            b.cv14.updateUI()
            b.cv15.updateUI()
            b.cv16.updateUI()
            b.cv17.updateUI()
            b.cv18.updateUI()
            b.cv19.updateUI()
            b.cv20.updateUI()
            b.cv21.updateUI()
            b.cv22.updateUI()
            b.cv23.updateUI()
            b.cv24.updateUI()
            b.cv25.updateUI()
            b.cv26.updateUI()
            b.cv27.updateUI()
            b.cv28.updateUI()
            b.cv29.updateUI()
            b.cv30.updateUI()
            b.cv31.updateUI()
            b.cv32.updateUI()
            b.cv33.updateUI()
            b.cv34.updateUI()
            b.cv35.updateUI()
            b.cv36.updateUI()
            b.cv37.updateUI()
            b.cv38.updateUI()
            b.cv39.updateUI()
            b.cv40.updateUI()
        }
        return binding.root
    }

    private fun initNames(binding: FragmentDataBinding) {
        binding.cv1.name = mmkv?.decodeString("1", "exoMode")
        binding.cv2.name = mmkv?.decodeString("2", "gradeSitStand")
        binding.cv3.name = mmkv?.decodeString("3", "gradeStandForce")
        binding.cv4.name = mmkv?.decodeString("4", "gradeFlexAngle")
        binding.cv5.name = mmkv?.decodeString("5", "gradeAcc")
        binding.cv6.name = mmkv?.decodeString("6", "gradeWalkCadence")
        binding.cv7.name = mmkv?.decodeString("7", "walkPhase")
        binding.cv8.name = mmkv?.decodeString("8", "walkGait")
        binding.cv9.name = mmkv?.decodeString("9", "gaitThighImuLR")
        binding.cv10.name = mmkv?.decodeString("10", "gaitSitStand")
        binding.cv11.name = mmkv?.decodeString("11", "targetJointT")
        binding.cv12.name = mmkv?.decodeString("12", "targetMotorI")
        binding.cv13.name = mmkv?.decodeString("13", "currentMotorI")
        binding.cv14.name = mmkv?.decodeString("14", "targetMotorV")
        binding.cv15.name = mmkv?.decodeString("15", "currentMotorV")
        binding.cv16.name = mmkv?.decodeString("16", "targetMotorP")
        binding.cv17.name = mmkv?.decodeString("17", "currentMotorP")
        binding.cv18.name = mmkv?.decodeString("18", "currentJointP")
        binding.cv19.name = mmkv?.decodeString("19", "currentJointV")
        binding.cv20.name = mmkv?.decodeString("20", "batteryStable")
        binding.cv21.name = mmkv?.decodeString("21", "badThighVelY")
        binding.cv22.name = mmkv?.decodeString("22", "badThighAngP")
        binding.cv23.name = mmkv?.decodeString("23", "goodThighVelY")
        binding.cv24.name = mmkv?.decodeString("24", "goodThighAngP")
        binding.cv25.name = mmkv?.decodeString("25", "badThighAccX")
        binding.cv26.name = mmkv?.decodeString("26", "goodThighAccX")
        binding.cv27.name = mmkv?.decodeString("27", "goodThighAngR")
        binding.cv28.name = mmkv?.decodeString("28", "sysStateJointKneeEncoder")
        binding.cv29.name = mmkv?.decodeString("29", "sysStateBadSideImuT")
        binding.cv30.name = mmkv?.decodeString("30", "sysStateGoodSideImuT")
        binding.cv31.name = mmkv?.decodeString("31", "taskReadyMotorPV")
        binding.cv32.name = mmkv?.decodeString("32", "taskReadyMotorI")
        binding.cv33.name = mmkv?.decodeString("33", "connStateMotorPV")
        binding.cv34.name = mmkv?.decodeString("34", "connStateMotorI")
        binding.cv35.name = mmkv?.decodeString("35", "outRangeStateMotorPV")
        binding.cv36.name = mmkv?.decodeString("36", "updateStateMotorI")
        binding.cv37.name = mmkv?.decodeString("37", "jointStateJointEncWrong")
        binding.cv38.name = mmkv?.decodeString("38", "jointStateJointPosJump")
        binding.cv39.name = mmkv?.decodeString("39", "jointStateJointVelShake")
        binding.cv40.name = mmkv?.decodeString("40", "jointStateMotorEncWrong")
    }

//    private fun updateName(index: Int, newName: String) {
//        mmkv?.encode("$index", newName)
//    }

    private fun updateValues(
        params: ExoParams
    ) {
        binding.cv1.value = "${params.exoMode}"
        binding.cv2.value = "${params.gradeSitStand}"
        binding.cv3.value = "${params.gradeStandForce}"
        binding.cv4.value = "${params.gradeFlexAngle}"
        binding.cv5.value = "${params.gradeAcc}"
        binding.cv6.value = "${params.gradeWalkCadence}"
        binding.cv7.value = "${params.walkPhase}"
        binding.cv8.value = "${params.walkGait}"
        binding.cv9.value = "${params.gaitThighImuLR}"
        binding.cv10.value = "${params.gaitSitStand}"
        binding.cv11.value = "${params.targetJointT}"
        binding.cv12.value = "${params.targetMotorI}"
        binding.cv13.value = "${params.currentMotorI}"
        binding.cv14.value = "${params.targetMotorV}"
        binding.cv15.value = "${params.currentMotorV}"
        binding.cv16.value = "${params.targetMotorP}"
        binding.cv17.value = "${params.currentMotorP}"
        binding.cv18.value = "${params.currentJointP}"
        binding.cv19.value = "${params.currentJointV}"
        binding.cv20.value = "${params.batteryStable}"
        binding.cv21.value = "${params.badThighVelY}"
        binding.cv22.value = "${params.badThighAngP}"
        binding.cv23.value = "${params.goodThighVelY}"
        binding.cv24.value = "${params.goodThighAngP}"
        binding.cv25.value = "${params.badThighAccX}"
        binding.cv26.value = "${params.goodThighAccX}"
        binding.cv27.value = "${params.goodThighAngR}"
        binding.cv28.value = "${params.sysStateJointKneeEncoder}"
        binding.cv29.value = "${params.sysStateBadSideImuT}"
        binding.cv30.value = "${params.sysStateGoodSideImuT}"
        binding.cv31.value = "${params.taskReadyMotorPV}"
        binding.cv32.value = "${params.taskReadyMotorI}"
        binding.cv33.value = "${params.connStateMotorPV}"
        binding.cv34.value = "${params.connStateMotorI}"
        binding.cv35.value = "${params.outRangeStateMotorPV}"
        binding.cv36.value = "${params.updateStateMotorI}"
        binding.cv37.value = "${params.jointStateJointEncWrong}"
        binding.cv38.value = "${params.jointStateJointPosJump}"
        binding.cv39.value = "${params.jointStateJointVelShake}"
        binding.cv40.value = "${params.jointStateMotorEncWrong}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scheduledExecutorService.shutdown()
        BleManager.getInstance().disconnectAllDevice()
        BleManager.getInstance().destroy()
    }

    private fun sendCmd2Dev(bleDev: BleDeviceInfo, cmd: ByteArray?) {
        BleManager.getInstance().write(bleDev.dev, BleUUID.instance.serviceUUID!!.toString(),
            BleUUID.instance.charactWriteUUID!!.toString(), cmd, object : BleWriteCallback(){
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    requireActivity().showShortToast("向${bleDev.dev.name}发送指令${justWrite?.formatHexString(" ")}成功,时间:${System.currentTimeMillis()}")

                    startReceiveData(bleDev)
                }

                override fun onWriteFailure(exception: BleException?) {
                    requireActivity().showShortToast("向${bleDev.dev.name}发送指令失败:${exception.toString()}")
                }
            })
    }

    private fun startReceiveData(it: BleDeviceInfo) {
        updateOriginalAndChartData(it)
        cycleCheckRssi(it.dev)
    }

    private fun stopReceiveData(bleDeviceInfo: BleDeviceInfo?) {
        bleDeviceInfo?.also {
            it.stopReceive()
//        holder.tvReceiveTimeLen.text = "总接收时长:${bleDeviceInfo.totalReceiveTime}ms"
            BleManager.getInstance().stopNotify(
                it.dev,
                BleUUID.instance.serviceUUID.toString(),
                BleUUID.instance.charactNotifyUUID.toString()
            )
            scheduledExecutorService.shutdown()
        }
    }

    private fun CheckableView.updateUI() {
        clickStrategy = {
            if (filters.value?.size.orDefault() >= 5 && !isChecked) {
                context?.showShortToast("最多展示5条曲线")
                false
            } else {
                true
            }
        }
        checkedLiveData.observe(viewLifecycleOwner) {
            if (it) {
                filters.value?.add(this)
            } else filters.value?.remove(
                this
            )
            filters.value = filters.value
        }
    }

    fun showBleListDialog() {
        filterVM.connectedDev.value?.also {
            BleManager.getInstance().clearCharacterCallback(it.dev)
            BleManager.getInstance().disconnect(it.dev)
        }
        DeviceListDialog(requireContext(), viewLifecycleOwner, filterVM, object : DeviceListDialog.IConnListener{
            override fun onConnSuccess(bleDevice: BleDevice, gatt: BluetoothGatt) {
                filterVM.connectedDev.value = BleDeviceInfo(bleDevice).apply {
                    binding.tvOriginalData.text = ""
//                    sendCmd2Dev(this, null)
                    startReceiveData(this)
                }
            }

            override fun onDisconnDev(isActiveDisConnected: Boolean,
                                      device: BleDevice?,
                                      gatt: BluetoothGatt?,
                                      status: Int) {
                stopReceiveData(filterVM.connectedDev.value)
                if (isActiveDisConnected) {
                    requireActivity().showShortToast(R.string.disconnect)
                } else {
                    requireActivity().showShortToast(R.string.innormal_disconnect)
                }
                filterVM.connectedDev.value = null
            }
        }).show()
    }

    private fun updateOriginalAndChartData(bleDeviceInfo: BleDeviceInfo) {
        bleDeviceInfo.startReceive()
        BleManager.getInstance().notify(
            bleDeviceInfo.dev,
            BleUUID.instance.serviceUUID.toString(),
            BleUUID.instance.charactNotifyUUID.toString(),
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    appendData(binding.tvOriginalData, "notify success")
                }

                override fun onNotifyFailure(exception: BleException?) {
                    appendData(binding.tvOriginalData, exception.toString())
                }

                override fun onCharacteristicChanged(data: ByteArray?) {
                    data?.let { d ->
                        bleDeviceInfo.lastData = d
                        binding.tvTransSpeed.text =
                            "${bleDeviceInfo.speed}B/s"
                        binding.tvTotalLen.text =
                            "总接收数据:${bleDeviceInfo.totalSize}B"
                        filterVM.bleData.value = d
//                        binding.chart.addEntry(d)
//                        appendData(
//                            binding.tvOriginalData,
//                            HexUtil.formatHexString(d, true)
//                        )
                    }
                }
            })
    }

    private fun updateRssi(bleDev: BleDevice?) {
        if (bleDev == null) {
            return
        }
        BleManager.getInstance().readRssi(bleDev, object : BleRssiCallback() {
            override fun onRssiFailure(exception: BleException?) {
                "BLE".logE("信号读取失败")
            }

            override fun onRssiSuccess(rssi: Int) {
//                "BLE".logE("信号强度:${rssi}dBm")
                val colorResId = when {
                    rssi >= -60 -> R.color.green_01FD01
                    rssi >= -70 -> R.color.yellow_EAB11D
                    rssi >= -80 -> R.color.brown_D95218
                    else -> R.color.red_FE0000
                }
                val ssb = SpannableStringBuilder("rssi:${rssi}dBm")
                ssb.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), colorResId)),
                    5,
                    ssb.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.tvRssi.text = ssb
            }
        })
    }

    private fun cycleCheckRssi(bleDev: BleDevice?) {
        scheduledExecutorService = Executors.newScheduledThreadPool(1)
        scheduledExecutorService.scheduleAtFixedRate({ updateRssi(bleDev) }, 1, 2, TimeUnit.SECONDS)
    }

    private fun appendData(tv: TextView, data: String?) {
        lifecycleScope.launch {
            tv.appendDataAutoScroll(data)
        }
    }
}