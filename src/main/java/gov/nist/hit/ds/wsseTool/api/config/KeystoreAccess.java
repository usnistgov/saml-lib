package gov.nist.hit.ds.wsseTool.api.config;

import gov.nist.hit.ds.wsseTool.api.WsseHeaderGenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeystoreAccess provides easy access to a keystore info. It expects the
 * keystore to be configured with a unique private key
 * 
 * 
 * @author gerardin
 * 
 */
public class KeystoreAccess {

	private static final Logger log = LoggerFactory.getLogger(KeystoreAccess.class);
	
	private KeyStore keystore;
	public KeyPair keyPair;
	public PrivateKey privateKey;
	public PublicKey publicKey;
	public Certificate certificate;

	public KeystoreAccess(InputStream is, String storePass, String privateKeyAlias, String privateKeyPass)
			throws KeyStoreException {
		loadKeyStoreAccess(is, storePass, privateKeyAlias, privateKeyPass);
	}	

	public KeystoreAccess(String storePath, String storePass, String privateKeyAlias, String privateKeyPass)
			throws KeyStoreException {
		try {
			FileInputStream is = new FileInputStream(storePath);
			loadKeyStoreAccess(is, storePass, privateKeyAlias, privateKeyPass);
			log.info("keystore successfully loaded from :" + storePath);
		} catch (FileNotFoundException e) {
			throw new KeyStoreException("cannot properly access keystore located at : " + storePath, e);
		}
	}

	public void loadKeyStoreAccess(InputStream is, String storePass, String privateKeyAlias, String privateKeyPass)
			throws KeyStoreException {

		try {
			keystore = loadKeyStore(is, storePass);
			loadKeyStoreInfo(privateKeyAlias, privateKeyPass);
			log.info("keystore successfully loaded!");
		} catch (KeyStoreException e) {
			throw new KeyStoreException("cannot properly access keystore");
		}
	}

	private void loadKeyStoreInfo(String privateKeyAlias, String privateKeyPass) throws KeyStoreException {
		if (!keystore.containsAlias(privateKeyAlias)) {
			//for debugging problem related to aliases not found
			String debug = "aliases in this keystore:";
			Enumeration<String> aliases = keystore.aliases();
			while(aliases.hasMoreElements()){
				debug += aliases.nextElement();
			}
			log.debug(debug);
			
			throw new KeyStoreException("alias not found : " + privateKeyAlias);
		}

		try {
			privateKey = (PrivateKey) keystore.getKey(privateKeyAlias, privateKeyPass.toCharArray());
			certificate = keystore.getCertificate(privateKeyAlias);
			publicKey = certificate.getPublicKey();
			keyPair = new KeyPair(publicKey, privateKey);
		} catch (Exception e) {
			throw new KeyStoreException("cannot retrieve info from keystore for alias : " + privateKeyAlias, e);
		}
	}

	private KeyStore loadKeyStore(String store, String sPass) throws KeyStoreException {
		try {
			return loadKeyStore(new FileInputStream(store), sPass);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new KeyStoreException("cannot load keystore with pass : " + sPass, e);
		}
	}
	
	private KeyStore loadKeyStore(InputStream is, String sPass) throws KeyStoreException {
		try {
			KeyStore  mykeystore = KeyStore.getInstance("JKS");
			//try first on the classpath
			//InputStream is = new FileInputStream(store);
			mykeystore.load(is, sPass.toCharArray());
			is.close();
			return mykeystore;
		} catch (Exception e) {
			throw new KeyStoreException("cannot load keystore with pass : " + sPass, e);
		}
	}
}
