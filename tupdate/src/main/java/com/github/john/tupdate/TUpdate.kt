package com.github.john.tupdate

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider.getUriForFile
import kotlinx.android.synthetic.main.dialog_update_default.view.*
import java.io.File

class TUpdate(private val context: Context, private val baseData: BaseUpdateData) {
    private val dialog: AlertDialog = AlertDialog.Builder(context,R.style.t_update_dialog_style).apply {
        setCancelable(!baseData.forceUpdate)
    }.create()
    private var isDownloadComplete: Boolean = false
    private var nowProgress: Int = 0
    private var downloadId: Long = -1
    private val dm: DownloadManager? by lazy {
        try {
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        } catch (e: Exception) {
            Toast.makeText(context, R.string.t_update_module_error, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(baseData.downloadUrl)))
            null
        }
    }

    init {
        baseData.downloadPosition =
            baseData.downloadPosition.ifEmpty {Environment.DIRECTORY_DOWNLOADS + File.separator + "apk"}
    }

    fun showDefault(@DrawableRes bannerDrawable: Int?) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_update_default, null).apply {
                // 头部背景
                if (bannerDrawable != null) banner_t_update.setImageResource(bannerDrawable)

                // 标题提示文字
                title_t_update.text =
                    baseData.updateTitle.ifEmpty { "Sure update to version 1.3.3.3?" }
                // 安装包大小提示文字
                size_t_update.text = baseData.updateSize.ifEmpty { "版本大小：7.5M" }
                // 新版本更新内容
                description_t_update.text =
                    baseData.updateDescription.ifEmpty { "1. xxx\n2. xx\n3.x" }
                // 更新内容可滚动
                description_t_update.movementMethod = ScrollingMovementMethod.getInstance()
                // 安装按钮
                confirm_t_update.text = baseData.updatePositiveText.ifEmpty { "Update" }
                // 强制更新《===》隐藏"❌"按钮
                if (baseData.forceUpdate) {
                    title_t_update.visibility = GONE
                    close_t_update.visibility = GONE
                    //
                } else {
                    line_t_update.visibility = VISIBLE
                    close_t_update.visibility = VISIBLE
                    close_t_update.setOnClickListener { dialog.dismiss() }
                }

                confirm_t_update.setOnClickListener {

                    if (!isDownloadComplete) {
                        it.visibility = GONE
                        progress_t_update.visibility = VISIBLE
                        download()
                        dialog.let {d->
                            Thread {
                                while (d.isShowing && progress_t_update.progress != 1000) {
                                    Thread.sleep(200)
                                    checkDownloadStatus({ progress ->
                                        progress_t_update.post {
                                            progress_t_update.progress = progress
                                        }
                                    }, {
                                        progress_t_update.post {
                                            progress_t_update.progress = 1000
                                            isDownloadComplete = true
                                            progress_t_update.visibility = GONE
                                            confirm_t_update.let { btn ->
                                                btn.visibility = VISIBLE
                                                btn.setText(R.string.t_update_now)
                                            }
                                            installApk()
                                        }
                                    })
                                }
                            }.start()
                        }
                    } else {
                        installApk()
                    }
                }
            }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(view)
    }

    /**
     * @param contentView 传入自己的布局,并设置好响应事件
     */
    fun showYourself(contentView: View) {
        dialog.show()
        dialog.setContentView(contentView)
    }

    private fun download() {
        context.getExternalFilesDir(baseData.downloadPosition)?.deleteRecursively()
        val uri = Uri.parse(baseData.downloadUrl)
        val req = DownloadManager.Request(uri)
        //设置WIFI下进行更新
//        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
        //显示通知栏
        req.setNotificationVisibility(baseData.shouldShowNotification)
        //使用系统默认的下载路径 此处为应用内 /android/data/packages ,所以兼容7.0
        // 由于应用安装程序默认有读取权限，故可以使用此目录，但...某些第三方安装程序，可能没有权限。
        req.setDestinationInExternalFilesDir(
            context,
            baseData.downloadPosition,
            baseData.apkName
        )
        //通知栏标题
        req.setTitle(baseData.apkName)
        //通知栏描述信息
        req.setDescription(baseData.NotificationDesc)
        //设置类型为.apk
        req.setMimeType("application/vnd.android.package-archive")
//      下载前先移除前一个任务，防止重复下载
        clearCurrentTask()
        downloadId = dm!!.enqueue(req)

//        downloadReceiver = DownloadReceiver(downloadId)
//
//
//        val filter = IntentFilter()
//        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//        filter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS)
//        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
//        context.registerReceiver(downloadReceiver, filter)
    }

    private fun clearCurrentTask() {
        try {
            if (downloadId != -1L) dm!!.remove(downloadId)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }


    /**
     * @param downloadingCallBack 下载进度更新的回调
     * @param completeCallBack 下载完成的回调
     */
    private fun checkDownloadStatus(
        downloadingCallBack: (progress: Int) -> Unit,
        completeCallBack: () -> Unit
    ): Boolean {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId) //筛选下载任务，传入任务ID，可变参数
        val c: Cursor = dm!!.query(query)
        if (c.moveToFirst()) {
            when (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_PAUSED -> {
                    Toast.makeText(context, R.string.t_update_download_pause, Toast.LENGTH_SHORT)
                        .show()
                }
                DownloadManager.STATUS_RUNNING -> {
                    val hasDownloadedBytes =
                        c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val totalByte =
                        c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val newProgress: Int = (1000 * hasDownloadedBytes / totalByte).toInt()
                    if (newProgress > nowProgress) {
                        nowProgress = newProgress
                        downloadingCallBack(nowProgress)
                    }
                    c.close()
                    return true
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    completeCallBack()
                    installApk()
                    c.close()
                    return false
                }
            }
        }
        c.close()
        return true
    }

    private fun installApk() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val apkFile =
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), baseData.apkName)
        val downloadFileUri =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) getUri4UnderN(apkFile)
            else getUri4OverN(apkFile, "com.venpoo.android.musicscore.fileProvider")
        if (downloadFileUri != null) {
            intent.setDataAndType(downloadFileUri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            Toast.makeText(context, R.string.t_update_download_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUri4UnderN(file: File): Uri? {
        return Uri.fromFile(file)
    }

    private fun getUri4OverN(file: File, providerName: String): Uri? {
        return getUriForFile(
            context,
            providerName,
            file
        )
    }
}