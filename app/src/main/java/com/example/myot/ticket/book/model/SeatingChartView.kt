package com.example.myot.ticket.book.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myot.R
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

class SeatingChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var seatSize = 30f
    private var seatSpacingRatio = 0.2f
    private var blockSpacingRatio = 1f
    private var floorSpacingRatio = 5f

    private var seatSpacing = seatSpacingRatio * seatSize
    private var blockSpacing = blockSpacingRatio * seatSize
    private var floorSpacing = floorSpacingRatio * seatSize

    private val seatPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var maxSeats = 0
    private var maxBlocks = 0
    private var totalRows = 0
    private var totalBlocks = 0
    private var floorNum = 0

    private var floorsString: JSONArray? = null

    fun setSeatData(json: JSONObject) {
        val totalWidth = json.getJSONObject("totalWidth")
        maxSeats = totalWidth.getInt("maxSeats")
        maxBlocks = totalWidth.getInt("maxBlocks")

        val totalHeight = json.getJSONObject("totalHeight")
        totalRows = totalHeight.getInt("totalRow")
        totalBlocks = totalHeight.getInt("totalBlocks")
        floorNum = totalHeight.getInt("floorNum")

        floorsString = json.getJSONArray("floors")

        invalidate()
    }

    private var highlightedSeats = mutableListOf<SeatHighlightInfo>()

    fun setHighlightedSeats(seatInfoList: List<SeatHighlightInfo>) {
        highlightedSeats = seatInfoList.toMutableList()
        //Log.d("seathigh", highlightedSeats.toString())
        invalidate()
    }

    private val seatFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.white)
    }
    private val seatBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = ContextCompat.getColor(context, R.color.point_pink)
    }
    private val seatTransparentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.TRANSPARENT
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // 모서리/여백
    private val seatCorner = dp(3f)
    private val highlightInset = dp(1.2f) // 테두리 안쪽으로 조금 줄여서 채움
    private fun dp(v: Float) = v * resources.displayMetrics.density


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (floorsString == null) return

        // View 사이즈에 맞는 seatSize 계산
        val constWidth = maxSeats + (maxSeats - maxBlocks) * seatSpacingRatio + (maxBlocks - 1) * blockSpacingRatio
        val widthSeatSize = width / constWidth

        val constHeight = totalRows + (totalRows - totalBlocks) * seatSpacingRatio + (maxBlocks - floorNum) * blockSpacingRatio
        val heightSeatSize = (height / constHeight)

        seatSize = min(widthSeatSize, heightSeatSize)       // 가로 세로 중 작은 값으로 선정
        seatSpacing = seatSize * 0.1f       // 변경된 seatSize 반영
        blockSpacing = seatSize * 1f
        floorSpacing = seatSize * 5f

        var currentTop = 0f
        var maxBlockHeightInLine = 0f
        var currentLeft = 0f

        var numOfSeat = 0

        for (floorIndex in 0 until floorsString!!.length()) {
            val floor = floorsString!!.getJSONObject(floorIndex)
            val blocks = floor.getJSONArray("blocks")

            // blockNumber로 그룹핑
            val blockGroups = mutableMapOf<Int, MutableList<JSONObject>>()
            for (blockIndex in 0 until blocks.length()) {
                val block = blocks.getJSONObject(blockIndex)
                val blockNumber = block.optInt("columnNumber", 1)

                if (!blockGroups.containsKey(blockNumber)) {
                    blockGroups[blockNumber] = mutableListOf()
                }
                blockGroups[blockNumber]?.add(block)
            }

            val sortedBlockNumbers = blockGroups.keys.sorted()

            for (blockNumber in sortedBlockNumbers) {
                val blocksInGroup = blockGroups[blockNumber] ?: continue

                //currentTop = 0f  // 층이 바뀌면 top 초기화
                currentLeft = 0f
                maxBlockHeightInLine = 0f

                for (block in blocksInGroup) {
                    val rows = block.getJSONArray("rows")

                    var blockHeight = 0f
                    var maxRowWidth = 0f

                    for (rowIndex in 0 until rows.length()) {
                        val row = rows.getJSONObject(rowIndex)
                        val seats = row.getJSONArray("seats")

                        var rowWidth = 0f

                        for (seatIndex in 0 until seats.length()) {
                            val seatValue = seats.getInt(seatIndex)
                            if (seatValue != 0) numOfSeat++
                            val seatHighlight = highlightedSeats.find {
                                it.floor == floor.getInt("floorNumber") &&
                                        it.zone == block.optString("zone") &&
                                        it.seatIndex == numOfSeat || numOfSeat == 2755
                                //it.rowNumber == row.getInt("rowNumber") &&
                            }
                            //if (seatHighlight != null) Log.d("seat", seatHighlight.toString())

                            seatPaint.color = when {
                                seatValue == 0 -> Color.TRANSPARENT
                                seatHighlight != null -> when (seatHighlight.numberOfSittings) {
                                    1 -> {
                                        ContextCompat.getColor(context, R.color.point_pink)
                                        //numOfSeat++
                                    }
                                    2 -> {
                                        Color.RED
                                        //numOfSeat++
                                    }
                                    else -> {
                                        Color.BLUE
                                        //numOfSeat++
                                    }
                                }
                                seatIndex == 2755 ->Color.GREEN
                                else -> {
                                    ContextCompat.getColor(context, R.color.gray5)
                                    //numOfSeat++
                                }
                            }


                            val left = currentLeft + rowWidth
                            val top = currentTop + blockHeight

                            canvas.drawRect(
                                left, top,
                                left + seatSize, top + seatSize,
                                seatPaint
                            )

                            rowWidth += seatSize + seatSpacing
                        }

                        blockHeight += seatSize + seatSpacing
                        maxRowWidth = max(maxRowWidth, rowWidth)
                    }

                    currentLeft += maxRowWidth + blockSpacing
                    maxBlockHeightInLine = max(maxBlockHeightInLine, blockHeight)
                }

                // blockNumber가 바뀌면 다음 줄로 내리기
                currentTop += maxBlockHeightInLine + blockSpacing
            }
            currentTop += floorSpacing
        }
    }
}
