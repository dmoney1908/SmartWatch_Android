package com.linhua.smartwatch.fragment

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.linhua.smartwatch.R
import com.linhua.smartwatch.databinding.FragmentTribeBinding
import com.linhua.smartwatch.helper.Tribe
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.mine.PersonalInfoActivity
import com.linhua.smartwatch.tribe.TribeCreateActivity
import com.linhua.smartwatch.tribe.TribeEditActivity
import com.linhua.smartwatch.tribe.TribeJoinActivity
import com.linhua.smartwatch.utils.DialogHelperNew
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout

class TribeFragment: Fragment(){
    private lateinit var binding: FragmentTribeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTribeBinding.inflate(inflater, container, false)
        showLoading()
        UserData.fetchTribe {
            hideLoading()
            if (it) {
                reloadUI()
            }
        }
        reloadUI()
        binding.tvCreateTribe.setOnClickListener {
            val intent = Intent(this.context, TribeCreateActivity::class.java)
            startActivity(intent)
        }
        binding.tvJoinTribe.setOnClickListener {
            val intent = Intent(this.context, TribeJoinActivity::class.java)
            startActivity(intent)
        }

        binding.ivSettings.setOnClickListener {
            val intent = Intent(this.context, TribeEditActivity::class.java)
            startActivity(intent)
        }

        binding.refreshLayout.setRefreshHeader(ClassicsHeader(this.activity))

        binding.refreshLayout.apply {
            setOnRefreshListener {
                UserData.fetchTribe {
                    if (it) {
                        reloadUI()
                    }
                    binding.refreshLayout.finishRefresh(it)
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        reloadUI()
    }

    private fun reloadUI() {
        if (hasTribe()) {
            binding.llNotCreated.visibility = View.GONE
            binding.rlCreated.visibility = View.VISIBLE
        } else {
            binding.llNotCreated.visibility = View.VISIBLE
            binding.rlCreated.visibility = View.GONE
        }
    }

    private fun hasTribe(): Boolean {
        return UserData.tribe.tribeInfo != null && UserData.tribe.tribeDetail != null
    }

    open fun showLoading() {
        runOnUiThread {
            DialogHelperNew.buildWaitDialog(this.activity, true)
        }
    }

    open fun hideLoading() {
        runOnUiThread {
            DialogHelperNew.dismissWait()
        }
    }

}