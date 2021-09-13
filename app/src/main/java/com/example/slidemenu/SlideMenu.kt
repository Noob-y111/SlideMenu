package com.example.slidemenu

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Scroller
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.children
import java.lang.IllegalArgumentException

class SlideMenu : ViewGroup {

    companion object {
        const val TAG = "SlideMenu"
    }

    //每个子菜单属性
    data class SlideAttrs(
        var text: String,
        @ColorInt
        var backgroundColor: Int,
        @ColorInt
        var textColor: Int,
        var clickListener: OnClickListener
    )

    enum class Direction {
        LEFT, RIGHT
    }

    //子菜单初始个数
    private var subMenuCount = 0
    //内容View
    private var contentView: View? = null
    //子菜单container
    private var linearLayout: LinearLayout? = null
    //子菜单字体初始大小
    private var slideTextSize = 15f
    //按下坐标x
    private var startX = 0
    //滑动器
    private var scroller = Scroller(context)
    //子菜单是否打开
    private var slideMenuIsOpen = false
    //滑动方向
    private var direction: Direction? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        getAttrs(context = context, attrs = attrs)
    }

    private fun setTextView(view: TextView, attrs: SlideAttrs) {
        view.setTextColor(attrs.textColor)
        view.setBackgroundColor(attrs.backgroundColor)
        view.text = attrs.text
        view.textSize = slideTextSize
        view.setOnClickListener(attrs.clickListener)
    }

    fun addMenu(behavior: () -> Array<SlideAttrs>) {
        val array = behavior()
        if (array.size != subMenuCount) throw IllegalArgumentException("属性数量和子菜单不匹配")
        linearLayout?.children?.forEachIndexed { index, view ->
            setTextView(view = (view as TextView), attrs = array[index])
        }
    }

    private fun getAttrs(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.SlideMenu).apply {
            subMenuCount = getInt(R.styleable.SlideMenu_subMenuCount, 0)
            slideTextSize = getDimension(R.styleable.SlideMenu_slideTextSize, 10f)
            recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Log.d(TAG, "onFinishInflate: childCount: $childCount")
        if (childCount > 1) throw IllegalArgumentException("子视图数量不能超过1")
        contentView = getChildAt(0)
        if (contentView == null) throw Exception("至少需要一个子视图")
        linearLayout = LinearLayout(context)
        initSubMenu()
        addView(linearLayout)
    }

    private fun initSubMenu() {
        contentView?.let {
            linearLayout?.orientation = LinearLayout.HORIZONTAL
            repeat(subMenuCount) { _ ->
                TextView(context).also {
                    it.gravity = Gravity.CENTER
                    it.layoutParams =
                        LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT).apply {
                            weight = 1f
                        }
                    linearLayout?.addView(it)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "onMeasure: ")
        //控件内容部分
        val containerHeight = MeasureSpec.getSize(heightMeasureSpec)
        val containerWidth = MeasureSpec.getSize(widthMeasureSpec)
        val contentHeightMeasureSpec =
            when (val contentHeight = contentView!!.layoutParams.height) {
                LayoutParams.MATCH_PARENT -> MeasureSpec.makeMeasureSpec(
                    containerHeight,
                    MeasureSpec.EXACTLY
                )
                LayoutParams.WRAP_CONTENT -> MeasureSpec.makeMeasureSpec(
                    containerHeight,
                    MeasureSpec.AT_MOST
                )
                else -> MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY)
            }
        contentView!!.measure(widthMeasureSpec, contentHeightMeasureSpec)

        //测量滑动菜单部分
        val slideWidth = containerWidth / 4 * 3
        linearLayout?.measure(
            MeasureSpec.makeMeasureSpec(
                slideWidth,
                MeasureSpec.EXACTLY
            ), contentHeightMeasureSpec
        )

        //控件整体部分
        //宽度为两个控件相加 高度相等
        setMeasuredDimension(containerWidth + slideWidth, contentView!!.measuredHeight)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = (it.x).toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    onTouchMove(it.x)
                }
                MotionEvent.ACTION_UP -> {
                    onTouchUp()
                }
                else -> {
                }
            }
        }
        performClick()
        return true
    }

    private fun onTouchMove(x: Float) {
        val distant = x - startX
        direction = if (distant < 0) Direction.LEFT else Direction.RIGHT
        when {
            scrollX - distant <= 0 -> scrollTo(0, 0)
            scrollX - distant >= linearLayout!!.measuredWidth -> scrollTo(
                linearLayout!!.measuredWidth,
                0
            )
            else -> scrollBy((-distant).toInt(), 0)
        }
        startX = x.toInt()
    }

    private fun onTouchUp() {
        val linearLayoutWidth = linearLayout!!.measuredWidth
        if (slideMenuIsOpen) { // 打开状态
            if (direction == Direction.RIGHT) {
                if (scrollX > linearLayoutWidth / 4 * 3) {
                    openMenu()
                } else {
                    closeMenu()
                }
            } else {
                openMenu()
            }
        } else { // 关闭状态
            if (direction == Direction.LEFT) {
                if (scrollX > linearLayoutWidth / 4) {
                    openMenu()
                } else {
                    closeMenu()
                }
            } else {
                closeMenu()
            }
        }
        invalidate()
    }

    private fun openMenu() {
        scroller.startScroll(scrollX, 0, linearLayout!!.measuredWidth - scrollX, 500)
        slideMenuIsOpen = true
    }

    private fun closeMenu() {
        scroller.startScroll(scrollX, 0, -scrollX, 500)
        slideMenuIsOpen = false
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, 0)
            invalidate()
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        Log.d(TAG, "onLayout: ")
        val contentLeft = 0
        val contentTop = 0
        val contentRight = contentView!!.measuredWidth + contentLeft
        val contentBottom = contentView!!.measuredHeight + contentTop
        //摆放内容 ==> contentView
        contentView!!.layout(contentLeft, contentTop, contentRight, contentBottom)
        //摆放菜单 ==> linearLayout
        val slideRight = contentRight + linearLayout!!.measuredWidth
        linearLayout?.layout(contentRight, contentTop, slideRight, contentBottom)
    }
}