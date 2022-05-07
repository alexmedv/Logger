package ru.angryrobot.logger

/***
 * Добавить принтТехникалДата и всякие интересные штуки для логирования
 * Добавить логирование номера линии
 * Лог левелы добавить
 *
 */
import android.annotation.SuppressLint
import android.os.Process
import android.os.SystemClock
import android.util.Log
import androidx.annotation.IntRange
import androidx.annotation.Size
import androidx.annotation.StringDef
import org.jetbrains.annotations.NotNull
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Logger(logsDir: File?) {

    var writeToLogcat = true
    var writeToFile = true

    var firebaseLogger: ((String) -> Unit)? = null
    var exceptionLogger: ((Throwable) -> Unit)? = null

    private val fileHandler: FileHandler?
    @SuppressLint("ConstantLocale")
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)
    private val pid  = Process.myPid().toString()


    val settings: LoggerSettings

    init {
        settings = LoggerSettings(logsDir)
        fileHandler = if (logsDir != null) {

            if (!logsDir.exists()) logsDir.mkdirs()

            FileHandler("${logsDir.absolutePath}/logfile.txt", settings.fileSettings.logFilesSize, settings.fileSettings.logFilesCount, true).apply {
                formatter = object : java.util.logging.Formatter() {
                    override fun format(record: LogRecord) = record.message
                }
            }

        } else {
            null
        }
    }


    fun createLogBundle(zipFile: File) {
        //TODO надо проверить зачем нам это вообще?
        val files = settings.fileSettings.logsDir?.listFiles() ?: throw IOException("Can't create zip file. No logs yet")
        val zipOutput = ZipOutputStream(FileOutputStream(zipFile))
        for (file in files) {
            if (file.name.endsWith(".lck")) continue
            zipOutput.putNextEntry(ZipEntry(file.name))
            val inputStream = FileInputStream(file)
            inputStream.copyTo(zipOutput)
            zipOutput.closeEntry()
            inputStream.close()
        }
        zipOutput.close()
    }

    private fun priorityString(priority:Int):String = when (priority) {
        Log.VERBOSE -> "V"
        Log.DEBUG -> "D"
        Log.INFO -> "I"
        Log.WARN -> "W"
        Log.ERROR -> "E"
        Log.ASSERT -> "A"
        else -> ""
    }

    private fun writeToFile(priority: Int, tag: String, message: String) {
        if (fileHandler != null) {
            val string = "${dateFormat.format(Date())} ${priorityString(priority)}/$tag ($pid): $message\n"
            fileHandler.publish(LogRecord(Level.INFO, string))
        }
    }

    fun measure(msg: String = "Execution time", logEvent: Boolean = false, tag: String? = null, block: () -> Unit) {
        val startTime = SystemClock.elapsedRealtime()
        block()
        val result = SystemClock.elapsedRealtime() - startTime
        w("$msg: $result msec", logEvent, tag)
    }


    fun v(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(Log.VERBOSE, message, null, logEvent, tag)
    }

    fun d(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(Log.DEBUG, message, null, logEvent, tag)
    }

    fun i(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(Log.INFO, message, null, logEvent, tag)
    }

    fun w(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(Log.WARN, message, null, logEvent, tag)
    }

    fun e(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(Log.ERROR, message, null, logEvent, tag)
    }

    fun e(message: Any, exception: Throwable? = null, logEvent: Boolean = false, tag: String? = null) {
        writeLog(Log.ERROR, message, exception, logEvent, tag)
    }

    fun a(message: Any, logEvent: Boolean = false, tag:String? = null) {
        writeLog(Log.ASSERT, message, null, logEvent, tag)
    }


    fun destroy() {
        //TODO
    }

    private fun writeLog(logLevel: Int, message: Any, exception: Throwable?, logEvent: Boolean, customTag: String? = null) {

        val tag = if (customTag != null) {
            customTag
        } else {
            val element = Thread.currentThread().stackTrace[5] // Magic number ? Why 5 ?
            "[${element.className.split(".").last()}.${element.methodName}]"
        }

        if (writeToLogcat) Log.println(logLevel, tag, message.toString())

        if (logEvent) {
            firebaseLogger?.invoke("${priorityString(logLevel)}/$tag $message")
            if (exception != null) {
                exceptionLogger?.invoke(exception)
            }
        }

        if (writeToFile) {
            writeToFile(logLevel, tag, message.toString())
            if (exception != null) {
                exception.printStackTrace()
                val exceptionString = getStackTrace(exception)
                writeToFile(logLevel, tag, exceptionString)
            }
        }
    }


    /**
     * Get the stack trace from a Throwable as a String.
     * @param throwable  the `Throwable` to be examined
     * @return the stack trace as generated by the exception's `printStackTrace(PrintWriter)` method
     */
    private fun getStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw, true)
        throwable.printStackTrace(pw)
        return sw.buffer.toString()
    }
}

class LoggerSettings(logsDir: File?) {

    var writeToLogcat = true
    var writeToFile = logsDir != null
    val fileSettings: FileSettings = FileSettings(logsDir)

    class FileSettings(
        /**
         * The directory where the logs will be stored. If the field is `null`, no logs are written to the file
         */
        val logsDir: File?,

        /**
         * The maximum number of bytes to write to any one file. When the limit is reached - a new file will be created
         * If the field is zero, the file size is unlimited
         */
        @IntRange(from = 0) val logFilesSize: Int = 1024 * 1024,

        /**
         * Maximum number of log files in the directory
         */
        @IntRange(from = 1) val logFilesCount: Int = 5,

        /*
         * Each log file
         */
        val logFilesName:String = "logfile.txt"
    )

}
