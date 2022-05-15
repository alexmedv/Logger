package ru.angryrobot.logger.demo

import android.content.Context
import ru.angryrobot.logger.Logger
import ru.angryrobot.logger.LoggerSettings
import java.io.File

class Application : android.app.Application() {

    lateinit var log: Logger

    override fun onCreate() {
        super.onCreate()
        log = Logger(
            LoggerSettings(
                logsDir = File(filesDir, "logs"),
                logFilesSize = 1024 * 100,
                logFilesCount = 4,
                logFilesName = "my_log_file.txt"
            )
        )
    }

}

/**
 * Allows you to get the logger wherever there is context
 */
fun Context.getLogger() = (applicationContext as Application).log