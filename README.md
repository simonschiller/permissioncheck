[![Build Status](https://img.shields.io/github/workflow/status/simonschiller/permissioncheck/CI)](https://github.com/simonschiller/permissioncheck/actions)
[![GitHub Release](https://img.shields.io/github/v/release/simonschiller/permissioncheck)](https://github.com/simonschiller/permissioncheck/releases)
[![License](https://img.shields.io/github/license/simonschiller/permissioncheck)](https://github.com/simonschiller/permissioncheck/blob/main/LICENSE)

# PermissionCheck

In large scale Android applications, it can be hard to continuously monitor app permissions to make sure no unintended permissions are introduced by third-party libraries. PermissionCheck helps you to detect such permission regressions easily. 

## Usage

After the PermissionCheck plugin is applied, you can use the `checkPermissions` task to generate the initial baseline file. The task will fail on the initial run, to avoid creating baselines on accident. You should include the generated baseline file your VCS.

Once the baseline exists, each subsequent invocation of `checkPermissions` will compare the current app permissions against the created baseline. The task will fail if changes are detected, allowing you to automatically catch regressions by running this task as part of your CI pipeline. These tasks will also run as part of the standard Gradle `check` task. In case you only want to verify a single variant, you can use the `check<Variant>Permissions` tasks.

#### Recreating the baseline

When you deliberately want to add new permissions to the app, you need to recreate the baseline so it matches the updated permissions. You can either add the new entry to the baseline manually or just recreate it using `checkPermissions --recreate`.

#### Strict mode

By default, PermissionCheck only reports issues that would cause the app to require more permissions than specified in the baseline. In case a permission is removed or the max SDK of a permission is decreased, the tasks would not fail. To also detect these cases, you can enable strict mode in the task configuration (see next section) or use the `--strict` command line option.

#### Configuring the tasks

For greater customization, you can change the default behaviour of the tasks using the Gradle DSL.

```groovy
permissionCheck {
    // Location of the baseline file, defaults to "$projectDir/permission-baseline.xml"
    baselineFile.set(layout.projectDirectory.file("baselines/permissions.xml"))

    // Directory for all generated reports, defaults to "$project.buildDir/reports/permissioncheck"
    reportDirectory.set(layout.buildDirectory.dir("reports"))
    
    // Always perform strict checking, defaults to false
    strict.set(true)
}
```

## Adding the plugin to your project

To add the PermissionCheck plugin to your project, you have to add this block of code to your `build.gradle`.

```groovy
plugins {
    id "io.github.simonschiller.permissioncheck" version "1.6.0"
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
        classpath "io.github.simonschiller:permissioncheck:1.6.0"
    }
}
```

When you're using the legacy plugin API, you also have to apply the plugin in the `build.gradle` of your module.

```groovy
apply plugin: "io.github.simonschiller.permissioncheck"
```

You can also find instructions on how to use the PermissionCheck plugin on the [Gradle plugin portal](https://plugins.gradle.org/plugin/io.github.simonschiller.permissioncheck).

## Working with this project

The main part of the source code is located in the `plugin` and `plugin-core` modules. The `plugin-configurator-*` modules are used to compile against different AGP versions, to stay backwards compatible. The `sample` module contains a sample project, which shows how the plugin is used.

* Build the plugin: `./gradlew jar`
* Run tests: `./gradlew test`

The first test execution can take a while, since [Gradle TestKit](https://docs.gradle.org/current/userguide/test_kit.html) needs to download all dependencies, subsequent runs should be faster.

To work with the sample project, you first have to publish the plugin to your local Maven repository. You can do so by running `./gradlew publishToMavenLocal`. Afterwards, uncomment the highlighted lines in the `settings.gradle.kts` and the `build.gradle.kts` files to include the sample project.

* Run the task in the sample project (single variant): `./gradlew checkReleasePermissions`
* Run the task in the sample project (all variants): `./gradlew checkPermissions`

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
