package ru.angryrobot.logger.demo

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import ru.angryrobot.logger.LogLevel
import ru.angryrobot.logger.Logger
import ru.angryrobot.logger.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var log:Logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log = getLogger()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        log.crashlyticsExceptionLogger = {
            //FirebaseCrashlytics.getInstance().recordException(it)
        }

        log.crashlyticsLogger = {
            //FirebaseCrashlytics.getInstance().log(it)
        }

        binding.logLevel.apply {
            adapter = ArrayAdapter(this@MainActivity , android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, LogLevel.values())
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long ) {
                    log.logLevel = LogLevel.values()[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {  }
            }
        }

        binding.destroy.setOnClickListener {
            log.destroy()
        }

        binding.logVerbose.setOnClickListener {
            log.v("Simple \'verbose\' log entry")
        }

        binding.logDebug.setOnClickListener {
            log.d("\'Debug\' log entry with custom tag", tag = "SomeTag")
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