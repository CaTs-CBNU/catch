package com.cbnu.cat_ch.gallery.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.gallery.data.StoryItem
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PdfUtils {
    private fun breakTextIntoLines(text: String, paint: Paint, pageWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var line = ""
        var lineWidth = 0f

        for (word in words) {
            val wordWidth = paint.measureText("$word ")
            if (lineWidth + wordWidth > pageWidth) {
                lines.add(line.trim())
                line = ""
                lineWidth = 0f
            }
            line += "$word "
            lineWidth += wordWidth
        }

        if (line.isNotEmpty()) {
            lines.add(line.trim())
        }

        return lines
    }
    private fun sanitizeFilename(title: String): String {
        // Remove or replace invalid filename characters
        return title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    fun openPdf(pdfUri: Uri, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            startActivity(context, intent, null)
        } catch (e: Exception) {
            Toast.makeText(context, "No application found to open PDF", Toast.LENGTH_SHORT).show()
        }
    }

//    fun generatePdfFile(storyItem: StoryItem, context: Context): File? {
//        try {
//            val pdfDocument = PdfDocument()
//            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
//            val page = pdfDocument.startPage(pageInfo)
//
//            val canvas = page.canvas
//
//            // Load custom font
//            val titleFont = ResourcesCompat.getFont(context, R.font.crb)
//            val bodyFont = ResourcesCompat.getFont(context, R.font.crr)
//
//            // Paint for the image
//            val paint = Paint()
//            paint.color = Color.BLACK
//
//            // Draw background image (full-page background)
//            val backgroundDrawableId = R.drawable.background // Replace with your background drawable resource
//            val backgroundBitmap = BitmapFactory.decodeResource(context.resources, backgroundDrawableId)
//            if (backgroundBitmap != null) {
//                // Draw the background image to cover the entire page
//                val backgroundRect = RectF(0f, 0f, pageInfo.pageWidth.toFloat(), pageInfo.pageHeight.toFloat())
//                canvas.drawBitmap(backgroundBitmap, null, backgroundRect, paint)
//            } else {
//                Log.e("GalleryStoryDetailedFragment", "Error decoding background image")
//            }
//            // Initialize textY
//            var textY = 80f // Start position
//
//            // Draw image if available
//            val imagePath = storyItem.storyImagePath
//            if (imagePath != null) {
//                val imageUri = Uri.parse(imagePath)
//                val imageFile = getFileFromUri(imageUri, context)
//                if (imageFile?.exists() == true) {
//                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
//                    if (bitmap != null) {
//                        val imageWidth = pageInfo.pageWidth.toFloat() - 160f // Adjust width as needed
//                        val aspectRatio = bitmap.height.toFloat() / bitmap.width
//                        val imageHeight = (imageWidth * aspectRatio).toInt()
//                        val imageX = 80f
//                        val imageYImage = textY // Position at the top
//
//                        canvas.drawBitmap(bitmap, null, RectF(imageX, imageYImage, imageX + imageWidth, imageYImage + imageHeight), paint)
//
//                        // Update textY to below the image
//                        textY = imageYImage + imageHeight + 20f
//                    } else {
//                        Log.e("GalleryStoryDetailedFragment", "Error decoding image")
//                    }
//                } else {
//                    Log.e("GalleryStoryDetailedFragment", "Image file does not exist")
//                }
//            }
//
//            // Paint for the title and body text
//            val titlePaint = Paint().apply {
//                color = Color.BLACK
//                textSize = 20f
//                typeface = titleFont
//            }
//            val bodyPaint = Paint().apply {
//                color = Color.BLACK
//                textSize = 16f
//                typeface = bodyFont
//            }
//
//            // Draw title
//            val titleX = 80f
//            val titleY = textY // Position below the image
//            canvas.drawText("제목: ${storyItem.storyTitle}", titleX, titleY, titlePaint)
//
//            // Draw description background (CardView-like background)
//            val cardPadding = 20f
//            val cardMargin = 40f
//            val cardLeft = cardMargin
//            val cardRight = pageInfo.pageWidth - cardMargin
//            val cardTop = titleY + 40f
//            val cardBottom = cardTop + 300f // Adjust height as needed
//            val cardBackgroundPaint = Paint().apply {
//                color = Color.WHITE // Background color of the "CardView"
//                style = Paint.Style.FILL
//                setShadowLayer(8f, 4f, 4f, Color.LTGRAY) // Optional: Shadow effect
//            }
//
//            // Draw card background rectangle
//            canvas.drawRoundRect(
//                cardLeft,
//                cardTop,
//                cardRight,
//                cardBottom,
//                20f, // Corner radius
//                20f, // Corner radius
//                cardBackgroundPaint
//            )
//
//            // Draw description with word wrapping inside the "CardView"
//            val textX = cardLeft + cardPadding
//            textY = cardTop + cardPadding + bodyPaint.textSize // Start position inside the card
//            val pageWidth = cardRight - cardLeft - cardPadding * 2 // Account for padding inside the card
//
//            val textLines = breakTextIntoLines(storyItem.storyText, bodyPaint, pageWidth)
//
//            for (line in textLines) {
//                canvas.drawText(line, textX, textY, bodyPaint)
//                textY += bodyPaint.descent() - bodyPaint.ascent()
//            }
//
//            pdfDocument.finishPage(page)
//
//            // Save the document with title as filename
//            val sanitizedTitle = sanitizeFilename(storyItem.storyTitle)
//            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$sanitizedTitle.pdf")
//            pdfDocument.writeTo(FileOutputStream(file))
//            pdfDocument.close()
//
//            return file
//        } catch (e: Exception) {
//            Log.e("GalleryStoryDetailedFragment", "Error generating PDF", e)
//            return null
//        }
//    }
fun generatePdfFile(storyItems: List<StoryItem>, pdfFileName: String, storyTitle: String, context: Context): File? {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points

        // Title Page (First Page)
        val titlePage = pdfDocument.startPage(pageInfo)
        val canvas = titlePage.canvas

        // Title Font
        val titleFont = ResourcesCompat.getFont(context, R.font.crb)
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 36f
            typeface = titleFont
            textAlign = Paint.Align.CENTER
        }

        // Draw the story title centered on the title page
        canvas.drawText(storyTitle, pageInfo.pageWidth / 2f, pageInfo.pageHeight / 2f, titlePaint)
        pdfDocument.finishPage(titlePage)

        // Table of Contents Page (Second Page)
        val tocPage = pdfDocument.startPage(pageInfo)
        val tocCanvas = tocPage.canvas

        // Table of Contents Header, Centered
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = titleFont
            textAlign = Paint.Align.CENTER
        }
        tocCanvas.drawText("목차", pageInfo.pageWidth / 2f, 80f, headerPaint)

        // Body Font for TOC entries
        val bodyFont = ResourcesCompat.getFont(context, R.font.crr)
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = bodyFont
        }

        // Draw each entry in Table of Contents
        var tocYPosition = 120f // Starting position for TOC entries
        val dotSpacing = 5 // Space between each dot in the dotted line

        storyItems.forEachIndexed { index, storyItem ->
            val pageTitle = "${index + 1}. ${storyItem.storyTitle}" // Add numbering prefix
            val pageNum = index + 3 // Start page numbers after TOC

            // Draw the title on the left
            tocCanvas.drawText(pageTitle, 60f, tocYPosition, bodyPaint)

            // Draw the dotted line
            val titleWidth = bodyPaint.measureText(pageTitle)
            val pageNumWidth = bodyPaint.measureText("page $pageNum")
            val dotsWidth = pageInfo.pageWidth - 120f - titleWidth - pageNumWidth
            val numDots = (dotsWidth / dotSpacing).toInt()

            var dotX = 60f + titleWidth
            repeat(numDots) {
                tocCanvas.drawText(".", dotX, tocYPosition, bodyPaint)
                dotX += dotSpacing
            }

            // Draw the page number on the right
            tocCanvas.drawText("page $pageNum", pageInfo.pageWidth - 60f - pageNumWidth, tocYPosition, bodyPaint)

            tocYPosition += 30f // Move down for the next entry
        }
        pdfDocument.finishPage(tocPage)

        // Content Pages for each story
        storyItems.forEachIndexed { index, storyItem ->
            val contentPage = pdfDocument.startPage(pageInfo)
            val contentCanvas = contentPage.canvas
            var yPosition = 80f
            // Set bodyPaint alignment to center for the date
            bodyPaint.textAlign = Paint.Align.CENTER
            // Draw Date
            contentCanvas.drawText(storyItem.formattedDate ?: "날짜 없음", pageInfo.pageWidth / 2f, yPosition, bodyPaint)
            yPosition += 40f

            // Reset text alignment back to left for the rest of the content
            bodyPaint.textAlign = Paint.Align.LEFT


            // Draw Title
            contentCanvas.drawText("제목: ${storyItem.storyTitle}", pageInfo.pageWidth / 2f, yPosition, titlePaint)
            yPosition += 40f

            // Draw Divider
            val dividerPaint = Paint().apply {
                color = Color.LTGRAY // Choose the color for the divider
                strokeWidth = 2f     // Adjust the thickness of the divider
            }
            contentCanvas.drawLine(60f, yPosition, pageInfo.pageWidth - 60f, yPosition, dividerPaint)
            yPosition += 20f // Add some spacing after the divider

            // Draw Image
            storyItem.storyImagePath?.let { imagePath ->
                val imageUri = Uri.parse(imagePath)
                val imageFile = getFileFromUri(imageUri, context)
                if (imageFile?.exists() == true) {
                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 230, 230, false)
                    contentCanvas.drawBitmap(scaledBitmap, (pageInfo.pageWidth - scaledBitmap.width) / 2f, yPosition, Paint())
                    yPosition += 240f

                    // Draw divider between image and content
                    contentCanvas.drawLine(60f, yPosition, pageInfo.pageWidth - 60f, yPosition, dividerPaint)
                    yPosition += 30f
                }
            }

            // Draw Description Text after the image
            val storyTextLines = breakTextIntoLines(storyItem.storyText, bodyPaint, pageInfo.pageWidth - 80f)
            for (line in storyTextLines) {
                contentCanvas.drawText(line, 60f, yPosition, bodyPaint)
                yPosition += bodyPaint.descent() - bodyPaint.ascent()
            }

            pdfDocument.finishPage(contentPage)
        }

        // Save the PDF document
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$pdfFileName.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        return file
    } catch (e: Exception) {
        Log.e("PdfUtils", "Error generating PDF", e)
        return null
    }
}




    private fun getFileFromUri(uri: Uri, context: Context): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, uri.lastPathSegment ?: "temp")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            Log.e("GalleryStoryDetailedFragment", "Error getting file from URI", e)
            null
        }
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        // 캐시 디렉토리에 PDF를 저장할 임시 파일 생성
        val file = File(context.cacheDir, "temp_pdf_file.pdf")

        return try {
            // URI를 통해 InputStream을 열어 파일 데이터를 읽어옴
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)  // 데이터를 캐시 디렉토리에 복사
                }
            }
            file // 파일 반환
        } catch (e: Exception) {
            e.printStackTrace()
            null // 에러 발생 시 null 반환
        }
    }
}
