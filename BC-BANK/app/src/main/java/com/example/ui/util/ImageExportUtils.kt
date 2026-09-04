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
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageExportUtils {

    fun generateReceiptBitmap(
        title: String,
        opCode: String,
        amount: Double,
        beneficiary: String,
        concept: String,
        serial: String = ""
    ): Bitmap {
        val width = 800
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark background #0F172A
        val bgPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card container #1E293B
        val cardPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val cardRect = RectF(40f, 40f, (width - 40).toFloat(), (height - 40).toFloat())
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        // Card Border #10B981
        val borderPaint = Paint().apply {
            color = Color.rgb(16, 185, 129)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

        // Header Title
        val titlePaint = Paint().apply {
            color = Color.rgb(16, 185, 129)
            textSize = 36f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("BC-BANK", width / 2f, 120f, titlePaint)

        val subTitlePaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 24f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Comprobante Digital de Operación", width / 2f, 170f, subTitlePaint)

        // Amount
        val amountPaint = Paint().apply {
            color = Color.WHITE
            textSize = 54f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(String.format(Locale.US, "S/ %.2f", amount), width / 2f, 260f, amountPaint)

        val descPaint = Paint().apply {
            color = Color.rgb(16, 185, 129)
            textSize = 26f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, width / 2f, 310f, descPaint)

        // Divider
        val linePaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            strokeWidth = 2f
        }
        canvas.drawLine(80f, 360f, (width - 80).toFloat(), 360f, linePaint)

        // Details List
        val labelPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 24f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        var y = 430f
        fun drawRow(label: String, value: String) {
            canvas.drawText(label, 80f, y, labelPaint)
            canvas.drawText(value, (width - 80).toFloat(), y, valuePaint)
            y += 65f
        }

        drawRow("Beneficiario / Destino", beneficiary.take(24))
        drawRow("Concepto", concept.take(24))
        drawRow("N° Operación", opCode)
        if (serial.isNotBlank()) {
            drawRow("Serial de Validación", serial)
        }
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        drawRow("Fecha y Hora", dateStr)
        drawRow("Canal", "App Móvil BC-BANK")

        // Bottom Guarantee
        val footerPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 20f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Constancia electrónica emitida bajo cifrado bancario seguro.", width / 2f, (height - 90).toFloat(), footerPaint)

        return bitmap
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Uri? {
        val fileName = "$title.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "BC-BANK")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val bcDir = File(imagesDir, "BC-BANK").apply { mkdirs() }
                val image = File(bcDir, fileName)
                fos = FileOutputStream(image)
                imageUri = Uri.fromFile(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return imageUri
    }

    fun shareReceiptCard(
        context: Context,
        bitmap: Bitmap,
        opCode: String,
        amount: Double,
        beneficiary: String
    ) {
        try {
            val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
            val stream = FileOutputStream(File(cachePath, "constancia_bcbank_$opCode.png"))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val imagePath = File(context.cacheDir, "images")
            val newFile = File(imagePath, "constancia_bcbank_$opCode.png")
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Constancia de pago BC-BANK por S/ %.2f a %s. Operación: %s".format(Locale.US, amount, beneficiary, opCode)
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir constancia"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateBrandedQrCard(
        qrContent: String,
        holderName: String,
        phoneOrCci: String,
        accountType: String
    ): Bitmap? {
        return try {
            val width = 700
            val height = 900
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background
            val bgPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Card
            val cardRect = RectF(30f, 30f, (width - 30).toFloat(), (height - 30).toFloat())
            val cardPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(cardRect, 28f, 28f, cardPaint)

            val borderPaint = Paint().apply {
                color = Color.rgb(16, 185, 129)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(cardRect, 28f, 28f, borderPaint)

            // Brand title
            val brandPaint = Paint().apply {
                color = Color.rgb(16, 185, 129)
                textSize = 38f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("BC-BANK", width / 2f, 95f, brandPaint)

            val subPaint = Paint().apply {
                color = Color.rgb(148, 163, 184)
                textSize = 20f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(accountType, width / 2f, 130f, subPaint)

            // QR Generation using ZXing
            val qrSize = 380
            val bitMatrix = com.google.zxing.qrcode.QRCodeWriter().encode(
                qrContent,
                com.google.zxing.BarcodeFormat.QR_CODE,
                qrSize,
                qrSize
            )
            val qrBitmap = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.ARGB_8888)
            for (x in 0 until qrSize) {
                for (y in 0 until qrSize) {
                    qrBitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }

            // QR Card Container
            val qrBgRect = RectF((width - qrSize) / 2f - 16f, 165f, (width + qrSize) / 2f + 16f, 165f + qrSize + 32f)
            val qrBgPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(qrBgRect, 20f, 20f, qrBgPaint)
            canvas.drawBitmap(qrBitmap, (width - qrSize) / 2f, 181f, null)

            // User info
            val namePaint = Paint().apply {
                color = Color.WHITE
                textSize = 30f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(holderName, width / 2f, 665f, namePaint)

            val phonePaint = Paint().apply {
                color = Color.rgb(52, 211, 153)
                textSize = 24f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(phoneOrCci, width / 2f, 710f, phonePaint)

            val footerPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 18f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Escanea desde tu app BC-BANK para transferir", width / 2f, 790f, footerPaint)

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareQrCard(
        context: Context,
        bitmap: Bitmap?,
        holderName: String,
        phoneOrCci: String
    ) {
        if (bitmap == null) return
        try {
            val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(cachePath, "bcbank_qr_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Transfiere a $holderName ($phoneOrCci) mediante BC-BANK escaneando este código QR."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir código QR BC-BANK"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
