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

import android.app.ActivityManager
import android.app.Application
import android.app.Application.getProcessName
import android.os.Build
import android.os.Process
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Compress all log files to zip archive
 *
 * @param zipFile Path to the output ZIP file. The file will be overwritten if it exists.
 *
 * @throws IllegalStateException if logger is not configured to write logs to the files
 */
fun Logger.compressLogs(zipFile: File) {
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

/**
 * Returns the name of the current process. A package's default process name
 * is the same as its package name. Non-default processes will look like
 * "$PACKAGE_NAME:$NAME", where $NAME corresponds to an android:process
 * attribute within AndroidManifest.xml.
 */
fun Application.getProcessNameCompat(): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return getProcessName()
    } else {
        val myPid = Process.myPid()
        val activityManager = getSystemService(Application.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses
        for (processInfo in runningAppProcesses) {
            if (processInfo.pid == myPid) {
                return processInfo.processName
            }
        }
        throw RuntimeException("Unable to get process name")
    }
}