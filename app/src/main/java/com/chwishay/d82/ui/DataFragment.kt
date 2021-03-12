package com.chwishay.d82.ui

import android.bluetooth.BluetoothGatt
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chwishay.d82.R
import com.chwishay.d82.databinding.FragmentDataBinding
import com.chwishay.d82.entity.BleDeviceInfo
import com.chwishay.d82.tools.*
import com.chwishay.d82.tools.D82ProtocolUtil.parseImuData
import com.chwishay.d82.viewmodels.D82ViewModel
import com.chwishay.d82.views.CheckableView
import com.chwishay.d82.views.DeviceListDialog
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleRssiCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.fragment_data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            b.tvOriginalData.movementMethod = ScrollingMovementMethod.getInstance()
            b.chart.init()

            filterVM.connectedDev.observe(viewLifecycleOwner) {
                btnChooseDev.text = if (it == null) {
//                    binding.tvOriginalData.text = ""
                    resources.getString(R.string.choose_dev)
                } else {
                    "${it.dev.name}\n${it.dev.mac}"
                }
            }
            filters.observe(viewLifecycleOwner) {
                tvColor1.isInvisible = true
                tvColor2.isInvisible = true
                tvColor3.isInvisible = true
                tvColor4.isInvisible = true
                tvColor5.isInvisible = true
                when(it.size) {
                    0 -> { }
                    1 -> {
                        tvColor1.isVisible = true
                        tvColor1.text = "${it[0].index}:"
                    }
                    2 -> {
                        tvColor1.isVisible = true
                        tvColor2.isVisible = true
                        tvColor1.text = "${it[0].index}:"
                        tvColor2.text = "${it[1].index}:"
                    }
                    3 -> {
                        tvColor1.isVisible = true
                        tvColor2.isVisible = true
                        tvColor3.isVisible = true
                        tvColor1.text = "${it[0].index}:"
                        tvColor3.text = "${it[1].index}:"
                        tvColor3.text = "${it[2].index}:"
                    }
                    4 -> {
                        tvColor1.isVisible = true
                        tvColor2.isVisible = true
                        tvColor3.isVisible = true
                        tvColor4.isVisible = true
                        tvColor1.text = "${it[0].index}:"
                        tvColor2.text = "${it[1].index}:"
                        tvColor3.text = "${it[2].index}:"
                        tvColor4.text = "${it[3].index}:"
                    }
                    5 -> {
                        tvColor1.isVisible = true
                        tvColor2.isVisible = true
                        tvColor3.isVisible = true
                        tvColor4.isVisible = true
                        tvColor5.isVisible = true
                        tvColor1.text = "${it[0].index}:"
                        tvColor2.text = "${it[1].index}:"
                        tvColor3.text = "${it[2].index}:"
                        tvColor4.text = "${it[3].index}:"
                        tvColor5.text = "${it[4].index}:"
                    }
                    else -> {
                        context?.showShortToast("最多展示5条曲线")
                        it[5].isChecked = false
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
        checkedLiveData.observe(viewLifecycleOwner) {
            if (it) {
                filters.value?.add(this)
            } else filters.value?.remove(
                this
            )
            filters.value = filters.value
        }
    }

    fun LineChart.init() {
        this.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {}

            override fun onNothingSelected() {}
        })
        this.description.isEnabled = true
        this.setTouchEnabled(true)
        this.isDragEnabled = true
        this.setScaleEnabled(true)
        this.setDrawGridBackground(false)
        this.setPinchZoom(true)
        this.setBackgroundColor(Color.LTGRAY)
        this.data = LineData().apply { setValueTextColor(Color.WHITE) }
        this.legend.apply {
            form = Legend.LegendForm.LINE
            textColor = Color.WHITE
        }
        this.xAxis.apply {
            textColor = Color.WHITE
            setDrawGridLines(false)
            setAvoidFirstLastClipping(false)
            isEnabled = true
        }
//            it.axisLeft.apply {
//                textColor = Color.WHITE
//                axisMaximum = 40f
//                axisMinimum = -40f
//                setDrawGridLines(true)
//            }
        //根据数据自动缩放展示最大最小值,不能设置axisLeft,否则自动缩放无效
        this.isAutoScaleMinMaxEnabled = true
        this.axisRight.isEnabled = false
        this.description.isEnabled = false
    }

    fun LineChart.addEntry(imuData: ByteArray) = this.data?.let { d ->
        fun parseValue(value: UShort) = value.toFloat()

        //        fun getGyrValue(gyr: UShort) = (gyr.toInt() - 32768) * 3.14f / 16.4f / 180
        fun getDataSet(index: Int, @ColorInt color: Int, name: String) =
            d.getDataSetByIndex(index) ?: LineDataSet(null, name).also { lds ->
                lds.axisDependency = YAxis.AxisDependency.LEFT
                lds.color = color
                lds.setCircleColor(Color.WHITE)
                lds.lineWidth = 1f
                lds.circleRadius = 2f
                lds.fillAlpha = 65
                lds.fillColor = color
                lds.highLightColor = Color.rgb(244, 177, 177)
                lds.valueTextColor = Color.WHITE
                lds.valueTextSize = 9f
                lds.setDrawCircles(false)
                d.addDataSet(lds)
            }
        imuData.parseImuData()?.resultList.takeIf { !it.isNullOrEmpty() }?.run { this[0] }
            ?.let { entity ->
                val value1Set = getDataSet(0, ColorTemplate.getHoloBlue(), "value1")
//            val gyrX1Set = getDataSet(1, context.getColor1(R.color.green01FD01), "gyrX1")
//            val accY1Set = getDataSet(2, context.getColor1(R.color.yellowFFFF00), "accY1")
//            val gyrY1Set = getDataSet(3, context.getColor1(R.color.purple7E2E8D), "gyrY1")
//            val accZ1Set = getDataSet(4, context.getColor1(R.color.colorPrimary), "accZ1")
//            val gyrZ1Set = getDataSet(5, context.getColor1(R.color.redFE0000), "gyrZ1")
                val value1 = parseValue(entity.value1)
//            val gyrX1 = getGyrValue(entity.gyrX1)
//            val accY1 = getAccValue(entity.accY1)
//            val gyrY1 = getGyrValue(entity.gyrY1)
//            val accZ1 = getAccValue(entity.accZ1)
//            val gyrZ1 = getGyrValue(entity.gyrZ1)
//            "IMU_Value".logE("accX1:$accX1, gyrX1:$gyrX1")
                d.addEntry(Entry(value1Set.entryCount.toFloat(), value1), 0)
//            d.addEntry(Entry(gyrX1Set.entryCount.toFloat(), gyrX1), 1)
//            d.addEntry(Entry(accY1Set.entryCount.toFloat(), accY1), 2)
//            d.addEntry(Entry(gyrY1Set.entryCount.toFloat(), gyrY1), 3)
//            d.addEntry(Entry(accZ1Set.entryCount.toFloat(), accZ1), 4)
//            d.addEntry(Entry(gyrZ1Set.entryCount.toFloat(), gyrZ1), 5)
                d.notifyDataChanged()

                this.notifyDataSetChanged()

                this.setVisibleXRangeMaximum(1000f)

                this.moveViewToX(d.entryCount.toFloat())

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
                        binding.chart.addEntry(d)
                        appendData(
                            binding.tvOriginalData,
                            HexUtil.formatHexString(d, true)
                        )
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