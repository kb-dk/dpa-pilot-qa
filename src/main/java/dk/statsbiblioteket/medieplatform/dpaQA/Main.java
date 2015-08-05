package dk.statsbiblioteket.medieplatform.dpaQA;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

import java.io.File;
import java.util.Properties;

/**
 * Created by abr on 8/4/15.
 */
public class Main {



    public static void main(String[] args) throws Exception {
        doMain(args);
    }

    public static int doMain(String[] args) throws Exception {

        ResultCollector resultCollector = new ResultCollector("tool","version",1000);
        Properties props = new Properties(System.getProperties());
        File batchAsFile = new File(args[0]);
        props.setProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, batchAsFile.getParentFile().getAbsolutePath());
        Batch batch = new Batch(batchAsFile.getName());
        props.setProperty(ConfigConstants.JPYLYZER_PATH,"PATH_TO_PDF_VALIDATOR_TOOL");


        DPAStructureComponent dpaStructureComponent = new DPAStructureComponent(props);
        dpaStructureComponent.doWorkOnItem(batch,resultCollector);

        DPAFileComponent dpaFileComponent = new DPAFileComponent(props, dpaStructureComponent.getBatchStructure());
        dpaFileComponent.doWorkOnItem(batch, resultCollector);

        if (resultCollector.isSuccess()){
            return 0;
        } else {
            return 1;
        }
    }


}
