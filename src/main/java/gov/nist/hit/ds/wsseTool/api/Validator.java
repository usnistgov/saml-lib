package gov.nist.hit.ds.wsseTool.api;

import gov.nist.hit.ds.wsseTool.api.config.Context;
import gov.nist.hit.ds.wsseTool.api.exceptions.ValidationException;

import org.w3c.dom.Element;

public interface Validator {

	/**
	 * Former way to transparently access the validation service.
	 * It should be replaced by an interface allowing the pooling of the result.
	 * @param securityHeader : a DOM representation of the security header
	 * @param context : all context information needed to perform the validation
	 * @throws ValidationException if sth went wrong.
	 */
	public void validate(Element securityHeader, Context context) throws ValidationException;

}
