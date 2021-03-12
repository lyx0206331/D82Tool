package com.chwishay.d82.adapters

import android.bluetooth.BluetoothGatt
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chwishay.d82.databinding.ItemDeviceListBinding
import com.chwishay.d82.entity.BleDeviceInfo
import com.chwishay.d82.tools.logE
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.data.BleMsg
import com.clj.fastble.exception.BleException

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
 * date:2021/3/8 0008 11:15
 * description:
 */
class DevListAdapter(private val connCallback: BleGattCallback): ListAdapter<BleDeviceInfo, RecyclerView.ViewHolder>(BleDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DevViewHolder(ItemDeviceListBinding.inflate(LayoutInflater.from(parent.context), parent, false), connCallback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DevViewHolder).bind(getItem(position))
    }

    class DevViewHolder(private val binding: ItemDeviceListBinding, callback: BleGattCallback): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener {
                binding.bleDev?.connectDev(callback)
            }
        }

        fun bind(item: BleDeviceInfo) {
            binding.apply {
                bleDev = item
                executePendingBindings()
            }
        }
    }
}

private class BleDiffCallback: DiffUtil.ItemCallback<BleDeviceInfo>() {
    override fun areItemsTheSame(oldItem: BleDeviceInfo, newItem: BleDeviceInfo): Boolean {
        return oldItem.dev.mac == newItem.dev.mac
    }

    override fun areContentsTheSame(oldItem: BleDeviceInfo, newItem: BleDeviceInfo): Boolean {
        return oldItem.isConnected() == newItem.isConnected()
    }
}