package dk.statsbiblioteket.medieplatform.dpaQA;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.util.Strings;
import org.w3c.dom.Document;

import java.io.IOException;

/**
 * Created by abr on 8/4/15.
 */
public class ArticleXmlChecker extends DefaultTreeEventHandler
{
    private final ResultCollector resultCollector;
    private final DocumentCache documentCache;
    private final Document batchStructure;

    public ArticleXmlChecker(ResultCollector resultCollector, DocumentCache documentCache, Document batchStructure) {

        this.resultCollector = resultCollector;
        this.documentCache = documentCache;
        this.batchStructure = batchStructure;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (!event.getName().endsWith(".xml")){
            return;
        }
        try {
            Document articleXML = documentCache.getDocument(event);

            //TODO Now do validation that checks this article against the batch structure


        } catch (IOException e) {
            resultCollector.addFailure(event.getName(),
                    "exception",
                    getClass().getSimpleName(),
                    "Exception reading metadata. Error was " + e.toString(),
                    Strings.getStackTrace(e));
            return;
        }
    }
}
