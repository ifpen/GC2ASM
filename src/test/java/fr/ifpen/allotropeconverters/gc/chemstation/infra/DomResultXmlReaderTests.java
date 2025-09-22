package fr.ifpen.allotropeconverters.gc.chemstation.infra;

import fr.ifpen.allotropeconverters.gc.TestConstants;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static fr.ifpen.allotropeconverters.gc.TestConstants.DEFAULT_DATE_TIME_FORMATTERS;

class DomResultXmlReaderTests {
    @Test
    void testParseXmlResult() {
        DomResultXmlReader xmlReader = new DomResultXmlReader(TestConstants.TIME_ZONE_PARIS, DEFAULT_DATE_TIME_FORMATTERS);
        ResultXmlReader.ResultData resultData = xmlReader.read(TestConstants.RESOURCE_V_179_D_FOLDER, "Result.xml");
        Assertions.assertThat(resultData.sampleNameFromXml()).isEqualTo("22-00465-1");
        Assertions.assertThat(resultData.injectionDateTime().toInstant()).isEqualTo(Instant.parse("2022-05-12T09:24:28Z"));
        Assertions.assertThat(resultData.signals().get(0).integrationRows()).hasSize(24);
    }
}
