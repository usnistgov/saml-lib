package ibmXML.namespaceresolver;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class HardcodedNamespaceResolver implements NamespaceContext {

	/**
	 * This method returns the uri for all prefixes needed. Whereever possible
	 * it uses XMLConstants.
	 * @param prefix
	 * @return uri
	 */
	public String getNamespaceURI(String prefix) {
		if (prefix == null) {
			throw new IllegalArgumentException("No prefix provided!");
		} else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
			return "http://univNaSpResolver/book";
		} else if (prefix.equals("books")) {
			return "http://univNaSpResolver/booklist";
		} else if (prefix.equals("fiction")) {
			return "http://univNaSpResolver/fictionbook";
		} else if (prefix.equals("technical")) {
			return "http://univNaSpResolver/sciencebook";
		} else {
			return XMLConstants.NULL_NS_URI;
		}
	}

	public String getPrefix(String namespaceURI) {
		// Not needed in this context.
		return null;
	}

	public Iterator getPrefixes(String namespaceURI) {
		// Not needed in this context.
		return null;
	}

}
