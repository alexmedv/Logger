#:memo:EasyLogger
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
//TODO

##:rocket:Multiprocess applications
As you know, android allows you to run application components in a separate process (not to be confused with threads). This is quite rarely used by developers, but in this case there are some nuances of using the logger. The main problem is that if two processes write logs in the same directory, it may cause data corruption. There are two ways to solve this problem:
1) Allow logging only from one process
2) Log data from each process to its own directory

##:balance_scale:License
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