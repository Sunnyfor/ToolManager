package com.sunny.toolmanager.flow_layout


import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sunny.toolmanager.MyApplication
import com.sunny.toolmanager.R
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Desc 流式标签,可以直接设置标签样式
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019年11月12日 10:59:54
 */
class FlowLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ViewGroup(context, attrs) {

    companion object {
        /**
         * Special value for the child view spacing.
         * SPACING_AUTO means that the actual spacing is calculated according to the size of the
         * container and the number of the child views, so that the child views are placed evenly in
         * the container.
         */
        const val SPACING_AUTO = -65536
        /**
         * Special value for the horizontal spacing of the child views in the last row
         * SPACING_ALIGN means that the horizontal spacing of the child views in the last row keeps
         * the same with the spacing used in the row above. If there is only one row, this value is
         * ignored and the spacing will be calculated according to childSpacing.
         */
        const val SPACING_ALIGN = -65537
        private const val SPACING_UNDEFINED = -65538

        private const val DEFAULT_FLOW = true
        private const val DEFAULT_CHILD_SPACING = 0
        private const val DEFAULT_ROW_SPACING = 0f
        private const val DEFAULT_RTL = false
        private const val DEFAULT_MAX_ROWS = Integer.MAX_VALUE
    }

    private var mFlow = DEFAULT_FLOW
    private var mChildSpacing = DEFAULT_CHILD_SPACING
    private var mChildSpacingForLastRow = SPACING_UNDEFINED
    private var mRowSpacing = DEFAULT_ROW_SPACING
    private var mAdjustedRowSpacing = DEFAULT_ROW_SPACING
    private var mRtl = DEFAULT_RTL
    private var mMaxRows = DEFAULT_MAX_ROWS

    private val mHorizontalSpacingForRow = ArrayList<Float>()
    private val mHeightForRow = ArrayList<Int>()
    private val mChildNumForRow = ArrayList<Int>()

    /**
     * Sets whether to allow child views flow to next row when there is no enough space.
     * Returns whether to allow child views flow to next row when there is no enough space.
     *
     * @return Whether to flow child views to next row when there is no enough space.
     */
    var isFlow: Boolean
        get() = mFlow
        set(flow) {
            mFlow = flow
            requestLayout()
        }

    /**
     * Sets the horizontal spacing between child views.
     * Returns the horizontal spacing between child views.
     *
     * @return The spacing, either [FlowLayout.SPACING_AUTO], or a fixed size in pixels.
     * pixels.
     */
    var childSpacing: Int
        get() = mChildSpacing
        set(childSpacing) {
            mChildSpacing = childSpacing
            requestLayout()
        }

    /**
     * Sets the horizontal spacing between child views of the last row.
     * Returns the horizontal spacing between child views of the last row.
     *
     * @return The spacing, either [FlowLayout.SPACING_AUTO], [FlowLayout.SPACING_ALIGN], or a fixed size in pixels
     */
    var childSpacingForLastRow: Int
        get() = mChildSpacingForLastRow
        set(childSpacingForLastRow) {
            mChildSpacingForLastRow = childSpacingForLastRow
            requestLayout()
        }

    /**
     * Returns the vertical spacing between rows.
     * Sets the vertical spacing between rows in pixels. Use SPACING_AUTO to evenly place all rows
     * in vertical.
     *
     * pixels.
     */
    var rowSpacing: Float
        get() = mRowSpacing
        set(rowSpacing) {
            mRowSpacing = rowSpacing
            requestLayout()
        }

    /**
     * Returns the maximum number of rows of the FlowLayout.
     * Sets the height of the FlowLayout to be at most maxRows tall.
     */
    var maxRows: Int
        get() = mMaxRows
        set(maxRows) {
            mMaxRows = maxRows
            requestLayout()
        }

