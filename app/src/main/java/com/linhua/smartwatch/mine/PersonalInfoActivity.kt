package com.linhua.smartwatch.mine

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.blankj.utilcode.util.ColorUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.databinding.ActivityPersonalInfoBinding
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.utils.DateUtil
import java.util.*


class PersonalInfoActivity : CommonActivity() {
    private lateinit var binding: ActivityPersonalInfoBinding
    private var userInfo = UserData.userInfo.deepCopy()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etName.setText(userInfo.name)
        binding.etSignature.setText(userInfo.signature)
        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvSave.setOnClickListener {
            userInfo.name = binding.etName.text.toString()
            userInfo.signature = binding.etSignature.text.toString()
            UserData.userInfo = userInfo
            val db = Firebase.firestore
            val profile = hashMapOf(
                "name" to userInfo.name,
                "avatar" to userInfo.avatar,
                "signature" to userInfo.signature,
                "email" to userInfo.email,
                "sex" to userInfo.sex,
                "height" to userInfo.height,
                "weight" to userInfo.weight,
                "birthday" to userInfo.birthday,
            )

            db.collection("profile").document(FirebaseAuth.getInstance().currentUser!!.uid).set(
                profile).addOnSuccessListener {
                    finish()
            }.addOnFailureListener {
                showToast( "Sign Up Failded")
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
//                    binding.spinnerHeight.prompt = heightArray[pos]
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
//                    binding.spinnerWeight.prompt = weightArray[pos]
                    userInfo.weight = 150 + pos
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        if (userInfo.weight in 150..2000) {
            binding.spinnerWeight.setSelection(userInfo.weight - 150)
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
}