# :memo: EasyLogger
Logging is one of the most important tools for finding bugs in an application. Android has a `LogCat` for this purpose, but it is often not enough. `LogCat` is only convenient for the developer, app users don't know what it is at all. So the app should log the data into files, which will then be analyzed by the developer in case of failures.

## :wrench: Installation
Add the following dependency to your `build.gradle` file:
```groovy
dependencies {
  implementation 'com.github.alexmedv:Logger:1.0.0'
}
```
:warning: Don't forget to add the JitPack maven repository to the list of repositories: `maven { url "https://jitpack.io" }`


## :gear: Logger configuration
The logger can be created with default parameters, in which case only the output to the `LogCat` will work. In other words, the logger works as a wrapper over the `LogCat`:
```kotlin
val log = Logger()
log.d("Debug message")
```
If you want to write the logs to files, you must at least specify the path to the logs directory:
```kotlin
val log = Logger(File(context.filesDir, "logs"))
log.d("write to files and LogCat")
```
If more fine-grained logging settings are needed, they can be set using the `LoggerSettings` class:
```kotlin
val log = Logger(
    LoggerSettings(
        logsDir = File(context.filesDir, "logs"), // Logs directory
        logFilesSize = 1024 * 100, // Max file size (100 kBytes)
        logFilesCount = 4, // Max number of log files in the directory
        logFilesName = "my_log_file.txt" // LogFiles name
    )
)
```
## :technologist: Usage
To log messages, simply call a function with the appropriate logging level. You no longer need to pass the `tag` every time, the logger will generate it itself:
```kotlin
log.i("Hello world")
```
At the same time it is possible to pass your own tag:
```kotlin
log.d("\'Debug\' log entry with custom tag", tag = "SomeTag")
```
For error messages there is an option to log the stacktrace:
```kotlin
val exception = IOException()
log.e("Something went wrong :(", exception)
```
To limit the flow of messages into the logs, you can increase the logging level:
```kotlin
log.logLevel = LogLevel.INFO
log.v("verbose message") // This will not be logged
log.d("debug message")   // This will not be logged either
log.i("info message")    // This message and others with a higher level will be logged
log.w("warning message")
log.e("error message")
log.a("assert message")
```
At any time logging to files and `LogCat` can be completely paused and resumed:
```kotlin
log.writeToLogcat = false // disable logging to LogCat
log.writeToFile = false  // disable logging to files
```
## :robot: Integration with crash reporter systems (Firebase etc)
//TODO 

## :rocket: Multiprocess applications
As you know, android allows you to run application components in a separate process (not to be confused with threads). This is quite rarely used by developers, but in this case there are some nuances of using the logger. The main problem is that if two processes write logs in the same directory, it may cause data corruption. There are two ways to solve this problem:
1) Allow logging only from one process
2) Log data from each process to its own directory

## :balance_scale: License
```
  Copyright 2022 Alexander Medvedev
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```