    init {

        val a = context.theme.obtainStyledAttributes(
            attrs, R.styleable.FlowLayout, 0, 0
        )
        try {
            mFlow = a.getBoolean(R.styleable.FlowLayout_flFlow, DEFAULT_FLOW)
            mChildSpacing = try {
                a.getInt(R.styleable.FlowLayout_flChildSpacing, DEFAULT_CHILD_SPACING)
            } catch (e: NumberFormatException) {
                a.getDimensionPixelSize(
                    R.styleable.FlowLayout_flChildSpacing,
                    dpToPx(DEFAULT_CHILD_SPACING.toFloat()).toInt()
                )
            }

            mChildSpacingForLastRow = try {
                a.getInt(R.styleable.FlowLayout_flChildSpacingForLastRow, SPACING_UNDEFINED)
            } catch (e: NumberFormatException) {
                a.getDimensionPixelSize(
                    R.styleable.FlowLayout_flChildSpacingForLastRow,
                    dpToPx(DEFAULT_CHILD_SPACING.toFloat()).toInt()
                )
            }

            mRowSpacing = try {
                a.getInt(R.styleable.FlowLayout_flRowSpacing, 0).toFloat()
            } catch (e: NumberFormatException) {
                a.getDimension(R.styleable.FlowLayout_flRowSpacing, dpToPx(DEFAULT_ROW_SPACING))
            }

            mMaxRows = a.getInt(R.styleable.FlowLayout_flMaxRows, DEFAULT_MAX_ROWS)
            mRtl = a.getBoolean(R.styleable.FlowLayout_flRtl, DEFAULT_RTL)
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        mHorizontalSpacingForRow.clear()
        mChildNumForRow.clear()
        mHeightForRow.clear()

        var measuredHeight = 0
        var measuredWidth = 0
        val childCount = childCount
        var rowWidth = 0
        var maxChildHeightInRow = 0
        var childNumInRow = 0
        val rowSize = widthSize - paddingLeft - paddingRight
        val allowFlow = widthMode != MeasureSpec.UNSPECIFIED && mFlow
        val childSpacing =
            if (mChildSpacing == SPACING_AUTO && widthMode == MeasureSpec.UNSPECIFIED)
                0
            else
                mChildSpacing
        val tmpSpacing = (if (childSpacing == SPACING_AUTO) 0 else childSpacing).toFloat()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }

            val childParams = child.layoutParams
            var horizontalMargin = 0
            var verticalMargin = 0
            if (childParams is MarginLayoutParams) {
                measureChildWithMargins(
                    child,
                    widthMeasureSpec,
                    0,
                    heightMeasureSpec,
                    measuredHeight
                )
                horizontalMargin = childParams.leftMargin + childParams.rightMargin
                verticalMargin = childParams.topMargin + childParams.bottomMargin
            } else {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }

            val childWidth = child.measuredWidth + horizontalMargin
            val childHeight = child.measuredHeight + verticalMargin
            if (allowFlow && rowWidth + childWidth > rowSize) { // Need flow to next row
                // Save parameters for current row
                mHorizontalSpacingForRow.add(
                    getSpacingForRow(childSpacing, rowSize, rowWidth, childNumInRow)
                )
                mChildNumForRow.add(childNumInRow)
                mHeightForRow.add(maxChildHeightInRow)
                if (mHorizontalSpacingForRow.size <= mMaxRows) {
                    measuredHeight += maxChildHeightInRow
                }
                measuredWidth = Math.max(measuredWidth, rowWidth)

                // Place the child view to next row
                childNumInRow = 1
                rowWidth = childWidth + tmpSpacing.toInt()
                maxChildHeightInRow = childHeight
            } else {
                childNumInRow++
                rowWidth += (childWidth + tmpSpacing).toInt()
                maxChildHeightInRow = max(maxChildHeightInRow, childHeight)
            }
        }

        // Measure remaining child views in the last row
        when {
            mChildSpacingForLastRow == SPACING_ALIGN ->
                // For SPACING_ALIGN, use the same spacing from the row above if there is more than one row.
                if (mHorizontalSpacingForRow.size >= 1) {
                    mHorizontalSpacingForRow.add(mHorizontalSpacingForRow[mHorizontalSpacingForRow.size - 1])
                } else {
                    mHorizontalSpacingForRow.add(
                        getSpacingForRow(
                            childSpacing,
                            rowSize,
                            rowWidth,
                            childNumInRow
                        )
                    )
                }
            mChildSpacingForLastRow != SPACING_UNDEFINED ->
                // For SPACING_AUTO and specific DP values, apply them to the spacing strategy.
                mHorizontalSpacingForRow.add(
                    getSpacingForRow(
                        mChildSpacingForLastRow,
                        rowSize,
                        rowWidth,
                        childNumInRow
                    )
                )
            else ->
                // For SPACING_UNDEFINED, apply childSpacing to the spacing strategy for the last row.
                mHorizontalSpacingForRow.add(
                    getSpacingForRow(
                        childSpacing,
                        rowSize,
                        rowWidth,
                        childNumInRow
                    )
                )
        }

        mChildNumForRow.add(childNumInRow)
        mHeightForRow.add(maxChildHeightInRow)
        if (mHorizontalSpacingForRow.size <= mMaxRows) {
            measuredHeight += maxChildHeightInRow
        }
        measuredWidth = max(measuredWidth, rowWidth)

        when {
            childSpacing == SPACING_AUTO -> measuredWidth = widthSize
            widthMode == MeasureSpec.UNSPECIFIED -> measuredWidth += paddingLeft + paddingRight
            else -> measuredWidth = min(measuredWidth + paddingLeft + paddingRight, widthSize)
        }

