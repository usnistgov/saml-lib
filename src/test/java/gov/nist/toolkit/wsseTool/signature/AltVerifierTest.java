package gov.nist.toolkit.wsseTool.signature;


import gov.nist.hit.ds.wsseTool.api.config.ContextFactory;
import gov.nist.hit.ds.wsseTool.api.config.GenContext;
import gov.nist.hit.ds.wsseTool.api.config.KeystoreAccess;
import gov.nist.hit.ds.wsseTool.generation.opensaml.OpenSamlWsseSecurityGenerator;
import gov.nist.hit.ds.wsseTool.parsing.Message;
import gov.nist.hit.ds.wsseTool.parsing.MessageFactory;
import gov.nist.hit.ds.wsseTool.signature.api.AltVerifier;
import gov.nist.hit.ds.wsseTool.util.MyXmlUtils;
import gov.nist.toolkit.wsseTool.BaseTest;

import java.security.KeyStoreException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AltVerifierTest extends BaseTest {
	
		GenContext context;
		
		private static Logger log = LoggerFactory.getLogger("");

		@Before
		public void loadKeystore() throws KeyStoreException {
			String store = "src/test/resources/keystore/keystore";
			String sPass = "changeit";
			String alias = "hit-testing.nist.gov";
			String kPass = "changeit";
			context = ContextFactory.getInstance();
			context.setKeystore(new KeystoreAccess(store,sPass,alias,kPass));
			context.getParams().put("patientId", "D123401^^^&1.1&ISO");
		}

		/*
		 * Make sure we can sign an element. We use an empty element
		 */
		@Test
		public void testAssertionSignatureWithDOM() throws Exception{
			OpenSamlWsseSecurityGenerator wsse = new OpenSamlWsseSecurityGenerator();
			Document doc = wsse.generateWsseHeader(context);
			
			log.debug("header to validate : \n {}", MyXmlUtils.DomToString(doc) );
			
			Element header = doc.getDocumentElement();

			Message message = MessageFactory.getMessage(header, context);

			// Get the Signature node.
			System.setProperty("javax.xml.xpath.XPathFactory","net.sf.saxon.xpath.XPathFactoryImpl");
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(message.getNamespaces());
			
			new AltVerifier().validateAssertionSignature(message);
		}
}
