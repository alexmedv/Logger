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

import android.util.Log

/**
 * LogLevel is a piece of information telling how important a given log message is.
 */
enum class LogLevel(val code: Int, val string: String) {

    /**
     * The most fine-grained information only used in rare cases where you need the full visibility of what is happening in your application
     */
    VERBOSE(Log.VERBOSE, "V"),

    /**
     * it should be used for information that may be needed for diagnosing issues and troubleshooting or when running application
     * in the test environment for the purpose of making sure everything is running correctly
     */
    DEBUG(Log.DEBUG, "D"),

    /**
     * The standard log level indicating that something happened, the application entered a certain state, etc. The information logged  using the
     * `INFO` log level should be purely informative and not looking into them on a regular basis shouldn’t result in missing any important information.
     */
    INFO(Log.INFO, "I"),

    /**
     * The log level that indicates that something unexpected happened in the application, a problem, or a situation that might disturb one of
     * the processes. But that does’t mean that the application failed. The WARN level should be used in situations that are unexpected,
     * but the code can continue the work.
     */
    WARN(Log.WARN, "W"),

    /**
     * The log level that should be used when the application hits an issue preventing one or more functionalities from properly functioning
     */
    ERROR(Log.ERROR, "E"),

    /**
     * This log level should be used for issues that the developer expects should never happen
     */
    ASSERT(Log.ASSERT, "A"),
}