package com.sunny.toolmanager

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sunny.toolmanager.base.BaseActivity
import kotlinx.android.synthetic.main.act_flow_layout.*

/**
 * Desc
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/11/12 11:03
 */
class FlowLayoutActivity : BaseActivity() {

    private val labelList =
        arrayOf("Joann", "Jackson", "Elise", "Elsie", "Cherry", "JoannChen", "Susan", "hah")

    override fun setLayout(): Int = R.layout.act_flow_layout

    override fun initTitle(): View? = titleManager.defaultTitle("流式标签")

    override fun initView() {


        flowLayout.removeAllViews()
        flowLayout.childSpacing = resources.getDimensionPixelOffset(R.dimen.flow_layout_margin)
        flowLayout.rowSpacing = 17f

        val lp = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        lp.rightMargin = 4
        labelList.indices
            .filterNot { TextUtils.isEmpty(labelList[it]) }
            .map { flowLayout.buildLabel(labelList[it]) }
            .forEach { flowLayout.addView(it, lp) }

        /************************************** 分割线 **************************************/

        flowLayout2.removeAllViews()
        flowLayout2.childSpacing = resources.getDimensionPixelOffset(R.dimen.flow_layout_margin)
        flowLayout2.rowSpacing = 17f

        val lp2 = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        lp2.rightMargin = 4
        labelList.indices
            .filterNot { TextUtils.isEmpty(labelList[it]) }
            .map { buildLabel(labelList[it], it) }
            .forEach { flowLayout2.addView(it, lp2) }

    }

    override fun onClickEvent(v: View) {

    }

    /************************************** 以下只适用 FlowLayout2 **************************************/

    /**
     * 添加标签，设置标签样式
     */
    private fun buildLabel(text: String, index: Int): TextView {
        val position = index % labelColor.size

        val left = resources.getDimensionPixelOffset(R.dimen.flow_layout_right)
        val right = resources.getDimensionPixelOffset(R.dimen.flow_layout_right)
        val top = resources.getDimensionPixelOffset(R.dimen.flow_layout_top)
        val bottom = resources.getDimensionPixelOffset(R.dimen.flow_layout_top)
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 12f
        textView.gravity = Gravity.CENTER

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setTextColor(ContextCompat.getColor(this, labelColor[position]))
            textView.background = getLabelBgColor(labelColorAlpha[position], labelColor[position])
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.flow_layout_label_default))
            textView.setBackgroundResource(R.drawable.flow_layout_label_default_bg)
        }

        textView.setPadding(left, top, right, bottom)

        textView.setOnClickListener {

        }


        return textView
    }


    /**
     * 边框和文字颜色
     */
    private val labelColor = intArrayOf(
        R.color.flow_layout_label_pink,
        R.color.flow_layout_label_green,
        R.color.flow_layout_label_blue,
        R.color.flow_layout_label_yellow,
        R.color.flow_layout_label_purple
    )

    /**
     * 填充色
     */
    private val labelColorAlpha = intArrayOf(
        R.color.flow_layout_label_pink_alpha,
        R.color.flow_layout_label_green_alpha,
        R.color.flow_layout_label_blue_alpha,
        R.color.flow_layout_label_yellow_alpha,
        R.color.flow_layout_label_purple_alpha
    )

    /**
     * 设置边框，背景，圆角
     */
    private fun getLabelBgColor(solidColor: Int, strokeColor: Int): GradientDrawable {
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.RECTANGLE
        gd.cornerRadius = 2f //圆角
        gd.setColor(ContextCompat.getColor(this, solidColor))//填充色
        gd.setStroke(2, ContextCompat.getColor(this, strokeColor))//边框
        return gd
    }

}