package com.github.john.tupdate

import android.app.DownloadManager

class BaseUpdateData(
    val updateTitle:String,
    val updateSize:String,
    val updateDescription:String,
    val updatePositiveText:String,
    val apkName:String,
    val NotificationDesc:String,
    val forceUpdate:Boolean,
    val downloadUrl:String,
    var downloadPosition:String,
    var shouldShowNotification:Int=DownloadManager.Request.VISIBILITY_VISIBLE)