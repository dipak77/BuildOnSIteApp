package com.aistudio.constructpro.kgrmqd.ui

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.aistudio.constructpro.kgrmqd.data.Transaction
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfUtils {

    // ── Formatters ───────────────────────────────────────────
    private val timestampFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateOnlyFormat  = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val currencyFmt     = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2; minimumFractionDigits = 2
    }

    // ── Color Palette ────────────────────────────────────────
    private val NAVY        = Color.parseColor("#0F172A")
    private val DARK_SLATE  = Color.parseColor("#1E293B")
    private val SLATE       = Color.parseColor("#334155")
    private val GRAY        = Color.parseColor("#64748B")
    private val LIGHT_GRAY  = Color.parseColor("#94A3B8")
    private val BORDER      = Color.parseColor("#CBD5E1")
    private val BG_LIGHT    = Color.parseColor("#F1F5F9")
    private val BG_PAGE     = Color.parseColor("#FAFBFC")
    private val WHITE       = Color.WHITE

    private val GREEN_DARK  = Color.parseColor("#15803D")
    private val GREEN_MID   = Color.parseColor("#22C55E")
    private val GREEN_LITE  = Color.parseColor("#DCFCE7")
    private val GREEN_ACCENT= Color.parseColor("#86EFAC")

    private val RED_DARK    = Color.parseColor("#B91C1C")
    private val RED_MID     = Color.parseColor("#EF4444")
    private val RED_LITE    = Color.parseColor("#FEE2E2")

    private val BLUE_DARK   = Color.parseColor("#1D4ED8")
    private val BLUE_LITE   = Color.parseColor("#DBEAFE")

    private val PURPLE      = Color.parseColor("#5D53EA")
    private val PURPLE_LT   = Color.parseColor("#EEF2FF")

    private val AMBER_DARK  = Color.parseColor("#92400E")
    private val AMBER_LITE  = Color.parseColor("#FEF3C7")

    private val HEADER_BAR  = Color.parseColor("#0F172A")
    private val ROW_ALT     = Color.parseColor("#F8FAFC")
    private val ROW_NORMAL  = Color.WHITE
    private val DIVIDER     = Color.parseColor("#E2E8F0")
    private val ACCENT_LINE = Color.parseColor("#3B82F6")

    // ── Constants ────────────────────────────────────────────
    private const val A4_W = 595
    private const val A4_H = 842
    private const val A5_W = 420
    private const val A5_H = 595
    private const val PAGE_MARGIN = 36f
    private const val ROW_HEIGHT  = 26f
    private const val HEADER_H    = 28f
    private const val CORNER_R    = 5f
    private const val FOOTER_H    = 70f

    // ════════════════════════════════════════════════════════
    // UTILITY HELPERS
    // ════════════════════════════════════════════════════════

    private fun Paint.reset(
        color: Int = NAVY,
        size: Float = 10f,
        bold: Boolean = false,
        align: Paint.Align = Paint.Align.LEFT
    ) {
        this.color = color
        textSize = size
        isFakeBoldText = bold
        textAlign = align
        style = Paint.Style.FILL
    }

    private fun truncateText(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var t = text
        while (t.isNotEmpty() && paint.measureText("$t…") > maxWidth) t = t.dropLast(1)
        return if (t.isEmpty()) "…" else "$t…"
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = ""
        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(test) <= maxWidth) {
                current = test
            } else {
                if (current.isNotEmpty()) lines.add(current)
                current = word
            }
        }
        if (current.isNotEmpty()) lines.add(current)
        return lines
    }

    /** Draws a filled rounded rect with optional border */
    private fun Canvas.drawCard(
        left: Float, top: Float, right: Float, bottom: Float,
        fillColor: Int, paint: Paint,
        borderColor: Int = Color.TRANSPARENT,
        borderWidth: Float = 0.8f,
        radius: Float = CORNER_R
    ) {
        paint.style = Paint.Style.FILL
        paint.color = fillColor
        drawRoundRect(left, top, right, bottom, radius, radius, paint)
        if (borderColor != Color.TRANSPARENT) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderWidth
            paint.color = borderColor
            drawRoundRect(left, top, right, bottom, radius, radius, paint)
            paint.style = Paint.Style.FILL
        }
    }

    /** Draws a horizontal separator line */
    private fun Canvas.drawHRule(
        x1: Float, x2: Float, y: Float, paint: Paint,
        color: Int = BORDER, width: Float = 0.5f
    ) {
        paint.color = color
        paint.strokeWidth = width
        drawLine(x1, y, x2, y, paint)
    }

    /** Draws an accent left-bar for section titles */
    private fun Canvas.drawSectionTitle(
        text: String, x: Float, y: Float, pw: Int,
        paint: Paint, subtitle: String = ""
    ) {
        // Accent bar
        paint.color = ACCENT_LINE
        paint.style = Paint.Style.FILL
        drawRect(x, y - 10f, x + 3f, y + 4f, paint)

        paint.reset(NAVY, 11f, true, Paint.Align.LEFT)
        drawText(text, x + 9f, y, paint)

        if (subtitle.isNotEmpty()) {
            paint.reset(GRAY, 8.5f, false, Paint.Align.RIGHT)
            drawText(subtitle, pw - PAGE_MARGIN, y, paint)
        }
    }

    // ════════════════════════════════════════════════════════
    // PRO COMMON HEADER  (used across all A4 reports)
    // ════════════════════════════════════════════════════════

    fun drawCommonHeader(
        c: Canvas, p: Paint, pw: Int,
        margin: Float,
        reportTitle: String,
        generatedBy: String,
        dateRange: String,
        projectName: String,
        siteAddress: String,
        yStart: Float,
        pageNum: Int = 1,
        totalPages: Int = 0
    ): Float {
        var y = yStart

        // ── Top accent strip ──────────────────────────────────
        p.color = HEADER_BAR
        p.style = Paint.Style.FILL
        c.drawRect(0f, 0f, pw.toFloat(), 5f, p)

        // ── Header background card ────────────────────────────
        c.drawCard(
            margin - 4f, y - 4f,
            (pw - margin + 4f), y + 58f,
            Color.parseColor("#F0F4FF"), p,
            borderColor = Color.parseColor("#C7D2FE"),
            borderWidth = 0.6f, radius = 6f
        )

        // Left: Company block
        p.reset(NAVY, 11f, true, Paint.Align.LEFT)
        c.drawText(truncateText(projectName, 180f, p), margin + 8f, y + 13f, p)

        p.reset(GRAY, 8f, false, Paint.Align.LEFT)
        c.drawText(truncateText(siteAddress.replace("\n", ", "), 180f, p), margin + 8f, y + 25f, p)
        c.drawText("GST: N/A  |  CIN: N/A", margin + 8f, y + 36f, p)

        // Right: Report title + meta
        p.reset(NAVY, 14f, true, Paint.Align.RIGHT)
        c.drawText(reportTitle.uppercase(), pw - margin - 8f, y + 16f, p)

        p.reset(GRAY, 8f, false, Paint.Align.RIGHT)
        c.drawText("Generated by: $generatedBy", pw - margin - 8f, y + 28f, p)
        c.drawText("Period: $dateRange", pw - margin - 8f, y + 39f, p)
        if (totalPages > 0) {
            c.drawText("Page $pageNum of $totalPages", pw - margin - 8f, y + 50f, p)
        }

        y += 66f

        // ── Project Info bar ──────────────────────────────────
        c.drawCard(
            margin - 4f, y,
            pw - margin + 4f, y + 32f,
            DARK_SLATE, p, radius = 5f
        )

        // Project label + value
        p.reset(Color.parseColor("#94A3B8"), 8f, false, Paint.Align.LEFT)
        c.drawText("PROJECT", margin + 8f, y + 12f, p)
        p.reset(WHITE, 9f, true, Paint.Align.LEFT)
        c.drawText(projectName, margin + 8f, y + 24f, p)

        // Divider
        p.color = Color.parseColor("#475569")
        p.strokeWidth = 0.5f
        c.drawLine(
            margin + pw * 0.38f, y + 6f,
            margin + pw * 0.38f, y + 26f, p
        )

        // Address label + value
        p.reset(Color.parseColor("#94A3B8"), 8f, false, Paint.Align.LEFT)
        c.drawText("SITE ADDRESS", margin + pw * 0.40f, y + 12f, p)
        p.reset(WHITE, 9f, true, Paint.Align.LEFT)
        val maxAddrW = pw - margin * 2 - pw * 0.42f
        c.drawText(
            truncateText(siteAddress.replace("\n", ", "), maxAddrW, p),
            margin + pw * 0.40f, y + 24f, p
        )

        y += 40f
        return y
    }

    // ════════════════════════════════════════════════════════
    // PRO FOOTER
    // ════════════════════════════════════════════════════════

    private fun drawFooter(
        c: Canvas, p: Paint, pw: Int, ph: Int,
        margin: Float, pageNum: Int, totalPages: Int = 0
    ) {
        val footerY = ph - FOOTER_H + 8f

        p.color = Color.parseColor("#E2E8F0")
        p.strokeWidth = 0.8f
        c.drawLine(margin - 4f, footerY, pw - margin + 4f, footerY, p)

        // Left: branding
        p.reset(GRAY, 8f, false, Paint.Align.LEFT)
        c.drawText("ConstructPro · Professional Report", margin, footerY + 14f, p)
        c.drawText("Generated: ${timestampFormat.format(Date())}", margin, footerY + 26f, p)

        // Center: disclaimer
        p.reset(LIGHT_GRAY, 7f, false, Paint.Align.CENTER)
        c.drawText(
            "This is a computer-generated document. No signature required.",
            pw / 2f, footerY + 20f, p
        )

        // Right: page number
        val pageLabel = if (totalPages > 0) "Page $pageNum / $totalPages" else "Page $pageNum"
        p.reset(GRAY, 8f, true, Paint.Align.RIGHT)
        c.drawText(pageLabel, pw - margin, footerY + 14f, p)
    }

    // ════════════════════════════════════════════════════════
    // SUMMARY STAT CARDS (3-column row)
    // ════════════════════════════════════════════════════════

    private fun drawStatCards(
        c: Canvas, p: Paint,
        margin: Float, yPos: Float,
        card1: Triple<String, String, Int>,  // label, value, color
        card2: Triple<String, String, Int>,
        card3: Triple<String, String, Int>,
        contentW: Float
    ): Float {
        val gap   = 10f
        val cardW = (contentW - 2 * gap) / 3f
        val cardH = 56f

        listOf(
            Pair(card1, margin),
            Pair(card2, margin + cardW + gap),
            Pair(card3, margin + 2 * (cardW + gap))
        ).forEach { (card, x) ->
            val (label, value, fg) = card
            val bg = when (fg) {
                GREEN_DARK -> GREEN_LITE
                RED_DARK   -> RED_LITE
                BLUE_DARK  -> BLUE_LITE
                AMBER_DARK -> AMBER_LITE
                else       -> BG_LIGHT
            }
            c.drawCard(x, yPos, x + cardW, yPos + cardH, bg, p, borderColor = BORDER, borderWidth = 0.6f)

            // Top accent line on card
            p.color = fg
            p.style = Paint.Style.FILL
            c.drawRoundRect(x, yPos, x + cardW, yPos + 3f, 4f, 4f, p)

            p.reset(GRAY, 8f, true, Paint.Align.CENTER)
            c.drawText(label.uppercase(), x + cardW / 2f, yPos + 18f, p)

            p.reset(fg, 13f, true, Paint.Align.CENTER)
            c.drawText(value, x + cardW / 2f, yPos + 38f, p)
        }

        return yPos + cardH + 14f
    }

    // ════════════════════════════════════════════════════════
    // TABLE HEADER DRAWER
    // ════════════════════════════════════════════════════════

    data class ColumnDef(
        val label: String,
        val x: Float,           // absolute x position
        val width: Float,       // column width
        val align: Paint.Align = Paint.Align.LEFT
    )

    private fun drawTableHeader(
        c: Canvas, p: Paint,
        yPos: Float, margin: Float, pw: Int,
        columns: List<ColumnDef>
    ): Float {
        val headerH = HEADER_H
        c.drawCard(
            margin - 4f, yPos,
            pw - margin + 4f, yPos + headerH,
            DARK_SLATE, p, radius = 4f
        )
        p.reset(WHITE, 9f, true)
        columns.forEach { col ->
            p.textAlign = col.align
            val textX = when (col.align) {
                Paint.Align.RIGHT  -> col.x + col.width - 6f
                Paint.Align.CENTER -> col.x + col.width / 2f
                else               -> col.x + 6f
            }
            c.drawText(col.label, textX, yPos + 18f, p)
        }
        p.isFakeBoldText = false
        return yPos + headerH + 4f
    }

    // ════════════════════════════════════════════════════════
    // 1. RECEIPT PDF  (A5 — 420 × 595) — PRO VERSION
    // ════════════════════════════════════════════════════════

    fun generateReceiptPdfFile(
        context: Context,
        txId: Int,
        name: String,
        amount: String,
        date: String,
        paymentMethod: String = "Cash",
        remark: String = "",
        isMoneyIn: Boolean = false,
        projectName: String = "Treasure Garden",
        siteAddress: String = "Treasure Garden Site, India"
    ): File {
        val fileName = "Receipt_${txId}_${System.currentTimeMillis()}.pdf"
        val file     = File(context.cacheDir, fileName)
        val doc      = PdfDocument()
        val pw = A5_W; val ph = A5_H
        val page  = doc.startPage(PdfDocument.PageInfo.Builder(pw, ph, 1).create())
        val c     = page.canvas
        val p     = Paint().apply { isAntiAlias = true }
        val mg    = 28f

        // Page background
        p.color = BG_PAGE
        c.drawRect(0f, 0f, pw.toFloat(), ph.toFloat(), p)

        // Top accent
        p.color = if (isMoneyIn) GREEN_DARK else RED_DARK
        c.drawRect(0f, 0f, pw.toFloat(), 4f, p)

        var y = 22f

        // ── Header ────────────────────────────────────────────
        // Left: company
        p.reset(NAVY, 10f, true, Paint.Align.LEFT)
        c.drawText(truncateText(projectName, 130f, p), mg, y + 10f, p)
        p.reset(GRAY, 7.5f, false, Paint.Align.LEFT)
        c.drawText(truncateText(siteAddress.replace("\n", ", "), 130f, p), mg, y + 20f, p)
        c.drawText("GST: N/A", mg, y + 30f, p)

        // Right: receipt type badge
        val docTitle  = if (isMoneyIn) "PAYMENT RECEIVED" else "PAYMENT PAID"
        val badgeColor = if (isMoneyIn) GREEN_DARK else RED_DARK
        val badgeBg    = if (isMoneyIn) GREEN_LITE  else RED_LITE

        c.drawCard(
            pw - mg - 115f, y + 2f,
            pw - mg, y + 18f,
            badgeBg, p, borderColor = if (isMoneyIn) GREEN_ACCENT else RED_MID,
            borderWidth = 0.5f, radius = 10f
        )
        p.reset(badgeColor, 8f, true, Paint.Align.CENTER)
        c.drawText(docTitle, pw - mg - 57.5f, y + 13f, p)

        // Receipt # and date right
        p.reset(GRAY, 7.5f, false, Paint.Align.RIGHT)
        c.drawText("Receipt #$txId", pw - mg, y + 24f, p)
        p.reset(NAVY, 7.5f, true, Paint.Align.RIGHT)
        c.drawText(date, pw - mg, y + 34f, p)

        y += 44f

        // ── Divider ───────────────────────────────────────────
        c.drawHRule(mg, pw - mg, y, p, BORDER, 0.8f)
        y += 10f

        // ── Project + Party Info side by side ─────────────────
        val halfW = (pw - 2 * mg - 8f) / 2f
        c.drawCard(mg, y, mg + halfW, y + 42f, BG_LIGHT, p, borderColor = BORDER, borderWidth = 0.5f)
        p.reset(GRAY, 7f, false, Paint.Align.LEFT)
        c.drawText("PROJECT", mg + 8f, y + 12f, p)
        p.reset(NAVY, 9f, true, Paint.Align.LEFT)
        c.drawText(truncateText(projectName, halfW - 16f, p), mg + 8f, y + 26f, p)
        p.reset(GRAY, 7f, false, Paint.Align.LEFT)
        c.drawText("GST: N/A", mg + 8f, y + 38f, p)

        val rx = mg + halfW + 8f
        c.drawCard(rx, y, rx + halfW, y + 42f, BG_LIGHT, p, borderColor = BORDER, borderWidth = 0.5f)
        p.reset(GRAY, 7f, false, Paint.Align.LEFT)
        c.drawText("BILLED TO", rx + 8f, y + 12f, p)
        p.reset(NAVY, 9f, true, Paint.Align.LEFT)
        c.drawText(truncateText(name, halfW - 16f, p), rx + 8f, y + 26f, p)
        p.reset(GRAY, 7f, false, Paint.Align.LEFT)
        c.drawText("GST: N/A", rx + 8f, y + 38f, p)

        y += 52f

        // ── Amount Highlight ──────────────────────────────────
        val amtBg = if (isMoneyIn) GREEN_LITE else RED_LITE
        val amtFg = if (isMoneyIn) GREEN_DARK else RED_DARK
        c.drawCard(mg, y, pw - mg, y + 36f, amtBg, p, borderColor = if (isMoneyIn) GREEN_ACCENT else RED_MID, borderWidth = 0.6f)
        p.reset(amtFg, 8f, false, Paint.Align.LEFT)
        c.drawText("TOTAL AMOUNT", mg + 12f, y + 14f, p)
        p.reset(amtFg, 15f, true, Paint.Align.RIGHT)
        c.drawText("₹ $amount", pw - mg - 12f, y + 26f, p)
        y += 46f

        // ── Details Table ─────────────────────────────────────
        data class ReceiptRow(val label: String, val value: String)
        val rows = listOf(
            ReceiptRow("Payment Date",   date),
            ReceiptRow("Payment Method", paymentMethod.ifEmpty { "—" }),
            ReceiptRow("Remark",         remark.ifEmpty { "—" }),
            ReceiptRow("Attachment",     "—")
        )

        val col1End = mg + 120f
        val col2End = pw - mg

        // Table header
        c.drawCard(mg, y, pw - mg, y + 20f, DARK_SLATE, p, radius = 4f)
        p.reset(WHITE, 8f, true, Paint.Align.LEFT)
        c.drawText("DETAILS", mg + 10f, y + 13f, p)
        p.textAlign = Paint.Align.RIGHT
        c.drawText("VALUE", pw - mg - 10f, y + 13f, p)
        y += 24f

        rows.forEachIndexed { idx, row ->
            val rowBg = if (idx % 2 == 0) ROW_NORMAL else ROW_ALT
            c.drawCard(mg, y, pw - mg, y + ROW_HEIGHT, rowBg, p, borderColor = DIVIDER, borderWidth = 0.3f, radius = 0f)

            p.reset(GRAY, 8.5f, false, Paint.Align.LEFT)
            c.drawText(row.label, mg + 10f, y + ROW_HEIGHT * 0.65f, p)

            p.reset(NAVY, 8.5f, true, Paint.Align.RIGHT)
            c.drawText(
                truncateText(row.value, col2End - col1End - 12f, p),
                col2End - 10f, y + ROW_HEIGHT * 0.65f, p
            )
            y += ROW_HEIGHT
        }
        y += 14f

        // ── Message ───────────────────────────────────────────
        val action = if (isMoneyIn) "received from" else "paid to"
        p.reset(GRAY, 7.5f, false, Paint.Align.LEFT)
        c.drawText("We confirm that payment of ₹ $amount was $action $name on $date.", mg, y, p)
        c.drawText("Thank you for your business. Please contact us for any clarification.", mg, y + 12f, p)
        y += 28f

        // ── Signature Block ───────────────────────────────────
        c.drawHRule(mg, pw - mg, ph - 46f, p, BORDER, 0.5f)
        p.reset(NAVY, 8f, true, Paint.Align.RIGHT)
        c.drawText("Authorised Signatory", pw - mg, ph - 30f, p)
        p.reset(GRAY, 7f, false, Paint.Align.RIGHT)
        c.drawText("ConstructPro", pw - mg, ph - 18f, p)

        // Bottom accent
        p.color = DARK_SLATE
        c.drawRect(0f, ph - 5f, pw.toFloat(), ph.toFloat(), p)

        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ════════════════════════════════════════════════════════
    // 2. PARTY LEDGER / BALANCE REVIEW PDF (A4)
    // ════════════════════════════════════════════════════════

    fun generateBalanceReviewPdfFile(
        context: Context,
        partyName: String,
        projectName: String,
        balance: String,
        statusText: String,
        received: String,
        paid: String,
        transactions: List<Transaction> = emptyList(),
        siteAddress: String = "Treasure Garden Site, India",
        generatedBy: String = "Tejas Harane",
        dateRange: String = "All Time"
    ): File {
        val fileName = "Ledger_${partyName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val file     = File(context.cacheDir, fileName)
        val doc      = PdfDocument()
        val pw = A4_W; val ph = A4_H
        val margin   = PAGE_MARGIN
        val contentW = pw - 2 * margin
        val p        = Paint().apply { isAntiAlias = true }

        var pageNum   = 0
        var curPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var yPos      = 0f

        // Column definitions for transaction table
        val cols = listOf(
            ColumnDef("S.No",    margin,            36f,  Paint.Align.LEFT),
            ColumnDef("Date",    margin + 36f,      80f,  Paint.Align.LEFT),
            ColumnDef("Type",    margin + 116f,     68f,  Paint.Align.LEFT),
            ColumnDef("Method",  margin + 184f,     80f,  Paint.Align.LEFT),
            ColumnDef("Remark",  margin + 264f,     110f, Paint.Align.LEFT),
            ColumnDef("Amount",  margin + 374f,     contentW - 374f, Paint.Align.RIGHT)
        )

        fun newPage(): Canvas {
            curPage?.let { doc.finishPage(it) }
            pageNum++
            val pi = PdfDocument.PageInfo.Builder(pw, ph, pageNum).create()
            curPage = doc.startPage(pi)
            canvas  = curPage!!.canvas
            val c   = canvas!!
            p.color = BG_PAGE
            c.drawRect(0f, 0f, pw.toFloat(), ph.toFloat(), p)
            yPos = drawCommonHeader(c, p, pw, margin,
                "Party Ledger Report", generatedBy, dateRange,
                projectName, siteAddress, 28f, pageNum)
            return c
        }

        fun drawTxHeader(c: Canvas) {
            yPos = drawTableHeader(c, p, yPos, margin, pw, cols)
        }

        var c = newPage()

        // ── Party Details card ────────────────────────────────
        c.drawSectionTitle("PARTY DETAILS", margin, yPos + 2f, pw, p)
        yPos += 14f

        c.drawCard(
            margin - 4f, yPos,
            pw - margin + 4f, yPos + 38f,
            WHITE, p, borderColor = BORDER, borderWidth = 0.5f, radius = 5f
        )
        // Left bar accent
        p.color = ACCENT_LINE
        p.style = Paint.Style.FILL
        c.drawRect(margin - 4f, yPos, margin, yPos + 38f, p)

        p.reset(GRAY, 8f, false, Paint.Align.LEFT)
        c.drawText("PARTY NAME", margin + 10f, yPos + 13f, p)
        p.reset(NAVY, 11f, true, Paint.Align.LEFT)
        c.drawText(partyName, margin + 10f, yPos + 28f, p)
        yPos += 50f

        // ── Stat Cards ────────────────────────────────────────
        val isAdv    = statusText.contains("Advance", true) || statusText.contains("Paid", true)
        val balColor = if (isAdv) RED_DARK else GREEN_DARK

        yPos = drawStatCards(
            c, p, margin, yPos,
            Triple("Total Received", "₹ $received", GREEN_DARK),
            Triple("Total Paid",     "₹ $paid",     RED_DARK),
            Triple("Net Balance",    "₹ $balance",  balColor),
            contentW
        )

        // ── Transaction history ───────────────────────────────
        c.drawSectionTitle(
            "TRANSACTION HISTORY", margin, yPos + 2f, pw, p,
            subtitle = "${transactions.size} entries"
        )
        yPos += 14f
        drawTxHeader(c)

        if (transactions.isEmpty()) {
            yPos += 24f
            p.reset(LIGHT_GRAY, 10f, false, Paint.Align.CENTER)
            c.drawText("No transactions found for the selected period.", pw / 2f, yPos, p)
        } else {
            var runBal = 0.0
            transactions.forEachIndexed { idx, tx ->
                if (yPos > ph - FOOTER_H - 30f) {
                    drawFooter(c, p, pw, ph, margin, pageNum)
                    c = newPage()
                    drawTxHeader(c)
                }
                val isIn   = tx.type == "Money In"
                val rowBg  = if (idx % 2 == 0) ROW_NORMAL else ROW_ALT
                runBal    += if (isIn) tx.amount else -tx.amount

                c.drawCard(
                    margin - 4f, yPos, pw - margin + 4f, yPos + ROW_HEIGHT,
                    rowBg, p, borderColor = DIVIDER, borderWidth = 0.3f, radius = 0f
                )

                val typeColor  = if (isIn) GREEN_DARK else RED_DARK
                val sign       = if (isIn) "+" else "-"
                val amtText    = sign + "₹" + currencyFmt.format(tx.amount)

                p.reset(SLATE, 8.5f, false, Paint.Align.LEFT)
                c.drawText("${idx + 1}", cols[0].x + 6f, yPos + 17f, p)
                c.drawText(tx.date, cols[1].x + 4f, yPos + 17f, p)

                // Type with dot indicator
                p.color = typeColor
                p.style = Paint.Style.FILL
                c.drawCircle(cols[2].x + 10f, yPos + 12f, 3.5f, p)
                p.reset(typeColor, 8.5f, true, Paint.Align.LEFT)
                c.drawText(if (isIn) "Received" else "Paid", cols[2].x + 18f, yPos + 17f, p)

                p.reset(SLATE, 8f, false, Paint.Align.LEFT)
                c.drawText(truncateText(tx.paymentMethod, cols[3].width - 6f, p), cols[3].x + 4f, yPos + 17f, p)

                val remark = if (tx.description.isNotEmpty()) tx.description else "—"
                c.drawText(truncateText(remark, cols[4].width - 6f, p), cols[4].x + 4f, yPos + 17f, p)

                p.reset(typeColor, 8.5f, true, Paint.Align.RIGHT)
                c.drawText(amtText, cols[5].x + cols[5].width - 4f, yPos + 17f, p)

                yPos += ROW_HEIGHT
            }

            // Running balance summary row
            yPos += 4f
            c.drawCard(
                margin - 4f, yPos, pw - margin + 4f, yPos + 26f,
                DARK_SLATE, p, radius = 4f
            )
            p.reset(WHITE, 9f, true, Paint.Align.LEFT)
            c.drawText("CLOSING BALANCE", margin + 8f, yPos + 17f, p)
            p.textAlign = Paint.Align.RIGHT
            val balFmt  = (if (runBal >= 0) "+" else "") + "₹" + currencyFmt.format(runBal)
            p.color     = if (runBal >= 0) GREEN_MID else RED_MID
            c.drawText(balFmt, pw - margin - 8f, yPos + 17f, p)
            yPos += 36f
        }

        drawFooter(canvas!!, p, pw, ph, margin, pageNum)
        doc.finishPage(curPage!!)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ════════════════════════════════════════════════════════
    // 3. PARTY BALANCE REPORT PDF (A4)
    // ════════════════════════════════════════════════════════

    fun generatePartyBalanceReportPdfFile(
        context: Context,
        projectName: String,
        siteAddress: String,
        generatedBy: String,
        parties: List<com.aistudio.constructpro.kgrmqd.data.Worker>,
        transactions: List<Transaction>,
        dateRange: String = "From Start – Till Now"
    ): File {
        val fileName = "PartyBalanceReport_${System.currentTimeMillis()}.pdf"
        val file     = File(context.cacheDir, fileName)
        val doc      = PdfDocument()
        val pw = A4_W; val ph = A4_H
        val margin   = PAGE_MARGIN
        val contentW = pw - 2 * margin
        val p        = Paint().apply { isAntiAlias = true }

        var pageNum   = 0
        var curPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var yPos      = 0f

        val cols = listOf(
            ColumnDef("#",            margin,              24f,  Paint.Align.LEFT),
            ColumnDef("Party Name",   margin + 24f,        140f, Paint.Align.LEFT),
            ColumnDef("Type",         margin + 164f,       70f,  Paint.Align.LEFT),
            ColumnDef("Sales & Exp",  margin + 234f,       80f,  Paint.Align.RIGHT),
            ColumnDef("Payments",     margin + 314f,       74f,  Paint.Align.RIGHT),
            ColumnDef("Net Balance",  margin + 388f,       contentW - 388f, Paint.Align.RIGHT)
        )

        fun newPage(): Canvas {
            curPage?.let { doc.finishPage(it) }
            pageNum++
            val pi = PdfDocument.PageInfo.Builder(pw, ph, pageNum).create()
            curPage = doc.startPage(pi)
            canvas  = curPage!!.canvas
            val c   = canvas!!
            p.color = BG_PAGE
            c.drawRect(0f, 0f, pw.toFloat(), ph.toFloat(), p)
            yPos = drawCommonHeader(c, p, pw, margin,
                "Party Balance Report", generatedBy, dateRange,
                projectName, siteAddress, 28f, pageNum)
            return c
        }

        fun drawHeader(c: Canvas) {
            yPos = drawTableHeader(c, p, yPos, margin, pw, cols)
        }

        var c = newPage()

        // ── Grand totals ──────────────────────────────────────
        val grandIn  = transactions.filter { it.type == "Money In"  }.sumOf { it.amount }
        val grandOut = transactions.filter { it.type == "Money Out" }.sumOf { it.amount }
        val grandBal = grandIn - grandOut

        yPos = drawStatCards(
            c, p, margin, yPos,
            Triple("Total Received", "₹${currencyFmt.format(grandIn)}",  GREEN_DARK),
            Triple("Total Paid",     "₹${currencyFmt.format(grandOut)}", RED_DARK),
            Triple("Net Balance",    "₹${currencyFmt.format(grandBal)}", if (grandBal >= 0) GREEN_DARK else RED_DARK),
            contentW
        )

        c.drawSectionTitle("PARTY WISE BALANCE", margin, yPos + 2f, pw, p,
            subtitle = "${parties.size} parties")
        yPos += 14f
        drawHeader(c)

        if (parties.isEmpty()) {
            yPos += 24f
            p.reset(LIGHT_GRAY, 10f, false, Paint.Align.CENTER)
            c.drawText("No parties registered.", pw / 2f, yPos, p)
        } else {
            parties.forEachIndexed { idx, party ->
                if (yPos > ph - FOOTER_H - 30f) {
                    drawFooter(c, p, pw, ph, margin, pageNum)
                    c = newPage()
                    drawHeader(c)
                }

                val partyTx  = transactions.filter { it.partyId == party.id || it.partyName == party.name }
                val totalIn  = partyTx.filter { it.type == "Money In"  }.sumOf { it.amount }
                val totalOut = partyTx.filter { it.type == "Money Out" }.sumOf { it.amount }
                val net      = totalIn - totalOut
                val isClient = party.partyType == "Client" || party.partyType == "Investor"
                val payments = if (isClient) totalIn else totalOut
                val salesExp = if (isClient) totalOut else totalIn

                val rowBg = if (idx % 2 == 0) ROW_NORMAL else ROW_ALT
                c.drawCard(
                    margin - 4f, yPos, pw - margin + 4f, yPos + ROW_HEIGHT,
                    rowBg, p, borderColor = DIVIDER, borderWidth = 0.3f, radius = 0f
                )

                val balColor = if (net >= 0) GREEN_DARK else RED_DARK

                p.reset(GRAY, 8f, false, Paint.Align.LEFT)
                c.drawText("${idx + 1}", cols[0].x + 4f, yPos + 17f, p)

                p.reset(NAVY, 9f, true, Paint.Align.LEFT)
                c.drawText(truncateText(party.name, cols[1].width - 6f, p), cols[1].x + 4f, yPos + 17f, p)

                // Party type badge
                val typeBg = when (party.partyType) {
                    "Client"   -> BLUE_LITE
                    "Investor" -> PURPLE_LT
                    "Vendor"   -> AMBER_LITE
                    else       -> BG_LIGHT
                }
                val typeFg = when (party.partyType) {
                    "Client"   -> BLUE_DARK
                    "Investor" -> PURPLE
                    "Vendor"   -> AMBER_DARK
                    else       -> GRAY
                }
                val typeLabel = truncateText(party.partyType ?: "—", cols[2].width - 10f, p)
                val badgeW    = p.measureText(typeLabel) + 12f
                c.drawCard(
                    cols[2].x + 2f, yPos + 5f,
                    cols[2].x + 2f + badgeW, yPos + ROW_HEIGHT - 4f,
                    typeBg, p, borderColor = Color.TRANSPARENT, radius = 8f
                )
                p.reset(typeFg, 7.5f, true, Paint.Align.LEFT)
                c.drawText(typeLabel, cols[2].x + 8f, yPos + 16f, p)

                p.reset(SLATE, 8.5f, false, Paint.Align.RIGHT)
                c.drawText(
                    if (salesExp > 0) "₹${currencyFmt.format(salesExp)}" else "—",
                    cols[3].x + cols[3].width - 4f, yPos + 17f, p
                )
                c.drawText(
                    if (payments > 0) "₹${currencyFmt.format(payments)}" else "—",
                    cols[4].x + cols[4].width - 4f, yPos + 17f, p
                )

                p.reset(balColor, 8.5f, true, Paint.Align.RIGHT)
                val balLabel = "₹${currencyFmt.format(kotlin.math.abs(net))} ${if (net >= 0) "↑" else "↓"}"
                c.drawText(balLabel, cols[5].x + cols[5].width - 4f, yPos + 17f, p)

                yPos += ROW_HEIGHT
            }

            // Grand total row
            yPos += 4f
            c.drawCard(margin - 4f, yPos, pw - margin + 4f, yPos + 26f, DARK_SLATE, p, radius = 4f)
            p.reset(WHITE, 9f, true, Paint.Align.LEFT)
            c.drawText("GRAND TOTAL  (${parties.size} Parties)", margin + 8f, yPos + 17f, p)
            p.reset(GREEN_MID, 9f, true, Paint.Align.RIGHT)
            c.drawText("₹${currencyFmt.format(grandIn)}", cols[3].x + cols[3].width - 4f, yPos + 17f, p)
            p.reset(RED_MID, 9f, true, Paint.Align.RIGHT)
            c.drawText("₹${currencyFmt.format(grandOut)}", cols[4].x + cols[4].width - 4f, yPos + 17f, p)
            p.reset(if (grandBal >= 0) GREEN_MID else RED_MID, 9f, true, Paint.Align.RIGHT)
            c.drawText("₹${currencyFmt.format(kotlin.math.abs(grandBal))}", cols[5].x + cols[5].width - 4f, yPos + 17f, p)
            yPos += 36f
        }

        drawFooter(canvas!!, p, pw, ph, margin, pageNum)
        doc.finishPage(curPage!!)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ════════════════════════════════════════════════════════
    // 4. PAYMENT SUMMARY REPORT PDF (A4)
    // ════════════════════════════════════════════════════════

    fun generatePaymentSummaryReportPdfFile(
        context: Context,
        projectName: String,
        siteAddress: String,
        generatedBy: String,
        transactions: List<Transaction>,
        parties: List<com.aistudio.constructpro.kgrmqd.data.Worker>,
        dateRange: String = "All Time"
    ): File {
        val fileName = "PaymentSummary_${System.currentTimeMillis()}.pdf"
        val file     = File(context.cacheDir, fileName)
        val doc      = PdfDocument()
        val pw = A4_W; val ph = A4_H
        val margin   = PAGE_MARGIN
        val contentW = pw - 2 * margin
        val p        = Paint().apply { isAntiAlias = true }

        var pageNum   = 0
        var curPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var yPos      = 0f

        fun newPage(): Canvas {
            curPage?.let { doc.finishPage(it) }
            pageNum++
            val pi = PdfDocument.PageInfo.Builder(pw, ph, pageNum).create()
            curPage = doc.startPage(pi)
            canvas  = curPage!!.canvas
            val c   = canvas!!
            p.color = BG_PAGE
            c.drawRect(0f, 0f, pw.toFloat(), ph.toFloat(), p)
            yPos = drawCommonHeader(c, p, pw, margin,
                "Payment Summary Report", generatedBy, dateRange,
                projectName, siteAddress, 28f, pageNum)
            return c
        }

        var c = newPage()

        val totalIn  = transactions.filter { it.type == "Money In"  }.sumOf { it.amount }
        val totalOut = transactions.filter { it.type == "Money Out" }.sumOf { it.amount }
        val netBal   = totalIn - totalOut

        yPos = drawStatCards(
            c, p, margin, yPos,
            Triple("Total Received", "₹${currencyFmt.format(totalIn)}",  GREEN_DARK),
            Triple("Total Paid",     "₹${currencyFmt.format(totalOut)}", RED_DARK),
            Triple("Net Balance",    "₹${currencyFmt.format(netBal)}",   if (netBal >= 0) GREEN_DARK else RED_DARK),
            contentW
        )

        // ── Generic summary table helper ──────────────────────
        fun drawSummarySection(
            title: String,
            colLabels: List<String>,
            rows: List<List<String>>,
            totalRow: List<String>,
            colXPositions: List<Float>,
            colAligns: List<Paint.Align>
        ) {
            if (yPos > ph - FOOTER_H - 120f) {
                drawFooter(c, p, pw, ph, margin, pageNum)
                c = newPage()
            }
            c.drawSectionTitle(title, margin, yPos + 2f, pw, p)
            yPos += 14f

            // Header
            c.drawCard(margin - 4f, yPos, pw - margin + 4f, yPos + HEADER_H, DARK_SLATE, p, radius = 4f)
            p.reset(WHITE, 9f, true)
            colLabels.forEachIndexed { i, lbl ->
                p.textAlign = colAligns[i]
                c.drawText(lbl, colXPositions[i], yPos + 18f, p)
            }
            yPos += HEADER_H + 4f

            rows.forEachIndexed { idx, row ->
                if (yPos > ph - FOOTER_H - 36f) {
                    drawFooter(c, p, pw, ph, margin, pageNum)
                    c = newPage()
                    // Redraw header
                    c.drawCard(margin - 4f, yPos, pw - margin + 4f, yPos + HEADER_H, DARK_SLATE, p, radius = 4f)
                    p.reset(WHITE, 9f, true)
                    colLabels.forEachIndexed { i, lbl ->
                        p.textAlign = colAligns[i]
                        c.drawText(lbl, colXPositions[i], yPos + 18f, p)
                    }
                    yPos += HEADER_H + 4f
                }

                val rowBg = if (idx % 2 == 0) ROW_NORMAL else ROW_ALT
                c.drawCard(
                    margin - 4f, yPos, pw - margin + 4f, yPos + ROW_HEIGHT,
                    rowBg, p, borderColor = DIVIDER, borderWidth = 0.3f, radius = 0f
                )

                row.forEachIndexed { i, cell ->
                    val cellColor = when {
                        i == 2 -> GREEN_DARK
                        i == 3 -> RED_DARK
                        else   -> NAVY
                    }
                    p.reset(cellColor, if (i == 0) 9f else 8.5f, i == 0, colAligns[i])
                    val maxW = if (i == 0) 130f else 70f
                    c.drawText(truncateText(cell, maxW, p), colXPositions[i], yPos + 17f, p)
                }
                yPos += ROW_HEIGHT
            }

            // Total row
            yPos += 2f
            c.drawCard(margin - 4f, yPos, pw - margin + 4f, yPos + 26f, DARK_SLATE, p, radius = 4f)
            totalRow.forEachIndexed { i, cell ->
                p.reset(WHITE, 9f, true, colAligns[i])
                c.drawText(cell, colXPositions[i], yPos + 17f, p)
            }
            yPos += 36f
        }

        // Column x positions & alignments
        val sumXPos    = listOf(margin + 8f, margin + 220f, margin + 320f, pw - margin - 8f)
        val sumAligns  = listOf(Paint.Align.LEFT, Paint.Align.RIGHT, Paint.Align.RIGHT, Paint.Align.RIGHT)

        // ── Trade Summary ─────────────────────────────────────
        val tradeGroups = transactions.groupBy { tx ->
            parties.find { it.id == tx.partyId || it.name == tx.partyName }?.partyType ?: "Other"
        }
        val tradeRows = tradeGroups.map { (trade, txs) ->
            val tIn  = txs.filter { it.type == "Money In"  }.sumOf { it.amount }
            val tOut = txs.filter { it.type == "Money Out" }.sumOf { it.amount }
            listOf(trade, "${txs.size} entries",
                "₹${currencyFmt.format(tIn)}", "₹${currencyFmt.format(tOut)}")
        }
        drawSummarySection(
            "Trade Summary",
            listOf("Trade / Party Type", "Entries", "Received (₹)", "Paid (₹)"),
            tradeRows,
            listOf("Total", "${transactions.size}",
                "₹${currencyFmt.format(totalIn)}", "₹${currencyFmt.format(totalOut)}"),
            sumXPos, sumAligns
        )

        // ── Category Summary ──────────────────────────────────
        val catGroups = transactions.groupBy { it.category.ifEmpty { "Uncategorised" } }
        val catRows   = catGroups.map { (cat, txs) ->
            val tIn  = txs.filter { it.type == "Money In"  }.sumOf { it.amount }
            val tOut = txs.filter { it.type == "Money Out" }.sumOf { it.amount }
            listOf(cat, "${txs.size} entries",
                "₹${currencyFmt.format(tIn)}", "₹${currencyFmt.format(tOut)}")
        }
        drawSummarySection(
            "Category Summary",
            listOf("Category", "Entries", "Received (₹)", "Paid (₹)"),
            catRows,
            listOf("Total", "${transactions.size}",
                "₹${currencyFmt.format(totalIn)}", "₹${currencyFmt.format(totalOut)}"),
            sumXPos, sumAligns
        )

        drawFooter(canvas!!, p, pw, ph, margin, pageNum)
        doc.finishPage(curPage!!)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ════════════════════════════════════════════════════════
    // 5. PAYMENT TRANSACTIONS REPORT PDF (A4)
    // ════════════════════════════════════════════════════════

    fun generatePaymentTransactionsReportPdfFile(
        context: Context,
        projectName: String,
        siteAddress: String,
        generatedBy: String,
        transactions: List<Transaction>,
        dateRange: String = "All Time"
    ): File {
        val fileName = "PaymentTransactions_${System.currentTimeMillis()}.pdf"
        val file     = File(context.cacheDir, fileName)
        val doc      = PdfDocument()
        val pw = A4_W; val ph = A4_H
        val margin   = PAGE_MARGIN
        val contentW = pw - 2 * margin
        val p        = Paint().apply { isAntiAlias = true }

        var pageNum   = 0
        var curPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var yPos      = 0f

        val cols = listOf(
            ColumnDef("Date",        margin,             76f,  Paint.Align.LEFT),
            ColumnDef("Party",       margin + 76f,       110f, Paint.Align.LEFT),
            ColumnDef("Type",        margin + 186f,      64f,  Paint.Align.LEFT),
            ColumnDef("Category",    margin + 250f,      84f,  Paint.Align.LEFT),
            ColumnDef("Description", margin + 334f,      104f, Paint.Align.LEFT),
            ColumnDef("Amount",      margin + 438f,      contentW - 438f, Paint.Align.RIGHT)
        )

        fun newPage(): Canvas {
            curPage?.let { doc.finishPage(it) }
            pageNum++
            val pi = PdfDocument.PageInfo.Builder(pw, ph, pageNum).create()
            curPage = doc.startPage(pi)
            canvas  = curPage!!.canvas
            val c   = canvas!!
            p.color = BG_PAGE
            c.drawRect(0f, 0f, pw.toFloat(), ph.toFloat(), p)
            yPos = drawCommonHeader(c, p, pw, margin,
                "Payment Transactions Report", generatedBy, dateRange,
                projectName, siteAddress, 28f, pageNum)
            return c
        }

        fun drawTxHeader(c: Canvas) {
            yPos = drawTableHeader(c, p, yPos, margin, pw, cols)
        }

        var c = newPage()

        val totalIn  = transactions.filter { it.type == "Money In"  }.sumOf { it.amount }
        val totalOut = transactions.filter { it.type == "Money Out" }.sumOf { it.amount }
        val netBal   = totalIn - totalOut

        yPos = drawStatCards(
            c, p, margin, yPos,
            Triple("Total Received", "₹${currencyFmt.format(totalIn)}",  GREEN_DARK),
            Triple("Total Paid",     "₹${currencyFmt.format(totalOut)}", RED_DARK),
            Triple("Net Balance",    "₹${currencyFmt.format(netBal)}",   if (netBal >= 0) GREEN_DARK else RED_DARK),
            contentW
        )

        c.drawSectionTitle("ALL TRANSACTIONS", margin, yPos + 2f, pw, p,
            subtitle = "${transactions.size} records")
        yPos += 14f
        drawTxHeader(c)

        val sorted = transactions.sortedBy { it.date }
        sorted.forEachIndexed { idx, tx ->
            if (yPos > ph - FOOTER_H - 30f) {
                drawFooter(c, p, pw, ph, margin, pageNum)
                c = newPage()
                drawTxHeader(c)
            }
            val isIn      = tx.type == "Money In"
            val typeColor = if (isIn) GREEN_DARK else RED_DARK
            val rowBg     = if (idx % 2 == 0) ROW_NORMAL else ROW_ALT

            c.drawCard(
                margin - 4f, yPos, pw - margin + 4f, yPos + ROW_HEIGHT,
                rowBg, p, borderColor = DIVIDER, borderWidth = 0.3f, radius = 0f
            )

            p.reset(NAVY, 8.5f, false, Paint.Align.LEFT)
            c.drawText(tx.date, cols[0].x + 4f, yPos + 17f, p)
            c.drawText(truncateText(tx.partyName ?: "—", cols[1].width - 6f, p), cols[1].x + 4f, yPos + 17f, p)

            // Type badge
            p.color = typeColor
            p.style = Paint.Style.FILL
            c.drawCircle(cols[2].x + 8f, yPos + 12f, 3f, p)
            p.reset(typeColor, 8.5f, true, Paint.Align.LEFT)
            c.drawText(if (isIn) "In" else "Out", cols[2].x + 16f, yPos + 17f, p)

            p.reset(SLATE, 8f, false, Paint.Align.LEFT)
            c.drawText(
                truncateText(tx.category.ifEmpty { "—" }, cols[3].width - 6f, p),
                cols[3].x + 4f, yPos + 17f, p
            )
            c.drawText(
                truncateText(tx.description.ifEmpty { "—" }, cols[4].width - 6f, p),
                cols[4].x + 4f, yPos + 17f, p
            )

            p.reset(typeColor, 8.5f, true, Paint.Align.RIGHT)
            c.drawText(
                "${if (isIn) "+" else "-"}₹${currencyFmt.format(tx.amount)}",
                cols[5].x + cols[5].width - 4f, yPos + 17f, p
            )

            yPos += ROW_HEIGHT
        }

        // Grand total row
        yPos += 4f
        c.drawCard(margin - 4f, yPos, pw - margin + 4f, yPos + 26f, DARK_SLATE, p, radius = 4f)
        p.reset(WHITE, 9f, true, Paint.Align.LEFT)
        c.drawText("TOTAL  (${sorted.size} transactions)", margin + 8f, yPos + 17f, p)
        p.reset(GREEN_MID, 9f, true, Paint.Align.RIGHT)
        c.drawText("+₹${currencyFmt.format(totalIn)}", cols[4].x + cols[4].width - 4f, yPos + 17f, p)
        p.reset(RED_MID, 9f, true, Paint.Align.RIGHT)
        c.drawText("-₹${currencyFmt.format(totalOut)}", cols[5].x + cols[5].width - 4f, yPos + 17f, p)
        yPos += 36f

        drawFooter(canvas!!, p, pw, ph, margin, pageNum)
        doc.finishPage(curPage!!)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ════════════════════════════════════════════════════════
    // 6. PARTY TRANSACTIONS REPORT PDF (A4)
    // ════════════════════════════════════════════════════════

    fun generatePartyTransactionsReportPdfFile(
        context: Context,
        projectName: String,
        siteAddress: String,
        generatedBy: String,
        party: com.aistudio.constructpro.kgrmqd.data.Worker,
        transactions: List<Transaction>,
        dateRange: String = "All Time"
    ): File {
        val fileName = "PartyTx_${party.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val file     = File(context.cacheDir, fileName)
        val doc      = PdfDocument()
        val pw = A4_W; val ph = A4_H
        val margin   = PAGE_MARGIN
        val contentW = pw - 2 * margin
        val p        = Paint().apply { isAntiAlias = true }

        var pageNum   = 0
        var curPage: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var yPos      = 0f

        val cols = listOf(
            ColumnDef("#",           margin,             24f,  Paint.Align.LEFT),
            ColumnDef("Date",        margin + 24f,       76f,  Paint.Align.LEFT),
            ColumnDef("Type",        margin + 100f,      64f,  Paint.Align.LEFT),
            ColumnDef("Description", margin + 164f,      160f, Paint.Align.LEFT),
            ColumnDef("Debit",       margin + 324f,      74f,  Paint.Align.RIGHT),
            ColumnDef("Credit",      margin + 398f,      60f,  Paint.Align.RIGHT),
            ColumnDef("Balance",     margin + 458f,      contentW - 458f, Paint.Align.RIGHT)
        )

        fun newPage(): Canvas {
            curPage?.let { doc.finishPage(it) }
            pageNum++
            val pi = PdfDocument.PageInfo.Builder(pw, ph, pageNum).create()
            curPage = doc.startPage(pi)
            canvas  = curPage!!.canvas
            val c   = canvas!!
            p.color = BG_PAGE
            c.drawRect(0f, 0f, pw.toFloat(), ph.toFloat(), p)
            yPos = drawCommonHeader(c, p, pw, margin,
                "Party Statement", generatedBy, dateRange,
                projectName, siteAddress, 28f, pageNum)
            return c
        }

        fun drawTxHeader(c: Canvas) {
            yPos = drawTableHeader(c, p, yPos, margin, pw, cols)
        }

        var c = newPage()

        // Party header card
        c.drawSectionTitle("PARTY STATEMENT", margin, yPos + 2f, pw, p)
        yPos += 14f

        c.drawCard(margin - 4f, yPos, pw - margin + 4f, yPos + 40f, WHITE, p, borderColor = BORDER, borderWidth = 0.5f)
        p.color = ACCENT_LINE
        p.style = Paint.Style.FILL
        c.drawRect(margin - 4f, yPos, margin + 1f, yPos + 40f, p)

        p.reset(GRAY, 8f, false, Paint.Align.LEFT)
        c.drawText("PARTY NAME", margin + 10f, yPos + 14f, p)
        p.reset(NAVY, 11f, true, Paint.Align.LEFT)
        c.drawText(party.name, margin + 10f, yPos + 28f, p)

        p.reset(GRAY, 8f, false, Paint.Align.RIGHT)
        c.drawText("PARTY TYPE", pw - margin - 10f, yPos + 14f, p)
        p.reset(PURPLE, 10f, true, Paint.Align.RIGHT)
        c.drawText(party.partyType ?: "—", pw - margin - 10f, yPos + 28f, p)
        yPos += 52f

        val partyTxs = transactions.filter { it.partyId == party.id || it.partyName == party.name }
        val sorted   = partyTxs.sortedBy { it.date }
        val totalIn  = sorted.filter { it.type == "Money In"  }.sumOf { it.amount }
        val totalOut = sorted.filter { it.type == "Money Out" }.sumOf { it.amount }
        val netBal   = totalIn - totalOut

        yPos = drawStatCards(
            c, p, margin, yPos,
            Triple("Total Credit",  "₹${currencyFmt.format(totalIn)}",  GREEN_DARK),
            Triple("Total Debit",   "₹${currencyFmt.format(totalOut)}", RED_DARK),
            Triple("Closing Balance","₹${currencyFmt.format(kotlin.math.abs(netBal))}",
                if (netBal >= 0) GREEN_DARK else RED_DARK),
            contentW
        )

        c.drawSectionTitle("STATEMENT DETAILS", margin, yPos + 2f, pw, p,
            subtitle = "${sorted.size} transactions")
        yPos += 14f
        drawTxHeader(c)

        var runBal = 0.0
        sorted.forEachIndexed { idx, tx ->
            if (yPos > ph - FOOTER_H - 30f) {
                drawFooter(c, p, pw, ph, margin, pageNum)
                c = newPage()
                drawTxHeader(c)
            }
            val isIn      = tx.type == "Money In"
            val typeColor = if (isIn) GREEN_DARK else RED_DARK
            runBal       += if (isIn) tx.amount else -tx.amount
            val rowBg     = if (idx % 2 == 0) ROW_NORMAL else ROW_ALT

            c.drawCard(
                margin - 4f, yPos, pw - margin + 4f, yPos + ROW_HEIGHT,
                rowBg, p, borderColor = DIVIDER, borderWidth = 0.3f, radius = 0f
            )

            p.reset(GRAY, 8f, false, Paint.Align.LEFT)
            c.drawText("${idx + 1}", cols[0].x + 4f, yPos + 17f, p)

            p.reset(NAVY, 8.5f, false, Paint.Align.LEFT)
            c.drawText(tx.date, cols[1].x + 4f, yPos + 17f, p)

            p.color = typeColor
            p.style = Paint.Style.FILL
            c.drawCircle(cols[2].x + 8f, yPos + 12f, 3f, p)
            p.reset(typeColor, 8.5f, true, Paint.Align.LEFT)
            c.drawText(if (isIn) "Credit" else "Debit", cols[2].x + 16f, yPos + 17f, p)

            p.reset(SLATE, 8f, false, Paint.Align.LEFT)
            c.drawText(
                truncateText(tx.description.ifEmpty { "—" }, cols[3].width - 6f, p),
                cols[3].x + 4f, yPos + 17f, p
            )

            // Debit column (Money Out)
            if (!isIn) {
                p.reset(RED_DARK, 8.5f, true, Paint.Align.RIGHT)
                c.drawText("₹${currencyFmt.format(tx.amount)}", cols[4].x + cols[4].width - 4f, yPos + 17f, p)
            } else {
                p.reset(LIGHT_GRAY, 8f, false, Paint.Align.RIGHT)
                c.drawText("—", cols[4].x + cols[4].width - 4f, yPos + 17f, p)
            }

            // Credit column (Money In)
            if (isIn) {
                p.reset(GREEN_DARK, 8.5f, true, Paint.Align.RIGHT)
                c.drawText("₹${currencyFmt.format(tx.amount)}", cols[5].x + cols[5].width - 4f, yPos + 17f, p)
            } else {
                p.reset(LIGHT_GRAY, 8f, false, Paint.Align.RIGHT)
                c.drawText("—", cols[5].x + cols[5].width - 4f, yPos + 17f, p)
            }

            // Running balance
            p.reset(if (runBal >= 0) GREEN_DARK else RED_DARK, 8.5f, true, Paint.Align.RIGHT)
            c.drawText(
                (if (runBal >= 0) "+" else "") + "₹${currencyFmt.format(runBal)}",
                cols[6].x + cols[6].width - 4f, yPos + 17f, p
            )

            yPos += ROW_HEIGHT
        }

        // Closing row
        yPos += 4f
        c.drawCard(margin - 4f, yPos, pw - margin + 4f, yPos + 26f, DARK_SLATE, p, radius = 4f)
        p.reset(WHITE, 9f, true, Paint.Align.LEFT)
        c.drawText("CLOSING BALANCE", margin + 8f, yPos + 17f, p)
        p.reset(RED_MID, 9f, true, Paint.Align.RIGHT)
        c.drawText("₹${currencyFmt.format(totalOut)}", cols[4].x + cols[4].width - 4f, yPos + 17f, p)
        p.reset(GREEN_MID, 9f, true, Paint.Align.RIGHT)
        c.drawText("₹${currencyFmt.format(totalIn)}", cols[5].x + cols[5].width - 4f, yPos + 17f, p)
        val finalBal = (if (runBal >= 0) "+" else "") + "₹${currencyFmt.format(runBal)}"
        p.reset(if (runBal >= 0) GREEN_MID else RED_MID, 9f, true, Paint.Align.RIGHT)
        c.drawText(finalBal, cols[6].x + cols[6].width - 4f, yPos + 17f, p)
        yPos += 36f

        drawFooter(canvas!!, p, pw, ph, margin, pageNum)
        doc.finishPage(curPage!!)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    // ════════════════════════════════════════════════════════
    // SHARE
    // ════════════════════════════════════════════════════════

    fun sharePdfFile(context: Context, file: File, title: String = "Share PDF") {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
