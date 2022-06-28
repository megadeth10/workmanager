package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.R

/**
 * Created by YourName on 2022/06/28.
 */
class BluerWorker(ctx : Context, param : WorkerParameters) : Worker(ctx, param) {
    private val appContext = applicationContext
    private val resourceUri = inputData.getString(KEY_IMAGE_URI)

    override fun doWork() : Result {
        if (resourceUri.isNullOrEmpty()) {
            throw IllegalArgumentException("no have image uri")
        }
        makeStatusNotification("시작", appContext)
        val resolver = appContext.contentResolver
        val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))

        val blurPicture = blurBitmap(picture, appContext)
        return try {
            val uri = writeBitmapToFile(appContext, blurPicture)
            makeStatusNotification("임시파일 생성 uri: $uri", appContext)
            val resultData = workDataOf(KEY_IMAGE_URI to uri.toString())
            Result.success(resultData)
        } catch (throwable : Throwable) {
            throwable.printStackTrace()
            Result.failure()
        }
    }
}