package com.example.myot.ticket.calendar.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withSave
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.myot.R
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class TicketCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    enum class Mode { DEFAULT, MINI }

    private var mode: Mode = Mode.DEFAULT

    init {
        context.obtainStyledAttributes(attrs, R.styleable.TicketCalendarView).apply {
            mode = if (getInt(R.styleable.TicketCalendarView_tcv_mode, 0) == 1)
                Mode.MINI else Mode.DEFAULT
        }.recycle()
    }

    // 날짜 계산
    private var ym: YearMonth = YearMonth.now()
    private var firstDayIndex: Int = 0 // 0=Sun..6=Sat
    private var daysInMonth: Int = ym.lengthOfMonth()

    // 렌더 관련
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(0.75f)
        color = Color.TRANSPARENT
    }
    private val dayTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(10f)
        color = ContextCompat.getColor(context, R.color.gray3)
        typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
    }

    private val weekdayTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(12f)
        color = Color.parseColor("#8E8E93")
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    private var weekdayLabels = arrayOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
    fun setWeekdayLabels(labels: List<String>) {
        if (labels.size == 7) {
            weekdayLabels = labels.toTypedArray()
            invalidate()
        }
    }

    private val dayTextBounds = Rect()
    private val cellPadding = dp(2f)
    private var cellW = 0f
    private var cellH = 0f
    private val colCount = 7
    private val rowCount = 6

    private val cellCorner = dp(3f)

    /** 헤더 높이(텍스트 높이 + 여백) */
    private var headerH = computeHeaderHeight()
    private fun computeHeaderHeight(): Float {
        val fm = weekdayTextPaint.fontMetrics
        return (fm.descent - fm.ascent) + dp(10f)
    }

    // 배경 드로어블(각 날짜 칸에 적용)
    private val dayBg: Drawable? by lazy {
        ContextCompat.getDrawable(context, R.drawable.bg_calendar_day)
    }

    // 이미지 캐시
    private val imageCache: MutableMap<Pair<Int, Int>, Bitmap> = mutableMapOf()
    // 기록 맵(day -> imageUrl)
    private val recordMap: MutableMap<Int, List<String>> = mutableMapOf()

    interface Listener {
        fun onClickDay(date: LocalDate, hasRecord: Boolean)
        fun onClickWholeMini() {}
    }
    var listener: Listener? = null

    fun setMode(m: Mode) { mode = m; invalidate() }

    fun setMonth(year: Int, month: Int) {
        ym = YearMonth.of(year, month)
        daysInMonth = ym.lengthOfMonth()
        val first = ym.atDay(1)
        firstDayIndex = (first.dayOfWeek.value % 7) // Sun->0
        imageCache.clear()
        invalidate()
    }

    fun setRecords(dayToImageUrls: Map<Int, List<String>>) {
        recordMap.clear()
        // 4장 이상이 들어와도 3장으로 자름
        dayToImageUrls.forEach { (d, urls) ->
            recordMap[d] = urls.take(3)
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        headerH = computeHeaderHeight()
        val availH = (h - headerH).coerceAtLeast(0f)
        cellW = w / colCount.toFloat()
        cellH = availH / rowCount.toFloat()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawWeekHeader(canvas)
        drawGrid(canvas)
        drawCellsBackground(canvas)
        drawDays(canvas)
        drawImages(canvas)
    }

    private fun drawWeekHeader(canvas: Canvas) {
        val fm = weekdayTextPaint.fontMetrics
        val baseY = -fm.ascent + dp(2f)
        for (c in 0 until colCount) {
            val cx = c * cellW + cellW / 2f
            canvas.drawText(weekdayLabels[c], cx, baseY, weekdayTextPaint)
        }
    }

    private fun drawGrid(canvas: Canvas) {
        for (r in 0..rowCount) {
            val y = r * cellH
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }
        for (c in 0..colCount) {
            val x = c * cellW
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
        }
    }

    /** 각 날짜 칸에 bg_calendar_day.xml 그리기 */
    private fun drawCellsBackground(canvas: Canvas) {
        val rect = Rect()
        for (day in 1..daysInMonth) {
            val pos = firstDayIndex + (day - 1)
            val r = pos / colCount
            val c = pos % colCount
            rect.set(
                (c * cellW).toInt(),
                (headerH + r * cellH).toInt(),
                ((c + 1) * cellW).toInt(),
                (headerH + (r + 1) * cellH).toInt()
            )
            dayBg?.setBounds(rect)
            dayBg?.draw(canvas)
        }
    }

    private fun drawDays(canvas: Canvas) {
        val fm = dayTextPaint.fontMetrics
        val baseY = -fm.ascent + dp(2f)
        for (day in 1..daysInMonth) {
            // 일정 있으면 숫자 그리지 않기
            val hasImages = recordMap[day]?.isNotEmpty() == true
            if (hasImages) continue
            
            val pos = firstDayIndex + (day - 1)
            val r = pos / colCount
            val c = pos % colCount
            val cx = c * cellW
            val cy = headerH + r * cellH
            val label = day.toString()
            dayTextPaint.getTextBounds(label, 0, label.length, dayTextBounds)
            val tx = cx + dp(6f)
            val ty = cy + baseY
            canvas.drawText(label, tx, ty, dayTextPaint)
        }
    }

    private fun drawImages(canvas: Canvas) {
        for (day in 1..daysInMonth) {
            val urls = recordMap[day] ?: continue
            if (urls.isEmpty()) continue

            // 셀 전체 영역(패딩만 남기고 전부 이미지로)
            val pos = firstDayIndex + (day - 1)
            val r = pos / colCount
            val c = pos % colCount
            val outer = RectF(
                c * cellW + cellPadding,
                headerH + r * cellH + cellPadding,
                (c + 1) * cellW - cellPadding,
                headerH + (r + 1) * cellH - cellPadding
            )

            // 라운드 클리핑(셀 전체)
            val cellPath = Path().apply { addRoundRect(outer, cellCorner, cellCorner, Path.Direction.CW) }
            canvas.withSave {
                canvas.clipPath(cellPath)

                // 타일 rect 계산 (1~3개)
                val tiles = calcTiles(outer, urls.size)

                // 각 타일에 개별 이미지(centerCrop)
                tiles.forEachIndexed { idx, tile ->
                    val key = day to idx
                    val cached = imageCache[key]
                    if (cached == null) {
                        Glide.with(this@TicketCalendarView)
                            .asBitmap()
                            .load(urls[idx])
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    imageCache[key] = resource
                                    invalidate()
                                }
                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })
                    } else {
                        val src = centerCropSrcRect(cached, tile)
                        canvas.drawBitmap(cached, src, tile, null)
                    }
                }
            }
        }
    }

    /** 1개: 전체 / 2개: 좌우 반반 / 3개: 상단 전체(1/2h) + 하단 좌우 반반 */
    private fun calcTiles(outer: RectF, count: Int): List<RectF> {
        return when (count.coerceIn(1, 3)) {
            1 -> listOf(RectF(outer))
            2 -> {
                val midX = (outer.left + outer.right) / 2f
                listOf(
                    RectF(outer.left, outer.top, midX, outer.bottom),
                    RectF(midX, outer.top, outer.right, outer.bottom)
                )
            }
            else -> {
                val midY = (outer.top + outer.bottom) / 2f
                val halfY = RectF(outer.left, outer.top, outer.right, midY)           // 상단 전체
                val midX = (outer.left + outer.right) / 2f
                val bottomLeft = RectF(outer.left, midY, midX, outer.bottom)         // 하단 좌
                val bottomRight = RectF(midX, midY, outer.right, outer.bottom)       // 하단 우
                listOf(halfY, bottomLeft, bottomRight)
            }
        }
    }

    /** centerCrop: dst를 꽉 채우도록 src를 중앙에서 잘라냄 */
    private fun centerCropSrcRect(bmp: Bitmap, dst: RectF): Rect {
        val bw = bmp.width.toFloat()
        val bh = bmp.height.toFloat()
        val scale = max(dst.width() / bw, dst.height() / bh)
        val sw = dst.width() / scale
        val sh = dst.height() / scale
        val left = (bw - sw) / 2f
        val top = (bh - sh) / 2f
        return Rect(
            floor(left).toInt(),
            floor(top).toInt(),
            floor(left + sw).toInt(),
            floor(top + sh).toInt()
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        when (mode) {
            Mode.MINI -> listener?.onClickWholeMini()
            Mode.DEFAULT -> {
                // 요일 헤더 영역 클릭은 무시
                if (event.y < headerH) return true

                val c = (event.x / cellW).toInt().coerceIn(0, colCount - 1)
                val r = (event.y / cellH).toInt().coerceIn(0, rowCount - 1)
                val pos = r * colCount + c
                val day = pos - firstDayIndex + 1
                if (day in 1..daysInMonth) {
                    val date = ym.atDay(day)
                    val has = recordMap.containsKey(day)
                    // ✅ 클릭 확인용 Toast
                    Toast.makeText(
                        context,
                        "Clicked: ${date}  hasRecord=$has",
                        Toast.LENGTH_SHORT
                    ).show()
                    listener?.onClickDay(date, has)
                }
            }
        }
        return true
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity
}