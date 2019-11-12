package com.sunny.toolmanager.util

import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.StringRes
import com.sunny.toolmanager.MyApplication
import com.sunny.toolmanager.R
import kotlinx.android.synthetic.main.layout_toast_center.view.*


object ToastUtil {
    private var mToast: Toast? = null
    @JvmField
    val mainLooperHandler = Handler(Looper.getMainLooper())

    /***
     * 主线程运行
     * **/
    fun runOnMain(run: () -> Unit) {
        mainLooperHandler.post {
            run.invoke()
        }
    }

    /***
     * 展示Toast
     */
    fun showToast(@StringRes id: Int) {
        showToast(MyApplication.getInstance().resources.getString(id))
    }

    /***
     * 展示Toast
     */
    fun showToast(message: String) {
        showToast(message)
    }


//    private fun showToast(message: String) {
//        runOnMain {
//            showToastCenter(message)
//        }
//    }

//    /***
//     * 延迟展示Toast
//     */
//    fun showToast(message: String, delay: Long?) {
//        Observable.timer(delay!!, TimeUnit.MICROSECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { showToastMessage(message) }
//    }
//
//    /***
//     * 延迟展示Toast
//     */
//    fun showToast(@StringRes id: Int, delay: Long?) {
//        Observable.timer(delay!!, TimeUnit.MICROSECONDS)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { showToastMessage(id) }
//    }

    /**
     * 显示debug Toast
     */
    fun debugToast(msg: String) {
        if (!MyApplication.isDebug) {
            return
        }
        showToast(msg)
    }


    /**
     * 普通Toast
     */
    private fun showToastCenter(message: String) {
        if (mToast == null) {
            mToast = Toast.makeText(MyApplication.getInstance(), message, Toast.LENGTH_SHORT)
            mToast?.show()
        } else {
            mToast?.setText(message)
            mToast?.duration = Toast.LENGTH_SHORT
            mToast?.show()
        }
    }

    /**
     * 自定义圆角、中部显示的Toast
     */
    private fun showToastCircle(message: String) {

        val view = LayoutInflater.from(MyApplication.getInstance())
            .inflate(R.layout.layout_toast_center, null)
        view.tv_toast.text = message

        if (mToast == null) {
            mToast = Toast(MyApplication.getInstance())
        }

        mToast?.view = view
        mToast?.setGravity(Gravity.CENTER, 0, 0)
        mToast?.duration = Toast.LENGTH_SHORT
        mToast?.show()
    }

}