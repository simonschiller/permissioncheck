[![Build Status](https://img.shields.io/travis/com/simonschiller/permissioncheck)](https://travis-ci.com/github/simonschiller/permissioncheck)
[![GitHub Release](https://img.shields.io/github/v/release/simonschiller/permissioncheck)](https://github.com/simonschiller/permissioncheck/releases)
[![License](https://img.shields.io/github/license/simonschiller/permissioncheck)](https://github.com/simonschiller/permissioncheck/blob/master/LICENSE)

# PermissionCheck

In large scale Android applications, it can be hard to continuously monitor app permissions to make sure no unintended permissions are introduced by third-party libraries. PermissionCheck helps you to detect such permission regressions easily. 

## Usage

After the PermissionCheck plugin is applied, you can use the `check<Variant>Permissions` tasks to generate the initial baseline file. Make sure you run this task for all variants that you're publishing, if your app contains multiple flavors. The task will fail on the initial run, to avoid creating baselines on accident. You should include the generated baseline file your VCS.

Once the baseline exists, each subsequent invocation of `check<Variant>Permissions` will compare the current app permissions against the created baseline. The task will fail if changes are detected, allowing you to automatically catch regressions by running this task as part of your CI pipeline. These tasks will also run as part of the standard Gradle `check` task. 

#### Recreating the baseline

When you deliberately want to add new permissions to the app, you need to recreate the baseline so it matches the updated permissions. You can either add the new entry to the baseline manually or just recreate it using `check<Variant>Permissions --recreate`.

#### Strict mode

By default, PermissionCheck only reports issues that would cause the app to require more permissions than specified in the baseline. In case a permission is removed or the max SDK of a permission is decreased, the tasks would not fail. To also detect these cases, you can enable strict mode in the task configuration (see next section) or use the `--strict` command line option.

#### Configuring the tasks

For greater customization, you can change the default behaviour of the tasks using the Gradle DSL.

```groovy
permissionCheck {
    // Location of the baseline file, defaults to "$projectDir/permission-baseline.xml"
    baselineFile.set(layout.projectDirectory.file("baselines/permissions.xml"))
    
    // Always perform strict checking, defaults to false
    strict.set(true)
}
```

## Adding the plugin to your project

To add the PermissionCheck plugin to your project, you have to add this block of code to your `build.gradle`.

```groovy
plugins {
    id "io.github.simonschiller.permissioncheck" version "1.2.0"
}
```

Alternatively, you can also use the legacy plugin API. Simply add the following snippet to your top-level `build.gradle`.

```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "io.github.simonschiller:permissioncheck:1.2.0"
    }
}
```

When you're using the legacy plugin API, you also have to apply the plugin in the `build.gradle` of your module.

```groovy
apply plugin: "io.github.simonschiller.permissioncheck"
```

You can also find instructions on how to use the PermissionCheck plugin on the [Gradle plugin portal](https://plugins.gradle.org/plugin/io.github.simonschiller.permissioncheck).

## Working with this project

The source code of the plugin is located in the `buildSrc` folder. The `sample` folder contains a sample project that shows how the plugin is used.

* Build the plugin: `./gradlew -b buildSrc/build.gradle.kts assemble`
* Run tests: `./gradlew -b buildSrc/build.gradle.kts test`
* Run the tasks in the sample project: `./gradlew checkReleasePermissions`

The first test execution can take a while, since [Gradle TestKit](https://docs.gradle.org/current/userguide/test_kit.html) needs to re-download all dependencies, subsequent runs should be faster. Tests are executed during project sync, so the initial sync of this projects in Android Studio or IntelliJ can also be slow.

## License

```
Copyright 2020 Simon Schiller

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
