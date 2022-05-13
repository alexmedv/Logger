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
 * Подумать насчет многопоточности и работы из разных процессов одного приложения
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
 * Simple and easy to use (according to the author) logger for android.
 *
 * Key features:
 * - Configurable log level
 * - Integration with crash reporter systems
 * - Writing log to `LogCat` and files (with rotation)
 * - Automatically generated tag (for `LogCat`)
 *
 *  @see <a href="https://github.com/alexmedv/Logger">More information and examples</a>
 *
 *  @author
 *  Alexander Medvedev
 */
class Logger {

    /**
     * If enabled, logging to `LogCat` is allowed.
     * Typical use case - disable `LogCat` logging in a release build
     */
    var writeToLogcat = true

    /**
     * If enabled, logging to files is allowed (`settings` must be non null)
     *
     * @see settings
     */
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

    /**
     * Used for integration with crash reporter systems like Firebase Crashlytics.
     * Typical use case, call `FirebaseCrashlytics.getInstance().log(message)` in this function.
     * It's needed to give more context for the events leading up to a crash.
     */
    var crashlyticsLogger: ((String) -> Unit)? = null

    /**
     * Used for integration with crash reporter systems like Firebase Crashlytics.
     * Typical use case, call `FirebaseCrashlytics.getInstance().recordException(throwable)` in this function.
     * It's needed to log non-fatal exceptions in the app.
     */
    var crashlyticsExceptionLogger: ((Throwable) -> Unit)? = null

    /**
     * If true - the logger is destroyed
     *
     * @see destroy()
     */
    var isDestroyed = false
        private set

    private val fileHandler: FileHandler?

    /**
     * The identifier of this process (used in the log files)
     */
    private val pid = Process.myPid().toString()

    /**
     * Settings for logging to files
     */
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

    /**
     * Write a verbose message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via `crashlyticsLogger`
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun v(message: Any, useCrashlyticsLog: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.VERBOSE, message,  useCrashlyticsLog, tag)
    }

    /**
     * Write a debug message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via `crashlyticsLogger`
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun d(message: Any, useCrashlyticsLog: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.DEBUG, message,  useCrashlyticsLog, tag)
    }

    /**
     * Write an information message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via `crashlyticsLogger`
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun i(message: Any, useCrashlyticsLog: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.INFO, message, useCrashlyticsLog, tag)
    }

    /**
     * Write a warning message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via `crashlyticsLogger`
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun w(message: Any, useCrashlyticsLog: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.WARN, message, useCrashlyticsLog, tag)
    }

    /**
     * Write an error message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via `crashlyticsLogger`
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun e(message: Any, useCrashlyticsLog: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.ERROR, message, useCrashlyticsLog, tag)
    }

    /**
     * Write an error message and exception
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via `crashlyticsLogger`
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     * @param exception An exception to be logged
     * @param useCrashlyticsExceptionLogger If the parameter is `true`, the exception will also be logged via `crashlyticsExceptionLogger`
     */
    fun e(message: Any, exception: Throwable? = null, tag: String? = null, useCrashlyticsExceptionLogger:Boolean = false, useCrashlyticsLog: Boolean = false) {
        writeLog(LogLevel.ERROR, message, useCrashlyticsLog, tag, exception)
    }
    /**
     * Write an assert message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via `crashlyticsLogger`
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun a(message: Any, useCrashlyticsLog: Boolean = false, tag: String? = null) {
        writeLog(LogLevel.ASSERT, message, useCrashlyticsLog, tag, null)
    }

    /**
     * Close all the files and stop writing logs to `LogCat`. Call this function if you don't need this logger anymore
     */
    fun destroy() {
        isDestroyed = true
        fileHandler?.close()
    }

    private fun writeLog(logLevel: LogLevel, message: Any, useCrashlyticsLog: Boolean, customTag: String? = null,
                         exception: Throwable? = null, useCrashlyticsExceptionLogger : Boolean = false) {
        if (isDestroyed || logLevel.code < this.logLevel.code) return
        val tag = if (customTag != null) {
            customTag
        } else {
            val stackTrace = Thread.currentThread().stackTrace
            val element = stackTrace[5] // It looks like a magic number, but if you look into it, it's not
//            The function from which the logger was called is always the fifth. Below is an example stacktrace:
//            0  "dalvik.system.VMStack.getThreadStackTrace(Native Method)"
//            1  "java.lang.Thread.getStackTrace(Thread.java:1730)"
//            2  "ru.angryrobot.logger.Logger.writeLog(Logger.kt:166)"
//            3  "ru.angryrobot.logger.Logger.e(Logger.kt:146)"
//            4  "ru.angryrobot.logger.Logger.e$default(Logger.kt:145)"
//     ---->  5  "ru.angryrobot.logger.demo.MainActivity.someFunction(MainActivity.kt:72)"
//            6  "ru.angryrobot.logger.demo.MainActivity.onCreate$lambda-5(MainActivity.kt:57)"
//            N  ...
            "[${element.className.split(".").last()}.${element.methodName}]"
        }

        if (writeToLogcat) {
            Log.println(logLevel.code, tag, message.toString())
            exception?.printStackTrace() //TODO - печатает с тем логлевелом всегда
        }

        if (useCrashlyticsLog) {
            crashlyticsLogger?.invoke("${logLevel.string}/$tag $message")
            if (exception != null) {
                crashlyticsExceptionLogger?.invoke(exception)
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

    private fun writeToFile(priority: LogLevel, tag: String, message: String) {
        if (settings != null) {
            val string = "${settings.timeFormat.format(Date())} ${priority.string}/$tag ($pid): $message\n"
            fileHandler?.publish(LogRecord(Level.INFO, string))
        }
    }


    /**
     * Get the stack trace from a Throwable as a String.
     * @param throwable  the `Throwable` to be examined
     * @return the stack trace as generated by the exception's `printStackTrace(PrintWriter)` method
     */
    private fun getStackTrace(throwable: Throwable) = StringWriter().use { sw ->
        PrintWriter(sw, true).use { pw ->
            throwable.printStackTrace(pw)
            sw.buffer.toString()
        }
    }
}