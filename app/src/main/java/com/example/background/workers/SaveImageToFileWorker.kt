package com.example.background.workers

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by YourName on 2022/06/28.
 */
class SaveImageToFileWorker(context : Context, parameters : WorkerParameters) : Worker(context, parameters) {
    private val title = "Blurred Image"
    private val dataFormatter = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())

    override fun doWork() : Result {
        makeStatusNotification("Saving image...", applicationContext)
        sleep()

        val resolver = applicationContext.contentResolver
        var fos: OutputStream? = null
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            Log.e(SaveImageToFileWorker::class.simpleName, "doWork() uri: ${resourceUri.toString()}")
            val baseImageUri = Uri.parse(resourceUri)
            var resultImageUri: String? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, title)
                    put(MediaStore.Files.FileColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.apply {
                    val bitmap = BitmapFactory.decodeStream(resolver.openInputStream(baseImageUri))
                    fos = resolver.openOutputStream(this)
                    fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(this, contentValues, null, null)
                    resultImageUri = this.toString()
                }
            } else {
                val bitmap = BitmapFactory.decodeStream(resolver.openInputStream(baseImageUri))
                resultImageUri = MediaStore.Images.Media.insertImage(resolver, bitmap, title, dataFormatter.format(Date()))
            }
            if (resultImageUri != null) {
                makeStatusNotification("Saving image success", applicationContext)
                val output = workDataOf(KEY_IMAGE_URI to resultImageUri)
                Result.success(output)
            } else {
                Result.failure()
            }
        } catch (ex : Exception) {
            ex.printStackTrace()
            Result.failure()
        }
    }
}