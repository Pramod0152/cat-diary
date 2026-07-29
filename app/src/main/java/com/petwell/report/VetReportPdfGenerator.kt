package com.petwell.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.petwell.data.entity.DailyLog
import com.petwell.data.entity.PetProfile
import com.petwell.data.entity.PetReminder
import com.petwell.data.entity.PetReminderLog
import com.petwell.data.entity.enums.Species
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VetReportPdfGenerator(private val context: Context) {

    private data class CachedReminders(
        val reminder: PetReminder,
        val logs: List<PetReminderLog>
    )

    private data class ReminderRow(
        val reminderTitle: String,
        val logDate: Long?,
        val wasAdministered: Boolean?
    )

    companion object {
        private const val TAG = "VetReportPdfGen"
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 40f
        private const val ROW_HEIGHT = 22f
    }

    private fun startNewPageIfNeeded(
        document: PdfDocument,
        currentPage: PdfDocument.Page?,
        currentCanvas: Canvas?,
        currentY: Float,
        pageFinished: Boolean
    ): Triple<PdfDocument.Page, Canvas, Float> {
        if (currentPage != null && !pageFinished) {
            try {
                document.finishPage(currentPage)
            } catch (_: Exception) { }
        }
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val newPage = document.startPage(pageInfo)
        val newCanvas = newPage.canvas
        return Triple(newPage, newCanvas, MARGIN)
    }

    fun generate(
        petProfile: PetProfile,
        dailyLogs: List<DailyLog>,
        reminders: List<PetReminder>,
        reminderLogs: List<PetReminderLog>,
        startDate: Long,
        endDate: Long
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "PetWell_Report_${petProfile.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val file = File(reportsDir, fileName)

        val document = PdfDocument()

        var currentPage: PdfDocument.Page? = null
        var currentCanvas: Canvas? = null
        var pageFinished = true

        fun ensurePage(): Pair<Canvas, Float> {
            val (p, c, y) = startNewPageIfNeeded(document, currentPage, currentCanvas, 0f, pageFinished)
            currentPage = p
            currentCanvas = c
            pageFinished = false
            return Pair(c, y)
        }

        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

        val flatReminderRows = mutableListOf<ReminderRow>()
        for (rem in reminders) {
            val logs = reminderLogs.filter { it.reminderId == rem.id }
            if (logs.isEmpty()) {
                flatReminderRows.add(
                    ReminderRow(reminderTitle = rem.title, logDate = null, wasAdministered = null)
                )
            } else {
                for (log in logs.sortedBy { it.timestamp }) {
                    flatReminderRows.add(
                        ReminderRow(reminderTitle = rem.title, logDate = log.timestamp, wasAdministered = log.wasAdministered)
                    )
                }
            }
        }

        var (canvas, y) = ensurePage()
        y = drawHeader(canvas, petProfile, startDate, endDate, dateFormat, y)

        y += 16f
        var logIndex = 0
        val sortedLogs = dailyLogs.sortedBy { it.timestamp }

        if (sortedLogs.isNotEmpty()) {
            val wtResult = drawWeightTable(canvas, sortedLogs, dateFormat, y, 0)
            y = wtResult.first
            logIndex = wtResult.second
            while (logIndex < sortedLogs.size) {
                val (c, _) = ensurePage()
                canvas = c
                val r = drawWeightTable(canvas, sortedLogs, dateFormat, MARGIN, logIndex)
                y = r.first
                logIndex = r.second
            }

            y += 20f
            val dsResult = drawDailyLogSummary(canvas, sortedLogs, dateFormat, dateTimeFormat, y, 0, petProfile.species)
            y = dsResult.first
            logIndex = dsResult.second
            while (logIndex < sortedLogs.size) {
                val (c, _) = ensurePage()
                canvas = c
                val r = drawDailyLogSummary(canvas, sortedLogs, dateFormat, dateTimeFormat, MARGIN, logIndex, petProfile.species)
                y = r.first
                logIndex = r.second
                if (logIndex == 0 && sortedLogs.isNotEmpty()) {
                    Log.w(TAG, "Single log entry too large to fit on a fresh page, skipping")
                    break
                }
            }
        }

        if (flatReminderRows.isNotEmpty()) {
            y += 20f
            val rtResult = drawReminderTable(canvas, flatReminderRows, dateFormat, y, 0)
            y = rtResult.first
            var remIndex = rtResult.second
            while (remIndex < flatReminderRows.size) {
                val (c, _) = ensurePage()
                canvas = c
                val r = drawReminderTable(canvas, flatReminderRows, dateFormat, MARGIN, remIndex)
                y = r.first
                remIndex = r.second
                if (remIndex == 0 && flatReminderRows.isNotEmpty()) {
                    Log.w(TAG, "Single reminder row too large to fit on a fresh page, skipping")
                    break
                }
            }
        }

        if (!pageFinished && currentPage != null) {
            document.finishPage(currentPage)
            pageFinished = true
        }

        document.writeTo(file.outputStream())
        document.close()

        return file
    }

    // sectionTitlePaint, tableHeaderPaint, cellPaint, cellPaintAlt, wrapText omitted for brevity

    private fun drawWeightTable(
        canvas: Canvas,
        logs: List<DailyLog>,
        dateFormat: SimpleDateFormat,
        y: Float,
        startIndex: Int
    ): Pair<Float, Int> {
        var currentY = y
        val headerPaint = tableHeaderPaint()
        val cellPaint = cellPaint()
        val lightPaint = cellPaintAlt()
        val colX = floatArrayOf(MARGIN, MARGIN + 180f, MARGIN + 320f)
        val bottomLimit = PAGE_HEIGHT - MARGIN - 40f

        if (startIndex == 0) {
            val sectionPaint = sectionTitlePaint()
            canvas.drawText("Weight Progression", MARGIN, currentY, sectionPaint)
            currentY += 24f
            if (currentY > bottomLimit) return Pair(currentY, startIndex)
            canvas.drawText("Date", colX[0], currentY, headerPaint)
            canvas.drawText("Weight (kg)", colX[1], currentY, headerPaint)
            canvas.drawText("Appetite", colX[2], currentY, headerPaint)
            currentY += ROW_HEIGHT
            if (currentY > bottomLimit) return Pair(currentY, startIndex)
        }

        for (i in startIndex until logs.size) {
            if (currentY + ROW_HEIGHT > bottomLimit) return Pair(currentY, i)
            val log = logs[i]
            val bg = if (i % 2 == 0) cellPaint else lightPaint
            canvas.drawText(dateFormat.format(Date(log.timestamp)), colX[0], currentY, bg)
            canvas.drawText("%.1f".format(log.weight), colX[1], currentY, bg)
            canvas.drawText("${log.appetiteScore}/5", colX[2], currentY, bg)
            currentY += ROW_HEIGHT
        }

        return Pair(currentY, logs.size)
    }

    private fun drawDailyLogSummary(
        canvas: Canvas,
        logs: List<DailyLog>,
        dateFormat: SimpleDateFormat,
        dateTimeFormat: SimpleDateFormat,
        y: Float,
        startIndex: Int,
        species: Species
    ): Pair<Float, Int> {
        var currentY = y
        val bottomLimit = PAGE_HEIGHT - MARGIN - 20f
        val valuePaint = Paint().apply { textSize = 9f; isAntiAlias = true }
        val dividerPaint = Paint().apply {
            color = 0xFFE0E0E0.toInt()
            strokeWidth = 0.5f
        }

        if (startIndex == 0) {
            val sectionPaint = sectionTitlePaint()
            canvas.drawText("Daily Log Details", MARGIN, currentY, sectionPaint)
            currentY += 24f
            if (currentY > bottomLimit) return Pair(currentY, startIndex)
        }

        for (i in startIndex until logs.size) {
            val log = logs[i]

            val bathroomLine = bathroomLine(species, log)
            val bathroomHeight = if (bathroomLine != null) 14f else 0f
            val moodHeight = if (log.mood != null) 14f else 0f
            val entryHeight = 16f + bathroomHeight + moodHeight + 6f
            if (currentY + entryHeight > bottomLimit) return Pair(currentY, i)

            if (i > 0 || startIndex > 0) {
                canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, dividerPaint)
                currentY += 4f
            }

            canvas.drawText(dateTimeFormat.format(Date(log.timestamp)), MARGIN, currentY, valuePaint)
            currentY += 16f

            if (bathroomLine != null) {
                canvas.drawText(bathroomLine, MARGIN, currentY, valuePaint)
                currentY += 14f
            }

            if (log.mood != null) {
                canvas.drawText("Mood: ${log.mood.displayName}", MARGIN, currentY, valuePaint)
                currentY += 14f
            }
            currentY += 6f
        }

        return Pair(currentY, logs.size)
    }

    private fun bathroomLine(species: Species, log: DailyLog): String? {
        val stool = log.litterStoolScore
        return when (species) {
            Species.CAT, Species.SMALL_ANIMAL -> {
                val urination = log.litterUrination?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "N/A"
                "Water: ${log.waterIntake.name} | Stool: $stool/7 | Urination: $urination"
            }
            Species.DOG -> {
                val stoolLabel = when (stool) {
                    1 -> "Normal"
                    2 -> "Soft"
                    3 -> "Diarrhea"
                    else -> "Not recorded"
                }
                "Water: ${log.waterIntake.name} | Stool: $stoolLabel"
            }
            Species.BIRD -> {
                val droppingsLabel = when (stool) {
                    1 -> "Normal"
                    0 -> "Abnormal"
                    else -> "Not recorded"
                }
                "Water: ${log.waterIntake.name} | Droppings: $droppingsLabel"
            }
            Species.OTHER -> {
                "Water: ${log.waterIntake.name}"
            }
        }
    }

    private fun drawReminderTable(
        canvas: Canvas,
        rows: List<ReminderRow>,
        dateFormat: SimpleDateFormat,
        y: Float,
        startIndex: Int
    ): Pair<Float, Int> {
        var currentY = y
        val bottomLimit = PAGE_HEIGHT - MARGIN - 20f
        val headerPaint = tableHeaderPaint()
        val cellPaint = cellPaint()
        val lightPaint = cellPaintAlt()

        if (startIndex == 0) {
            val sectionPaint = sectionTitlePaint()
            canvas.drawText("Reminder Adherence", MARGIN, currentY, sectionPaint)
            currentY += 24f
            if (currentY > bottomLimit) return Pair(currentY, startIndex)

            canvas.drawText("Reminder", MARGIN, currentY, headerPaint)
            canvas.drawText("Date Taken", MARGIN + 140f, currentY, headerPaint)
            canvas.drawText("Done", MARGIN + 280f, currentY, headerPaint)
            currentY += ROW_HEIGHT
            if (currentY > bottomLimit) return Pair(currentY, startIndex)
        }

        for (i in startIndex until rows.size) {
            if (currentY + ROW_HEIGHT > bottomLimit) return Pair(currentY, i)
            val row = rows[i]
            val bg = if (i % 2 == 0) cellPaint else lightPaint

            if (row.logDate == null) {
                canvas.drawText(row.reminderTitle, MARGIN, currentY, bg)
                canvas.drawText("No log recorded", MARGIN + 140f, currentY, bg)
                canvas.drawText("-", MARGIN + 280f, currentY, bg)
            } else {
                canvas.drawText(row.reminderTitle, MARGIN, currentY, bg)
                canvas.drawText(dateFormat.format(Date(row.logDate)), MARGIN + 140f, currentY, bg)
                canvas.drawText(if (row.wasAdministered == true) "Yes" else "Missed", MARGIN + 280f, currentY, bg)
            }
            currentY += ROW_HEIGHT
        }

        return Pair(currentY, rows.size)
    }

    private fun drawHeader(
        canvas: Canvas,
        petProfile: PetProfile,
        startDate: Long,
        endDate: Long,
        dateFormat: SimpleDateFormat,
        y: Float
    ): Float {
        var currentY = y

        val titlePaint = Paint().apply {
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("PetWell Veterinary Report", MARGIN, currentY, titlePaint)
        currentY += 30f

        val subtitlePaint = Paint().apply {
            textSize = 10f
            color = 0xFF666666.toInt()
            isAntiAlias = true
        }

        val age = Calendar.getInstance().get(Calendar.YEAR) - petProfile.birthYear
        val labels = listOf(
            "Pet: ${petProfile.name}",
            "Species: ${petProfile.species.displayName}",
            "Age: $age years",
            "Target Weight: ${petProfile.targetWeight} kg",
            "Period: ${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}"
        )
        for (label in labels) {
            canvas.drawText(label, MARGIN, currentY, subtitlePaint)
            currentY += 16f
        }

        if (petProfile.conditionNotes.isNotBlank()) {
            val notesPaint = Paint().apply {
                textSize = 10f
                color = 0xFF888888.toInt()
                isAntiAlias = true
            }
            canvas.drawText("Notes: ${petProfile.conditionNotes}", MARGIN, currentY, notesPaint)
            currentY += 16f
        }

        currentY += 4f
        val linePaint = Paint().apply {
            color = 0xFFCCCCCC.toInt()
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
        currentY += 12f

        return currentY
    }

    private fun sectionTitlePaint() = Paint().apply {
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
        color = 0xFF333333.toInt()
    }

    private fun tableHeaderPaint() = Paint().apply {
        textSize = 9f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
        color = 0xFF444444.toInt()
    }

    private fun cellPaint() = Paint().apply {
        textSize = 9f
        isAntiAlias = true
    }

    private fun cellPaintAlt() = Paint().apply {
        textSize = 9f
        isAntiAlias = true
        color = 0xFF777777.toInt()
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        if (lines.isEmpty()) lines.add(text)
        return lines
    }
}