        measuredHeight += paddingTop + paddingBottom
        val rowNum = min(mHorizontalSpacingForRow.size, mMaxRows)
        val rowSpacing =
            if (mRowSpacing == SPACING_AUTO.toFloat() && heightMode == MeasureSpec.UNSPECIFIED)
                0f
            else
                mRowSpacing
        if (rowSpacing == SPACING_AUTO.toFloat()) {
            mAdjustedRowSpacing =
                if (rowNum > 1) ((heightSize - measuredHeight) / (rowNum - 1)).toFloat() else 0f
            measuredHeight = heightSize
        } else {
            mAdjustedRowSpacing = rowSpacing
            if (rowNum > 1) {
                measuredHeight = if (heightMode == MeasureSpec.UNSPECIFIED)
                    (measuredHeight + mAdjustedRowSpacing * (rowNum - 1)).toInt()
                else
                    min((measuredHeight + mAdjustedRowSpacing * (rowNum - 1)).toInt(), heightSize)
            }
        }

        measuredWidth = if (widthMode == MeasureSpec.EXACTLY) widthSize else measuredWidth
        measuredHeight = if (heightMode == MeasureSpec.EXACTLY) heightSize else measuredHeight
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        var x = if (mRtl) width - paddingRight else paddingLeft
        var y = paddingTop

        val rowCount = mChildNumForRow.size
        var childIdx = 0
        for (row in 0 until rowCount) {
            val childNum = mChildNumForRow[row]
            val rowHeight = mHeightForRow[row]
            val spacing = mHorizontalSpacingForRow[row]
            var i = 0
            while (i < childNum && childIdx < childCount) {
                val child = getChildAt(childIdx++)
                if (child.visibility == View.GONE) continue else i++

                val childParams = child.layoutParams
                var marginLeft = 0
                var marginTop = 0
                var marginRight = 0
                if (childParams is MarginLayoutParams) {
                    marginLeft = childParams.leftMargin
                    marginRight = childParams.rightMargin
                    marginTop = childParams.topMargin
                }

                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                if (mRtl) {
                    child.layout(
                        x - marginRight - childWidth,
                        y + marginTop,
                        x - marginRight,
                        y + marginTop + childHeight
                    )
                    x -= (childWidth.toFloat() + spacing + marginLeft.toFloat() + marginRight.toFloat()).toInt()
                } else {
                    child.layout(
                        x + marginLeft,
                        y + marginTop,
                        x + marginLeft + childWidth,
                        y + marginTop + childHeight
                    )
                    x += (childWidth.toFloat() + spacing + marginLeft.toFloat() + marginRight.toFloat()).toInt()
                }
            }
            x = if (mRtl) width - paddingRight else paddingLeft
            y += (rowHeight + mAdjustedRowSpacing).toInt()
        }
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    private fun getSpacingForRow(
        spacingAttribute: Int,
        rowSize: Int,
        usedSize: Int,
        childNum: Int
    ): Float {
        return if (spacingAttribute == SPACING_AUTO) {
            if (childNum > 1) ((rowSize - usedSize) / (childNum - 1)).toFloat() else 0f
        } else {
            spacingAttribute.toFloat()
        }
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
        )
    }


    /**
     * 添加标签，设置标签样式
     */
    fun buildLabel(str: String): TextView {

        val left = MyApplication.getInstance()
            .resources.getDimensionPixelOffset(R.dimen.flow_layout_margin)
        val right = MyApplication.getInstance()
            .resources.getDimensionPixelOffset(R.dimen.flow_layout_margin)
        val top =
            MyApplication.getInstance().resources.getDimensionPixelOffset(R.dimen.flow_layout_top)
        val bottom =
            MyApplication.getInstance().resources.getDimensionPixelOffset(R.dimen.flow_layout_top)
        val textView = TextView(MyApplication.getInstance())
        textView.text = str
        textView.textSize = 12f
        textView.gravity = Gravity.CENTER

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.flow_layout_label_pink))
            textView.background = getLabelBgColor(
                R.color.flow_layout_label_pink_alpha,
                R.color.flow_layout_label_pink
            )
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.flow_layout_label_blue))
            textView.setBackgroundResource(R.drawable.flow_layout_label_default_bg)
        }

        textView.setPadding(left, top, right, bottom)

        return textView
    }

    /**
     * 设置边框，背景，圆角
     */
    private fun getLabelBgColor(solidColor: Int, strokeColor: Int): GradientDrawable {
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.RECTANGLE
        gd.cornerRadius = 2f
        gd.setColor(ContextCompat.getColor(MyApplication.getInstance(), solidColor))
        gd.setStroke(2, ContextCompat.getColor(MyApplication.getInstance(), strokeColor))
        return gd
    }
}