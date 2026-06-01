package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfUtils {

    fun generateReceiptPdfFile(context: Context, txId: Int, name: String, amount: String, date: String): File {
        val fileName = "Receipt_${txId}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 400, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        paint.textSize = 12f
        paint.color = Color.BLACK
        
        var y = 30f
        val x = 20f
        paint.isFakeBoldText = true
        canvas.drawText("CONSTRUCTPRO - SITE RECEIPT", x, y, paint)
        paint.isFakeBoldText = false
        y += 20f
        canvas.drawText("====================================", x, y, paint)
        y += 30f
        canvas.drawText("Party   : $name", x, y, paint)
        y += 20f
        canvas.drawText("Amount  : Rs. $amount", x, y, paint)
        y += 20f
        canvas.drawText("Date    : $date", x, y, paint)
        y += 20f
        canvas.drawText("Ref ID  : #$txId", x, y, paint)
        y += 40f
        canvas.drawText("------------------------------------", x, y, paint)
        y += 20f
        paint.isFakeBoldText = true
        paint.textSize = 10f
        canvas.drawText("Digitally Verified & Authenticated", x, y, paint)
        pdfDocument.finishPage(page)
        
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    fun generateBalanceReviewPdfFile(
        context: Context,
        partyName: String,
        projectName: String,
        balance: String,
        statusText: String,
        received: String,
        paid: String
    ): File {
        val fileName = "Balance_${partyName}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(350, 450, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        paint.textSize = 13f
        paint.color = Color.BLACK
        
        var y = 30f
        val x = 20f
        paint.isFakeBoldText = true
        canvas.drawText("CONSTRUCTPRO - BALANCE REVIEW", x, y, paint)
        paint.isFakeBoldText = false
        y += 20f
        canvas.drawText("========================================", x, y, paint)
        y += 30f
        canvas.drawText("Party    : $partyName", x, y, paint)
        y += 20f
        canvas.drawText("Project  : $projectName", x, y, paint)
        y += 30f
        paint.isFakeBoldText = true
        canvas.drawText("Balance  : $balance ($statusText)", x, y, paint)
        paint.isFakeBoldText = false
        y += 30f
        canvas.drawText("Total Received : $received", x, y, paint)
        y += 20f
        canvas.drawText("Total Paid     : $paid", x, y, paint)
        y += 50f
        canvas.drawText("----------------------------------------", x, y, paint)
        y += 20f
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText("Generated from ConstructPro App", x, y, paint)
        pdfDocument.finishPage(page)
        
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    fun sharePdfFile(context: Context, file: File, title: String = "Share PDF") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
