package com.instructure.teacher.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.isRTL
import com.instructure.teacher.R

class CommentTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {


    /** The radius of the three normal corner */
    var cornerRadius: Float = context.DP(12f)
        set(value) {
            field = value; invalidate()
        }

    /** Paint used to draw the bubble background */
    private var mBubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Path that defines the area of the bubble background */
    private val mBubblePath = Path()

    /** Radius of the tail's curve*/
    private var mTailRadius = context.DP(20)

    /** Distance between this view and the avatar view around which the tail curls */
    private var mAvatarMargin = context.DP(4)

    /**
     * ID of the avatar view around which the tail curls. This may be specified to automatically
     * determine [mAvatarMargin] and [mTailRadius]. The target avatar view must be a sibling of
     * this view
     */
    private var mTargetAvatarId = 0

    /** Sets the fill color of the bubble background */
    fun setBubbleColor(@ColorInt color: Int) {
        mBubblePaint.color = color
        invalidate()
    }

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CommentTextView)
            mBubblePaint.color = a.getColor(R.styleable.CommentTextView_bubbleColor, Color.GRAY)
            mTargetAvatarId = a.getResourceId(R.styleable.CommentTextView_targetAvatarId, 0)
            a.recycle()
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // See if we can automatically determine mAvatarMargin and mTailRadius from the avatar view
        if (mTargetAvatarId > 0) {
            (parent as? ViewGroup)?.findViewById<View>(mTargetAvatarId)?.let {
                mTailRadius = Math.max(it.measuredWidth, it.measuredHeight) / 2f
                mAvatarMargin = (t - it.bottom).toFloat()
            }
        }

        val outerRadius = mTailRadius + mAvatarMargin

        val tHeight = outerRadius - Math.sqrt(Math.pow(outerRadius.toDouble(), 2.0) - Math.pow(mTailRadius.toDouble(), 2.0))
        val tailHeight = tHeight.toFloat()

        val tailArcRect = RectF().apply {
            right = outerRadius * 2
            bottom = outerRadius * 2
            offsetTo(mTailRadius - outerRadius, -outerRadius * 2)
        }

        val cornerDiameter = cornerRadius * 2

        val cornerRect = RectF().apply {
            right = cornerDiameter
            bottom = cornerDiameter
        }

        mBubblePath.apply {
            rewind()
            moveTo(0f, -tailHeight)
            val startAngle = 180 - Math.toDegrees(Math.acos(mTailRadius / outerRadius.toDouble())).toFloat()
            arcTo(tailArcRect, startAngle, -startAngle + 90, false)
            lineTo(width - cornerRadius, 0f)
            cornerRect.offsetTo(width - cornerDiameter, 0f)
            arcTo(cornerRect, -90f, 90f, false)
            lineTo(width.toFloat(), height - cornerRadius)
            cornerRect.offsetTo(width - cornerDiameter, height - cornerDiameter)
            arcTo(cornerRect, 0f, 90f, false)
            lineTo(cornerRadius, height.toFloat())
            cornerRect.offsetTo(0f, height - cornerDiameter)
            arcTo(cornerRect, 90f, 90f, false)
            close()
        }
    }

    override fun onDraw(canvas: Canvas) {
        // reverse for RTL and 'outgoing' direction
        if (isRTL()) {
            canvas.save()
            canvas.scale(-1f, 1f, width / 2f, 0f)
            canvas.drawPath(mBubblePath, mBubblePaint)
        }
        canvas.drawPath(mBubblePath, mBubblePaint)
        if (isRTL()) canvas.restore()
        super.onDraw(canvas)
    }

}
