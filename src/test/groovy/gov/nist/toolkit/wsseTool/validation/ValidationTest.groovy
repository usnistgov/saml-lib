package gov.nist.toolkit.wsseTool.validation
import gov.nist.hit.ds.wsseTool.api.config.Context
import gov.nist.hit.ds.wsseTool.api.config.ContextFactory
import gov.nist.hit.ds.wsseTool.api.config.KeystoreAccess
import gov.nist.hit.ds.wsseTool.api.config.ValConfig
import gov.nist.hit.ds.wsseTool.api.exceptions.GenerationException
import gov.nist.hit.ds.wsseTool.generation.opensaml.OpenSamlWsseSecurityGenerator
import gov.nist.hit.ds.wsseTool.util.MyXmlUtils
import gov.nist.hit.ds.wsseTool.validation.ValidationResult
import gov.nist.hit.ds.wsseTool.validation.WsseHeaderValidator
import gov.nist.toolkit.wsseTool.BaseTest
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.xml.sax.SAXException

import javax.xml.parsers.ParserConfigurationException
import java.security.KeyStoreException

class ValidationTest extends BaseTest {

	private static final Logger log = LoggerFactory.getLogger(ValidationTest.class);

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

	@Test void runBatch(){
		Document xml = new OpenSamlWsseSecurityGenerator().generateWsseHeader(context);
		WsseHeaderValidator val = new WsseHeaderValidator();
		ValidationResult r = val.validateAndReport(xml.getDocumentElement(),config,context);
		assert r.getFailureCount() == 0
	}


	@Test void runConnect4Message(){
		def file = "sets/connect4RequestSecHeader.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		val.validate(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),context);
	}

	@Test void runMessageWithoutAuthzDecisionStatement(){
		def file = "sets/connect4RequestSecHeaderWithoutAuthzDecisionStatement.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		val.validate(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),context);
	}

	@Test void runMessageWith2AuthzDecisionStatements(){
		def file = "sets/connect4RequestSecHeaderWith2AuthzDecisionStatements.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		val.validate(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),context);
	}

	@Test void runUnparseableMessage(){
		def file = "validation/unparseableMessage.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		val.validate(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),context);
	}

	@Test void runPurposeOfUseAndSignatureProblems(){
		def file = "sets/debuggingSession/from_hmack101.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		val.validate(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),context);
	}

	@Test
	public void purposeOfUseTest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/SoapRequest.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		val.validate(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),context);
	}

	@Test
	public void purposeOfUseTest2() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/SoapRequest2.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		val.validate(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),context);
	}

	@Test
	public void zliTest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/debuggingSession/reqContent.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		val.validate(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),context);
	}

	@Test
	public void myTEst() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/debuggingSession/generatedAsFile.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
		assert r.getFailureCount() == 0
	}

    @Test
    public void myTEst2() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/generatedByTTT.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() == 4
    }

    @Test
    public void myTEst3() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/generatedByTTTWithIssuerEmail.xml";
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
        assert r.getFailureCount() == 4
    }

	@Test
	public void soapRequest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/SoapRequest.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
	}

	@Test
	public void harveyTest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/debuggingSession/harvey/tttout.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
	}
	
	@Test
	public void daveHarissonTest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/debuggingSession/request_body.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
	}

    @Test
    public void dragonTest() throws GenerationException, SAXException, IOException, ParserConfigurationException {
        def file = "sets/debuggingSession/dragon.xml";
        context.setParam('To',"https://localhost:8181/Gateway/DocumentSubmission/2_0/DocumentRepositoryXDR_Service")
        WsseHeaderValidator val = new WsseHeaderValidator();
        ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
    }

	@Test
	public void doNotCrashWhenNPIMissing() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/generatedByTTTwithoutNPI.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
		assert r.getFailureCount() == 4
	}

	@Test
	public void assertionSignatureIDPresent() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/generatedByTTTWithAssertionSignatureID.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
		assert r.getFailureCount() == 4
	}

	@Test
	public void conditionsBug() throws GenerationException, SAXException, IOException, ParserConfigurationException {
		def file = "sets/debuggingSession/SOAPRequest10292016.xml";
		WsseHeaderValidator val = new WsseHeaderValidator();
		ValidationResult r = val.validateAndReport(MyXmlUtils.getDocumentWithResourcePath(file).getDocumentElement(),config,context);
		assert r.getFailureCount() == 4
	}
	
 
}
