package com.nikdi.recipefyai.utils

import android.graphics.Bitmap
import androidx.core.graphics.scale

class ImageHandler {
    fun resizeImage(inputBitmap: Bitmap, outputWidth: Int, outputHeight: Int) : Bitmap {
        return inputBitmap.scale(outputWidth, outputHeight, false)
    }
}