package ru.angryrobot.logger.demo


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.angryrobot.logger.Logger
import ru.angryrobot.logger.demo.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var log:Logger

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        log = Logger(File(filesDir, "logs"))

        log.exceptionLogger = {

        }

        log.firebaseLogger = {

        }

        binding.logVerbose.setOnClickListener {
            log.v("Simple \'verbose\' log entry")
        }
        binding.logDebug.setOnClickListener {
            log.v("\'Debug\' log entry with custom tag", tag = "SomeTag")
        }
        binding.logInfo.setOnClickListener {
            log.i("Info")
        }
        binding.logWarning.setOnClickListener {
            log.w("Warning")
        }
        binding.logError.setOnClickListener {
            someFunction()
        }
        binding.logAssert.setOnClickListener {
            log.a("Assert")
        }
        binding.writeToFiles.setOnCheckedChangeListener { _, isChecked ->
            log.writeToFile = isChecked
        }
        binding.writeToLogcat.setOnCheckedChangeListener { _, isChecked ->
            log.writeToLogcat = isChecked
        }
    }

    fun someFunction() {

        log.e("Something went wrong :(", Exception("Ex"))
    }
}