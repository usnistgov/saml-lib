package gov.nist.hit.ds.wsseTool.validation

import gov.nist.hit.ds.wsseTool.validation.reporting.AdditionalResult
import org.junit.runner.Result

class ValidationResult {
	
	@Delegate
	Result basicResult
	
	@Delegate
	AdditionalResult addResult
	
	
	public ValidationResult(){}
	
	

}
