package ru.angryrobot.logger.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import ru.angryrobot.logger.Logger



import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var log:Logger

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        log = Logger(File(filesDir, "logs"))

        log.exceptionLogger = {

        }

        log.firebaseLogger = {

        }

        findViewById<Button>(R.id.button).setOnClickListener {

        }
        findViewById<Button>(R.id.button2).setOnClickListener {

        }
        findViewById<Button>(R.id.button3).setOnClickListener {
            someFunction()
        }
    }

    fun someFunction() {

    }
}