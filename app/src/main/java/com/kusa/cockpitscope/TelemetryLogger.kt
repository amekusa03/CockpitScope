package com.kusa.cockpitscope

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelemetryLogger(private val context: Context) {

    private var printWriter: PrintWriter? = null
    private var currentFile: File? = null
    private var isLogging = false

    fun startLogging(): String? {
        if (isLogging) return currentFile?.absolutePath

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "telemetry_$timeStamp.csv"
        
        // アプリ専用の外部ストレージディレクトリを使用
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(dir, fileName)
        
        try {
            val fos = FileOutputStream(file)
            printWriter = PrintWriter(fos)
            // ヘッダーの書き込み
            printWriter?.println("timestamp,id,value")
            
            currentFile = file
            isLogging = true
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun logData(id: String, value: Float) {
        if (!isLogging) return
        
        val timestamp = System.currentTimeMillis()
        printWriter?.println("$timestamp,$id,$value")
    }

    fun stopLogging() {
        if (!isLogging) return
        
        printWriter?.flush()
        printWriter?.close()
        printWriter = null
        isLogging = false
    }

    fun isLogging(): Boolean = isLogging

    fun getLogs(): List<File> {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return dir?.listFiles { file -> file.extension == "csv" }?.toList() ?: emptyList()
    }
}
