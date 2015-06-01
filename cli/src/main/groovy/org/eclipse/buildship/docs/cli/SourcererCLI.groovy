package org.eclipse.buildship.docs.cli

import org.eclipse.buildship.docs.JvmSourceParser
import org.eclipse.buildship.docs.export.TaskExporter
import org.eclipse.buildship.docs.model.ClassMetaDataRepository
import org.eclipse.buildship.docs.source.model.ClassMetaData

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
        def tasks = filterTasks(metaDataRepository)

        File outputDir = new File(args[1])
        outputDir.delete()
        outputDir.mkdirs()
        new TaskExporter(metaDataRepository).export(outputDir, tasks)
    }

    static Map<String, ClassMetaData> filterTasks(ClassMetaDataRepository<ClassMetaData> repository) {
        Map<String, ClassMetaData> filteredRepository = new HashMap<String, ClassMetaData>()

        repository.each { String name, ClassMetaData classMetadata ->
            if (isTaskClass(repository, classMetadata)) {
                if(!classMetadata.isInterface() && !classMetadata.isAbstract()){
                    filteredRepository.put(classMetadata.getClassName(), classMetadata);
                }
            }
        }
        return filteredRepository;
    }

    private static boolean isTaskClass(ClassMetaDataRepository repository, ClassMetaData classMetaData) {
        if (classMetaData == null) {
            return false
        }
        classMetaData.attach(repository);
        ClassMetaData superClazz= repository.find(classMetaData.getSuperClassName())

        if(superClazz){
            if(isTaskClass(repository, superClazz)){
                return true;
            }
        }

        boolean implementsTask = classMetaData.getInterfaceNames().any { ifName ->
            if (ifName.equals("org.gradle.api.Task")) {
                return true
            }
            isTaskClass(repository, repository.find(ifName));
        };
        return implementsTask;
    }
}
