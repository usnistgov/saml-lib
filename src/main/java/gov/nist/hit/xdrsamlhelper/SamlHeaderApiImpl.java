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
import gov.nist.hit.ds.wsseTool.validation.ValidationResult;
import gov.nist.hit.ds.wsseTool.validation.WsseHeaderValidator;
import gov.nist.hit.ds.wsseTool.validation.reporting.AdditionalResult;
import gov.nist.hit.xdrsamlhelper.SamlHeaderApi.SamlHeaderException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.util.XMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SamlHeaderApiImpl extends SamlHeaderApi {
	private static final Logger log = LoggerFactory.getLogger(WsseHeaderValidator.class);

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



	public SamlHeaderValidationResults validate(Document document, String patientId, InputStream is, String alias, String keyStorePass,
			String privateKeyPass) throws SamlHeaderException {
		GenContext context = ContextFactory.getInstance();
		SamlHeaderValidationResults res = new SamlHeaderValidationResults();
		try {
            context.setKeystore(new KeystoreAccess(is, keyStorePass, alias, privateKeyPass));
			context.setParam("patientId", patientId);
			WsseHeaderValidator validator = new WsseHeaderValidator();
			
			ValidationResult r = validator.validateWithResults(document.getDocumentElement(), context);
			
			SamlHeaderApiUtils.convertResultsObject(r, res);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new SamlHeaderExceptionImpl(
					e instanceof ValidationException ? (ValidationException) e : new ValidationException(e));
		}
		
		return res;
	}
	
	public SamlHeaderValidationResults validate(String document, String patientId, InputStream is, String alias, String keyStorePass,
			String privateKeyPass) throws SamlHeaderException {
		GenContext context = ContextFactory.getInstance();
		SamlHeaderValidationResults res = new SamlHeaderValidationResults();
		try {
            Document doc = SamlHeaderApiUtils.stringToDom(document);
            context.setKeystore(new KeystoreAccess(is, keyStorePass, alias, privateKeyPass));
			context.setParam("patientId", patientId);
			WsseHeaderValidator validator = new WsseHeaderValidator();
			
			ValidationResult r = validator.validateWithResults(doc.getDocumentElement(), context);
			
			SamlHeaderApiUtils.convertResultsObject(r, res);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new SamlHeaderExceptionImpl(
					e instanceof ValidationException ? (ValidationException) e : new ValidationException(e));
		}
		
		return res;
	}

	
	
	public SamlHeaderException generateExceptionWrapper(String s, Exception e, boolean isValidation) {
		return new SamlHeaderExceptionImpl(isValidation ? (ValidationException) e : new ValidationException(e));
	}

	public Document generateD(String patientId, InputStream is, String alias, String keyStorePass, String privateKeyPass)
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
		
		return doc;

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
		
		String s = MyXmlUtils.DomToString(doc);
		System.out.println("----------------------------------------------------------------------------");
		try {
			System.in.read();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("START" + s +"END");
		try {
			System.out.println("----------------------------------------------------------------------------");
						System.out.println("START" +  MyXmlUtils.DomToString(SamlHeaderApiUtils.stringToDom(s)) +"END");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.in.read();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return MyXmlUtils.DomToString(doc);

	}

	public static void main(String[] args) throws SamlHeaderException, IOException, IllegalArgumentException, IllegalAccessException {
		// test1();
		System.out.println("----------------- building directly --------");
		System.in.read();
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

	public static void printobj(Document someObject) throws IllegalArgumentException, IllegalAccessException {
		for (Field field : someObject.getClass().getDeclaredFields()) {
		    field.setAccessible(true); // You might want to set modifier to public first.
		    Object value = field.get(someObject); 
		        System.out.println(field.getName() + "=" + (value == null ? "null" : value));
		}
	}
	public static void test2() throws SamlHeaderException, IOException, IllegalArgumentException, IllegalAccessException {
		String s = "";
		SamlHeaderApiImpl saml = (SamlHeaderApiImpl) SamlHeaderApi.getInstance();
		FileInputStream is = null, is2 = null;
		try {
			is = new FileInputStream("src/test/resources/keystore/keystore");
			is2 = new FileInputStream("src/test/resources/keystore/keystore");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Read file src/test/resources/keystore/keystore  " + (is != null));

		Document d = null, d1 = null;
		System.out.println("This is the header: \n" + (d= saml.generateD("abcd", is,
				"hit-testing.nist.gov", "changeit", "changeit")));
        
		 PrintWriter writer1 = new PrintWriter("c:\\temp\\dom1.txt", "UTF-8");		
		 PrintWriter writer2 = new PrintWriter("c:\\temp\\dom2.txt", "UTF-8");		
		writer1.println(Dumper.dump(d));
		try {
			d1 = SamlHeaderApiUtils.stringToDom(s = (MyXmlUtils.DomToString(d)));
			writer2.println(Dumper.dump(d1));
			writer1.close();
			writer2.close();
			//d1.setDocumentURI(d.getDocumentURI());
			//d1.setPrefix(d.getPrefix());
			//d1.setXmlVersion(d.getXmlVersion());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String a = MyXmlUtils.DomToString(d);
		String a1 = MyXmlUtils.DomToString(d1);
		
		System.out.println("Value of comparison = " + s.compareTo(a1));
		System.in.read();
		
		SamlHeaderValidationResults res = saml.validate(d1, "abcd", is2, "hit-testing.nist.gov", "changeit", "changeit");
		
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
