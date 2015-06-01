package org.eclipse.buildship.docs.integtest

import org.eclipse.buildship.docs.cli.SourcererCLI
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.util.zip.ZipInputStream

class GradleTaskGlossarySpec extends Specification {


    public static final File GRADLE_DOWNLOAD_DIR = new File("build/download").absoluteFile
    public static final String BASE_DOWNLOAD_URL =  'https://services.gradle.org/distributions/gradle-%s-all.zip'

    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Unroll
    def "can build task glossary for gradle #version"() {
        given:
        File gradleDistribution = maybeDownloadGradle(version);
        def targetFolder = temporaryFolder.newFolder("gradle-task-glossary-$version")

        when:
        SourcererCLI.main(new File(gradleDistribution, "src").absolutePath, targetFolder.absolutePath)

        then:
        targetFolder.listFiles().size() > 0
        targetFolder.eachFile { assert it.name.endsWith("json") }
        where:
        version << ["2.3", "2.4"]
    }

    def maybeDownloadGradle(String version) {
        String downloadURL = String.format(BASE_DOWNLOAD_URL, version)


        def targetDirectory = new File(GRADLE_DOWNLOAD_DIR, "gradle-$version")
        if(!targetDirectory.exists()) {

            downloadURL.toURL().withInputStream { s ->
                new ZipInputStream(s).with { zs ->
                    def zent
                    while (zent = zs.nextEntry) {
                        println "Downloading $entry.name"
                        GRADLE_DOWNLOAD_DIR.mkdirs()
                        def local = new File(GRADLE_DOWNLOAD_DIR, entry.name)
                        // Do stuff with the entry
                        if (zent.isDirectory()) {
                            local.mkdir()
                        } else {
                            local << zs
                        }

                        zs.closeEntry()
                    }
                }
            }
        }

        return targetDirectory;

    }
}

