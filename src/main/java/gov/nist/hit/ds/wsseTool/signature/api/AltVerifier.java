package gov.nist.hit.ds.wsseTool.signature.api;

import gov.nist.hit.ds.wsseTool.parsing.Message;

import java.security.Key;
import java.security.KeyException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AltVerifier {
	
	private static Logger log = LoggerFactory.getLogger(AltVerifier.class);

	public boolean validateAssertionSignature(Message message) throws Exception {

		Element header = message.getDomHeader();

		// Get the Signature node.
		System.setProperty("javax.xml.xpath.XPathFactory","net.sf.saxon.xpath.XPathFactoryImpl");
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();
		xpath.setNamespaceContext(message.getNamespaces());

        String sigUri = "http://www.w3.org/2000/09/xmldsig#";
      //  String sigUri = message.getDefaultNamespaces().getNamespaceURI("ds");
        String prefix = message.getNamespaces().getPrefix(sigUri);

        String samlUri = "urn:oasis:names:tc:SAML:2.0:assertion";

        //Get the assertion
        Node a = (Node) xpath.evaluate("*[local-name()='Assertion']" , header, XPathConstants.NODE);

        // Note : you are supposed to be able to do things like this, but it does not work so far!
        // //*[local-name()="Signature" and namespace-uri()='$sigUri']/*[local-name()="Signature"]/text()


        Node s = (Node) xpath.evaluate("*[local-name()='Signature']" , a, XPathConstants.NODE);

		//we expect only one keyInfo
		Node keyInfoNode = (Node) xpath.evaluate("*[local-name()='KeyInfo']", s, XPathConstants.NODE);

		//Kept for documentation
		// // Only if we use X509 certs
		// DOMValidateContext valContext = new DOMValidateContext(new
		// X509KeySelector(), s);

		 verifyWithKeyInfo(keyInfoNode, s);
		 //if no exception, validation was successful
		 return true;
	}

	public boolean verifyTimestampSignature(Message message) throws Exception {
		
		System.setProperty("javax.xml.xpath.XPathFactory","net.sf.saxon.xpath.XPathFactoryImpl");
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(message.getDefaultNamespaces());

        Element header = message.getDomHeader();

		// get timestamp signature
        Node s = (Node) xpath.evaluate("*[local-name()='Signature']" , header, XPathConstants.NODE);

        // collect all subject confirmations of type holder-of-key
        Node subject = (Node) xpath.evaluate("*[local-name()='Assertion']/*[local-name()='Subject']" , header, XPathConstants.NODE);

//		NodeList confirmations = (NodeList) xpath.evaluate(
//				"//saml2:SubjectConfirmation[@Method='urn:oasis:names:tc:SAML:2.0:cm:holder-of-key']", subject,
//				XPathConstants.NODESET);

        NodeList confirmations = (NodeList) xpath.evaluate(
                "*[local-name()='SubjectConfirmation' and @Method='urn:oasis:names:tc:SAML:2.0:cm:holder-of-key']", subject,
                XPathConstants.NODESET);


        String exceptionMsg = "";

		// try to validate for any keyInfos until we find a good match
		for (int i = 0; i < confirmations.getLength(); i++) {
            Node data = (Node) xpath
                    .evaluate("*[local-name()='SubjectConfirmationData']", confirmations.item(i), XPathConstants.NODE);

			NodeList keyInfos = (NodeList) xpath
					.evaluate("*[local-name()='KeyInfo']", data, XPathConstants.NODESET);
			for (int j = 0; j < keyInfos.getLength(); j++) {

				try {
					verifyWithKeyInfo(keyInfos.item(i), s);
					return true;
				} catch (XMLSignatureException e) {
                    exceptionMsg += e.getMessage();
					log.info(e.getMessage());
				}
			}
		}

        throw new Exception(exceptionMsg);

	}

	private void verifyWithKeyInfo(Node keyInfoNode, Node s) throws MarshalException, KeyException,
			XMLSignatureException {
		KeyInfoFactory keyInfoFac = KeyInfoFactory.getInstance("DOM");

		DOMStructure keyInfoElt = new DOMStructure(keyInfoNode);
		javax.xml.crypto.dsig.keyinfo.KeyInfo keyInfo = keyInfoFac.unmarshalKeyInfo(keyInfoElt);
		javax.xml.crypto.dsig.keyinfo.KeyValue keyValue = (javax.xml.crypto.dsig.keyinfo.KeyValue) keyInfo.getContent()
				.get(0);
		PublicKey key = keyValue.getPublicKey();

		DOMValidateContext valContext2 = new DOMValidateContext(KeySelector.singletonKeySelector(key), s);

		// Create an signature factory and unmarshal the signature from XML.
		XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
		XMLSignature signature = factory.unmarshalXMLSignature(valContext2);

		// Validate the signature.
		boolean coreValidity = signature.validate(valContext2);

		if (!coreValidity){
			// log and throw if not valid.

			boolean sv = signature.getSignatureValue().validate(valContext2);

			String msg = "signature validation status: " + sv;

			Iterator i = signature.getSignedInfo().getReferences().iterator();
			for (int j = 0; i.hasNext(); j++) {
				Reference r = (Reference) i.next();
				boolean refValid = r.validate(valContext2);
				msg += "Reference with ID " + r.getURI() + " in position" + j + " has validity status: " + refValid
						+ "\n";
				msg += "Calculated digest: " + r.getCalculatedDigestValue() + "\n";
				msg += "Digest value: " + r.getDigestValue() + "\n";
				msg += "Transforms performed: \n";
				for (Object t : r.getTransforms()) {
					msg += ((Transform) t).getAlgorithm() + "\n";
				}
			}

			throw new XMLSignatureException(msg);
		}
	}

	public class SignatureNamespaceContext implements NamespaceContext {
		public String getNamespaceURI(String prefix) {
			if (prefix.equals("ds"))
				return XMLSignature.XMLNS;
			else
				return XMLConstants.NULL_NS_URI;
		}

		public String getPrefix(String namespace) {
			if (namespace.equals(XMLSignature.XMLNS))
				return "ds";
			else
				return null;
		}

		public Iterator getPrefixes(String namespace) {
			return null;
		}
	}

	public static class X509KeySelector extends KeySelector {
		public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method,
				XMLCryptoContext context) throws KeySelectorException {
			Iterator ki = keyInfo.getContent().iterator();
			while (ki.hasNext()) {
				XMLStructure info = (XMLStructure) ki.next();
				if (!(info instanceof X509Data))
					continue;
				X509Data x509Data = (X509Data) info;
				Iterator xi = x509Data.getContent().iterator();
				while (xi.hasNext()) {
					Object o = xi.next();
					if (!(o instanceof X509Certificate))
						continue;
					final PublicKey key = ((X509Certificate) o).getPublicKey();

					if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
						return new KeySelectorResult() {
							public Key getKey() {
								return key;
							}
						};
					}
				}
			}
			throw new KeySelectorException("No key found!");
		}

		static boolean algEquals(String algURI, String algName) {
			if ((algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1))
					|| (algName.equalsIgnoreCase("RSA") && algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1))) {
				return true;
			} else {
				return false;
			}
		}
	}
}
