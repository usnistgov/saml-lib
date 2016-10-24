package gov.nist.hit.xdrsamlhelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;

/**
 * For validating and generating saml headers used by the Xdstoolkit
 * @version 1.0
 *
 */

public abstract class SamlHeaderApi {
	public static SamlHeaderApi getInstance() { return new SamlHeaderApiImpl(); }
	public void validate(String document, String patientId, String keystoreFileWithPath, 
			String alias, String keyStorePass, String privateKeyPass) throws SamlHeaderException {
		try {
			FileInputStream is = new FileInputStream(keystoreFileWithPath);
			validate(document, patientId, is, alias, keyStorePass, privateKeyPass);
		} catch (FileNotFoundException e) {
			throw generateExceptionWrapper("cannot properly access keystore located at : " + keystoreFileWithPath, e, true);
		}

	}

	public abstract SamlHeaderValidationResults validate(String document, String patientId, InputStream is, 
			String alias, String keyStorePass, String privateKeyPass) throws SamlHeaderException;
	
	public String generate(String patientId, String keystoreFileWithPath, String alias, String keyStorePass, String privateKeyPass) throws SamlHeaderException {
		try {
			FileInputStream is = new FileInputStream(keystoreFileWithPath);
			return generate(patientId, is, alias, keyStorePass, privateKeyPass);
		} catch (FileNotFoundException e) {
			throw generateExceptionWrapper("cannot properly access keystore located at : " + keystoreFileWithPath, e, true);
		}
	}
	public abstract String generate(String patientId, InputStream is, String alias, String keyStorePass, String privateKeyPass) throws SamlHeaderException;

	public abstract SamlHeaderException generateExceptionWrapper(String s, Exception e, boolean isValidation);
		
	/* Exception - for 1.0 just a wrapper around the java.lang.Exception */
	public abstract class SamlHeaderException extends Exception {
		public SamlHeaderException(Exception e) {
			super(e);
		}
        public abstract boolean isGenerationException();
        public abstract boolean isValidationException();

		public abstract String getHeaderExceptionMessage();
		/* future */
		public abstract String getHeaderExceptionMessage(String type);
	}

	public class SamlHeaderValidationResults {
		List<String> errors;
		List<String> warnings;
		List<String> details;
		
		public SamlHeaderValidationResults() {
			errors = new ArrayList<String>();
			warnings = new ArrayList<String>();
			details = new ArrayList<String>();
		}
		/* more to come */
	}
	

}
