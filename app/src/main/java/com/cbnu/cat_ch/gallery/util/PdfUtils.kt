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
                textSize = 30f
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

                // 카드뷰처럼 보이는 배경 영역을 그리기 위한 설정
                val cardPaint = Paint().apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                    setShadowLayer(8f, 0f, 0f, Color.GRAY) // 그림자 추가
                }

                val cardStrokePaint = Paint().apply {
                    color = Color.LTGRAY // 카드뷰 테두리 색
                    style = Paint.Style.STROKE
                    strokeWidth = 3f // 테두리 두께
                }

                // 전체 페이지를 카드뷰처럼 보이게 하기 위한 설정
                val cardLeft = 20f
                val cardRight = pageInfo.pageWidth - 20f
                val cardTop = 20f
                val cardBottom = pageInfo.pageHeight - 20f

                val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)

                // 카드 그리기 (배경 + 테두리)
                contentCanvas.drawRoundRect(cardRect, 15f, 15f, cardPaint)  // 둥근 모서리 배경
                contentCanvas.drawRoundRect(cardRect, 15f, 15f, cardStrokePaint)  // 둥근 모서리 테두리

                yPosition += 40f

                // Set bodyPaint alignment to center for the date
                bodyPaint.textAlign = Paint.Align.CENTER
                // Draw Date
                contentCanvas.drawText(storyItem.formattedDate ?: "날짜 없음", pageInfo.pageWidth / 2f, yPosition, bodyPaint)
                yPosition += 40f

                // Reset text alignment back to left for the rest of the content
                bodyPaint.textAlign = Paint.Align.LEFT

                // Draw Title inside the card
                contentCanvas.drawText(storyItem.storyTitle, pageInfo.pageWidth / 2f, yPosition, titlePaint)
                yPosition += 40f

                // Draw Divider inside the card
                val dividerPaint = Paint().apply {
                    color = Color.LTGRAY
                    strokeWidth = 2f
                }
                contentCanvas.drawLine(cardLeft + 20f, yPosition, cardRight - 20f, yPosition, dividerPaint)
                yPosition += 20f // Add some spacing after the divider

                // Draw Image inside the card
                storyItem.storyImagePath?.let { imagePath ->
                    val imageUri = Uri.parse(imagePath)
                    val imageFile = getFileFromUri(imageUri, context)
                    if (imageFile?.exists() == true) {
                        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false) // 고정된 이미지 크기
                        contentCanvas.drawBitmap(scaledBitmap, (pageInfo.pageWidth - scaledBitmap.width) / 2f, yPosition, Paint())
                        yPosition += 180f
                    }
                }

                // Draw Description Text after the image inside the card
                val storyTextLines = breakTextIntoLines(storyItem.storyText, bodyPaint, pageInfo.pageWidth - 80f)
                for (line in storyTextLines) {
                    contentCanvas.drawText(line, cardLeft + 20f, yPosition, bodyPaint)
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

// Helper functions



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
