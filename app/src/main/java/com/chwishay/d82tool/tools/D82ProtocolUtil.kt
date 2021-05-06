package com.chwishay.d82tool.tools

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

    fun ByteArray.parseImuData(): D82Entity? = if (this.size < 44) {
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
            this.sliceArray(IntRange(startIndex, endIndex)).takeIf { it.size % 44 == 0 }
                ?.also { d ->
                    val frameCount = d.size / 44
                    for (i in 0 until frameCount) {
                        d.sliceArray(IntRange(i * 44, (i + 1) * 44 - 1)).takeIf { it.isIMUData() }
                            ?.also { item ->
                                d82Entity.origList.add(item)
                                createReceiveData(item).let { data ->
                                    data.readUnsignedShortLE()
                                    val imuEntity = IMUEntity(
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toUShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort(),
                                        data.readUnsignedShortLE().toShort()
                                    )
//                                    "imuEntity".logE("IMU_Entity:${imuEntity}")
                                    d82Entity.resultList.add(imuEntity)
                                }
                            }
                    }
                }
        }
        d82Entity
    }

    fun getExoParams(entity: IMUEntity): ExoParams {
        fun getSensorSysState(offset: Int) = entity.sensorSysState.toInt().shr(offset).and(0x0001)

        val sysStateJointKneeEncoder = getSensorSysState(0)
        val sysStateBadSideImuT = getSensorSysState(1)
        val sysStateGoodSideImuT= getSensorSysState(2)
        val taskReadyMotorPV = getSensorSysState(3)
        val taskReadyMotorI = getSensorSysState(5)
        val connStateMotorPV = getSensorSysState(3)
        val connStateMotorI = getSensorSysState(5)
        val outRangeStateMotorPV = getSensorSysState(4)
        val updateStateMotorI = getSensorSysState(6)
        val jointStateJointEncWrong = getSensorSysState(7)
        val jointStateJointPosJump = getSensorSysState(8)
        val jointStateJointVelShake = getSensorSysState(9)
        val jointStateMotorEncWrong = getSensorSysState(10)
        val jointStateTcCurrNoEqual = getSensorSysState(11)
        val jointStateMotorPosJump = getSensorSysState(12)

        val exoMode = getSensorSysState(13)
        val gradeSitStand = (entity.gradeState.toInt()/10000-1) * 10000
        val gradeStandForce = (entity.gradeState.toInt()%10000/1000-1)*1000
        val gradeFlexAngle = (entity.gradeState.toInt()%1000/100-1)*100
        val gradeAcc = (entity.gradeState.toInt()%100/10-1)*10
        val gradeWalkCadence = entity.gradeState.toInt()%10-1

        val walkPhase = entity.gaitResult.toInt().and(0x0003)
        val walkGait = entity.gaitResult.toInt().shr(2).and(0x0007)
        val gaitThighImuLR = entity.gaitResult.toInt().shr(10).and(0x0007)
        val gaitSitStand = entity.gaitResult.toInt().shr(13).and(0x0007)

        val targetJointT = entity.value1.toInt()*1000
        val targetMotorI = entity.value2.toInt()*1000
        val currentMotorI = entity.value3.toInt()*1000
        val targetMotorV = entity.value4.toInt()
        val currentMotorV = entity.value5.toInt()
        val targetMotorP = entity.value6.toInt()
        val currentMotorP = entity.value7.toInt()
        val currentJointP = entity.value8.toInt()*100
        val currentJointV = entity.value9.toInt()*10
        val batteryStable = entity.value10.toInt()*100
        val badThighVelY = entity.value11.toInt()
        val badThighAngP = entity.value12.toInt()
        val goodThighVelY = entity.value13.toInt()
        val goodThighAngP = entity.value14.toInt()
        val badThighAccX = entity.value15.toInt()
        val goodThighAccX = entity.value16.toInt()
        val goodThighAngR = entity.value17.toInt()
        return ExoParams(exoMode, gradeSitStand, gradeStandForce, gradeFlexAngle, gradeAcc, gradeWalkCadence,
            walkPhase, walkGait, gaitThighImuLR, gaitSitStand,
            targetJointT, targetMotorI, currentMotorI, targetMotorV, currentMotorV, targetMotorP, currentMotorP, currentJointP, currentJointV, batteryStable, badThighVelY, badThighAngP, goodThighVelY, goodThighAngP, badThighAccX, goodThighAccX, goodThighAngR,
            sysStateJointKneeEncoder, sysStateBadSideImuT, sysStateGoodSideImuT, taskReadyMotorPV, taskReadyMotorI, connStateMotorPV, connStateMotorI, outRangeStateMotorPV, updateStateMotorI, jointStateJointEncWrong, jointStateJointPosJump, jointStateJointVelShake, jointStateMotorEncWrong)
    }

    fun getParamByIndex(exoParams: ExoParams, index: Int): Int = when(index) {
        0 -> exoParams.exoMode
        1 -> exoParams.gradeSitStand
        2 -> exoParams.gradeStandForce
        3 -> exoParams.gradeFlexAngle
        4 -> exoParams.gradeAcc
        5 -> exoParams.gradeWalkCadence
        6 -> exoParams.walkPhase
        7 -> exoParams.walkGait
        8 -> exoParams.gaitThighImuLR
        9 -> exoParams.gaitSitStand
        10 -> exoParams.targetJointT
        11 -> exoParams.targetMotorI
        12 -> exoParams.currentMotorI
        13 -> exoParams.targetMotorV
        14 -> exoParams.currentMotorV
        15 -> exoParams.targetMotorP
        16 -> exoParams.currentMotorP
        17 -> exoParams.currentJointP
        18 -> exoParams.currentJointV
        19 -> exoParams.batteryStable
        20 -> exoParams.badThighVelY
        21 -> exoParams.badThighAngP
        22 -> exoParams.goodThighVelY
        23 -> exoParams.goodThighAngP
        24 -> exoParams.badThighAccX
        25 -> exoParams.goodThighAccX
        26 -> exoParams.goodThighAngR
        27 -> exoParams.sysStateJointKneeEncoder
        28 -> exoParams.sysStateBadSideImuT
        29 -> exoParams.sysStateGoodSideImuT
        30 -> exoParams.taskReadyMotorPV
        31 -> exoParams.taskReadyMotorI
        32 -> exoParams.connStateMotorPV
        33 -> exoParams.connStateMotorI
        34 -> exoParams.outRangeStateMotorPV
        35 -> exoParams.updateStateMotorI
        36 -> exoParams.jointStateJointEncWrong
        37 -> exoParams.jointStateJointPosJump
        38 -> exoParams.jointStateJointVelShake
        39 -> exoParams.jointStateMotorEncWrong
        else -> 0
    }
}

