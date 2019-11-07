package com.sunny.toolmanager

import android.view.View
import android.widget.Toast
import com.sunny.toolmanager.base.BaseActivity

class TitleActivity : BaseActivity() {

    override fun setLayout(): Int = R.layout.activity_main

    // 隐藏标题栏
//    override fun initTitle(): View? = null

    // 默认标题栏
//    override fun initTitle(): View? = titleManager.defaultTitle("主页")

    override fun initTitle(): View? =
        titleManager.rightTitle("home", "setting", onRightTextClickListener = {
            Toast.makeText(this, "hah", Toast.LENGTH_SHORT).show()
        })


    override fun initView() {

    }

    override fun onClickEvent(v: View) {
    }
}
