package gov.nist.hit.xdrsamlhelper;

import gov.nist.hit.ds.wsseTool.api.WsseHeaderGenerator;
import gov.nist.hit.ds.wsseTool.api.config.Context;
import gov.nist.hit.ds.wsseTool.api.config.ContextFactory;
import gov.nist.hit.ds.wsseTool.api.config.GenContext;
import gov.nist.hit.ds.wsseTool.api.config.KeystoreAccess;
import gov.nist.hit.ds.wsseTool.api.exceptions.GenerationException;
import gov.nist.hit.ds.wsseTool.api.exceptions.ValidationException;
import gov.nist.hit.ds.wsseTool.generation.opensaml.OpenSamlWsseSecurityGenerator;
import gov.nist.hit.ds.wsseTool.util.MyXmlUtils;
import gov.nist.hit.ds.wsseTool.validation.WsseHeaderValidator;
import gov.nist.hit.xdrsamlhelper.SamlHeaderApi.SamlHeaderException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SamlHeaderApiImpl extends SamlHeaderApi {
	public class SamlHeaderExceptionImpl extends SamlHeaderException {
		protected SamlHeaderExceptionImpl(ValidationException e) {
			super(e);
			isValidation = true;
		}

		protected SamlHeaderExceptionImpl(GenerationException e) {
			super(e);
			isGeneration = true;
		}

		private boolean isGeneration = false;
		private boolean isValidation = false;

		public boolean isGenerationException() {
			return isGeneration;
		}

		public boolean isValidationException() {
			return isValidation;
		}

		public String getHeaderExceptionMessage() {
			return getMessage();
		}

		/* future */
		public String getHeaderExceptionMessage(String type) {
			return getMessage();
		}

	}

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

	public static Document stringToDom(String xmlSource) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlSource)));
	}
	
	
	public/* static */class AppenderForErrorRecorder extends AppenderSkeleton {
		
		SamlHeaderValidationResults results;
		
		public AppenderForErrorRecorder(){
			results = new SamlHeaderValidationResults();
		}
		
		public SamlHeaderValidationResults getResults() {
			return results;
		}
		
		@Override
		protected void append(LoggingEvent event) {
			
			if (event.getLevel() == Level.ERROR) {
				results.errors.add(/*XdsErrorCode.Code.NoCode,*/event.getRenderedMessage()/*,event.getLevel().toString(),event.getLoggerName()*/);
			}
			if (event.getLevel() == Level.WARN) {
				results.warnings.add(event.getRenderedMessage());
			}
			if (event.getLevel() == Level.INFO) {
				results.details.add(event.getRenderedMessage());
			}
		}

		public void close() {
		}

		public boolean requiresLayout() {
			return false;
		}

	}

	public SamlHeaderValidationResults validate(String document, String patientId, InputStream is, String alias, String keyStorePass,
			String privateKeyPass) throws SamlHeaderException {
		GenContext context = ContextFactory.getInstance();
		AppenderForErrorRecorder er = new AppenderForErrorRecorder();
		
		try {
            Document doc = stringToDom(document);
        	org.apache.log4j.Logger logMainVal = org.apache.log4j.Logger
					.getLogger(WsseHeaderValidator.class);
        	logMainVal.addAppender(er);
			//System.out.println(printXML(doc, "\t"));
			// System.in.read();
			context.setKeystore(new KeystoreAccess(is, keyStorePass, alias, privateKeyPass));
			context.setParam("patientId", patientId);
			new WsseHeaderValidator().validate(doc.getDocumentElement(), context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new SamlHeaderExceptionImpl(
					e instanceof ValidationException ? (ValidationException) e : new ValidationException(e));
		}
		
		return er.getResults();
	}

	
	
	public SamlHeaderException generateExceptionWrapper(String s, Exception e, boolean isValidation) {
		return new SamlHeaderExceptionImpl(isValidation ? (ValidationException) e : new ValidationException(e));
	}

	public String generate(String patientId, InputStream is, String alias, String keyStorePass, String privateKeyPass)
			throws SamlHeaderException {
		GenContext context = ContextFactory.getInstance();
		Document doc = null;

		try {
			context.setKeystore(new KeystoreAccess(is, keyStorePass, alias, privateKeyPass));
			context.setParam("patientId", patientId);
			doc = new OpenSamlWsseSecurityGenerator().generateWsseHeader(context);
			// new
			// WsseHeaderValidator().validate(doc.getDocumentElement(),context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new SamlHeaderExceptionImpl(
					e instanceof GenerationException ? (GenerationException) e : new GenerationException(e));
		}
		return MyXmlUtils.DomToString(doc);

	}

	public static void main(String[] args) throws SamlHeaderException {
		// test1();
		test2();
	}

	public static void test1() throws SamlHeaderException {
		String s = "";
		SamlHeaderApi saml = SamlHeaderApi.getInstance();
		System.out.println(s = saml.generate("abcd", "src/test/resources/keystore/keystore", "hit-testing.nist.gov",
				"changeit", "changeit"));
		saml.validate(s, "abcd", "src/test/resources/keystore/keystore", "hit-testing.nist.gov", "changeit",
				"changeit");
	}

	public static void test2() throws SamlHeaderException {
		String s = "";
		SamlHeaderApi saml = SamlHeaderApi.getInstance();
		FileInputStream is = null;
		try {
			is = new FileInputStream("src/test/resources/keystore/keystore");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Read file src/test/resources/keystore/keystore  " + (is != null));

		System.out.println("This is the header: \n" + (s = saml.generate("abcd", "src/test/resources/keystore/keystore",
				"hit-testing.nist.gov", "changeit", "changeit")));
		
		SamlHeaderValidationResults res = saml.validate(s, "abcd", is, "hit-testing.nist.gov", "changeit", "changeit");
		
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("Results ------------------------------------------------------------------------");
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("-----------------------------------------------------------------------------");


		System.out.println("Errors: " + res.errors.size());
		
		for (String e : res.errors) { System.out.println(e);}
		System.out.println("-----------------------------------------------------------------------------");

		System.out.println("Warnings: " + res.warnings.size());
		for (String w : res.warnings) { System.out.println(w);}
		System.out.println("-----------------------------------------------------------------------------");

		System.out.println("Info: " + res.details.size());
	
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("End Results ------------------------------------------------------------------------");
}

}
