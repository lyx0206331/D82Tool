package com.chwishay.d82.entity

import android.os.Environment
import com.chwishay.d82.tools.D82ProtocolUtil.parseImuData
import com.chwishay.d82.tools.formatDateString
import com.chwishay.d82.tools.orDefault
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.utils.HexUtil
import io.netty.buffer.Unpooled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteOrder
import java.util.*

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
 * date:2021/3/8 0008 10:30
 * description:
 */
data class BleDeviceInfo(val dev: BleDevice) {
    fun isConnected(): Boolean = BleManager.getInstance().isConnected(dev)
    fun connectDev(callback: BleGattCallback) = BleManager.getInstance().connect(dev, callback)

//    var serviceUUID: UUID? = null
//    var notifyUUID: UUID? = null

    private var startTime = 0L
    var speed: Int = 0
    var totalSize: Int = 0

    //上一秒总大小
    private var lastSecondTotalSize = 0

    //最后一次包大小
    private var lastDataSize = 0
        set(value) {
            field = value
            val timeLen = System.currentTimeMillis() - startTime
            if (startTime == 0L) {
                startTime = System.currentTimeMillis()
                lastSecondTotalSize = 0
                speed = 0
            } else if (timeLen >= 1000) {
                startTime = System.currentTimeMillis()
                speed = ((totalSize - lastSecondTotalSize) / (timeLen / 1000f)).toInt()
                lastSecondTotalSize = totalSize
            }
            totalSize += field
        }

    //最后一次传输数据
    var lastData: ByteArray? = null
        set(value) {
            field = value
            lastDataSize = field?.size.orDefault()
            if (needSave && !fileName.isNullOrEmpty()) {

                dataCache.capacity(dataCache.capacity() + lastDataSize)
                dataCache.writeBytes(lastData)
//                writeStr2File(fileName!!, field, true)
//                writeStr2File(fileName!!, field, false)
            }
        }

    //是否需要保存记录
    var needSave = false
        set(value) {
            field = value
            if (field) {
                dataCache.clear()
                startSaveTime = System.currentTimeMillis()
                totalReceiveTime = 0L
            } else {
                stopSaveTime = System.currentTimeMillis()
                runBlocking {
                    writeStr2File(fileName!!, dataCache.array(), true)
                }
            }
        }
    private var startSaveTime = 0L
    private var stopSaveTime = 0L
    var fileName: String = "${System.currentTimeMillis()}"
        set(value) {
            field = "${value}_${startSaveTime.formatDateString("yyyy-MM-dd-HH_mm_ss")}"
        }
    var filePath: String? = null

    //数据总接收时长，数据保存记录状态下，只累计接收数据时段时长，但每次重新保存记录会清零(ms)
    var totalReceiveTime = 0L

    //开始接收数据时间(ms)
    private var startReceiveTime = 0L

    //停止接收数据时间(ms)
    private var stopReceiveTime = 0L

    private val dataCache = Unpooled.buffer(0).apply { order(ByteOrder.LITTLE_ENDIAN) }

    /**
     * 开始接收数据
     */
    fun startReceive() {
        startReceiveTime = System.currentTimeMillis()
    }

    /**
     * 停止接收数据
     */
    fun stopReceive() {
        stopReceiveTime = System.currentTimeMillis()
        if (startReceiveTime <= 0L) {
            throw IllegalStateException("请先调用startReceive()方法开始接收数据")
        } else {
            totalReceiveTime += stopReceiveTime - startReceiveTime
        }
    }

    /**
     * 保存文件
     */
    private suspend fun writeStr2File(
        fileName: String,
        data: ByteArray?,
        needParse: Boolean = true
    ) {
        if (data == null) return
        withContext(Dispatchers.IO) {
            data?.parseImuData()?.also { entity ->
                val origFile = checkFileExists("${fileName}_orig")
                origFile.bufferedWriter().use {
                    entity.origList.forEach { byteArray ->
                        val format = "${HexUtil.formatHexString(byteArray, true)}\n"
                        it.write(format)
                    }
                }
                if (needParse) {
                    val resultFile = checkFileExists("${fileName}_rst")
                    resultFile.bufferedWriter().use {
                        entity.resultList.forEach { imuData ->
                            it.write("${imuData.valuesString()}\n")
                        }
                    }
                }
            }
        }
    }

    /**
     * 检测目标文件是否存在，不存在则自动创建
     */
    private fun checkFileExists(fileName: String): File {
        val dir = File("${Environment.getExternalStorageDirectory().absolutePath}/chws/")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "$fileName.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }
}