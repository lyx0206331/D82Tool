package com.chwishay.d82.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.chwishay.d82.R
import com.chwishay.d82.databinding.ActivityWelcomeBinding
import com.chwishay.d82.tools.getVersionCode
import com.chwishay.d82.tools.getVersionName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_welcome)
        val binding = DataBindingUtil.setContentView<ActivityWelcomeBinding>(this,
            R.layout.activity_welcome
        )

        binding.tvVersion.text = "v${getVersionName()}"

        lifecycleScope.launch {
            delay(2000)
            MainActivity.startActivity(this@WelcomeActivity)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finishAfterTransition()
        }
    }
}