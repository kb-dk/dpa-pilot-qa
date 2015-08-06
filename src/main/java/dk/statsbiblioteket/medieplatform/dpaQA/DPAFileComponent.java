package dk.statsbiblioteket.medieplatform.dpaQA;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.TreeProcessorAbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.MultiThreadedEventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.metadatachecker.SchemaValidatorEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.SchematronValidatorEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.JpylyzingEventHandler;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

public class DPAFileComponent extends TreeProcessorAbstractRunnableComponent {

    private final Document batchStructure;


    protected DPAFileComponent(Properties properties, Document batchStructure) {
        super(properties);
        this.batchStructure = batchStructure;
    }

    @Override
    public String getEventID() {
        return null;
    }

    @Override
    public void doWorkOnItem(Batch batch, ResultCollector resultCollector) throws Exception {
        String batchFolder = getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER);

        /*Here the single files are checked, schema checks and the like*/
        List<TreeEventHandler> attributeEventHandlers = getAttributeEventHandlers(resultCollector, batchStructure, batchFolder);
        runBatch(new File(batchFolder, batch.getBatchID()).getAbsolutePath(), resultCollector, attributeEventHandlers, 4);

    }


    private List<TreeEventHandler> getAttributeEventHandlers(ResultCollector resultCollector, Document batchStructure, String batchFolder) {
        ArrayList<TreeEventHandler> treeEventHandlers = new ArrayList<>();
        DocumentCache documentCache = new DocumentCache();

        HashMap<String, String> POSTFIX_TO_XSD = new HashMap<>();
        POSTFIX_TO_XSD.put(".xml", "dpa.xsd");
        POSTFIX_TO_XSD.put(".pdf.xml", "pdf.xsd");
        HashMap<String, String> POSTFIX_TO_TYPE = new HashMap<>();
        POSTFIX_TO_TYPE.put(".xml", "metadata");
        POSTFIX_TO_TYPE.put(".pdf.xml", "data");
        HashMap<String, String> POSTFIX_TO_MESSAGE_PREFIX = new HashMap<>();
        POSTFIX_TO_MESSAGE_PREFIX.put(".xml", "2A: ");
        POSTFIX_TO_MESSAGE_PREFIX.put(".pdf.xml", "2A: ");
        treeEventHandlers.add(new SchemaValidatorEventHandler(resultCollector, documentCache, POSTFIX_TO_XSD, POSTFIX_TO_TYPE, POSTFIX_TO_MESSAGE_PREFIX));

        HashMap<String, String> POSTFIX_TO_SCH = new HashMap<>();
        POSTFIX_TO_SCH.put(".xml", "dpa.sch");
        POSTFIX_TO_SCH.put(".pdf.xml", "pdf.sch");
        treeEventHandlers.add(new SchematronValidatorEventHandler(resultCollector, documentCache, POSTFIX_TO_SCH, POSTFIX_TO_TYPE));

        treeEventHandlers.add(new ArticleXmlChecker(resultCollector, documentCache, batchStructure));

        String jpylyzerPath = getProperties().getProperty(ConfigConstants.JPYLYZER_PATH);
        treeEventHandlers.add(new JpylyzingEventHandler(resultCollector, batchFolder, jpylyzerPath) {
            @Override
            protected String getJpylyzerName(String jp2Name) {
                return jp2Name.replaceFirst("\\.pdf$", ".pdf.xml");
            }
        });

        return treeEventHandlers;
    }


    private void runBatch(String arg, ResultCollector resultCollector, List<TreeEventHandler> eventHandlers, int threads) {
        TransformingIteratorForFileSystems iterator = new TransformingIteratorForFileSystems(new File(arg),
                "\\.",
                ".*\\.pdf$",
                ".md5",
                null);
        MultiThreadedEventRunner eventRunner = new MultiThreadedEventRunner(iterator, eventHandlers, resultCollector, getForker(), Executors.newFixedThreadPool(threads));
        eventRunner.run();
    }

    private MultiThreadedEventRunner.EventCondition getForker() {
        return new MultiThreadedEventRunner.EventCondition() {
            public boolean shouldFork(ParsingEvent parsingEvent) {
                String shortName = parsingEvent.getName().substring(parsingEvent.getName().indexOf('/') + 1);
                return shortName.matches("^[A-Z]{3}$");
            }

            public boolean shouldJoin(ParsingEvent parsingEvent) {
                String shortName = parsingEvent.getName().substring(parsingEvent.getName().indexOf('/') + 1);
                return shortName.matches("^[A-Z]{3}$");
            }
        };
    }

}
