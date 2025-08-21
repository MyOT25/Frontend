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
import com.example.myot.ticket.calendar.model.RecordCell
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

    private val weekdayPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(10f)
        color = ContextCompat.getColor(context, R.color.gray3)
        typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
        textAlign = Paint.Align.CENTER
    }
    private var headerH = run {
        val fm = weekdayPaint.fontMetrics; (fm.descent - fm.ascent) + dp(10f)
    }
    private var weekdayLabels = arrayOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
    fun setWeekdayLabels(labels: List<String>) { if (labels.size == 7) { weekdayLabels = labels.toTypedArray(); invalidate() } }

    private val dayTextBounds = Rect()
    private val cellPadding = dp(2f)
    private var cellW = 0f
    private var cellH = 0f
    private val colCount = 7
    private val rowCount = 6
    private val cellCorner = dp(8f)

    private val dayBg: Drawable? by lazy { ContextCompat.getDrawable(context, R.drawable.bg_calendar_day) }

    // ✅ 캐시/데이터
    private val imageCache: MutableMap<Pair<Int, Int>, Bitmap> = mutableMapOf() // (day, idx) -> bmp
    private val recordMap: MutableMap<Int, List<RecordCell>> = mutableMapOf()   // day -> cells(max3)

    interface Listener {
        fun onClickDay(date: LocalDate, hasRecord: Boolean)
        fun onClickWholeMini() {}
        fun onClickPost(postId: Int, musicalTitle: String) {} // ✅ postId 전달
    }
    var listener: Listener? = null

    fun setMode(m: Mode) { mode = m; invalidate() }
    fun setMonth(year: Int, month: Int) {
        ym = YearMonth.of(year, month)
        daysInMonth = ym.lengthOfMonth()
        val first = ym.atDay(1)
        firstDayIndex = (first.dayOfWeek.value % 7)
        imageCache.clear(); invalidate()
    }
    fun setRecords(dayToCells: Map<Int, List<RecordCell>>) {
        recordMap.clear(); recordMap.putAll(dayToCells); invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val fm = weekdayPaint.fontMetrics
        headerH = (fm.descent - fm.ascent) + dp(10f)
        val availH = (h - headerH).coerceAtLeast(0f)
        cellW = w / colCount.toFloat()
        cellH = availH / rowCount.toFloat()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawWeekHeader(canvas)
        drawCellsBackground(canvas)
        drawDays(canvas)    // 숫자: 이미지 없는 날만
        drawImages(canvas)  // centerCrop 1~3장
    }

    private fun drawWeekHeader(canvas: Canvas) {
        val fm = weekdayPaint.fontMetrics
        val baseY = -fm.ascent + dp(2f)
        for (c in 0 until colCount) {
            val cx = c * cellW + cellW / 2f
            canvas.drawText(weekdayLabels[c], cx, baseY, weekdayPaint)
        }
    }

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
            dayBg?.setBounds(rect); dayBg?.draw(canvas)
        }
    }

    private fun drawDays(canvas: Canvas) {
        val fm = dayTextPaint.fontMetrics
        val baseY = -fm.ascent + dp(2f)
        for (day in 1..daysInMonth) {
            if (recordMap[day]?.isNotEmpty() == true) continue // 이미지 있으면 숫자 숨김
            val pos = firstDayIndex + (day - 1)
            val r = pos / colCount
            val c = pos % colCount
            val x = c * cellW + dp(6f)
            val y = headerH + r * cellH + baseY
            canvas.drawText(day.toString(), x, y, dayTextPaint)
        }
    }

    private fun drawImages(canvas: Canvas) {
        for (day in 1..daysInMonth) {
            val cells = recordMap[day] ?: continue
            if (cells.isEmpty()) continue

            val pos = firstDayIndex + (day - 1)
            val r = pos / colCount
            val c = pos % colCount
            val outer = RectF(
                c * cellW + cellPadding,
                headerH + r * cellH + cellPadding,
                (c + 1) * cellW - cellPadding,
                headerH + (r + 1) * cellH - cellPadding
            )

            val path = Path().apply { addRoundRect(outer, cellCorner, cellCorner, Path.Direction.CW) }
            canvas.withSave {
                canvas.clipPath(path)
                val tiles = calcTiles(outer, cells.size)
                tiles.forEachIndexed { idx, tile ->
                    val key = day to idx
                    val bmp = imageCache[key]
                    if (bmp == null) {
                        Glide.with(this@TicketCalendarView)
                            .asBitmap()
                            .load(cells[idx].imageUrl)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    imageCache[key] = resource; invalidate()
                                }
                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })
                    } else {
                        val src = centerCropSrcRect(bmp, tile)
                        canvas.drawBitmap(bmp, src, tile, null)
                    }
                }
            }
        }
    }

    /** 1장: 전체 / 2장: 좌우 / 3장: 상단 전체 + 하단 좌우 */
    private fun calcTiles(outer: RectF, count: Int): List<RectF> = when (count.coerceIn(1, 3)) {
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
            val midX = (outer.left + outer.right) / 2f
            listOf(
                RectF(outer.left, outer.top, outer.right, midY),          // idx 0 (상단 전체)
                RectF(outer.left, midY, midX, outer.bottom),               // idx 1 (하단 좌)
                RectF(midX, midY, outer.right, outer.bottom)               // idx 2 (하단 우)
            )
        }
    }

    /** centerCrop: dst를 꽉 채우도록 src 영역을 중앙에서 잘라냄 */
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
                if (event.y < headerH) return true // 헤더 클릭 무시
                val col = (event.x / cellW).toInt().coerceIn(0, colCount - 1)
                val row = ((event.y - headerH) / cellH).toInt().coerceIn(0, rowCount - 1)
                val pos = row * colCount + col
                val day = pos - firstDayIndex + 1
                if (day !in 1..daysInMonth) return true

                val date = ym.atDay(day)
                val cells = recordMap[day].orEmpty()
                val has = cells.isNotEmpty()

                // ✅ 어느 타일을 눌렀는지 계산
                if (has) {
                    val outer = RectF(
                        col * cellW + cellPadding,
                        headerH + row * cellH + cellPadding,
                        (col + 1) * cellW - cellPadding,
                        headerH + (row + 1) * cellH - cellPadding
                    )
                    val idx = hitTileIndex(cells.size, outer, event.x, event.y).coerceIn(0, cells.size - 1)
                    listener?.onClickPost(cells[idx].postId,  cells[idx].musicalTitle)
                } else {
                    //Toast.makeText(context, "Clicked: $date  (no record)", Toast.LENGTH_SHORT).show()
                    listener?.onClickDay(date, false)
                }
            }
        }
        return true
    }

    /** 터치 좌표 → 타일 index(0..2) */
    private fun hitTileIndex(count: Int, outer: RectF, x: Float, y: Float): Int = when (count.coerceIn(1, 3)) {
        1 -> 0
        2 -> if (x < (outer.left + outer.right) / 2f) 0 else 1
        else -> {
            val midY = (outer.top + outer.bottom) / 2f
            if (y < midY) 0
            else {
                val midX = (outer.left + outer.right) / 2f
                if (x < midX) 1 else 2
            }
        }
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity
}