package gov.nist.hit.xdrsamlhelper;

/**
 * For validating and generating saml headers used by the Xdstoolkit
 * @version 1.0
 *
 */

public abstract class SamlHeaderApi {
	public static SamlHeaderApi getInstance() { return new SamlHeaderApiImpl(); }
	public abstract void validate(String document, String patientId, String keystoreFileWithPath, 
			String alias, String keyStorePass, String privateKeyPass) throws SamlHeaderException;
	public abstract String generate(String patientId, String keystoreFileWithPath, String alias, String keyStorePass, String privateKeyPass) throws SamlHeaderException;

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
