package com.linhua.smartwatch.tribe

import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.databinding.ActivityTribeCreateBinding
import com.linhua.smartwatch.databinding.ActivityTribeEditBinding
import com.linhua.smartwatch.entity.MultipleEntity
import com.linhua.smartwatch.helper.TribeDetail
import com.linhua.smartwatch.helper.TribeInfo
import com.linhua.smartwatch.helper.TribeMember
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.tribe.adapter.TribeMemberAdapter
import com.linhua.smartwatch.tribe.adapter.TribeMemberItem
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnInputConfirmListener
import com.yuyh.library.imgsel.ISNav
import com.yuyh.library.imgsel.config.ISCameraConfig
import com.yuyh.library.imgsel.config.ISListConfig
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil

class TribeCreateActivity : CommonActivity() {

    enum class CreateTribeProgress {
        First,
        Second,
        Third
    }

    private lateinit var binding: ActivityTribeCreateBinding
    var memberItemList = mutableListOf<TribeMemberItem>()
    private val memberAdapter = TribeMemberAdapter(mutableListOf())
    private val REQUEST_LIST_CODE = 1000
    private val REQUEST_CAMERA_CODE = 1001
    private var tribeInfo: TribeInfo = UserData.tribe.tribeInfo ?: TribeInfo()
    private var tribeDetail = TribeDetail()
    private var progress: CreateTribeProgress = CreateTribeProgress.First

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTribeCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.baseTitleBack.setOnClickListener {
            when (progress) {
                CreateTribeProgress.First -> {
                    onBackPressed()
                }
                CreateTribeProgress.Second -> {
                    gotoSteps(CreateTribeProgress.First)
                }
                CreateTribeProgress.Third -> {
                    gotoSteps(CreateTribeProgress.Second)
                }
            }
        }

        binding.llAdd.setOnClickListener {
            XPopup.Builder(this).asInputConfirm("Add member", "Please input email address",
                OnInputConfirmListener { text ->
                    val code = UserData.tribe.tribeInfo!!.code
                    UserData.sendEmail(text, code, completeBlock = {
                        if (it) {
                            ToastUtil.showToast(
                                this,
                                resources.getString(R.string.code_sent)
                            )
                        } else {
                            ToastUtil.showToast(
                                this,
                                resources.getString(R.string.code_fail_sent)
                            )
                        }
                    })
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
        binding.etName.setText(tribeInfo.name)
        binding.tvNext1.setOnClickListener {
            val name: String = binding.etName.text.toString().trim { it <= ' ' }
            if (name.length < 4) {
                showToast(resources.getString(R.string.enter_valid_tribe))
                return@setOnClickListener
            }
            tribeInfo.name = name
            gotoSteps(CreateTribeProgress.Second)
        }

        binding.tvNext2.setOnClickListener {
            checkCodeExist()
            gotoSteps(CreateTribeProgress.Third)
            reloadData()
        }
        binding.tvSkip.setOnClickListener {
            finish()
        }
        binding.tvAdd.setOnClickListener {
            finish()
        }
        binding.rvMembers.apply {
            adapter = memberAdapter
            layoutManager = LinearLayoutManager(this.context)
        }
        if (tribeInfo.avatar.isEmpty()) {
            tribeInfo.avatar = UserData.userInfo.avatar
        }
        tribeInfo.role = 1
        gotoSteps(CreateTribeProgress.First)
    }

    fun reloadData() {
        convertMemberItems()
        memberAdapter.setNewInstance(memberItemList)
        memberAdapter.notifyDataSetChanged()
    }

    fun getRandomString(length: Int) : String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    fun checkCodeExist() {
        if (tribeInfo.code.isEmpty()) {
            val randomString = getRandomString(10)
            UserData.checkCodeExist(randomString) {
                if (it) {
                    checkCodeExist()
                } else {
                    tribeInfo.code = randomString
                    checkCodeExist()
                }
            }
        } else {
            tribeDetail.name = tribeInfo.name
            tribeDetail.avatar = tribeInfo.avatar
            tribeDetail.members.clear()

            val member = TribeMember(UserData.userInfo.name,
                UserData.userInfo.email,
                UserData.userInfo.avatar,
                UserData.healthData.steps,
                UserData.healthData.sleepTime,
                1,
                UserData.healthData.date)
            tribeDetail.addMember(member)
            UserData.tribe.tribeInfo = tribeInfo
            UserData.tribe.tribeDetail = tribeDetail
            UserData.updateTribeInfo {  }
            UserData.updateTribeDetail {  }
            reloadData()
        }
    }

    private fun gotoSteps(progress: CreateTribeProgress) {
        this.progress = progress
        when (progress) {
            CreateTribeProgress.First -> {
                binding.tvSkip.visibility = View.INVISIBLE
                binding.llProgress1.visibility = View.VISIBLE
                binding.llProgress2.visibility = View.GONE
                binding.rlProgress3.visibility = View.GONE
                binding.ivFirst.setImageResource(R.drawable.tribe_progress_primary)
                binding.ivSecond.setImageResource(R.drawable.tribe_progress_2)
                binding.ivThird.setImageResource(R.drawable.tribe_progress_3)
                binding.ivProgress1.setImageResource(R.drawable.tribe_progress_already)
                binding.ivProgress2.setImageResource(R.drawable.tribe_progress_yet)
            }
            CreateTribeProgress.Second -> {
                binding.tvSkip.visibility = View.INVISIBLE
                if (tribeInfo.avatar.isNotEmpty()) {
                    Glide.with(this).load(tribeInfo.avatar).placeholder(R.drawable.avatar_user).centerCrop()
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(75))).into(binding.ivAvatar)
                } else {
                    Glide.with(this).load(R.drawable.avatar_user).centerCrop()
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(75))).into(binding.ivAvatar)
                }
                binding.llProgress1.visibility = View.GONE
                binding.llProgress2.visibility = View.VISIBLE
                binding.rlProgress3.visibility = View.GONE
                binding.ivFirst.setImageResource(R.drawable.icon_done_green)
                binding.ivSecond.setImageResource(R.drawable.tribe_progress_primary)
                binding.ivThird.setImageResource(R.drawable.tribe_progress_3)
                binding.ivProgress1.setImageResource(R.drawable.tribe_progress_already)
                binding.ivProgress2.setImageResource(R.drawable.tribe_progress_yet)

            }
            CreateTribeProgress.Third -> {
                binding.tvSkip.visibility = View.VISIBLE
                binding.llProgress1.visibility = View.GONE
                binding.llProgress2.visibility = View.GONE
                binding.rlProgress3.visibility = View.VISIBLE
                binding.ivFirst.setImageResource(R.drawable.icon_done_green)
                binding.ivSecond.setImageResource(R.drawable.icon_done_green)
                binding.ivThird.setImageResource(R.drawable.tribe_progress_primary)
                binding.ivProgress1.setImageResource(R.drawable.tribe_progress_already)
                binding.ivProgress2.setImageResource(R.drawable.tribe_progress_yet)
                reloadData()
            }
        }
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