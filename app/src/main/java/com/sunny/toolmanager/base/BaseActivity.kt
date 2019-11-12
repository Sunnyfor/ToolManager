package com.sunny.toolmanager.base

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.sunny.toolmanager.title_manager.TitleManager
import com.sunny.toolmanager.R
import kotlinx.android.synthetic.main.activity_base.*

/**
 * Desc Activity基类
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/11/2 00:31
 */
abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var titleManager: TitleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //强制屏幕

        titleManager = TitleManager(this)

        setContentView(R.layout.activity_base)

        // 初始化标题栏
        if (initTitle() != null) {
            fl_title.removeAllViews()
            fl_title.addView(initTitle())
        }

        // 初始化内容区
        val bodyView = LayoutInflater.from(this).inflate(setLayout(), null, false)
        fl_body.addView(bodyView)

        initView()
    }

    abstract fun setLayout(): Int

    abstract fun initTitle(): View?

    abstract fun initView()

    abstract fun onClickEvent(v: View)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (this.currentFocus != null) {
                if (this.currentFocus?.windowToken != null) {
                    imm.hideSoftInputFromWindow(
                        this.currentFocus?.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            }
        }
        return super.onTouchEvent(event)
    }


    /**
     * 隐藏输入法键盘
     */
    fun hideKeyboard() {
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(
            this.currentFocus?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    /**
     * 拦截按钮多次点击事件
     */
    private var lastClickId = 0
    private var lastClickTime = 0L
    override fun onClick(v: View) {
        if (v.id == lastClickId && System.currentTimeMillis() - lastClickTime < 500) {
            lastClickId = 0
            lastClickTime = 0
            Log.i("JoannChen -- ", "拦截重复点击生效")
            return
        }
        lastClickId = v.id
        lastClickTime = System.currentTimeMillis()
        onClickEvent(v)
    }

}