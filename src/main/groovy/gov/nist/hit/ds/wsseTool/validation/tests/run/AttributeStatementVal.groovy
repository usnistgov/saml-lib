package gov.nist.hit.ds.wsseTool.validation.tests.run
import gov.nist.hit.ds.wsseTool.api.config.ValConfig
import gov.nist.hit.ds.wsseTool.namespace.dom.NwhinNamespace
import gov.nist.hit.ds.wsseTool.util.MyXmlUtils
import gov.nist.hit.ds.wsseTool.validation.engine.ValRunnerWithOrder
import gov.nist.hit.ds.wsseTool.validation.engine.annotations.Optional
import gov.nist.hit.ds.wsseTool.validation.engine.annotations.Validation
import gov.nist.hit.ds.wsseTool.validation.tests.BaseVal
import gov.nist.hit.ds.wsseTool.validation.tests.CommonVal
import gov.nist.hit.ds.wsseTool.validation.tests.ValDescriptor
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NoChildren
import groovy.util.slurpersupport.NodeChild
import org.junit.Before
import org.junit.runner.RunWith

import java.text.MessageFormat

import static org.junit.Assert.*
import static org.junit.Assume.assumeTrue


@RunWith(ValRunnerWithOrder.class)
public class AttributeStatementVal extends BaseVal {

	/*
	 * Test initialization
	 */
	@Before
	public final void getAttributesList() {
		//		this.attrs = header.map.attributeStatement.children().findAll{it.name() == 'Attribute'}
		this.attributes = header.map.attributeStatements.depthFirst().findAll{it.name() == 'Attribute'}
	}

	//	private GPathResult attrs
	private ArrayList attributes

	@Validation(id="1081", rtm=["64", "86", "166", "167"])
	public void uniqueAttributeNames(){

		for(NodeChild attr : attributes){
			boolean isUnique = attributes.findAll{ it.@Name == attr.name}.size() <= 1 //each attr has a unique name
			assertTrue("each attribute must have a unique name", isUnique)
		}

		//		for(GPathResult attr : attrs){
		//			boolean isUnique = attrs.findAll{ it.@Name == attr.name}.size() <= 1 //each attr has a unique name
		//			assertTrue("each attribute must have a unique name", isUnique)
		//		}
	}

	@Validation(id="1082", rtm=["87", "97", "98"])
	public void subjectId(){
		// GPathResult subjectId = attrs.findAll{ it.@Name == "urn:oasis:names:tc:xspa:1.0:subject:subject-id"}

		def subjectId = attributes.findAll{ it.@Name == "urn:oasis:names:tc:xspa:1.0:subject:subject-id"}

		assertTrue("subjectId missing", subjectId[0] != null)
		assertTrue("subjectId attribute value missing", subjectId[0].AttributeValue[0] != null)
	}

	@Validation(id="1083", rtm=["88", "99", "100"])
	public void organization(){
		def organization = attributes.findAll{ it.@Name == "urn:oasis:names:tc:xspa:1.0:subject:organization"}

		assertTrue("organization missing", organization[0] != null)
		assertTrue("organization attribute value missing", organization[0].AttributeValue[0] != null)
	}

	@Validation(id="1084", rtm=[
		"92",
		"101",
		"102",
		"103",
		"104"
	])
	public void organizationId(){
		def organizationId = attributes.findAll{ it.@Name == "urn:oasis:names:tc:xspa:1.0:subject:organization-id"}

		assertFalse("organization-id missing", organizationId[0] == null)
		assertFalse("organization-id attribute value missing", organizationId[0].AttributeValue[0] == null)

		String oid = organizationId[0].AttributeValue[0].text().trim()

		assertTrue(MessageFormat.format("invalid organization-id. Should be a valid urn starting with prefix urn:oid: , found {0}",oid), CommonVal.validURNoid(oid))
	}

	@Validation(id="1085", rtm=["91", "105", "106"])
	public void homeCommunityId(){
		def homeCommunityId = attributes.findAll{ it.@Name == "urn:nhin:names:saml:homeCommunityId"}

		assertFalse("homeCommunityId missing", homeCommunityId[0] == null)
		assertFalse("homeCommunityId attribute value missing", homeCommunityId[0].AttributeValue[0] == null)

		String hcid = homeCommunityId[0].AttributeValue[0].text().trim()

		assertTrue(MessageFormat.format("invalid homeCommunityId. Should be a valid urn starting with prefix urn:oid: , found {0}", hcid), CommonVal.validURNoid(hcid))
	}

