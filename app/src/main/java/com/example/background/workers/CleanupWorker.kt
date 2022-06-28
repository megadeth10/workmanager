package com.example.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.OUTPUT_PATH
import java.io.File

/**
 * Created by YourName on 2022/06/28.
 */
class CleanupWorker(context : Context, params : WorkerParameters) : Worker(context, params) {
    override fun doWork() : Result {
        makeStatusNotification("Cleanup temporary files", applicationContext)
        sleep()
        return try {
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val fileList = outputDirectory.listFiles()
                for (file in fileList) {
                    if (file.isFile) {
                        file.delete()
                    }
                }
            }
            Result.success()
        } catch (ex : Exception) {
            ex.printStackTrace()
            Result.failure()
        }
    }
}