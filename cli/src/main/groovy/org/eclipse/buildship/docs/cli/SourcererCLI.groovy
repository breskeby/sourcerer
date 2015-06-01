package org.eclipse.buildship.docs.cli

import org.eclipse.buildship.docs.JvmSourceParser
import org.eclipse.buildship.docs.export.TaskExporter
import org.eclipse.buildship.docs.model.ClassMetaDataRepository

class SourcererCLI {
    public static void main(String[] args) {
        if(args.size() != 2) {
            println "wrong number of arguments"
            println "1st argument must be path to source folder"
            println "2nd argument target dir for json task glossary"
        }
        File sourceFolder = new File(args[0])

        JvmSourceParser parser = new JvmSourceParser();
        ClassMetaDataRepository metaDataRepository = parser.parseSourceFolder(sourceFolder)
        def tasks = parser.filterTasks(metaDataRepository)

        File outputDir = new File(args[1])
        outputDir.delete()
        outputDir.mkdirs()
        new TaskExporter(metaDataRepository).export(outputDir, tasks)

    }
}
