package gov.nist.toolkit.wsseTool.schemaValidation;

import gov.nist.hit.ds.wsseTool.api.config.ContextFactory;
import gov.nist.hit.ds.wsseTool.api.config.GenContext;
import gov.nist.hit.ds.wsseTool.api.config.KeystoreAccess;
import gov.nist.toolkit.wsseTool.BaseTest;

import java.security.KeyStoreException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class schemaValidationTest extends BaseTest {

	GenContext context;

	private static Logger log = LoggerFactory.getLogger("");

	@Before
	public void loadKeystore() throws KeyStoreException {
		String store = "src/test/resources/keystore/keystore";
		String sPass = "changeit";
		String alias = "hit-testing.nist.gov";
		String kPass = "changeit";
		context = ContextFactory.getInstance();
		context.setKeystore(new KeystoreAccess(store, sPass, alias, kPass));
		context.getParams().put("patientId", "D123401^^^&1.1&ISO");
	}

	/*
	 * A simple test of the schema validation
	 */
	@Ignore
	@Test
	public void schemaValidationTest() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new SimpleErrorHandler());

		Document document = builder.parse(new InputSource("src/test/resources/misc/document.xml"));
	}

	/*
	 * A simple test on our message
	 */
	@Ignore
	@Test
	public void tttSchemaValidationTest() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new SimpleErrorHandler());

		Document document = builder.parse(new InputSource("src/test/resources/sets/generatedByTTT.xml"));
	}

	/*
	 * Unecessary schema but capture problem with the assertion
	 */
	@Ignore
	@Test
	public void schemaValidationTest2() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		factory.setSchema(schemaFactory.newSchema(new Source[] {
				new StreamSource("src/main/resources/schemas/oasis-200401-wss-wssecurity-secext-1.0.xsd"),
				new StreamSource("src/main/resources/schemas/soap-envelope.xsd"),
				new StreamSource("src/main/resources/schemas/saml-schema-assertion-2.0.xsd"),
				new StreamSource("src/main/resources/schemas/xmldsig-core-schema.xsd") }));

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new SimpleErrorHandler());

		Document document = builder.parse(new InputSource(
				"src/test/resources/sets/tttVariants/badTTT_assertInvalidElement.xml"));
	}

	/*
	 * There are unecessary schemas here but capture problem with dsig
	 */
	@Ignore
	@Test
	public void schemaValidationTest3() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		factory.setSchema(schemaFactory.newSchema(new Source[] {
				new StreamSource("src/main/resources/schemas/oasis-200401-wss-wssecurity-secext-1.0.xsd"),
				new StreamSource("src/main/resources/schemas/soap-envelope.xsd"),
				new StreamSource("src/main/resources/schemas/saml-schema-assertion-2.0.xsd"),
				new StreamSource("src/main/resources/schemas/xmldsig-core-schema.xsd") }));

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new SimpleErrorHandler());

		Document document = builder.parse(new InputSource(
				"src/test/resources/sets/tttVariants/badTTT_assertSignInvalidElement.xml"));
	}

	/*
	 * Prove that xml signature was included by another schema
	 */
	@Ignore
	@Test
	public void schemaValidationTest4() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		factory.setSchema(schemaFactory.newSchema(new Source[] {
				new StreamSource("src/main/resources/schemas/oasis-200401-wss-wssecurity-secext-1.0.xsd"),
				new StreamSource("src/main/resources/schemas/soap-envelope.xsd"),
				new StreamSource("src/main/resources/schemas/saml-schema-assertion-2.0.xsd"), }));

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new SimpleErrorHandler());

		Document document = builder.parse(new InputSource(
				"src/test/resources/sets/tttVariants/badTTT_assertSignInvalidElement.xml"));
	}

	/*
	 * With only the security, we fail to check anything in the assertion
	 */
	@Ignore
	@Test
	public void schemaValidationTest5() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		factory.setSchema(schemaFactory.newSchema(new Source[] { new StreamSource(
				"src/main/resources/schemas/oasis-200401-wss-wssecurity-secext-1.0.xsd"), }));

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new SimpleErrorHandler());

		Document document = builder.parse(new InputSource(
				"src/test/resources/sets/tttVariants/badTTT_multipleInvalidElement.xml"));
	}

	/*
	 * Seems to be able to check everything
	 */
	@Ignore
	@Test
	public void schemaValidationTest6() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		factory.setSchema(schemaFactory.newSchema(new Source[] {
				new StreamSource("src/main/resources/schemas/oasis-200401-wss-wssecurity-secext-1.0.xsd"),
				new StreamSource("src/main/resources/schemas/saml-schema-assertion-2.0.xsd"), }));

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new SimpleErrorHandler());

		Document document = builder.parse(new InputSource(
				"src/test/resources/sets/tttVariants/badTTT_multipleInvalidElement.xml"));
	}

	/*
	 * Test draft schema for xspa found on mailing list
	 */
	@Ignore
	@Test
	public void schemaVal2() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		factory.setSchema(schemaFactory.newSchema(new Source[] { new StreamSource(
				"src/main/resources/schemas/xspa-for-healthcare-draft.xsd"), }));

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new SimpleErrorHandler());

		Document document = builder.parse(new InputSource("src/test/resources/sets/tttVariants/badTTT.xml"));
	}

	public class SimpleErrorHandler implements ErrorHandler {
		public void warning(SAXParseException e) throws SAXException {
			System.out.println(e.getMessage());
		}

		public void error(SAXParseException e) throws SAXException {
			System.out.println(e.getMessage());
		}

		public void fatalError(SAXParseException e) throws SAXException {
			System.out.println(e.getMessage());
		}
	}
}
