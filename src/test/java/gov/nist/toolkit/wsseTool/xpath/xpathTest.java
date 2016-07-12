package gov.nist.toolkit.wsseTool.xpath;

import gov.nist.hit.ds.wsseTool.api.config.ContextFactory;
import gov.nist.hit.ds.wsseTool.api.config.GenContext;
import gov.nist.hit.ds.wsseTool.api.config.KeystoreAccess;
import gov.nist.hit.ds.wsseTool.generation.opensaml.OpenSamlWsseSecurityGenerator;
import gov.nist.hit.ds.wsseTool.parsing.Message;
import gov.nist.hit.ds.wsseTool.parsing.MessageFactory;
import gov.nist.hit.ds.wsseTool.signature.api.AltVerifier.SignatureNamespaceContext;
import gov.nist.hit.ds.wsseTool.util.MyXmlUtils;
import gov.nist.toolkit.wsseTool.BaseTest;

import java.security.KeyStoreException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class xpathTest extends BaseTest {

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
	 * Make sure we can sign an element. We use an empty element
	 */
	@Ignore
	@Test
	public void getSignature() throws Exception {
		OpenSamlWsseSecurityGenerator wsse = new OpenSamlWsseSecurityGenerator();
		Document doc = wsse.generateWsseHeader(context);

		log.debug("header to validate : \n {}", MyXmlUtils.DomToString(doc));

		Element header = doc.getDocumentElement();

		Message message = MessageFactory.getMessage(header, context);

		// Get the Signature node.
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(message.getNamespaces());
		
		//Get a signature at the top level.
		NodeList signatureNode = (NodeList) xpath.evaluate("ds:Signature", header, XPathConstants.NODESET);
		
		assert signatureNode.getLength() == 1;

		//get the timestamp when properly prefixed
		NodeList timestamp = (NodeList) xpath.evaluate("wsu:Timestamp", header, XPathConstants.NODESET);

		assert timestamp.getLength() == 1;

		//cannot get the timestamp when not properly prefixed
		NodeList timestamp0 = (NodeList) xpath.evaluate("Timestamp", header, XPathConstants.NODESET);

		assert timestamp0.getLength() == 0;
		
		//find by selecting an attribute
		NodeList assertion = (NodeList) xpath.evaluate("*[@ID='a956b920-4956-47c6-8a05-8a3a56e418a0']", header,
				XPathConstants.NODESET);

		assert assertion.getLength() == 1;
		assert assertion.item(0).getLocalName().equals("Assertion");
		
		NodeList sign2 = (NodeList) xpath.evaluate("//ds:Signature", header, XPathConstants.NODESET);

		assert sign2.getLength() == 2;
		
		//find ID but not wsu:ID
		NodeList a = (NodeList) xpath.evaluate("*[@ID=*]", header,
				XPathConstants.NODESET);

		assert a.getLength() == 1;
		assert a.item(0).getLocalName().equals("Assertion");
		
		//find only wsu:ID
		NodeList t = (NodeList) xpath.evaluate("*[@wsu:ID=*]", header,
				XPathConstants.NODESET);

		assert t.getLength() == 1;
		assert t.item(0).getLocalName().equals("Timestamp");
		
		//Get a signature at a lower level.
		NodeList aSign = (NodeList) xpath.evaluate("saml2:Assertion/ds:Signature", header, XPathConstants.NODESET);
		
		assert aSign.getLength() == 1;
	}
}
