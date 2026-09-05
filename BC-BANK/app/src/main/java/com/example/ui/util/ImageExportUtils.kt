package com.example.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageExportUtils {

    fun generateReceiptBitmap(
        title: String,
        opCode: String,
        amount: Double,
        beneficiary: String,
        concept: String
    ): Bitmap {
        val width = 720
        val height = 960
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val cardPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.FILL
        }
        val cardRect = RectF(40f, 60f, width - 40f, height - 60f)
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        val borderPaint = Paint().apply {
            color = Color.rgb(16, 185, 129)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 36f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("BC-BANK CONSTANCIA", width / 2f, 140f, titlePaint)

        val subPaint = Paint().apply {
            color = Color.rgb(52, 211, 153)
            textSize = 26f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("¡Operación Exitosa!", width / 2f, 190f, subPaint)

        val amountPaint = Paint().apply {
            color = Color.WHITE
            textSize = 64f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("S/ ${String.format(Locale.US, "%.2f", amount)}", width / 2f, 290f, amountPaint)

        val labelPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 24f
            textAlign = Paint.Align.LEFT
        }
        val valuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 26f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }

        var currentY = 400f
        val leftX = 80f
        val rightX = width - 80f

        val details = listOf(
            "Beneficiario" to beneficiary,
            "Concepto" to if (concept.isBlank()) "Transferencia inmediata" else concept,
            "Código de Operación" to opCode,
            "Fecha y Hora" to SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
            "Canal" to "Banca Digital BC-BANK"
        )

        for ((label, value) in details) {
            canvas.drawText(label, leftX, currentY, labelPaint)
            val displayVal = if (value.length > 25) value.take(22) + "..." else value
            canvas.drawText(displayVal, rightX, currentY, valuePaint)

            val linePaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                strokeWidth = 1.5f
            }
            canvas.drawLine(leftX, currentY + 20f, rightX, currentY + 20f, linePaint)
            currentY += 80f
        }

        val footerPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Comprobante oficial emitido por BC-BANK", width / 2f, height - 100f, footerPaint)

        return bitmap
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Uri? {
        val filename = "${title}_${System.currentTimeMillis()}.png"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BCBank")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }
                uri
            } else {
                val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                FileOutputStream(image).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                Uri.fromFile(image)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareReceiptCard(
        context: Context,
        bitmap: Bitmap,
        opCode: String,
        amount: Double,
        beneficiary: String
    ) {
        try {
            val cachePath = File(context.cacheDir, "receipts")
            cachePath.mkdirs()
            val file = File(cachePath, "constancia_${opCode}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Constancia de transferencia BC-BANK por S/ $amount a $beneficiary. Operación: $opCode"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir constancia"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
