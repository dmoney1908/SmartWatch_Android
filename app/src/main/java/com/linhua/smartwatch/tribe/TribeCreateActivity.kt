package com.linhua.smartwatch.tribe

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.databinding.ActivityTribeCreateBinding
import com.linhua.smartwatch.databinding.ActivityTribeEditBinding
import com.linhua.smartwatch.entity.MultipleEntity
import com.linhua.smartwatch.helper.TribeDetail
import com.linhua.smartwatch.helper.TribeInfo
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.tribe.adapter.TribeMemberAdapter
import com.linhua.smartwatch.tribe.adapter.TribeMemberItem
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnInputConfirmListener
import com.yuyh.library.imgsel.ISNav
import com.yuyh.library.imgsel.config.ISCameraConfig
import com.yuyh.library.imgsel.config.ISListConfig

class TribeCreateActivity : CommonActivity() {
    private lateinit var binding: ActivityTribeCreateBinding
    var memberItemList = mutableListOf<TribeMemberItem>()
    private val memberAdapter = TribeMemberAdapter(mutableListOf())
    private val REQUEST_LIST_CODE = 1000
    private val REQUEST_CAMERA_CODE = 1001
    private var tribeInfo: TribeInfo = UserData.tribe.tribeInfo ?: TribeInfo()
    private var tribeDetail = TribeDetail()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTribeCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }

        binding.llAdd.setOnClickListener {
            XPopup.Builder(this).asInputConfirm("Add member", "Please input email address",
                OnInputConfirmListener { text ->

                }).show()
        }
        ISNav.getInstance().init { context, path, imageView ->
            Glide.with(context).load(path).into(imageView)
        }

        binding.ivPhoto.setOnClickListener {
            XPopup.Builder(this).atView(binding.ivPhoto).asAttachList(
                arrayOf("Select Photo", "Take Photo"), null
            ) { index, _ ->
                when (index) {
                    0 -> Single(null)
                    1 -> Camera(null)
                }
            }.show()
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
    private fun Single(view: View?) {
        val config = ISListConfig.Builder() // 是否多选
            .multiSelect(false)
            .btnText("Confirm") // 确定按钮背景色
            //.btnBgColor(Color.parseColor(""))
            // 确定按钮文字颜色
            .btnTextColor(Color.WHITE) // 使用沉浸式状态栏
            .statusBarColor(Color.parseColor("#FFFFFF")) // 设置状态栏字体风格黑色
            .isDarkStatusStyle(false) // 返回图标ResId
            .backResId(R.drawable.icon_navigation_back)
            .title("Images")
            .titleColor(ColorUtils.getColor(R.color.primary_black))
            .titleBgColor(Color.parseColor("#FFFFFF"))
            .allImagesText("All Images")
            .needCrop(true)
            .cropSize(1, 1, 200, 200) // 第一个是否显示相机
            .needCamera(true) // 最大选择图片数量
            .maxNum(1)
            .build()
        ISNav.getInstance().toListActivity(this, config, REQUEST_LIST_CODE)
    }

    private fun Camera(view: View?) {
        val config = ISCameraConfig.Builder()
            .needCrop(true)
            .cropSize(1, 1, 200, 200)
            .build()
        ISNav.getInstance().toCameraActivity(this, config, REQUEST_CAMERA_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LIST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val pathList: List<String>? = data.getStringArrayListExtra("result")
            if (pathList == null || pathList.isEmpty())return
            val path = pathList[0]
            UserData.uploadImageToFirebase(path) { complete, result ->
                if (complete && result != null) {
                    tribeInfo.avatar = result
                }
            }
            val filePath = "file://$path"
            Glide.with(this).load(filePath).transform(CenterInside(), RoundedCorners(50)).into(binding.ivAvatar)
        } else if (requestCode == REQUEST_CAMERA_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            var path = data.getStringExtra("result")

            if (path != null) {
                UserData.uploadImageToFirebase(path) { complete, result ->
                    if (complete && result != null) {
                        tribeInfo.avatar = result
                    }
                }
                val filePath = "file://$path"
                Glide.with(this).load(filePath).into(binding.ivAvatar)
            }
        }
    }
}