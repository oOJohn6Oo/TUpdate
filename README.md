## T系列——版本更新TUpdate
A project for android update. 一个安卓端版本更新的项目。Android版本更新小钢炮。弹窗样式，DownloadManager下载，进度条，更新内容可滑动。

## 用例

 * 默认
  ```
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
  
  ```
