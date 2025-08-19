package top.mizhoubaobei.womenhealth

import android.app.Application
import android.util.Log
import top.mizhoubaobei.womenhealth.data.AppDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 应用程序类
 */
class WomenHealthApplication : Application() {
    
    // 使用lazy初始化数据库，确保只在需要时创建
    val database by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        // 设置未捕获异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logCrash(throwable)
            // 默认处理(退出应用)
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        }
        
        // 预初始化数据库
        database
    }
    
    private fun logCrash(throwable: Throwable) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val logFile = File(getExternalFilesDir(null), "crash_$timestamp.log")
            
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            val stackTrace = sw.toString()
            
            val logMessage = """
                ======== CRASH LOG ========
                Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
                Thread: ${Thread.currentThread().name}
                Exception: ${throwable.javaClass.name}
                Message: ${throwable.message}
                Stack Trace:
                $stackTrace
                ===========================
            """.trimIndent()
            
            // 写入文件
            FileOutputStream(logFile).use {
                it.write(logMessage.toByteArray())
            }
            
            // 同时输出到Logcat
            Log.e("CrashHandler", "======== CRASH DETECTED ========")
            Log.e("CrashHandler", "Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            Log.e("CrashHandler", "Thread: ${Thread.currentThread().name}")
            Log.e("CrashHandler", "Exception: ${throwable.javaClass.name}")
            Log.e("CrashHandler", "Message: ${throwable.message}")
            Log.e("CrashHandler", "Stack Trace:\n$stackTrace")
            Log.e("CrashHandler", "Crash logged to ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("CrashHandler", "Failed to write crash log", e)
        }
    }
}