	@Validation(id="1086", rtm=[
		"89",
		"107",
		"108",
		"109",
		"110",
		"111",
		"112"
	])
	public void role(){

        def roleName = "urn:oasis:names:tc:xacml:2.0:subject:role"

		def role = attributes.findAll{ it.@Name == roleName}

		assertFalse("role missing", role[0] == null)
		assertFalse("role attribute value missing", role[0].AttributeValue[0] == null)

		GPathResult attr = role[0].AttributeValue[0]
		GPathResult r =  role[0].AttributeValue[0].Role[0]

        assertEquals( "roleName namespace must be 'urn:hl7-org:v3'",'urn:hl7-org:v3',r.namespaceURI())

        String prefix = header.namespaces.getPrefix(NwhinNamespace.XSI.uri())
        String type = r.@"${prefix}:type"

        assertTrue("no xsi:type attribute found", type != null)

        assertTrue(MessageFormat.format("wrong type value. expected : 'CE' or 'hl7:CE', got : {0}",type), (type == "CE" || type == "hl7:CE"));

		assertEquals("wrong codeSystem", "2.16.840.1.113883.6.96", r.@codeSystem.text())

        String codeSystem = r.@codeSystemName.text();

		assertTrue("wrong codeSystemName : expected SNOMED_CT or SNOMED CT, got :  $codeSystem", codeSystem == "SNOMED_CT" || "SNOMED CT")

		log.info("code is not checked")
		log.info("display name correlation with code is not checked")
	}

	@Validation(id="1087", rtm=[
		"90",
		"113",
		"114",
		"116",
		"117",
		"118",
		"120"
	], status=ValConfig.Status.review)
	public void purposeOfUse(){
		def pouName = "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse"

		def purposeOfUse = attributes.findAll{ it.@Name == pouName}

		assertTrue("purposeOfUse attribute statement missing", purposeOfUse[0] != null)

		assertTrue("purposeOfUse attribute value missing", purposeOfUse[0].AttributeValue[0] != null)

		GPathResult attr = purposeOfUse[0].AttributeValue[0]
		GPathResult p =  purposeOfUse[0].AttributeValue[0].PurposeOfUse[0]

		assertFalse("tag with @name=$pouName had not attribute value child element <PurposeOfUse>. <PurposeForUse> not supported since 2010 revision of the authorization framework.", p instanceof NoChildren);

		assertEquals( "purposeOfUse namespace must be 'urn:hl7-org:v3'",'urn:hl7-org:v3',p.namespaceURI())

		String prefix = header.namespaces.getPrefix(NwhinNamespace.XSI.uri())
		String type = p.@"${prefix}:type"

		assertTrue("no xsi:type attribute found", type != null)

		assertTrue(MessageFormat.format("wrong type value. expected : 'CE' or 'hl7:CE', got : {0}",type), (type == "CE" || type == "hl7:CE"));

		assertEquals("wrong codeSystem", "2.16.840.1.113883.3.18.7.1", p.@codeSystem.text() )
		assertEquals("wrong codeSystemName", "nhin-purpose", p.@codeSystemName.text() )

		//check if code has allowable value
		InputStream is = null;
		is = MyXmlUtils.class.getClassLoader().getResourceAsStream("codeLists/purposeOfUseCE");
		def code = p.@code.text().trim()
		def values = is.getText().split(",")
		assertTrue ("code does not belong to code list", values.any { it.trim() == code }.booleanValue())
		is.close();

		log.info("display name correlation with code value is not checked")
	}

	@Optional
	//TODO check
	//TODO need to check that instanceConsentAccessPolicy is present but it depends if multiple AuthDezStatement are possible or not.
	@Validation(id="1088", rtm=[
		"93",
		"122",
		"123",
		"124",
		"125",
		"126",
		"127"
	], status=ValConfig.Status.review)
	public void resourceId(){
		def resourceId = attributes.findAll{ it.@Name == "urn:oasis:names:tc:xacml:2.0:resource:resource-id"}

		assertTrue("resource-id not present", resourceId[0] != null);

	}

	@Validation(id="1088", rtm=[
		"93",
		"122",
		"123",
		"124",
		"125",
		"126",
		"127"
	], status=ValConfig.Status.review)
	public void resourceIdMatchPatientId(){
		def resourceId = attributes.findAll{ it.@Name == "urn:oasis:names:tc:xacml:2.0:resource:resource-id"}

		assumeTrue(ValDescriptor.NOT_IMPLEMENTED + "resource id should match patient id from the requesting organization",false);
	}

	//TODO check. Second assertion does not seem to work properly
	@Optional
	@Validation(id="1089", rtm=["129"])
	public void npi(){
		def npi = attributes.findAll{ it.@Name == "urn:oasis:names:tc:xspa:2.0:subject:npi"}

		assumeTrue("npi not present but is optional", npi.size() == 1)
		assertTrue("npi attribute value missing", npi[0].AttributeValue[0] != null)
		log.info("validation not fully implemented")
	}

}

