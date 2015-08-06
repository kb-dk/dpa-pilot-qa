package dk.statsbiblioteket.medieplatform.dpaQA;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

import java.io.File;
import java.util.Properties;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.JPYLYZER_PATH;

public class Main {

    public static void main(String[] args) throws Exception {
        doMain(args);
    }

    public static int doMain(String[] args) throws Exception {

        ResultCollector resultCollector = new ResultCollector("tool", "version", 1000);
        Properties props = new Properties(System.getProperties());

        // args[0] points to batch file.
        File batchAsFile = new File(args[0]);
        props.setProperty(ITERATOR_FILESYSTEM_BATCHES_FOLDER, batchAsFile.getParentFile().getAbsolutePath());

        Batch batch = new Batch(batchAsFile.getName());
        props.setProperty(JPYLYZER_PATH, "PATH_TO_PDF_VALIDATOR_TOOL");

        DPAStructureComponent dpaStructureComponent = new DPAStructureComponent(props);
        dpaStructureComponent.doWorkOnItem(batch, resultCollector);

        DPAFileComponent dpaFileComponent = new DPAFileComponent(props, dpaStructureComponent.getBatchStructure());
        dpaFileComponent.doWorkOnItem(batch, resultCollector);

        if (resultCollector.isSuccess()) {
            return 0;
        } else {
            return 1;
        }
    }


}
