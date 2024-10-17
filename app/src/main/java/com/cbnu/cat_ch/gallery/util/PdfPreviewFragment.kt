package com.cbnu.cat_ch.gallery.util

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.cbnu.cat_ch.databinding.FragmentPdfPreviewBinding
import com.cbnu.cat_ch.gallery.adapter.PdfThumbnailAdapter
import java.io.File

class PdfPreviewFragment : Fragment() {

    private var _binding: FragmentPdfPreviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var pdfFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pdfFilePath = arguments?.getString("pdf_file")
        pdfFilePath?.let {
            pdfFile = File(it) // File 객체로 변환
        } ?: run {
            Toast.makeText(requireContext(), "PDF 파일을 로드할 수 없습니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayPdf()
    }

    @SuppressLint("SetTextI18n")
    private fun displayPdf() {
        binding.pdfView.fromFile(pdfFile)
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(true)
            .enableDoubletap(true)
            .onPageChange { page, pageCount ->
                // 현재 페이지 및 총 페이지 정보를 업데이트
                binding.tvPageInfo.text = "페이지 ${page + 1} / $pageCount"
            }
            .pageFling(true) // 한 장씩 넘기기 설정
            .onLoad { Toast.makeText(requireContext(), "PDF 로드 완료", Toast.LENGTH_SHORT).show() }
            .onError { Toast.makeText(requireContext(), "PDF 로드 실패", Toast.LENGTH_SHORT).show() }
            .load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(pdfFile: File): PdfPreviewFragment {
            val fragment = PdfPreviewFragment()
            val args = Bundle().apply {
                putSerializable("pdf_file", pdfFile)
            }
            fragment.arguments = args
            return fragment
        }
    }
}