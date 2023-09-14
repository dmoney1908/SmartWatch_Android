package com.lately.tribe.tribe

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.lately.tribe.R
import com.lately.tribe.base.CommonActivity
import com.lately.tribe.databinding.ActivityTribeEditBinding
import com.lately.tribe.helper.UserData
import com.lxj.xpopup.XPopup
import com.yuyh.library.imgsel.ISNav
import com.yuyh.library.imgsel.config.ISCameraConfig
import com.yuyh.library.imgsel.config.ISListConfig

class TribeEditActivity : CommonActivity() {
    private lateinit var binding: ActivityTribeEditBinding
    private val REQUEST_LIST_CODE = 1000
    private val REQUEST_CAMERA_CODE = 1001

    private var avatarUrl = UserData.tribe.tribeInfo!!.avatar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTribeEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
        binding.etName.setText(UserData.tribe.tribeInfo!!.name)
        binding.ivAvatar.clipToOutline = true
        if (UserData.tribe.tribeInfo!!.avatar.isNotEmpty()) {
            Glide.with(this).load(UserData.tribe.tribeInfo!!.avatar).placeholder(R.drawable.avatar_user).centerCrop()
                .into(binding.ivAvatar)
        } else {
            Glide.with(this).load(R.drawable.avatar_user).centerCrop().into(binding.ivAvatar)
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

        binding.llDeleteTribe.setOnClickListener {
            XPopup.Builder(this)
                .asConfirm("", "Are you sure to delete this tribe？") {
                    UserData.deleteTribeDetail(UserData.tribe.tribeInfo!!.code, completeBlock = {})
                    UserData.deleteTribeInfo {  }
                    UserData.tribe.tribeDetail = null
                    UserData.tribe.tribeInfo = null
                    finish()
                }.show()
        }

        binding.tvSave.setOnClickListener {
            val name: String = binding.etName.text.toString().trim { it <= ' ' }
            if (name.length < 4) {
                showToast(resources.getString(R.string.enter_valid_tribe))
                return@setOnClickListener
            }
            if (UserData.tribe.tribeInfo!!.name == name && UserData.tribe.tribeInfo!!.avatar == avatarUrl) {
                finish()
            } else {
                UserData.tribe.tribeInfo!!.name = name
                UserData.tribe.tribeInfo!!.avatar = avatarUrl
                UserData.tribe.tribeDetail!!.name = name
                UserData.tribe.tribeDetail!!.avatar = avatarUrl
                UserData.updateTribeInfo(null)
                UserData.updateTribeDetail(null)
                finish()
            }
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
                    avatarUrl = result
                }
            }
            val filePath = "file://$path"
            Glide.with(this).load(filePath).transform(CenterInside(), RoundedCorners(50)).into(binding.ivAvatar)
        } else if (requestCode == REQUEST_CAMERA_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            var path = data.getStringExtra("result")

            if (path != null) {
                UserData.uploadImageToFirebase(path) { complete, result ->
                    if (complete && result != null) {
                        avatarUrl = result
                    }
                }
                val filePath = "file://$path"
                Glide.with(this).load(filePath).into(binding.ivAvatar)
            }
        }
    }
}