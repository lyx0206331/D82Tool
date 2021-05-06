package com.chwishay.d82tool.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.chwishay.d82tool.R
import com.chwishay.d82tool.databinding.ActivityMainBinding
import com.chwishay.d82tool.tools.PermissionUtil
import com.chwishay.d82tool.tools.showShortToast
import com.clj.fastble.BleManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context){
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    private val permissionUtil: PermissionUtil by lazy { PermissionUtil(this) }
    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if (!BleManager.getInstance().isSupportBle) {
            showShortToast("设备不支持BLE")
        } else if (!BleManager.getInstance().isBlueEnable) {
            BleManager.getInstance().enableBluetooth()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().disconnectAllDevice()
        BleManager.getInstance().destroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun checkPermissions() {
        permissionUtil.requestPermission(
            permissions,
            object : PermissionUtil.IPermissionCallback {
                override fun allowedPermissions() {
                    BleManager.getInstance().enableBluetooth()
//                    (binding.navHostFragment.childFragmentManager.findFragmentById(R.id.dataFragment) as DataFragment).showBleListDialog()
                    (supportFragmentManager.fragments[0].childFragmentManager.fragments[0] as DataFragment).showBleListDialog()
                }

                override fun deniedPermissions() {
                    permissionUtil.showTips("当前手机扫描蓝牙需要打开定位功能, 读写文件需要读写权限，是否手动设置?")
                }
            })
    }
}