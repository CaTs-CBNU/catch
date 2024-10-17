package com.cbnu.cat_ch.gallery.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.cbnu.cat_ch.R
import java.io.File
import java.io.IOException

class PdfThumbnailAdapter(
    private val context: Context,
    private val pdfFile: File,
    private val pageCount: Int,
    private val onThumbnailClick: (Int) -> Unit
) : RecyclerView.Adapter<PdfThumbnailAdapter.ThumbnailViewHolder>() {

    private val thumbnails: MutableList<Bitmap?> = MutableList(pageCount) { null }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_thumbnail, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val thumbnail = thumbnails[position]
        if (thumbnail != null) {
            holder.thumbnailImageView.setImageBitmap(thumbnail)
        } else {
            // PDF 페이지의 썸네일 이미지를 로드
            holder.thumbnailImageView.setImageResource(R.drawable.background) // 로드 전 기본 이미지 설정
            holder.thumbnailImageView.post {
                thumbnails[position] = createThumbnail(position)
                holder.thumbnailImageView.setImageBitmap(thumbnails[position])
            }
        }

        holder.itemView.setOnClickListener {
            onThumbnailClick(position) // 클릭 시 해당 페이지로 이동
        }
    }

    override fun getItemCount(): Int = pageCount

    private fun createThumbnail(page: Int): Bitmap? {
        var pdfRenderer: PdfRenderer? = null
        var fileDescriptor: ParcelFileDescriptor? = null
        try {
            // PDF 파일 디스크립터 열기
            fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)

            if (page < 0 || page >= pdfRenderer.pageCount) {
                Log.e("PdfThumbnailAdapter", "Page index out of range: $page")
                return null
            }

            val pdfPage = pdfRenderer.openPage(page)
            val width = 100 // 썸네일 너비
            val height = (width * pdfPage.height / pdfPage.width) // 비율에 맞춰 높이 조정

            // 썸네일 생성
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfPage.close()

            return bitmap
        } catch (e: IOException) {
            Log.e("PdfThumbnailAdapter", "Error creating thumbnail for page $page", e)
            return null
        } finally {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (e: IOException) {
                Log.e("PdfThumbnailAdapter", "Error closing PdfRenderer or file descriptor", e)
            }
        }
    }

    inner class ThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImage)
    }
}
