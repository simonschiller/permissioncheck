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

class XmlReporterTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `Empty report is generated when there are no issues`() {
        val reportFile = tempDir.resolve("report.xml")
        val reporter = XmlReporter(reportFile)

        val violations = emptyList<Violation>()
        reporter.report(mapOf("debug" to violations, "release" to violations))

        val expectedReportContent = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <violations>
                <variant name="debug"/>
                <variant name="release"/>
            </violations>
            
        """.trimIndent()
        assertEquals(expectedReportContent, reportFile.readText().normaliseLineSeparators())
    }

    @Test
    fun `Generated report contains all violations when they exist`() {
        val reportFile = tempDir.resolve("report.xml")
        val reporter = XmlReporter(reportFile)

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
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <violations>
                <variant name="debug">
                    <violation type="added">
                        <uses-permission name="android.permission.CAMERA"/>
                    </violation>
                    <violation previousMaxSdkVersion="27" type="max-sdk-increased">
                        <uses-permission name="android.permission.ACCESS_FINE_LOCATION"/>
                    </violation>
                </variant>
                <variant name="release">
                    <violation strict="true" type="removed">
                        <uses-permission-sdk-23 name="android.permission.ACCESS_COARSE_LOCATION"/>
                    </violation>
                    <violation previousMaxSdkVersion="28" strict="true" type="max-sdk-decreased">
                        <uses-permission-sdk-23 maxSdkVersion="25" name="android.permission.INTERNET"/>
                    </violation>
                </variant>
            </violations>
            
        """.trimIndent()
        assertEquals(expectedReportContent, reportFile.readText().normaliseLineSeparators())
    }

    @Test
    fun `Exception is thrown if report file has wrong type`() {
        val reportFile = tempDir.resolve("report.json")
        assertThrows<IllegalArgumentException> { XmlReporter(reportFile) }
    }
}
