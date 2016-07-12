package gov.nist.toolkit.wsseTool.schemaValidation;

	import java.io.FileInputStream;
	import java.security.KeyStore;
	import java.security.cert.X509Certificate;
	import java.util.ArrayList;
	import java.util.Collections;
	import java.util.List;

	import javax.xml.crypto.dsig.CanonicalizationMethod;
	import javax.xml.crypto.dsig.DigestMethod;
	import javax.xml.crypto.dsig.Reference;
	import javax.xml.crypto.dsig.SignatureMethod;
	import javax.xml.crypto.dsig.SignedInfo;
	import javax.xml.crypto.dsig.Transform;
	import javax.xml.crypto.dsig.XMLSignatureFactory;
	import javax.xml.crypto.dsig.dom.DOMSignContext;
	import javax.xml.crypto.dsig.keyinfo.KeyInfo;
	import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
	import javax.xml.crypto.dsig.keyinfo.KeyValue;
	import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
	import javax.xml.crypto.dsig.spec.TransformParameterSpec;

	import org.apache.xml.security.transforms.Transforms;
	import org.w3c.dom.Document;
	import org.w3c.dom.Element;
	import org.w3c.dom.Node;


public class ZhangTest {

	    public Element doCreateXmlSignature2(Document inputDoc, String sigidString, String parentNodeTag,String keystorePath, String certPassword, Node beforeNode, String assertId)
	            throws Exception {
	        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
	        Transform enveloped = fac.newTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE, (TransformParameterSpec) null);
	        Transform c14n = fac.newTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS, (TransformParameterSpec) null);
	        List<Transform> trans = new ArrayList<Transform>();
	        trans.add(enveloped);
	        trans.add(c14n);
	        Reference ref = fac.newReference("#" + sigidString, fac.newDigestMethod(DigestMethod.SHA1, null), trans, null, null);
	        // Create the SignedInfo.
	        SignedInfo si =
	                fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
	                        fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));
	        // Load the KeyStore and get the signing key and certificate.
	        FileInputStream in = new FileInputStream(keystorePath);
	        KeyStore ks = KeyStore.getInstance("JKS");
	        ks.load(in, certPassword.toCharArray());

	        KeyStore.PrivateKeyEntry keyEntry =
	                (KeyStore.PrivateKeyEntry) ks.getEntry("1", new KeyStore.PasswordProtection(certPassword.toCharArray()));

	        KeyInfoFactory kif = fac.getKeyInfoFactory();
	        List infoList = new ArrayList();
	        if(parentNodeTag == null){
	            X509Certificate cert = (X509Certificate) keyEntry.getCertificate();
	            List x509Content = new ArrayList();
	            x509Content.add(cert.getSubjectX500Principal().getName());
	            x509Content.add(cert);
	            javax.xml.crypto.dsig.keyinfo.X509Data xd = kif.newX509Data(x509Content);
	            KeyValue value = kif.newKeyValue(cert.getPublicKey());
	            infoList.add(value);
	            // infoList.add(xd);
	        }else{
	            X509Certificate cert = (X509Certificate) keyEntry.getCertificate();
	            KeyValue value = kif.newKeyValue(cert.getPublicKey());
	            infoList.add(value);
//	            Element securityToken = inputDoc.createElementNS(SamlUtils.SECURITY, "wsse:SecurityTokenReference");
//	            securityToken.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id", "asda12341324");
//	            securityToken.setAttributeNS("http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd", "wsse11:TokenType", "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0");
//	            Element keyIdentifier = inputDoc.createElementNS(SamlUtils.SECURITY, "wsse:KeyIdentifier");
//	            keyIdentifier.setAttribute("ValueType", "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLID");
//	            keyIdentifier.setTextContent(assertId);
//	            securityToken.appendChild(keyIdentifier);
//	            infoList.add(securityToken);
	        }
	        // Create the KeyInfo.
	        KeyInfo ki = kif.newKeyInfo(infoList);

	        // Create a DOMSignContext and specify the RSA PrivateKey and
	        // location of the resulting XMLSignature's parent element and before element.
	        DOMSignContext dsc = null;
	        Node parent = parentNodeTag == null ? inputDoc.getFirstChild() : inputDoc.getElementsByTagName(parentNodeTag).item(0);
	        if (beforeNode != null) {
	            dsc = new DOMSignContext(keyEntry.getPrivateKey(), parent, beforeNode);
	        } else {
	            dsc = new DOMSignContext(keyEntry.getPrivateKey(), parent);
	        }
	        dsc.putNamespacePrefix(javax.xml.crypto.dsig.XMLSignature.XMLNS, "ds");
	        // Create the XMLSignature, but don't sign it yet.
	        javax.xml.crypto.dsig.XMLSignature signature = fac.newXMLSignature(si, ki);

	        // Marshal, generate, and sign the enveloped signature.
	        signature.sign(dsc);
	        return inputDoc.getDocumentElement();
	    }
	}
