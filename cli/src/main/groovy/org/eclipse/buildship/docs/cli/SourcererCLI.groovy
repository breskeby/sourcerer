package org.eclipse.buildship.docs.cli

import org.eclipse.buildship.docs.JvmSourceParser
import org.eclipse.buildship.docs.export.TaskExporter
import org.eclipse.buildship.docs.model.ClassMetaDataRepository

class SourcererCLI {
    public static void main(String[] args) {
        File sourceFolder = new File("/Users/Rene/Downloads/gradle-2.4/src")

        JvmSourceParser parser = new JvmSourceParser();
        ClassMetaDataRepository metaDataRepository = parser.parseSourceFolder(sourceFolder)
        def tasks = parser.filterTasks(metaDataRepository)

        File outputDir = new File("glossary/gradle-2.4-glossary")
        outputDir.delete()
        outputDir.mkdirs()
        new TaskExporter(metaDataRepository).export(outputDir, tasks)

    }
}
