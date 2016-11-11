package gov.nist.hit.xdrsamlhelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import gov.nist.hit.ds.wsseTool.api.config.Context;
import gov.nist.hit.ds.wsseTool.api.config.GenContext;
import gov.nist.hit.ds.wsseTool.api.exceptions.GenerationException;
import gov.nist.hit.ds.wsseTool.generation.opensaml.OpenSamlFacade;
import gov.nist.hit.ds.wsseTool.namespace.dom.NhwinNamespaceContextFactory;
import gov.nist.hit.ds.wsseTool.parsing.opensaml.OpenSamlSecurityHeader;
import gov.nist.hit.ds.wsseTool.validation.ValidationResult;
import gov.nist.hit.ds.wsseTool.validation.WsseHeaderValidator;
import gov.nist.hit.ds.wsseTool.validation.reporting.AdditionalResult;
import gov.nist.hit.xdrsamlhelper.SamlHeaderApi.SamlHeaderValidationResults;

public class SamlHeaderApiUtils {
	private static final Logger log = LoggerFactory.getLogger(WsseHeaderValidator.class);	
	private static boolean skipNL;

	private static String printXML(Node rootNode, String tab) {
		String print = "";
		if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
			print += "\n" + tab + "<" + rootNode.getNodeName() + ">";
		}
		NodeList nl = rootNode.getChildNodes();
		if (nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				print += printXML(nl.item(i), tab + "  "); // \t
			}
		} else {
			if (rootNode.getNodeValue() != null) {
				print = rootNode.getNodeValue();
			}
			skipNL = true;
		}
		if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
			if (!skipNL) {
				print += "\n" + tab;
			}
			skipNL = false;
			print += "</" + rootNode.getNodeName() + ">";
		}
		return (print);
	}

	/**
	 * Constructing the SAML or XACML Objects from a String
	 *
	 * @param xmlString Decoded SAML or XACML String
	 * @return SAML or XACML Object
	 * @throws EntitlementProxyException
	 */
	private static XMLObject unmarshall(String xmlString) throws Exception {

	    try {
	        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	        documentBuilderFactory.setNamespaceAware(true);

	        documentBuilderFactory.setExpandEntityReferences(false);
	        //documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	        SecurityManager securityManager = new SecurityManager();
	        //securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
	        //documentBuilderFactory.setAttribute(SECURITY_MANAGER_PROPERTY, securityManager);

	        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
	        //docBuilder.setEntityResolver(new CarbonEntityResolver());
	        Document document = docBuilder.parse(new ByteArrayInputStream(xmlString.trim().getBytes(Charset.forName
	                ("UTF-8"))));
	        Element element = document.getDocumentElement();
	        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
	        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
	        return unmarshaller.unmarshall(element);
	    } catch (Exception e) {
	        log.error("Error in constructing XML(SAML or XACML) Object from the encoded String", e);
	        throw new Exception (
	                "Error in constructing XML(SAML or XACML) from the encoded String", e);
	    }
	}
	
	public static Document marshallAsNewDOMDocument(XMLObject object) throws IOException, MarshallingException, TransformerException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		factory.setNamespaceAware(true);
		builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Marshaller out = Configuration.getMarshallerFactory().getMarshaller(object);
		out.marshall(object, document);
		return document;
	}

	public static Document stringToDom2(String xmlSource) throws Exception {
		return marshallAsNewDOMDocument(unmarshall(xmlSource));
	}
	

	public static Document stringToDom(String xmlSource) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setNamespaceAware(true);
		
		return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlSource)));
	}

	
	private String templateFile = "templates/basic_template_unsigned.xml";


	// contains declaration of all namespaces
	private static NamespaceContext namespaces = NhwinNamespaceContextFactory.getDefaultContext();

	private Context context;

	public static Document stringToDom3(String xmlSource) throws GenerationException {

		
		System.setProperty("javax.xml.xpath.XPathFactory","net.sf.saxon.xpath.XPathFactoryImpl");
		
		Document securityHeader; // the DOM document we build
		OpenSamlSecurityHeader sec; // the XMLObject representation of this
									// document. Provides methods that
									// facilitates
									// document creation

		try {
			
			


			OpenSamlFacade saml = new OpenSamlFacade();
			xmlSource +=  "<!-- xyz -->";
			//log.info("unmarshall DOM template to XMLObject representation", templateFile);
			//InputStream is = new ByteArrayInputStream(xmlSource.getBytes(StandardCharsets.UTF_8));
			InputStream is = new ByteArrayInputStream(xmlSource.getBytes(StandardCharsets.UTF_8) );
			//InputStream is = new StringSource(xmlSource);
			sec = saml.createSecurityFromTemplate(is);

			log.info("generate time values");
			//generateTime();

			log.info("update message timestamp");
			//updateTimestamp(sec);

			log.info("marshall back to DOM");
			securityHeader = saml.marshallAsNewDOMDocument(sec.getSecurity());

			log.info("update message template");
			//updateTemplate(securityHeader);

			log.info("sign message");
			//signWsseHeader(sec, securityHeader, context.getKeystore());

			log.info("wsse header successfully generated");

		} catch (Exception e) {
			// we can't do nothing interesting so we return a standard failure
			throw new GenerationException(e);
		}

		return securityHeader;
	}

	
	public static void convertResultsObject(ValidationResult r, SamlHeaderValidationResults res){
		
		Result result = r.getBasicResult();
		AdditionalResult moreResultInfo = r.getAddResult();
		
		long time = result.getRunTime();
		int runs = result.getRunCount();
		int fails = result.getFailureCount();
		List<Failure> failures = result.getFailures();
		List<Description> descriptions = moreResultInfo.testsDescriptions;
		List<Failure> optionalTestsNotRun = moreResultInfo.optionalTestsNotRun;

		String s;
		log.info(s = "\n Summary: \n"+ runs + " tests runs in " + time + " milliseconds , "
				+ fails + " have failed, " +
				moreResultInfo.optionalTestsNotRun.size() + " optional tests not triggered, " +
				result.getIgnoreCount() + " ignored \n");

		res.details.add(s);
		log.info("\n Tests run: \n");
		for (Description d : descriptions) {
			log.info(d.getDisplayName() +"\n");
		}

		if(optionalTestsNotRun.size() != 0 ){
			log.info("\n Optional tests not triggered: \n");
			for (Failure f : optionalTestsNotRun) {
				log.warn(s = f.getTestHeader() + " : \n" + f.getMessage() +"\n");
				res.warnings.add(s);
			}
		}

		if(fails != 0 ){
			log.info("\n Failures recorded: \n");
			for (Failure f : failures) {
				log.error(s = f.getTestHeader() + " : \n" + f.getMessage() + "\n");
				res.errors.add(s);
			}
		}
	}
	

}
