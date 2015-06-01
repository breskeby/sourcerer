package org.eclipse.buildship.docs.export
import com.google.code.docbook4j.renderer.HTMLRenderer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.eclipse.buildship.docs.export.dto.TaskProperty
import org.eclipse.buildship.docs.export.dto.TaskType
import org.eclipse.buildship.docs.export.formatting.*
import org.eclipse.buildship.docs.formatting.DocComment
import org.eclipse.buildship.docs.formatting.GenerationListener
import org.eclipse.buildship.docs.formatting.JavadocConverter
import org.eclipse.buildship.docs.formatting.JavadocLinkConverter
import org.eclipse.buildship.docs.formatting.LinkRenderer
import org.eclipse.buildship.docs.model.ClassMetaDataRepository
import org.eclipse.buildship.docs.source.TypeNameResolver
import org.eclipse.buildship.docs.source.model.AbstractLanguageElement
import org.eclipse.buildship.docs.source.model.ClassMetaData
import org.eclipse.buildship.docs.source.model.PropertyMetaData
import org.w3c.dom.Document

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

public class TaskExporter {

    ClassMetaDataRepository metaDataRepository

    public TaskExporter(ClassMetaDataRepository metaDataRepository ){
        this.metaDataRepository = metaDataRepository;
    }

    public void export(File targetDirectory, Map<String, ClassMetaData> tasks) {
        final List<TaskType> taskTypes = convertToDto(tasks);
        exportToJson(targetDirectory, taskTypes);
    }

    private void exportToJson(File targetDirectory, List<TaskType> taskTypes) {
        for (TaskType taskType : taskTypes) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            final String s = gson.toJson(taskType);
            final File file = new File(targetDirectory, taskType.getClassName() + ".json");
            file.text = s;
        }
    }

    List<TaskType> convertToDto(Map<String, ClassMetaData> tasks) {
        List<TaskType> taskTypes = new ArrayList<TaskType>();

        File tempDir = new File("tempXml")
        tempDir.mkdirs()
        for (ClassMetaData taskMetaData : tasks.values()) {
            println "{taskMetaData.getClassName()} = ${taskMetaData.getClassName()}"

            Document document = newEmptyDocument()
            DocComment comment = createDocBookComment(document, taskMetaData)
            String clazzcomment = generateHtmlFormatttedComment(taskMetaData.getClassName(), document, comment, tempDir)

            final TaskType taskType = new TaskType(taskMetaData.getClassName(), taskMetaData.getSimpleName(), clazzcomment);
            final Set<PropertyMetaData> declaredProperties = taskMetaData.getDeclaredProperties();
            for (PropertyMetaData declaredProperty : declaredProperties) {

                if(declaredProperty.isWriteable()) {

                    Document propDocument = newEmptyDocument()
                    DocComment propComment = createDocBookComment(propDocument, declaredProperty)

                    String formattedPropComment = generateHtmlFormatttedComment(taskMetaData.getClassName()+declaredProperty.getName(), propDocument, propComment, tempDir)

                    taskType.addTaskProperty(new TaskProperty(declaredProperty.getName(), formattedPropComment, declaredProperty.getType().getName()));
                }
            }
            taskTypes.add(taskType);
        }

        return taskTypes;
    }



    private String generateHtmlFormatttedComment(String name, Document document, DocComment comment, File tempDir) {

        comment.docbook.each {
            try {
                document.appendChild(it)
            } catch (Exception e) {
                e.printStackTrace()
                println it
            }
        }

        def file = new File(tempDir, name + ".xml");
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(new FileWriter(file));
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            ex.printStackTrace();
        }

        HTMLRenderer htmlRenderer = HTMLRenderer.create(file.toURI().toString());
        InputStream htmlStream = htmlRenderer.render()

        def scanner = new Scanner(htmlStream, "UTF-8").useDelimiter("\\A")
        String clazzcomment;
        if (scanner.hasNext()) {
            String htmlComment = scanner.next();
            clazzcomment = htmlComment.substring(htmlComment.indexOf("<p>"), htmlComment.lastIndexOf("</p>") + 4)
        } else {
            clazzcomment = "";
        }
        clazzcomment
    }

    private DocComment createDocBookComment(Document document, AbstractLanguageElement metaData) {
        JavadocConverter converter = new JavadocConverter(document, new JavadocLinkConverter(document, new TypeNameResolver(metaDataRepository), new LinkRenderer(document), metaDataRepository))
        DocComment comment = converter.parse(metaData, newGenerationListener())
        comment
    }

    GenerationListener newGenerationListener() {
        return new GenerationListener() {
            @Override
            void warning(String message) {

            }

            @Override
            void start(String context) {

            }

            @Override
            void finish() {

            }
        }
    }

    public static Document newEmptyDocument() {
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        Document ret;

        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        ret = builder.newDocument();

        return ret;
    }
}
