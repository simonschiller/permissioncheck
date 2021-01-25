package io.github.simonschiller.permissioncheck.internal.report

import io.github.simonschiller.permissioncheck.internal.data.Permission
import io.github.simonschiller.permissioncheck.internal.data.Sdk23Permission
import io.github.simonschiller.permissioncheck.internal.data.Violation
import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File

class HtmlReporterTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `Empty report is generated when there are no issues`() {
        val reportFile = tempDir.resolve("report.html")
        val reporter = HtmlReporter(reportFile)

        val violations = emptyList<Violation>()
        reporter.report(mapOf("debug" to violations, "release" to violations))

        val expectedReportContent = """
            <html lang="en">
                <head>
                    <title>PermissionCheck Report</title>
                    <meta content="width=device-width, initial-scale=1, shrink-to-fit=no" name="viewport"/>
                </head>
                <body class="bg-light">
                    <div class="container">
                        <h1 class="mt-4 mb-4">PermissionCheck Report</h1>
                        <div class="card mt-4 mb-4 bg-dark">
                            <div class="card-header d-flex justify-content-between align-items-center text-light">
                                <b>debug</b>
                                <span class="badge badge-pill badge-light">0</span>
                            </div>
                            <div class="list-group list-group-flush">
                                <div class="list-group-item">
                                    <h5>No violations found</h5>
                                    <p>All permissions from the manifest match the baseline.</p>
                                </div>
                            </div>
                        </div>
                        <div class="card mt-4 mb-4 bg-dark">
                            <div class="card-header d-flex justify-content-between align-items-center text-light">
                                <b>release</b>
                                <span class="badge badge-pill badge-light">0</span>
                            </div>
                            <div class="list-group list-group-flush">
                                <div class="list-group-item">
                                    <h5>No violations found</h5>
                                    <p>All permissions from the manifest match the baseline.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </body>
            </html>
            
        """.trimIndent()
        assertEquals(expectedReportContent, reportFile.readText().normaliseLineSeparators().removeStyleTags())
    }

    @Test
    fun `Generated report contains all violations when they exist`() {
        val reportFile = tempDir.resolve("report.html")
        val reporter = HtmlReporter(reportFile)

        val debugViolations = listOf(
            Violation.Added(Permission("android.permission.CAMERA")),
            Violation.MaxSdkIncreased(Permission("android.permission.ACCESS_FINE_LOCATION"), 27)
        )
        val releaseViolations = listOf(
            Violation.Removed(Sdk23Permission("android.permission.ACCESS_COARSE_LOCATION")),
            Violation.MaxSdkDecreased(Sdk23Permission("android.permission.INTERNET", 25), 28)
        )
        reporter.report(mapOf("debug" to debugViolations, "release" to releaseViolations))

        val expectedReportContent = """
            <html lang="en">
                <head>
                    <title>PermissionCheck Report</title>
                    <meta content="width=device-width, initial-scale=1, shrink-to-fit=no" name="viewport"/>
                </head>
                <body class="bg-light">
                    <div class="container">
                        <h1 class="mt-4 mb-4">PermissionCheck Report</h1>
                        <div class="card mt-4 mb-4 bg-dark">
                            <div class="card-header d-flex justify-content-between align-items-center text-light">
                                <b>debug</b>
                                <span class="badge badge-pill badge-light">2</span>
                            </div>
                            <div class="list-group list-group-flush">
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <h5>New permission added</h5>
                                    </div>
                                    <p>Permission was found in the manifest, but not in the baseline.</p>
                                    <div class="list-group list-group-flush">
                                        <pre class="list-group-item list-group-item-success m-0">&lt;uses-permission android:name="android.permission.CAMERA" /&gt;</pre>
                                    </div>
                                </div>
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <h5>Max SDK increased</h5>
                                    </div>
                                    <p>Manifest contains a lower max SDK for permission than the baseline entry.</p>
                                    <div class="list-group list-group-flush">
                                        <pre class="list-group-item list-group-item-danger m-0">&lt;uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="27" /&gt;</pre>
                                        <pre class="list-group-item list-group-item-success m-0">&lt;uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /&gt;</pre>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="card mt-4 mb-4 bg-dark">
                            <div class="card-header d-flex justify-content-between align-items-center text-light">
                                <b>release</b>
                                <span class="badge badge-pill badge-light">2</span>
                            </div>
                            <div class="list-group list-group-flush">
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <h5>Permission removed</h5>
                                        <span class="badge badge-danger badge-pill">strict</span>
                                    </div>
                                    <p>Permission was found in the baseline, but not in the manifest.</p>
                                    <div class="list-group list-group-flush">
                                        <pre class="list-group-item list-group-item-danger m-0">&lt;uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION" /&gt;</pre>
                                    </div>
                                </div>
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <h5>Max SDK decreased</h5>
                                        <span class="badge badge-danger badge-pill">strict</span>
                                    </div>
                                    <p>Manifest contains a higher max SDK for permission than the baseline entry.</p>
                                    <div class="list-group list-group-flush">
                                        <pre class="list-group-item list-group-item-danger m-0">&lt;uses-permission-sdk-23 android:name="android.permission.INTERNET" android:maxSdkVersion="28" /&gt;</pre>
                                        <pre class="list-group-item list-group-item-success m-0">&lt;uses-permission-sdk-23 android:name="android.permission.INTERNET" android:maxSdkVersion="25" /&gt;</pre>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </body>
            </html>
            
        """.trimIndent()
        assertEquals(expectedReportContent, reportFile.readText().normaliseLineSeparators().removeStyleTags())
    }

    @Test
    fun `Report is generated correctly if some of the variants contain no issues`() {
        val reportFile = tempDir.resolve("report.html")
        val reporter = HtmlReporter(reportFile)

        val debugViolations = listOf(
            Violation.Added(Permission("android.permission.CAMERA")),
            Violation.MaxSdkIncreased(Permission("android.permission.ACCESS_FINE_LOCATION"), 27)
        )
        val releaseViolations = emptyList<Violation>()
        reporter.report(mapOf("debug" to debugViolations, "release" to releaseViolations))

        val expectedReportContent = """
            <html lang="en">
                <head>
                    <title>PermissionCheck Report</title>
                    <meta content="width=device-width, initial-scale=1, shrink-to-fit=no" name="viewport"/>
                </head>
                <body class="bg-light">
                    <div class="container">
                        <h1 class="mt-4 mb-4">PermissionCheck Report</h1>
                        <div class="card mt-4 mb-4 bg-dark">
                            <div class="card-header d-flex justify-content-between align-items-center text-light">
                                <b>debug</b>
                                <span class="badge badge-pill badge-light">2</span>
                            </div>
                            <div class="list-group list-group-flush">
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <h5>New permission added</h5>
                                    </div>
                                    <p>Permission was found in the manifest, but not in the baseline.</p>
                                    <div class="list-group list-group-flush">
                                        <pre class="list-group-item list-group-item-success m-0">&lt;uses-permission android:name="android.permission.CAMERA" /&gt;</pre>
                                    </div>
                                </div>
                                <div class="list-group-item">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <h5>Max SDK increased</h5>
                                    </div>
                                    <p>Manifest contains a lower max SDK for permission than the baseline entry.</p>
                                    <div class="list-group list-group-flush">
                                        <pre class="list-group-item list-group-item-danger m-0">&lt;uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="27" /&gt;</pre>
                                        <pre class="list-group-item list-group-item-success m-0">&lt;uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /&gt;</pre>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="card mt-4 mb-4 bg-dark">
                            <div class="card-header d-flex justify-content-between align-items-center text-light">
                                <b>release</b>
                                <span class="badge badge-pill badge-light">0</span>
                            </div>
                            <div class="list-group list-group-flush">
                                <div class="list-group-item">
                                    <h5>No violations found</h5>
                                    <p>All permissions from the manifest match the baseline.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </body>
            </html>
            
        """.trimIndent()
        assertEquals(expectedReportContent, reportFile.readText().normaliseLineSeparators().removeStyleTags())
    }

    @Test
    fun `Exception is thrown if report file has wrong type`() {
        val reportFile = tempDir.resolve("report.json")
        assertThrows<IllegalArgumentException> { HtmlReporter(reportFile) }
    }

    private fun String.removeStyleTags(): String {
        val styleStart = lastIndexOf('\n', indexOf("<style>"))
        val styleEnd = indexOf('\n', indexOf("</style>"))
        return substring(0, styleStart) + substring(styleEnd)
    }
}
