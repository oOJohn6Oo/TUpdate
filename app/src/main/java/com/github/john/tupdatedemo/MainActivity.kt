package com.github.john.tupdatedemo

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.github.john.tupdate.BaseUpdateData
import com.github.john.tupdate.TUpdate
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testBtn.setOnClickListener {

            TUpdate(
                this, BaseUpdateData(
                    "发现新版本：V1.3.7S Pro Max Plus Turbo Porsche SE",
                    "版本大小：1.39M",
                    "1. 修复了xxx\n2. 优化了了xxx\n3. 改进了xxx\n4. 改善了xxx",
                    "Yes",
                    "Demo.apk",
                    "正在下载..,",
                    false,
                    "http://resource.smartisan.com/resource/s/smartisan_note_v3.6.2.1_official.apk",
                    Environment.DIRECTORY_DOWNLOADS)
            ).showDefault(R.drawable.ic_update)
        }
    }
}
