package com.emrassist.audio.utils.filemanager

import android.content.Context
import android.util.Log
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


object FileManager {
    fun getBasePath(context: Context): String {
        return context.filesDir.absolutePath
    }

    public fun getFilePath(context: Context, fileName: String, extension: String = ""): String {
        return "${getBasePath(context)}/$fileName$extension"
    }

    private fun getLogFilePath(context: Context, fileName: Int): String {
        return getFilePath(context, "log_file$fileName.txt")
    }

    private fun getCurrentLogFile(context: Context): File {
        val file = File(getLogFilePath(context, SharedPrefsUtils.logFileNumber))
        if (file.exists()) {
            val size: Float = file.length().toFloat() / (1024 * 1024).toFloat()
            if (size >= 1) {
                SharedPrefsUtils.logFileNumber = SharedPrefsUtils.logFileNumber + 1
                return getCurrentLogFile(context)
            } else {
                return file
            }
        } else {
            file.createNewFile()
            updateLogFileData(context)
            return file
        }
    }

    private fun updateLogFileData(context: Context) {
        val list = SharedPrefsUtils.logFileList
        list.add(getLogFilePath(context, SharedPrefsUtils.logFileNumber))
        if (list.size > 5) {
            val file = File(list[0])
            if (file.exists())
                file.delete()
            list.removeAt(0)
        }
        SharedPrefsUtils.logFileList = list
    }


    public fun writeLogOnFile(context: Context, text: String) {
        val file: File = getCurrentLogFile(context)
        try {
            val fileWriter = FileWriter(file, true)
            fileWriter.write(
                "${
                    SimpleDateFormat(
                        "MMM DD, yyyy - hh:mm:ss",
                        Locale.getDefault()
                    ).format(Date())
                } : ${text}\n"
            );
            fileWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }

    fun readAllFiles(context: Context): String {
        val listOFfilesPath = SharedPrefsUtils.logFileList

        val stringBuilder = StringBuilder()
        var line: String?

        var bufferReader: BufferedReader? = null

        for (file in listOFfilesPath) {
            try {
                bufferReader = BufferedReader(FileReader(File(file)))
                line = bufferReader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = bufferReader.readLine()
                }
            } catch (e: FileNotFoundException) {
            } catch (e: IOException) {
            } catch (e: Exception) {
            }
        }

        return stringBuilder.toString()
    }
}