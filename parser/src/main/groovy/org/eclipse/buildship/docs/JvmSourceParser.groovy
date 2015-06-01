package org.eclipse.buildship.docs
import groovy.io.FileType
import groovyjarjarantlr.collections.AST
import org.codehaus.groovy.antlr.AntlrASTProcessor
import org.codehaus.groovy.antlr.SourceBuffer
import org.codehaus.groovy.antlr.UnicodeEscapingReader
import org.codehaus.groovy.antlr.java.Java2GroovyConverter
import org.codehaus.groovy.antlr.java.JavaLexer
import org.codehaus.groovy.antlr.java.JavaRecognizer
import org.codehaus.groovy.antlr.parser.GroovyLexer
import org.codehaus.groovy.antlr.parser.GroovyRecognizer
import org.codehaus.groovy.antlr.treewalker.PreOrderTraversal
import org.codehaus.groovy.antlr.treewalker.SourceCodeTraversal
import org.codehaus.groovy.antlr.treewalker.Visitor
import org.eclipse.buildship.docs.model.ClassMetaDataRepository
import org.eclipse.buildship.docs.model.SimpleClassMetaDataRepository
import org.eclipse.buildship.docs.source.SourceMetaDataVisitor
import org.eclipse.buildship.docs.source.TypeNameResolver
import org.eclipse.buildship.docs.source.model.Action
import org.eclipse.buildship.docs.source.model.ClassMetaData
import org.eclipse.buildship.docs.source.model.Transformer
import org.eclipse.buildship.docs.source.model.TypeMetaData

public class JvmSourceParser {



    public ClassMetaDataRepository parseSourceFolder(File sourceRootFolder) {
        //parsing all input files into metadata
        //and placing them in the repository object
        SimpleClassMetaDataRepository<ClassMetaData> repository = new SimpleClassMetaDataRepository<ClassMetaData>()
        int counter = 0

        sourceRootFolder.eachFileRecurse(FileType.FILES) { f ->
            if (f.name.endsWith(".java") || f.name.endsWith(".groovy")) {
                parse(f, repository)
            }
            counter++
        }

        println "parsed $counter source files!"

        //updating/modifying the metadata and making sure every type reference across the metadata is fully qualified
        //so, the superClassName, interafaces and types needed by declared properties and declared methods will have fully qualified name
        TypeNameResolver resolver = new TypeNameResolver(repository)
        repository.each { name, metaData ->
            fullyQualifyAllTypeNames(metaData, resolver)
        }
        return repository;
    }

    Map<String, ClassMetaData> filterTasks(ClassMetaDataRepository<ClassMetaData> repository) {
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

    private boolean isTaskClass(ClassMetaDataRepository repository, ClassMetaData classMetaData) {
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

    void parse(File sourceFile, ClassMetaDataRepository<ClassMetaData> repository) {
        try {
            sourceFile.withReader { reader ->
                if (sourceFile.name.endsWith('.java')) {
                    parseJava(sourceFile, reader, repository)
                } else {
                    parseGroovy(sourceFile, reader, repository)
                }
            }
        } catch (Exception e) {
            println "Cannot parse $sourceFile.absolutePath"
        }
    }

    void parseJava(File sourceFile, Reader input, ClassMetaDataRepository<ClassMetaData> repository) {
        SourceBuffer sourceBuffer = new SourceBuffer();
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(input, sourceBuffer);
        JavaLexer lexer = new JavaLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        JavaRecognizer parser = JavaRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        String[] tokenNames = parser.getTokenNames();

        parser.compilationUnit();
        AST ast = parser.getAST();

        // modify the Java AST into a Groovy AST (just token types)
        Visitor java2groovyConverter = new Java2GroovyConverter(tokenNames);
        AntlrASTProcessor java2groovyTraverser = new PreOrderTraversal(java2groovyConverter);
        java2groovyTraverser.process(ast);

        def visitor = new SourceMetaDataVisitor(sourceBuffer, repository, false)
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);
        traverser.process(ast);
        visitor.complete()
    }
//
    void parseGroovy(File sourceFile, Reader input, ClassMetaDataRepository<ClassMetaData> repository) {
        SourceBuffer sourceBuffer = new SourceBuffer();
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(input, sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        GroovyRecognizer parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);

        parser.compilationUnit();
        AST ast = parser.getAST();

        def visitor = new SourceMetaDataVisitor(sourceBuffer, repository, true)
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);
        traverser.process(ast);
        visitor.complete()
    }

    static fullyQualifyAllTypeNames(ClassMetaData classMetaData, TypeNameResolver resolver) {
        try {
            classMetaData.resolveTypes(new Transformer<String, String>() {
                String transform(String i) {
                    return resolver.resolve(i, classMetaData)
                }
            })
            classMetaData.visitTypes(new Action<TypeMetaData>() {
                void execute(TypeMetaData t) {
                    resolver.resolve(t, classMetaData)
                }
            })
        } catch (Exception e) {
            throw new RuntimeException("Could not resolve types in class '$classMetaData.className'.", e)
        }
    }
}
