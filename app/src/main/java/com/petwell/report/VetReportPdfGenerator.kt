package com.petwell.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.petwell.data.entity.DailyLog
import com.petwell.data.entity.PetProfile
import com.petwell.data.entity.PetReminder
import com.petwell.data.entity.PetReminderLog
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

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 40f
        private const val ROW_HEIGHT = 22f
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
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

        val cachedReminders: List<CachedReminders> = reminders.map { rem ->
            CachedReminders(rem, reminderLogs.filter { it.reminderId == rem.id })
        }

        var y = MARGIN
        y = drawHeader(canvas, petProfile, startDate, endDate, dateFormat, y)

        y += 16f
        if (dailyLogs.isNotEmpty()) {
            y = drawWeightTable(canvas, dailyLogs, dateFormat, y)
            y += 20f
            val remaining = drawDailyLogSummary(canvas, dailyLogs, dateFormat, dateTimeFormat, y)
            if (remaining > PAGE_HEIGHT - MARGIN - 40f) {
                document.finishPage(page)
                y = drawContinuedPage(document, canvas, remaining)
            } else {
                y = remaining
            }
        }

        if (cachedReminders.isNotEmpty()) {
            y += 20f
            y = drawReminderTable(canvas, cachedReminders, dateFormat, y)
        }

        document.finishPage(page)
        document.writeTo(file.outputStream())
        document.close()

        return file
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

    private fun drawWeightTable(
        canvas: Canvas,
        logs: List<DailyLog>,
        dateFormat: SimpleDateFormat,
        y: Float
    ): Float {
        var currentY = y

        val sectionPaint = sectionTitlePaint()
        canvas.drawText("Weight Progression", MARGIN, currentY, sectionPaint)
        currentY += 24f

        val headerPaint = tableHeaderPaint()
        val cellPaint = cellPaint()
        val lightPaint = cellPaintAlt()
        val colX = floatArrayOf(MARGIN, MARGIN + 180f, MARGIN + 320f)

        canvas.drawText("Date", colX[0], currentY, headerPaint)
        canvas.drawText("Weight (kg)", colX[1], currentY, headerPaint)
        canvas.drawText("Appetite", colX[2], currentY, headerPaint)
        currentY += ROW_HEIGHT

        val sorted = logs.sortedBy { it.timestamp }
        for ((i, log) in sorted.withIndex()) {
            if (currentY > PAGE_HEIGHT - MARGIN - 40f) break
            val bg = if (i % 2 == 0) cellPaint else lightPaint
            canvas.drawText(dateFormat.format(Date(log.timestamp)), colX[0], currentY, bg)
            canvas.drawText("%.1f".format(log.weight), colX[1], currentY, bg)
            canvas.drawText("${log.appetiteScore}/5", colX[2], currentY, bg)
            currentY += ROW_HEIGHT
        }

        return currentY
    }

    private fun drawDailyLogSummary(
        canvas: Canvas,
        logs: List<DailyLog>,
        dateFormat: SimpleDateFormat,
        dateTimeFormat: SimpleDateFormat,
        y: Float
    ): Float {
        var currentY = y

        val sectionPaint = sectionTitlePaint()
        canvas.drawText("Daily Log Details", MARGIN, currentY, sectionPaint)
        currentY += 24f

        val valuePaint = Paint().apply {
            textSize = 9f
            isAntiAlias = true
        }
        val dividerPaint = Paint().apply {
            color = 0xFFE0E0E0.toInt()
            strokeWidth = 0.5f
        }

        val sorted = logs.sortedBy { it.timestamp }
        for ((i, log) in sorted.withIndex()) {
            if (currentY > PAGE_HEIGHT - MARGIN - 20f) return currentY

            if (i > 0) {
                canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, dividerPaint)
                currentY += 4f
            }

            canvas.drawText(dateTimeFormat.format(Date(log.timestamp)), MARGIN, currentY, valuePaint)
            currentY += 16f

            canvas.drawText("Water: ${log.waterIntake.name} | Stool: ${log.litterStoolScore}/7 | Urination: ${log.litterUrination.name}", MARGIN, currentY, valuePaint)
            currentY += 14f

            if (log.customNotes.isNotBlank()) {
                val wrappedNotes = wrapText(log.customNotes, valuePaint, PAGE_WIDTH - MARGIN * 2)
                for (line in wrappedNotes) {
                    if (currentY > PAGE_HEIGHT - MARGIN - 20f) return currentY
                    canvas.drawText(line, MARGIN + 10f, currentY, valuePaint)
                    currentY += 14f
                }
            }
            currentY += 6f
        }

        return currentY
    }

    private fun drawReminderTable(
        canvas: Canvas,
        cachedReminders: List<CachedReminders>,
        dateFormat: SimpleDateFormat,
        y: Float
    ): Float {
        var currentY = y

        val sectionPaint = sectionTitlePaint()
        canvas.drawText("Reminder Adherence", MARGIN, currentY, sectionPaint)
        currentY += 24f

        val headerPaint = tableHeaderPaint()
        val cellPaint = cellPaint()
        val lightPaint = cellPaintAlt()

        canvas.drawText("Reminder", MARGIN, currentY, headerPaint)
        canvas.drawText("Date Taken", MARGIN + 140f, currentY, headerPaint)
        canvas.drawText("Done", MARGIN + 280f, currentY, headerPaint)
        currentY += ROW_HEIGHT

        var rowIndex = 0
        for (cached in cachedReminders) {
            if (cached.logs.isEmpty()) {
                if (currentY > PAGE_HEIGHT - MARGIN - 20f) break
                canvas.drawText(cached.reminder.title, MARGIN, currentY, cellPaint)
                canvas.drawText("No log recorded", MARGIN + 140f, currentY, cellPaint)
                canvas.drawText("-", MARGIN + 280f, currentY, cellPaint)
                currentY += ROW_HEIGHT
                rowIndex++
            } else {
                for (log in cached.logs.sortedBy { it.timestamp }) {
                    if (currentY > PAGE_HEIGHT - MARGIN - 20f) break
                    val bg = if (rowIndex % 2 == 0) cellPaint else lightPaint
                    canvas.drawText("${cached.reminder.reminderType.displayName}: ${cached.reminder.title}", MARGIN, currentY, bg)
                    canvas.drawText(dateFormat.format(Date(log.timestamp)), MARGIN + 140f, currentY, bg)
                    canvas.drawText(if (log.wasAdministered) "Yes" else "Missed", MARGIN + 280f, currentY, bg)
                    currentY += ROW_HEIGHT
                    rowIndex++
                }
            }
        }

        return currentY
    }

    private fun drawContinuedPage(
        document: PdfDocument,
        canvas: Canvas,
        startY: Float
    ): Float {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val newPage = document.startPage(pageInfo)
        val newCanvas = newPage.canvas

        val contPaint = Paint().apply {
            textSize = 10f
            color = 0xFF888888.toInt()
            isAntiAlias = true
        }
        newCanvas.drawText("Continued...", MARGIN, MARGIN + 10f, contPaint)

        return drawDailyLogSummary(newCanvas, emptyList(), SimpleDateFormat(), SimpleDateFormat(), MARGIN + 30f)
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
