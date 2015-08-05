package dk.statsbiblioteket.medieplatform.dpaQA;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.TreeProcessorAbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.MultiThreadedEventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.schematron.StructureValidator;
import dk.statsbiblioteket.newspaper.schematron.XmlBuilderEventHandler;
import dk.statsbiblioteket.util.xml.DOM;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * Created by abr on 8/5/15.
 */
public class DPAStructureComponent extends TreeProcessorAbstractRunnableComponent {

    public static final String DEMANDS_SCH = "dpa_demands.sch";
    private Document batchStructure;

    protected DPAStructureComponent(Properties properties) {
        super(properties);
    }

    @Override
    public String getEventID() {
        return null;
    }

    @Override
    public void doWorkOnItem(Batch batch, ResultCollector resultCollector) throws Exception {
        String batchFolder = getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER);

        /*Generate structure xml in the first iteration*/
        XmlBuilderEventHandler xmlBuilderEventHandler = new XmlBuilderEventHandler();
        //TODO create a sequence checker here
        runBatchSinglethreaded(new File(batchFolder,batch.getBatchID()).getAbsolutePath(), resultCollector, new ArrayList<TreeEventHandler>(Arrays.asList(xmlBuilderEventHandler)));
        batchStructure = DOM.stringToDOM(xmlBuilderEventHandler.getXml(), true);
        /*Validate structure here*/
        StructureValidator validator1 = new StructureValidator(DEMANDS_SCH);
        validator1.validate(batch, batchStructure, resultCollector);
    }

    public Document getBatchStructure() {
        return batchStructure;
    }

    private static void runBatchSinglethreaded(String arg, ResultCollector resultCollector, List<TreeEventHandler> eventHandlers) {
        TransformingIteratorForFileSystems iterator = new TransformingIteratorForFileSystems(new File(arg),
                "\\.",
                ".*\\.pdf$",
                ".md5",
                null);
        MultiThreadedEventRunner eventRunner = new MultiThreadedEventRunner(iterator, eventHandlers, resultCollector, MultiThreadedEventRunner.singleThreaded, Executors.newFixedThreadPool(1));
        eventRunner.run();
    }

}
