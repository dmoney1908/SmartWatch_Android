package com.linhua.smartwatch.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.linhua.smartwatch.R
import com.linhua.smartwatch.databinding.FragmentTribeBinding
import com.linhua.smartwatch.entity.MultipleEntity
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.tribe.TribeCreateActivity
import com.linhua.smartwatch.tribe.TribeEditActivity
import com.linhua.smartwatch.tribe.TribeJoinActivity
import com.linhua.smartwatch.tribe.TribeSettingsActivity
import com.linhua.smartwatch.tribe.adapter.TribeMemberAdapter
import com.linhua.smartwatch.tribe.adapter.TribeMemberItem
import com.linhua.smartwatch.utils.DialogHelperNew
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnInputConfirmListener
import com.scwang.smart.refresh.header.ClassicsHeader
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil.showToast


class TribeFragment: Fragment(){
    private lateinit var binding: FragmentTribeBinding
    var memberItemList = mutableListOf<TribeMemberItem>()
    private val memberAdapter = TribeMemberAdapter(mutableListOf())
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
            if (UserData.tribe.tribeInfo!!.role == 1) {
                val intent = Intent(this.context, TribeEditActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this.context, TribeSettingsActivity::class.java)
                startActivity(intent)
            }
        }

        binding.ivAddMember.setOnClickListener {
            if (UserData.tribe.tribeInfo!!.role != 1) return@setOnClickListener
            XPopup.Builder(context).asInputConfirm("Add member", "Please input email address",
                OnInputConfirmListener { text ->
                    val code = UserData.tribe.tribeInfo!!.code
                    UserData.sendEmail(text, code, completeBlock = {
                        if (it) {
                            ToastUtil.showToast(
                                activity,
                                resources.getString(R.string.code_sent)
                            )
                        } else {
                            ToastUtil.showToast(
                                activity,
                                resources.getString(R.string.code_fail_sent)
                            )
                        }
                    })
                }).show()
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
        binding.rvMembers.apply {
            adapter = memberAdapter
            layoutManager = LinearLayoutManager(this.context)
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
            binding.tvTribeName.text = UserData.tribe.tribeInfo!!.name
            binding.ivSettings.visibility = View.VISIBLE
            binding.ivAddMember.visibility = if (UserData.tribe.tribeInfo!!.role == 1) View.VISIBLE else View.INVISIBLE
            if (UserData.tribe.tribeInfo!!.avatar.isNotEmpty()) {
                Glide.with(this).load(UserData.tribe.tribeInfo!!.avatar).placeholder(R.drawable.avatar_user).centerCrop()
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(22))).into(binding.ivTribe)
            } else {
                Glide.with(this).load(R.drawable.avatar_user).centerCrop()
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(22))).into(binding.ivTribe)
            }
            var steps = 0
            var sleepTime = 0
            for (item in UserData.tribe.tribeDetail!!.members) {
                steps += item.steps
                sleepTime += item.sleep
            }
            if (steps > 2000) {
                binding.tvStepsNum.text = String.format("%.1fK", steps / 1000.0)
            } else {
                binding.tvStepsNum.text = steps.toString()
            }
            if (sleepTime % 60 == 0) {
                binding.tvSleepNum.text = String.format("%dh", sleepTime / 60)
            } else {
                binding.tvSleepNum.text = String.format("%dh %dm", sleepTime / 60, sleepTime % 60)
            }
            if (UserData.tribe.tribeDetail!!.members.size < 2) {
                binding.tvMemberNum.text = String.format("%d member", UserData.tribe.tribeDetail!!.members.size)
            } else {
                binding.tvMemberNum.text = String.format("%d members", UserData.tribe.tribeDetail!!.members.size)
            }
            reloadData()
        } else {
            binding.llNotCreated.visibility = View.VISIBLE
            binding.rlCreated.visibility = View.GONE
            binding.ivSettings.visibility = View.INVISIBLE
            binding.ivAddMember.visibility = View.INVISIBLE
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

    fun reloadData() {
        convertMemberItems()
        memberAdapter.setNewInstance(memberItemList)
        memberAdapter.notifyDataSetChanged()
    }

    private fun convertMemberItems() {
        memberItemList.clear()
        if (UserData.tribe.tribeDetail == null) {
            return
        }
        for (member in UserData.tribe.tribeDetail!!.members) {
            memberItemList.add(TribeMemberItem(MultipleEntity.ONE).apply {
                name = member.name
                email = member.email
                avatar = member.avatar
                steps = member.steps
                sleep = member.sleep
                role = member.role
                time = member.time
            })
        }
    }

}