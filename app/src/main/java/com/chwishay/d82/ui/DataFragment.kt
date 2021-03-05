package com.chwishay.d82.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chwishay.d82.R
import com.chwishay.d82.databinding.FragmentDataBinding
import com.chwishay.d82.tools.showShortToast

/**
 * A simple [Fragment] subclass.
 * Use the [DataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DataFragment : Fragment() {
    private var param1: Int? = null

    private val safeArgs: DataFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDataBinding.inflate(inflater, container, false).also { b ->
            b.toolbar.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.settingDest -> {
                        requireActivity().showShortToast("click menu")
                        findNavController().navigate(R.id.actionDataFragment2SettingFragment)
                    } else -> {}
                }
                true
            }
            b.btnChooseDev.setOnClickListener {
                (requireActivity() as MainActivity).checkPermissions()
            }
        }
        return binding.root
    }
}