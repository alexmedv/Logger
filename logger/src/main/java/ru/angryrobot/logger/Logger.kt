/*
 *  Copyright 2022 Alexander Medvedev
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ru.angryrobot.logger

/***
 * Добавить принтТехникалДата и всякие интересные штуки для логирования
 * Добавить логирование номера линии
 * Посмотреть как там в джаве оно будет работать
 */
import android.annotation.SuppressLint
import android.os.Process
import android.os.SystemClock
import android.util.Log
import androidx.annotation.IntRange
import java.io.*
import java.lang.IllegalStateException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * //TODO
 */
class Logger {

    var writeToLogcat = true
    var writeToFile = true
        set(value) {
            if (value && settings == null) throw IllegalStateException("Logger is not configured to write logs to the files")
            field = value
        }

    /**
     * You can control how many messages appear in the logs by setting the `logLevel`.
     * For example, if you want to see warnings and more serious events, set this parameter to `LogLevel.WARN`
     */
    var logLevel: LogLevel = LogLevel.VERBOSE

    var firebaseLogger: ((String) -> Unit)? = null
    var exceptionLogger: ((Throwable) -> Unit)? = null

    /**
     * If true - the logger is destroyed
     *
     * @see destroy()
     */
    var isDestroyed = false
        private set
    private val fileHandler: FileHandler?

    private val pid = Process.myPid().toString()

    val settings: LoggerSettings?

    constructor() : this(null)

    constructor(logsDir: File) : this(LoggerSettings(logsDir))

    constructor(settings: LoggerSettings?) {
        this.settings = settings
        fileHandler = if (settings != null) {
            if (!settings.logsDir.exists()) settings.logsDir.mkdirs()
            FileHandler(
                File(settings.logsDir.absolutePath, settings.logFilesName).absolutePath,
                settings.logFilesSize, settings.logFilesCount, true
            ).apply {
                formatter = object : java.util.logging.Formatter() {
                    override fun format(record: LogRecord) = record.message
                }
            }
        } else {
            writeToFile = false
            null
        }
    }

    fun createLogBundle(zipFile: File) {
        if (settings == null) throw IllegalStateException("Logger is not configured to write logs to the files")
        val files = settings.logsDir.listFiles() ?: emptyArray()
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

    private fun writeToFile(priority: LogLevel, tag: String, message: String) {
        if (settings != null) {
            val string = "${settings.timeFormat.format(Date())} ${priority.string}/$tag ($pid): $message\n"
            fileHandler?.publish(LogRecord(Level.INFO, string))
        }
    }

    fun measure(msg: String = "Execution time", logEvent: Boolean = false, tag: String? = null, block: () -> Unit) {
        val startTime = SystemClock.elapsedRealtime()
        block()
        val result = SystemClock.elapsedRealtime() - startTime
        w("$msg: $result msec", logEvent, tag)
    }


    fun v(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.VERBOSE, message, null, logEvent, tag)
    }

    fun d(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.DEBUG, message, null, logEvent, tag)
    }

    fun i(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.INFO, message, null, logEvent, tag)
    }

    fun w(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.WARN, message, null, logEvent, tag)
    }

    fun e(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.ERROR, message, null, logEvent, tag)
    }

    fun e(message: Any, exception: Throwable? = null, logEvent: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.ERROR, message, exception, logEvent, tag)
    }

    fun a(message: Any, logEvent: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.ASSERT, message, null, logEvent, tag)
    }

    /**
     * Close all the files and stop writing logs to `LogCat`. Call this function if you don't need this logger anymore
     */
    fun destroy() {
        isDestroyed = true
        fileHandler?.close()
    }

    private fun writeLog(logLevel: LogLevel, message: Any, exception: Throwable?, logEvent: Boolean, customTag: String? = null) {
        if (isDestroyed || logLevel.code < this.logLevel.code) return
        val tag = if (customTag != null) {
            customTag
        } else {
            val element = Thread.currentThread().stackTrace[5] // Magic number ? Why 5 ?
            "[${element.className.split(".").last()}.${element.methodName}]"
        }

        if (writeToLogcat) {
            Log.println(logLevel.code, tag, message.toString())
            exception?.printStackTrace()
        }

        if (logEvent) {
            firebaseLogger?.invoke("${logLevel.string}/$tag $message")
            if (exception != null) {
                exceptionLogger?.invoke(exception)
            }
        }

        if (writeToFile) {
            writeToFile(logLevel, tag, message.toString())
            if (exception != null) {
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

class LoggerSettings(

    /**
     * The directory where the logs will be stored. If the field is `null`, no logs are written to the file
     */
    val logsDir: File,

    /**
     * The maximum number of bytes to write to any one file. When the limit is reached - a new file will be created
     * If the field is zero, the file size is unlimited
     */
    @IntRange(from = 0) val logFilesSize: Int = 1024 * 1024,

    /**
     * Maximum number of log files in the directory
     */
    @IntRange(from = 1) val logFilesCount: Int = 5,

    /**
     * Sets the name for the log files. The logger will add a number to the end of the name,
     * so the files in the `logsDir` directory will have names likes "logfile.txt.0", "logfile.txt.1" etc...
     */
    val logFilesName: String = "logfile.txt",

    /**
     * Timestamp format
     */
    @SuppressLint("ConstantLocale")
    val timeFormat: DateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

)

/**
 * LogLevel is a piece of information telling how important a given log message is.
 */
enum class LogLevel(val code: Int, val string: String) {

    /**
     * The most fine-grained information only used in rare cases where you need the full visibility of what is happening in your application
     */
    VERBOSE(Log.VERBOSE, "V"),

    /**
     * it should be used for information that may be needed for diagnosing issues and troubleshooting or when running application
     * in the test environment for the purpose of making sure everything is running correctly
     */
    DEBUG(Log.DEBUG, "D"),

    /**
     * The standard log level indicating that something happened, the application entered a certain state, etc. The information logged  using the
     * `INFO` log level should be purely informative and not looking into them on a regular basis shouldn’t result in missing any important information.
     */
    INFO(Log.INFO, "I"),

    /**
     * The log level that indicates that something unexpected happened in the application, a problem, or a situation that might disturb one of
     * the processes. But that does’t mean that the application failed. The WARN level should be used in situations that are unexpected,
     * but the code can continue the work.
     */
    WARN(Log.WARN, "W"),

    /**
     * The log level that should be used when the application hits an issue preventing one or more functionalities from properly functioning
     */
    ERROR(Log.ERROR, "E"),

    /**
     * This log level should be used for issues that the developer expects should never happen
     */
    ASSERT(Log.ASSERT, "A"),
}