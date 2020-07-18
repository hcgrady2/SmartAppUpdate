package com.hc.smartappupdate

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import com.itheima.updatelib.PatchUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File

class MainActivity : AppCompatActivity() {

    //初始化加载进度
    private val mDialog:ProgressDialog by lazy {
        ProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.setText("这是 1.0 版本")

        //参考 https://github.com/jiyouliang2/SmartUpdateDemo
        
        //kotlin 不用 findViewById，通过 Anco 回调
        btn_update.onClick {
            update()
        }
    }


    //差分包通过 bsdiff 来完成
    private fun update() {

        var pm:PackageManager = packageManager
        //通过命令行或者当前设备运行项目的包名 : adb shell dumpsys window w | findstr  \/ findstr name =



        var appInfo =  pm.getApplicationInfo("com.ss.android.article.news",0);

        //获取旧版本
        val oldPath:String = appInfo.sourceDir

        //指定 path 文件保存路径
        var patchFile = File(Environment.getExternalStorageDirectory(),"toutiao.patch")

        //设置新版本 apk 保留路径
        var newApkFile = File(Environment.getExternalStorageDirectory(),"toutiao_new.apk")

        //通过 so SmartUpdateLib
        //合并成新版本，耗时操作，需要自线程，anko

        mDialog.show()
        //自线程
        doAsync {

            var result = PatchUtil.patch(oldPath,newApkFile.absolutePath,patchFile.absolutePath)

            if (result == 0){
                //合并成功
                runOnUiThread{
                    mDialog.dismiss()

                    val intent = Intent()
                    //设置 action、数据,都需要传入什么数据，可以通过查询系统 packageInstall 的 manifest 来获取
                    intent.setAction("android.intent.action.View")
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.setDataAndType(Uri.parse("file://" + newApkFile.absoluteFile),"application/vnd.android.package-archive")
                    //调用系统包安装器
                    startActivity(intent)

                }

            }else{
                //失败

            }

        }

    }
}