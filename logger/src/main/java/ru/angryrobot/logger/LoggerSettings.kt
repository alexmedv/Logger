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

import android.annotation.SuppressLint
import androidx.annotation.IntRange
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Configuration used for logging to files
 */
class LoggerSettings(

    /**
     * The directory where the logs will be stored. If the field is `null`, no logs are written to the file
     */
    val logsDir: File,

    /**
     * The maximum number of bytes to write to any one file. When the limit is reached - a new file will be created
     * If the field is zero, the file size is unlimited
     */
    @field:IntRange(from = 0) val logFilesSize: Int = 1024 * 1024,

    /**
     * Maximum number of log files in the directory
     */
    @field:IntRange(from = 1) val logFilesCount: Int = 5,

    /**
     * Sets the name for the log files. The logger will add a number to the end of the name,
     * so the files in the `logsDir` directory will have names likes "logfile.txt.0", "logfile.txt.1" etc...
     */
    val logFilesName: String = "logfile.txt",

    /**
     * Timestamp format
     */
    @field:SuppressLint("ConstantLocale")
    val timeFormat: DateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US),

    /**
     * Used for integration with crash reporter systems like Firebase Crashlytics.
     * Typical use case, call `FirebaseCrashlytics.getInstance().log(message)` in this function.
     * It's needed to give more context for the events leading up to a crash.
     */
    var crashlyticsLogger: ((String) -> Unit)? = null,

    /**
     * Used for integration with crash reporter systems like Firebase Crashlytics.
     * Typical use case, call `FirebaseCrashlytics.getInstance().recordException(throwable)` in this function.
     * It's needed to log non-fatal exceptions in the app.
     */
    var crashlyticsExceptionLogger: ((Throwable) -> Unit)? = null,

    /**
     * If enabled, logging to `LogCat` is allowed.
     * Typical use case - disable `LogCat` logging in a release build
     */
    var writeToLogcat: Boolean = true,

    /**
     * If enabled, logging to files is allowed ([settings] must be non null)
     *
     * @see settings
     */
    var writeToFile: Boolean = true,
)