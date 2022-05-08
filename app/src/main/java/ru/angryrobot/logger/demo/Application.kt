package ru.angryrobot.logger.demo

import android.os.Handler
import android.os.Looper
import ru.angryrobot.logger.Logger
import java.io.File


class Application : android.app.Application() {

//    val myLogger by lazy { Logger(File(filesDir, "logs")) }

    val handler = Handler(Looper.getMainLooper())

    val runnable = object : Runnable {
        override fun run() {
//            myLogger.w("Hello from other!")
            handler.postDelayed(this, 3000)
        }

    }

    override fun onCreate() {
        super.onCreate()
        handler.post(runnable)
    }

}