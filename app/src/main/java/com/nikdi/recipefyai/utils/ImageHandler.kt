package com.nikdi.recipefyai.utils

import android.graphics.Bitmap

class ImageHandler {
    fun resizeImage(inputBitmap: Bitmap, outputWidth: Int, outputHeight: Int) : Bitmap {
        return Bitmap.createScaledBitmap(inputBitmap, outputWidth, outputHeight, false)
    }
}