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

class Set1Test extends BaseTest {

	private static final Logger log = LoggerFactory.getLogger(Set1Test.class);

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

    //original, succeeds
    @Test
    public void harveyTest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/harvey/tttout.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() == 0
    }

    //file copy, then trim : succeeds
	@Test
	public void tttout2Test() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/debuggingSession/harvey/tttout-trimmed.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() == 0
    }

    //cut and paste fails
    @Test
    public void cutpasteTest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/harvey/cutpasteHarvey.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() != 0
    }

    // cut and paste again , fails
    @Test
    public void cutpaste2Test() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/harvey/copiedHarvey-2.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() != 0
    }

    // reformated, it fails
    @Test
    public void reformatedTest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/harvey/tttout-reformated.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() != 0
    }

    //copy and paste of the trimmed version, it fails
    @Test
    public void copiedTrimmed1test() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/harvey/copiedTrimmed1.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() != 0
    }


    // Just a file copy, this works
    @Test
    public void copiedTrimmed2test() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/harvey/tttout-trimmed2.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() == 0
    }

    //we modified unsigned part of the message + add new line/whitespace inside the assertion => it succeeds so canonicalization works ok.
    // some other characters must be introduced during cut/paste
    @Test
    public void copiedTamperedtest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/harvey/tttout-tampered-ok.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() == 0
    }

    //copy from above but add extra characters => fails
    @Test
    public void copiedTamperedtest2() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/harvey/tttout-tampered-fail.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() != 0
    }

    //cut and paste in intellij fail
    @Test
    public void copiedtttout2() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/harvey/cutpasteInItselfBug.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() != 0
    }

    //cut and paste in sublime succeeds!
    @Test
    public void copiedtttout3() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/harvey/cutpastesublime.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() == 0
    }




}
