package gov.nist.hit.xdrsamlhelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStoreException;

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

	public abstract void validate(String document, String patientId, InputStream is, 
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
	
}
