package com.example.shabbatalarm.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.shabbatalarm.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Copies the installed APK of this app into cache/shared/ and launches the
 * system share sheet so the user can send it via WhatsApp, Drive, etc.
 */
object ApkSharer {

    private const val TAG = "ApkSharer"
    private const val SHARED_APK_NAME = "ShabbatAlarm.apk"

    fun share(context: Context, scope: CoroutineScope) {
        scope.launch {
            val apk = withContext(Dispatchers.IO) { copyApkToCache(context) }
            if (apk == null) {
                Toast.makeText(
                    context,
                    context.getString(R.string.share_app_failed),
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }
            launchShareSheet(context, apk)
        }
    }

    private fun copyApkToCache(context: Context): File? {
        return try {
            val sourcePath = context.applicationInfo.sourceDir
            val sourceFile = File(sourcePath)
            val sharedDir = File(context.cacheDir, "shared").apply { mkdirs() }
            val destFile = File(sharedDir, SHARED_APK_NAME)
            sourceFile.copyTo(destFile, overwrite = true)
            destFile
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to copy APK", t)
            null
        }
    }

    private fun launchShareSheet(context: Context, apkFile: File) {
        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
        } catch (t: Throwable) {
            Log.e(TAG, "FileProvider.getUriForFile failed", t)
            Toast.makeText(
                context,
                context.getString(R.string.share_app_failed),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                context.getString(R.string.app_name)
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(
            sendIntent,
            context.getString(R.string.share_chooser_title)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(chooser)
        } catch (t: Throwable) {
            Log.e(TAG, "Unable to start share chooser", t)
            Toast.makeText(
                context,
                context.getString(R.string.share_app_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
