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

import java.io.StringReader;
import java.security.KeyStoreException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SamlHeaderApiImpl extends SamlHeaderApi {
	public class SamlHeaderExceptionImpl extends SamlHeaderException {
		SamlHeaderExceptionImpl(ValidationException e) {
			super(e);
			isValidation = true;
		}
		SamlHeaderExceptionImpl(GenerationException e) {
			super(e);
			isGeneration = true;
		}
		
		private boolean isGeneration = false;
		private boolean isValidation = false;
		
        public  boolean isGenerationException() { return isGeneration; }
        public  boolean isValidationException() { return isValidation; }
		
		public String getHeaderExceptionMessage() { return getMessage(); }
		/* future */
		public String getHeaderExceptionMessage(String type)  { return getMessage(); }

	}

	private static boolean skipNL;
	private static String printXML(Node rootNode, String tab) {
	    String print = "";
	    if(rootNode.getNodeType()==Node.ELEMENT_NODE) {
	        print += "\n"+tab+"<"+rootNode.getNodeName()+">";
	    }
	    NodeList nl = rootNode.getChildNodes();
	    if(nl.getLength()>0) {
	        for (int i = 0; i < nl.getLength(); i++) {
	            print += printXML(nl.item(i), tab+"  ");    // \t
	        }
	    } else {
	        if(rootNode.getNodeValue()!=null) {
	            print = rootNode.getNodeValue();
	        }
	        skipNL = true;
	    }
	    if(rootNode.getNodeType()==Node.ELEMENT_NODE) {
	        if(!skipNL) {
	            print += "\n"+tab;
	        }
	        skipNL = false;
	        print += "</"+rootNode.getNodeName()+">";
	    }
	    return(print);
	}
	public void validate(String document, String patientId, String keystoreFileWithPath, String alias, String keyStorePass, String privateKeyPass) throws SamlHeaderException {
		GenContext context = ContextFactory.getInstance();
		try {
			 Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
			            .parse(new InputSource(new StringReader(document)));
			
			 System.out.println(printXML(doc, "\t"));
			 //System.in.read();
			context.setKeystore(new KeystoreAccess(keystoreFileWithPath, keyStorePass, alias, privateKeyPass));
			context.setParam("patientId", patientId);
			new WsseHeaderValidator().validate(doc.getDocumentElement(),context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new SamlHeaderExceptionImpl(e instanceof ValidationException ? (ValidationException)e : new ValidationException(e));
		}
		
	}
	
	public String generate(String patientId, String keystoreFileWithPath, String alias, String keyStorePass, String privateKeyPass) throws SamlHeaderException {
		GenContext context = ContextFactory.getInstance();
		Document doc = null;
		
		try {
			context.setKeystore(new KeystoreAccess(keystoreFileWithPath, keyStorePass, alias, privateKeyPass));
			context.setParam("patientId", patientId);
			doc = new OpenSamlWsseSecurityGenerator().generateWsseHeader(context);
			//new WsseHeaderValidator().validate(doc.getDocumentElement(),context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new SamlHeaderExceptionImpl(e instanceof GenerationException ? (GenerationException)e : new GenerationException(e));
		}
		return MyXmlUtils.DomToString(doc);
		
	}
	
	public static void main(String [] args) throws SamlHeaderException {
		String s = "";
		SamlHeaderApi saml = SamlHeaderApi.getInstance();
		System.out.println(s = saml.generate("abcd", "src/test/resources/keystore/keystore", "hit-testing.nist.gov", "changeit", "changeit"));
		saml.validate(s, "abcd", "src/test/resources/keystore/keystore", "hit-testing.nist.gov", "changeit", "changeit");
	}

}
