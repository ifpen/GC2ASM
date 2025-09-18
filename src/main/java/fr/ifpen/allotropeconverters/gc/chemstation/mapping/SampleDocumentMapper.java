package fr.ifpen.allotropeconverters.gc.chemstation.mapping;

import fr.ifpen.allotropeconverters.allotrope_models.SampleDocument;
import fr.ifpen.allotropeconverters.gc.chemstation.chfile.ChFile;
import fr.ifpen.allotropeconverters.gc.chemstation.infra.ResultXmlReader;


public class SampleDocumentMapper {

    /**
     * @param resultData  données XML (Result.xml)
     * @param sampleNameFromCh nom d’échantillon lu du .ch (priorisé si non vide)
     */
    public SampleDocument toSampleDocument(ResultXmlReader.ResultData resultData, String sampleNameFromCh) {
        SampleDocument sample = new SampleDocument();

        String sampleId = (sampleNameFromCh != null && !sampleNameFromCh.isBlank())
                ? sampleNameFromCh
                : resultData.sampleNameFromXml();

        sample.setSampleIdentifier(sampleId);
        sample.setWrittenName(sampleId);
        sample.setDescription(resultData.sampleDescription());

        return sample;
    }

    public SampleDocument toSampleDocument(ChFile chFile){
        SampleDocument sampleDocument = new SampleDocument();
        sampleDocument.setSampleIdentifier(chFile.getSampleName());
        sampleDocument.setWrittenName(chFile.getSampleName());

        return sampleDocument;
    }
}