data class D82Entity(val origList: ArrayList<ByteArray>, val resultList: ArrayList<IMUEntity>)

data class IMUEntity(
    val sensorSysState: UShort, val gradeState: UShort, val gaitResult: UShort,
    val value1: Short, val value2: Short, val value3: Short,
    val value4: Short, val value5: Short, val value6: Short,
    val value7: Short, val value8: Short, val value9: Short,
    val value10: Short, val value11: Short, val value12: Short,
    val value13: Short, val value14: Short, val value15: Short,
    val value16: Short, val value17: Short
) {
    fun valuesString() =
        "$sensorSysState\t$gradeState\t$gaitResult\t$value1\t$value2\t$value3\t$value4\t$value5\t$value6\t$value7\t$value8\t$value9\t$value10\t$value11\t$value12\t$value13\t$value14\t$value15\t$value16\t$value17"
}

data class ExoParams(
    val exoMode: Int = 0,

    val gradeSitStand: Int = 0,
    val gradeStandForce: Int = 0,
    val gradeFlexAngle: Int = 0,
    val gradeAcc: Int = 0,
    val gradeWalkCadence: Int = 0,

    val walkPhase: Int = 0,
    val walkGait: Int = 0,
    val gaitThighImuLR: Int = 0,
    val gaitSitStand: Int = 0,

    val targetJointT: Int = 0,
    val targetMotorI: Int = 0,
    val currentMotorI: Int = 0,
    val targetMotorV: Int = 0,
    val currentMotorV: Int = 0,
    val targetMotorP: Int = 0,
    val currentMotorP: Int = 0,
    val currentJointP: Int = 0,
    val currentJointV: Int = 0,
    val batteryStable: Int = 0,
    val badThighVelY: Int = 0,
    val badThighAngP: Int = 0,
    val goodThighVelY: Int = 0,
    val goodThighAngP: Int = 0,
    val badThighAccX: Int = 0,
    val goodThighAccX: Int = 0,
    val goodThighAngR: Int = 0,

    val sysStateJointKneeEncoder: Int = 0,
    val sysStateBadSideImuT: Int = 0,
    val sysStateGoodSideImuT: Int = 0,
    val taskReadyMotorPV: Int = 0,
    val taskReadyMotorI: Int = 0,
    val connStateMotorPV: Int = 0,
    val connStateMotorI: Int = 0,
    val outRangeStateMotorPV: Int = 0,
    val updateStateMotorI: Int = 0,
    val jointStateJointEncWrong: Int = 0,
    val jointStateJointPosJump: Int = 0,
    val jointStateJointVelShake: Int = 0,
    val jointStateMotorEncWrong: Int = 0
)