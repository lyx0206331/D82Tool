package com.chwishay.d82.tools

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.util.ArrayList

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
 * date:2021/3/10 0010 14:04
 * description:
 */
object D82ProtocolUtil {

    private val cmdByteBuf = Unpooled.buffer()

    private val receiveByteBuf = Unpooled.buffer()

    /**
     * 包头数据
     */
    var headBytes: ByteArray? = byteArrayOf(0xAA.toByte(), 0xBB.toByte())
    var tailBytes: ByteArray? = byteArrayOf(0xCC.toByte(), 0xDD.toByte())

    private fun createCmdData(cmdData: ByteArray): ByteBuf {
        cmdByteBuf.clear()
        cmdByteBuf.capacity(headBytes?.size.orDefault() + cmdData.size + tailBytes?.size.orDefault())
        cmdByteBuf.writeBytes(headBytes)
        cmdByteBuf.writeBytes(cmdData)
//        cmdByteBuf.writeByte(cmdData.cmdVerify())
        cmdByteBuf.writeBytes(tailBytes)
        return cmdByteBuf
    }

    private fun createReceiveData(receiveData: ByteArray): ByteBuf {
        receiveByteBuf.clear()
        receiveByteBuf.capacity(receiveData.size)
        receiveByteBuf.writeBytes(receiveData)
        return receiveByteBuf
    }

    /**
     * 判断是IMU数据。由于IMU数据一帧包含三组有效数据，每一组都包含包头，类型，长度，数据及校验，所以单独做判断
     */
    fun ByteArray.isIMUData() =
        this[0] == headBytes!![0] && this[1] == headBytes!![1] && this[size - 2] == tailBytes!![0] && this[size - 1] == tailBytes!![1]

    fun ByteArray.parseImuData(): D82Entity? = if (this.size <= 44) {
        "IMU_DATA".logE("数据不完整: ${this.contentToString()}")
        null
    } else {
        val d82Entity = D82Entity(arrayListOf(), arrayListOf())
//        val list = arrayListOf<IMUEntity>()
        val startIndex = this.indexOfFirst { it.toUByte() == headBytes!![0].toUByte() }.let { i ->
            if (i < 0 || i > this.size - 44 || this[i + 1].toUByte() != headBytes!![1].toUByte()) {
                -1
            } else {
                i
            }
        }
        val endIndex = this.indexOfLast { it.toUByte() == tailBytes!![1].toUByte() }.let { i ->
            if (i < 43 || this[i - 1].toUByte() != tailBytes!![0].toUByte()) {
                -1
            } else {
                i
            }
        }
        "IMU_DATA".logE("startIndex: $startIndex, endIndex: $endIndex")
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            this.sliceArray(IntRange(startIndex, endIndex)).also { d ->
                if (d.size % 44 == 0) {
                    val frameCount = d.size / 44
                    for (i in 0 until frameCount) {
                        d.sliceArray(IntRange(i * 44, (i + 1) * 44 - 1)).also { item ->
                            if (item.isIMUData()) {
                                d82Entity.origList.add(item)
                                createReceiveData(item).let { data ->
                                    data.readUnsignedShortLE()
                                    val imuEntity = IMUEntity(
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort()
                                    )
//                                    "imuEntity".logE("IMU_Entity:${imuEntity}")
                                    d82Entity.resultList.add(imuEntity)
                                }
                            }
                        }
                    }
                }
            }
        }
        d82Entity
    }
}

data class D82Entity(val origList: ArrayList<ByteArray>, val resultList: ArrayList<IMUEntity>)

data class IMUEntity(
    val imuSysState: UShort, val modeState: UShort, val attResult: UShort,
    val value1: UShort, val value2: UShort, val value3: UShort,
    val value4: UShort, val value5: UShort, val value6: UShort,
    val value7: UShort, val value8: UShort, val value9: UShort,
    val value10: UShort, val value11: UShort, val value12: UShort,
    val value13: UShort, val value14: UShort, val value15: UShort,
    val value16: UShort, val value17: UShort
) {
    fun valuesString() =
        "$imuSysState\t$modeState\t$attResult\t$value1\t$value2\t$value3\t$value4\t$value5\t$value6\t$value7\t$value8\t$value9\t$value10\t$value11\t$value12\t$value13\t$value14\t$value15\t$value16\t$value17"
}