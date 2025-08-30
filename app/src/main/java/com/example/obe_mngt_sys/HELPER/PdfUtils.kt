//package com.example.obe_mngt_sys.HELPER
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.pdf.PdfDocument
//import android.os.Environment
//import android.view.View
//import android.widget.HorizontalScrollView
//import android.widget.ScrollView
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import kotlin.math.min


//object PdfUtils {
//    fun generatePdfFromMultiScrollViews(
//        rootView: View,
//        scrollViews: List<ScrollView>,
//        horizontalScrollViews: List<HorizontalScrollView>,
//        context: Context,
//        fileName: String
//    ): File? {
//        try {
//            // 1. Disable scrolling temporarily
//            scrollViews.forEach { it.isScrollContainer = false }
//            horizontalScrollViews.forEach { it.isScrollContainer = false }
//
//            // 2. Force measure the full content
//            rootView.measure(
//                View.MeasureSpec.makeMeasureSpec(
//                    rootView.width,
//                    View.MeasureSpec.EXACTLY
//                ),
//                View.MeasureSpec.makeMeasureSpec(
//                    0,
//                    View.MeasureSpec.UNSPECIFIED
//                )
//            )
//            rootView.layout(0, 0, rootView.measuredWidth, rootView.measuredHeight)
//
//            // 3. Create a bitmap of the entire content
//            val bitmap = Bitmap.createBitmap(
//                rootView.width,
//                rootView.measuredHeight,
//                Bitmap.Config.ARGB_8888
//            )
//            val canvas = Canvas(bitmap)
//            rootView.draw(canvas)
//
//            // 4. Create PDF
//            val document = PdfDocument()
//            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
//            val page = document.startPage(pageInfo)
//            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
//            document.finishPage(page)
//
//            // 5. Save to Downloads
//            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            val file = File(downloadsDir, "$fileName.pdf")
//            document.writeTo(FileOutputStream(file))
//            document.close()
//
//            // 6. Re-enable scrolling
//            scrollViews.forEach { it.isScrollContainer = true }
//            horizontalScrollViews.forEach { it.isScrollContainer = true }
//
//            return file
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return null
//        }
//    }
//}
package com.example.obe_mngt_sys.HELPER

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object PdfUtils {
    fun generatePdfFromScrollView(
        scrollView: ScrollView,
        context: Context,
        fileName: String,
        horizontalScrollViews: List<HorizontalScrollView> = emptyList()
    ): File? {
        return try {
            // 1. Disable scrollbars temporarily
            scrollView.isVerticalScrollBarEnabled = false
            horizontalScrollViews.forEach { it.isHorizontalScrollBarEnabled = false }

            // 2. Force measure and layout to get full height
            scrollView.measure(
                View.MeasureSpec.makeMeasureSpec(scrollView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            scrollView.layout(0, 0, scrollView.measuredWidth, scrollView.measuredHeight)

            // 3. Access the full content inside ScrollView
            val contentView = scrollView.getChildAt(0)

            contentView.measure(
                View.MeasureSpec.makeMeasureSpec(scrollView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            contentView.layout(0, 0, contentView.measuredWidth, contentView.measuredHeight)

            val totalHeight = contentView.measuredHeight
            val totalWidth = contentView.measuredWidth

            // 4. Create a bitmap of the full content
            val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.WHITE) // White background
            }
            val canvas = Canvas(bitmap)
            contentView.draw(canvas)

            // 5. Create a PDF and draw the bitmap onto the page
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(totalWidth, totalHeight, 1).create()
            val page = document.startPage(pageInfo)

            val scale = min(
                page.canvas.width.toFloat() / bitmap.width.toFloat(),
                page.canvas.height.toFloat() / bitmap.height.toFloat()
            )

            page.canvas.scale(scale, scale)
            page.canvas.drawBitmap(bitmap, 0f, 0f, Paint())
            document.finishPage(page)

            // 6. Save to Downloads directory (app-specific)
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            val file = File(downloadsDir, "$fileName.pdf").apply {
                parentFile?.mkdirs()
            }

            FileOutputStream(file).use { fos ->
                document.writeTo(fos)
            }

            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            // 7. Restore scrollbars
            scrollView.isVerticalScrollBarEnabled = true
            horizontalScrollViews.forEach { it.isHorizontalScrollBarEnabled = true }
        }
    }
}
