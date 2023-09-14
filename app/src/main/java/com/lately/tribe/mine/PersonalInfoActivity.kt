package com.lately.tribe.mine

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.blankj.utilcode.util.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.lately.tribe.R
import com.lately.tribe.base.CommonActivity
import com.lately.tribe.databinding.ActivityPersonalInfoBinding
import com.lately.tribe.helper.UserData
import com.lately.tribe.utils.DateUtil
import com.lxj.xpopup.XPopup
import com.yuyh.library.imgsel.ISNav
import com.yuyh.library.imgsel.config.ISCameraConfig
import com.yuyh.library.imgsel.config.ISListConfig
import java.util.*


class PersonalInfoActivity : CommonActivity() {
    private lateinit var binding: ActivityPersonalInfoBinding
    private var userInfo = UserData.userInfo.deepCopy()

    private val REQUEST_LIST_CODE = 1000
    private val REQUEST_CAMERA_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ISNav.getInstance().init { context, path, imageView ->
            Glide.with(context).load(path).into(imageView)
        }

        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivAvatar.clipToOutline = true
        if (userInfo.avatar.isNotEmpty()) {
            Glide.with(this).load(userInfo.avatar).centerCrop().into(binding.ivAvatar)
        } else {
            Glide.with(this).load(R.drawable.avatar_user).centerCrop().into(binding.ivAvatar)
        }


        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }

        if (userInfo.signature.isEmpty()) {
            binding.etSignature.setText("Love Sports, Love life~")
        } else {
            binding.etSignature.setText(userInfo.signature)
        }

        if (userInfo.name.isEmpty()) {
            binding.etName.setText("Tribe")
        } else {
            binding.etName.setText(userInfo.name)
        }

        binding.tvSave.setOnClickListener {
            userInfo.name = binding.etName.text.toString()
            userInfo.signature = binding.etSignature.text.toString()
            UserData.userInfo = userInfo
            UserData.saveUserInfo { result ->
                if (result) {
                    finish()
                } else {
                    showToast( "Save Failed")
                }
            }
        }

        if (userInfo.birthday.isNotEmpty()) {
            binding.tvBirthday.text = userInfo.birthday
        } else {
            binding.tvBirthday.text = "1995/05/27"
        }

        binding.rlBirthday.setOnClickListener {
            // 日期选择器
            val ca = Calendar.getInstance()
            if (userInfo.birthday.isNotEmpty()) {
                ca.time = DateUtil.getYMDDateString2(userInfo.birthday)
            } else {
                ca.time = DateUtil.getYMDDateString2("1995/05/27")
            }

            var mYear = ca[Calendar.YEAR]
            var mMonth = ca[Calendar.MONTH]
            var mDay = ca[Calendar.DAY_OF_MONTH]

            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    mYear = year
                    mMonth = month
                    mDay = dayOfMonth
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, dayOfMonth)
                    userInfo.birthday = DateUtil.getYMDDate2(calendar.time).toString()
                    binding.tvBirthday.text = userInfo.birthday
                },
                mYear, mMonth, mDay
            )
            datePickerDialog.show()
        }


        var heightArray = mutableListOf<String>()
        for (index in 30..300) {
            heightArray.add(index.toString())
        }

        val adapter = ArrayAdapter<String>(this, R.layout.item_spinner, heightArray)
        binding.spinnerHeight.adapter = adapter
        binding.spinnerHeight.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    userInfo.height = 30 + pos
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        if (userInfo.height in 30..300) {
            binding.spinnerHeight.setSelection(userInfo.height - 30)
        }

        var weightArray = mutableListOf<String>()
        for (index in 150..2000) {
            weightArray.add(String.format("%.1f", index / 10.0))
        }
        val adapter2 = ArrayAdapter<String>(this, R.layout.item_spinner, weightArray)
        binding.spinnerWeight.adapter = adapter2
        binding.spinnerWeight.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    userInfo.weight = 150 + pos
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        if (userInfo.weight in 150..2000) {
            binding.spinnerWeight.setSelection(userInfo.weight - 150)
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

        when(userInfo.sex) {
            0 -> {
                binding.tvMale.setTextColor(ColorUtils.getColor(R.color.primary_black))
                binding.tvFemale.setTextColor(ColorUtils.getColor(R.color.light_gray))
                binding.tvOther.setTextColor(ColorUtils.getColor(R.color.light_gray))
                binding.ivMaleCheck.setImageResource(R.drawable.simple_check)
                binding.ivFemaleCheck.setImageResource(R.drawable.simple_uncheck)
                binding.ivOtherCheck.setImageResource(R.drawable.simple_uncheck)
            }
            1 -> {
                binding.tvMale.setTextColor(ColorUtils.getColor(R.color.light_gray))
                binding.tvFemale.setTextColor(ColorUtils.getColor(R.color.primary_black))
                binding.tvOther.setTextColor(ColorUtils.getColor(R.color.light_gray))
                binding.ivMaleCheck.setImageResource(R.drawable.simple_uncheck)
                binding.ivFemaleCheck.setImageResource(R.drawable.simple_check)
                binding.ivOtherCheck.setImageResource(R.drawable.simple_uncheck)
            }
            2 -> {
                binding.tvMale.setTextColor(ColorUtils.getColor(R.color.light_gray))
                binding.tvFemale.setTextColor(ColorUtils.getColor(R.color.light_gray))
                binding.tvOther.setTextColor(ColorUtils.getColor(R.color.primary_black))
                binding.ivMaleCheck.setImageResource(R.drawable.simple_uncheck)
                binding.ivFemaleCheck.setImageResource(R.drawable.simple_uncheck)
                binding.ivOtherCheck.setImageResource(R.drawable.simple_check)
            }
        }
        binding.rlMale.setOnClickListener {
            binding.tvMale.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.tvFemale.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.tvOther.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.ivMaleCheck.setImageResource(R.drawable.simple_check)
            binding.ivFemaleCheck.setImageResource(R.drawable.simple_uncheck)
            binding.ivOtherCheck.setImageResource(R.drawable.simple_uncheck)
            userInfo.sex = 0
        }

        binding.rlFemale.setOnClickListener {
            binding.tvMale.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.tvFemale.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.tvOther.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.ivMaleCheck.setImageResource(R.drawable.simple_uncheck)
            binding.ivFemaleCheck.setImageResource(R.drawable.simple_check)
            binding.ivOtherCheck.setImageResource(R.drawable.simple_uncheck)
            userInfo.sex = 1
        }
        binding.rlOther.setOnClickListener {
            binding.tvMale.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.tvFemale.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.tvOther.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.ivMaleCheck.setImageResource(R.drawable.simple_uncheck)
            binding.ivFemaleCheck.setImageResource(R.drawable.simple_uncheck)
            binding.ivOtherCheck.setImageResource(R.drawable.simple_check)
            userInfo.sex = 2
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
        if (requestCode == REQUEST_LIST_CODE && resultCode == RESULT_OK && data != null) {
            val pathList: List<String>? = data.getStringArrayListExtra("result")

            ///storage/emulated/0/Android/media/com.linhua.smartwatch/1680442090933.jpg
            if (pathList == null || pathList.isEmpty())return
            val path = pathList[0]
            UserData.uploadImageToFirebase(path) { complete, result ->
                if (complete && result != null) {
                    UserData.userInfo.avatar = result
                    UserData.saveUserInfo(null)
                }
            }
            val filePath = "file://$path"
            Glide.with(this).load(filePath).transform(CenterInside(),RoundedCorners(50)).into(binding.ivAvatar)
        } else if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK && data != null) {
            val path = data.getStringExtra("result")
            if (path != null) {
                UserData.uploadImageToFirebase(path) { complete, result ->
                    if (complete && result != null) {
                        UserData.userInfo.avatar = result
                        UserData.saveUserInfo(null)
                    }
                }
                val filePath = "file://$path"
                Glide.with(this).load(filePath).into(binding.ivAvatar)
            }
        }
    }
}