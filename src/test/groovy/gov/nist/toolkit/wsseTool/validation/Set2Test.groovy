package gov.nist.toolkit.wsseTool.validation

import gov.nist.hit.ds.wsseTool.api.config.Context
import gov.nist.hit.ds.wsseTool.api.config.ContextFactory
import gov.nist.hit.ds.wsseTool.api.config.KeystoreAccess
import gov.nist.hit.ds.wsseTool.api.config.ValConfig
import gov.nist.hit.ds.wsseTool.api.exceptions.GenerationException
import gov.nist.hit.ds.wsseTool.util.MyXmlUtils
import gov.nist.hit.ds.wsseTool.validation.ValidationResult
import gov.nist.hit.ds.wsseTool.validation.WsseHeaderValidator
import gov.nist.toolkit.wsseTool.BaseTest
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.SAXException

import javax.xml.parsers.ParserConfigurationException
import java.security.KeyStoreException

class Set2Test extends BaseTest {

	private static final Logger log = LoggerFactory.getLogger(Set2Test.class);

	Context context;
	ValConfig config;

	@Before
	public void loadKeystore() throws KeyStoreException {
		String store = "src/test/resources/keystore/keystore";
		String sPass = "changeit";
		String alias = "hit-testing.nist.gov";
		String kPass = "changeit";
		context = ContextFactory.getInstance();
		context.setKeystore(new KeystoreAccess(store,sPass,alias,kPass));
		context.getParams().put("patientId", "D123401^^^&1.1&ISO");
		context.setParam('homeCommunityId', "urn:oid:1.1");
		context.setParam('To', "http://endpoint1.hostname1.nist.gov");
		config = new ValConfig("2.0")
	}

    //original
    @Test
    public void dimitriTest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/dimitri/textSign.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(), config, context);
        assert r.getFailureCount() == 2
    }

    //original, succeeds
    @Test
    public void dimitri2Test() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/dimitri/textSign-fixId.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(), config, context);
        assert r.getFailureCount() == 2
    }

}
