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

import android.os.Process
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord

/**
 * Simple and easy to use (according to the author) logger for android.
 *
 * Key features:
 * - Configurable log level
 * - Integration with crash reporter systems
 * - Writing log to `LogCat` and files (with rotation)
 *
 *  @see <a href="https://github.com/alexmedv/Logger">More information and examples</a>
 *
 *  @author
 *  Alexander Medvedev
 */
class Logger {

    /**
     * You can control how many messages appear in the logs by setting the `logLevel`.
     * For example, if you want to see warnings and more serious events, set this parameter to `LogLevel.WARN`
     */
    var logLevel: LogLevel = LogLevel.VERBOSE

    private val fileHandler: FileHandler?

    /**
     * The identifier of this process (used in the log files)
     */
    private val pid = Process.myPid().toString()

    /**
     * Settings for logging to files
     */
    val settings: LoggerSettings

    /**
     * Root logger
     */
    private val rootLogger: Logger?

    /**
     * tag to be used for this logger
     */
    var tag: String = "[RootLogger]"

    /**
     * Create a logger with specific settings
     *
     * @param settings Configuration used for logging to files
     */
    constructor(settings: LoggerSettings) {
        this.settings = settings
        if (!settings.logsDir.exists()) settings.logsDir.mkdirs()
        fileHandler = FileHandler(
            File(settings.logsDir.absolutePath, settings.logFilesName).absolutePath,
            settings.logFilesSize, settings.logFilesCount, true
        ).apply {
            formatter = object : Formatter() {
                override fun format(record: LogRecord) = record.message
            }
        }
        this.rootLogger = null
    }

    /**
     * Create a new logger with custom tag
     * @param logger root logger
     * @param tag tag
     */
    private constructor(logger: Logger, tag: String) {
        fileHandler = null
        settings = logger.settings
        this.rootLogger = logger.rootLogger ?: logger
        this.tag = tag
    }

    /**
     * Create a new logger with custom tag
     * @param tag tag
     */
    fun newLogger(tag: String)  = Logger(this, tag)

    /**
     * Write a verbose message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via [crashlyticsLogger]
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun v(message: Any, useCrashlyticsLog: Boolean = true, tag: String = this.tag) {
        writeLog(LogLevel.VERBOSE, message,  useCrashlyticsLog, tag)
    }

    /**
     * Write a debug message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via [crashlyticsLogger]
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun d(message: Any, useCrashlyticsLog: Boolean = true, tag: String = this.tag) {
        writeLog(LogLevel.DEBUG, message,  useCrashlyticsLog, tag)
    }

    /**
     * Write an information message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via [crashlyticsLogger]
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun i(message: Any, useCrashlyticsLog: Boolean = true, tag: String = this.tag) {
        writeLog(LogLevel.INFO, message, useCrashlyticsLog, tag)
    }

    /**
     * Write a warning message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via [crashlyticsLogger]
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun w(message: Any, useCrashlyticsLog: Boolean = true, tag: String = this.tag) {
        writeLog(LogLevel.WARN, message, useCrashlyticsLog, tag)
    }

    /**
     * Write an error message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via [crashlyticsLogger]
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun e(message: Any, useCrashlyticsLog: Boolean = true, tag: String = this.tag) {
        writeLog(LogLevel.ERROR, message, useCrashlyticsLog, tag)
    }

    /**
     * Write an error message and exception
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via [crashlyticsLogger]
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     * @param exception An exception to be logged
     * @param useCrashlyticsExceptionLogger If the parameter is `true`, the exception will also be logged via `crashlyticsExceptionLogger`
     */
    fun e(message: Any, exception: Throwable? = null, tag: String = this.tag, useCrashlyticsExceptionLogger:Boolean = false, useCrashlyticsLog: Boolean = true) {
        writeLog(LogLevel.ERROR, message, useCrashlyticsLog, tag, exception, useCrashlyticsExceptionLogger)
    }
    /**
     * Write an assert message
     *
     * @param message A message to be logged
     * @param useCrashlyticsLog If the parameter is `true`, the message will also be logged via [crashlyticsLogger]
     * @param tag Used to identify the source of a log message. If the parameter is `null`, the name of the calling function will be used
     */
    fun a(message: Any, useCrashlyticsLog: Boolean = true, tag: String = this.tag) {
        writeLog(LogLevel.ASSERT, message, useCrashlyticsLog, tag, null)
    }

    private fun writeLog(logLevel: LogLevel, message: Any, useCrashlyticsLog: Boolean, tag: String,
                         exception: Throwable? = null, useCrashlyticsExceptionLogger : Boolean = false) {

        if (logLevel.code < this.logLevel.code) return

        if (rootLogger != null) {
            rootLogger.writeLog(logLevel, message, useCrashlyticsLog, tag, exception, useCrashlyticsExceptionLogger)
            return
        }

        if (useCrashlyticsExceptionLogger && exception != null) {
            settings.crashlyticsExceptionLogger?.invoke(exception)
        }

        if (settings.writeToLogcat) {
            Log.println(logLevel.code, tag, message.toString())
            exception?.printStackTrace()
        }

        if (useCrashlyticsLog) {
            settings.crashlyticsLogger?.invoke("${logLevel.string}/$tag $message")
        }

        if (settings.writeToFile) {
            writeToFile(logLevel, tag, message.toString())
            if (exception != null) {
                val exceptionString = getStackTrace(exception)
                writeToFile(logLevel, tag, exceptionString)
            }
        }
    }

    private fun writeToFile(priority: LogLevel, tag: String, message: String) {
        val string = "${settings.timeFormat.format(Date())} ${priority.string}/$tag ($pid): $message\n"
        fileHandler?.publish(LogRecord(Level.INFO, string))
    }


    /**
     * Get the stack trace from a Throwable as a String.
     * @param throwable  the [Throwable] to be examined
     * @return the stack trace as generated by the exception's `printStackTrace(PrintWriter)` method
     */
    private fun getStackTrace(throwable: Throwable) = StringWriter().use { sw ->
        PrintWriter(sw, true).use { pw ->
            throwable.printStackTrace(pw)
            sw.buffer.toString()
        }
    }
}