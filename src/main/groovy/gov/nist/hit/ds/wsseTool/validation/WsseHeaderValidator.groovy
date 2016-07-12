package gov.nist.hit.ds.wsseTool.validation

import gov.nist.hit.ds.wsseTool.api.Validator
import gov.nist.hit.ds.wsseTool.api.WsseHeaderGenerator
import gov.nist.hit.ds.wsseTool.api.config.Context
import gov.nist.hit.ds.wsseTool.api.config.ContextFactory
import gov.nist.hit.ds.wsseTool.api.config.KeystoreAccess
import gov.nist.hit.ds.wsseTool.api.config.ValConfig
import gov.nist.hit.ds.wsseTool.api.exceptions.GenerationException
import gov.nist.hit.ds.wsseTool.api.exceptions.ValidationException
import gov.nist.hit.ds.wsseTool.parsing.Message
import gov.nist.hit.ds.wsseTool.parsing.MessageFactory
import gov.nist.hit.ds.wsseTool.parsing.ParseException
import gov.nist.hit.ds.wsseTool.parsing.GroovyHeaderParser
import gov.nist.hit.ds.wsseTool.validation.engine.ValRunnerBuilder
import gov.nist.hit.ds.wsseTool.validation.engine.ValRunnerWithOrder
import gov.nist.hit.ds.wsseTool.validation.engine.ValSuite
import gov.nist.hit.ds.wsseTool.validation.reporting.AdditionalResult;
import gov.nist.hit.ds.wsseTool.validation.reporting.TestListener;
import gov.nist.hit.ds.wsseTool.validation.reporting.TestReporter;
import gov.nist.hit.ds.wsseTool.validation.tests.run.*
import gov.nist.hit.ds.wsseTool.validation.engine.annotations.*
import gov.nist.hit.ds.wsseTool.namespace.dom.NhwinNamespaceContextFactory

import java.security.KeyStoreException
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.*
import org.junit.runner.Description
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.Result
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.junit.runners.model.RunnerBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList;
import org.w3c.dom.Node

/**
 * API of the validation module.
 *
 * @author gerardin
 *
 */
public class WsseHeaderValidator implements Validator {

	private static final Logger log = LoggerFactory.getLogger(WsseHeaderValidator.class)

	public static void main(String[] args) throws GenerationException, KeyStoreException {
		String store = "src/test/resources/keystore/keystore"
		String sPass = "changeit"
		String kPass = "changeit"
		String alias = "hit-testing.nist.gov"
		Context context = ContextFactory.getInstance()
		context.setKeystore(new KeystoreAccess(store,sPass,alias,kPass))
		context.getParams().put("patientId", "D123401^^^&1.1&ISO")
		Document doc = new WsseHeaderGenerator().generateWsseHeader(context)
		new WsseHeaderValidator().validate(doc.getDocumentElement(),context)
	}

	public WsseHeaderValidator(){
	}

	@Override
	public void validate(Element securityHeader, Context context) throws ValidationException {
		ValConfig config = new ValConfig("2.0")
		ValidationResult r = doValidation(securityHeader, config, context)
		TestReporter reporter = new TestReporter();
		reporter.report(r)
	}

	public ValidationResult validateAndReport(Element xml, ValConfig config, Context context) throws ValidationException {
		Element securityHeader = xml

		if(xml.localName != "Security"){
			log.warn("trying to extract wsse:Security element from message");
			securityHeader = getSecurityHeader(xml);
		}
		ValidationResult r = doValidation(securityHeader, config, context)
		TestReporter reporter = new TestReporter();
		reporter.report(r)
		return r;
	}

	private getSecurityHeader(Element xml){
		// Get the Signature node.
		XPathFactory xpf = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI,
				"net.sf.saxon.xpath.XPathFactoryImpl", ClassLoader.getSystemClassLoader());
		XPath xpath = xpf.newXPath();
		xpath.setNamespaceContext(NhwinNamespaceContextFactory.getDefaultContext())
		println NhwinNamespaceContextFactory.getDefaultContext().getNamespaceURI("wsse")
		//Get a signature at the top level.
		println xpath.getNamespaceContext().getNamespaceURI("wsse")
		Node security = (Node) xpath.evaluate("//wsse:Security", xml, XPathConstants.NODE);

		if(security == null){
			throw new ValidationException("$security security headers found in message")
		}

		return security
	}



	private ValidationResult doValidation(Element wsseHeader, ValConfig config, Context context){
		Message message = null;

		try{
			//create the message representation.
			message = MessageFactory.getMessage(wsseHeader,context)
		}
		catch(ParseException e){
			log.error(e.getMessage());
			log.warn("problem during parsing but validation will continue so we can report errors properly.");
		}

		RunnerBuilder builder = new ValRunnerBuilder(message)
		Runner runner = new ValSuite(CompleteTestSuite.class, builder)
		Request request = Request.runner(runner)
		run(request, config)


	}

	private ValidationResult run(Request request, ValConfig config) throws ValidationException {

		Request filteredRequest = applyFilters(request, config);

		ValidationResult result = new ValidationResult();

		try{
			//facade to junit engine
			JUnitCore facade = new JUnitCore()

			//we want to log more information than provided by junit so we add a new listener
			AdditionalResult moreResults = new AdditionalResult();
			facade.addListener(moreResults)

			//run the test
			Result junitbasicResult = facade.run(filteredRequest)

			result.basicResult = junitbasicResult
			result.addResult = moreResults

			return result
		}
		catch(Exception e){
			throw new ValidationException("an error occured during validation.", e)
		}
	}


	private Request applyFilters(Request request, ValConfig config){
		List<Filter> filters = createFilters(config);

		//TODO quick fix to give us some time to reconsider how we handle optional tests.
		//	filters.add(optionalFilter);
		//	log.info("optional tests will not be run.");

		for(Filter filter : filters){
			request = request.filterWith(filter);
		}

		return request;
	}


	//TODO to modify once we know how we will handle config
	private List<Filter> createFilters(ValConfig config){
		//	return Collections.singletonList(optionalFilter);
		return new LinkedList<Filter>();
	}

	Filter optionalFilter = new Filter() {

		@Override
		public boolean shouldRun(Description description) {
			boolean shouldRun = description.getAnnotation(Optional.class) == null;
			return shouldRun;
		}

		@Override
		public String describe() {
			return "optional filter : filtered out all tests marked with @Optional annotation";
		}

	};
}
