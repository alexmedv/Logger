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

import android.os.SystemClock
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.IllegalStateException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun Logger.createLogBundle(zipFile: File) {
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


fun Logger.measure(msg: String = "Execution time", logEvent: Boolean = false, tag: String? = null, block: () -> Unit) {
    val startTime = SystemClock.elapsedRealtime()
    block()
    val result = SystemClock.elapsedRealtime() - startTime
    w("$msg: $result msec", logEvent, tag)
}
