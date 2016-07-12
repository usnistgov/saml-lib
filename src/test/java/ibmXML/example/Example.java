package ibmXML.example;

import ibmXML.namespaceresolver.HardcodedNamespaceResolver;
import ibmXML.namespaceresolver.Tools;
import ibmXML.namespaceresolver.UniversalNamespaceCache;
import ibmXML.namespaceresolver.UniversalNamespaceResolver;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Example {

	public static void main(String[] args) {
		try {
			// Read the BookList and try out some XPath Expressions
			Document example = Tools.readExampleDocument();
			example0(example);
			example1(example);
			example2(example);
			example3(example);
			example4(example);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void example0(Document example)
			throws XPathExpressionException, TransformerException {
		sysout("\n*** Zero example - no namespaces provided ***");

		XPath xPath = XPathFactory.newInstance().newXPath();

		sysout("First try asking without namespace prefix:");
		sysout("--> booklist/book");
		NodeList result1 = (NodeList) xPath.evaluate("booklist/book", example,
				XPathConstants.NODESET);
		sysout("Result is of length " + result1.getLength());

		sysout("Then try asking with namespace prefix:");
		sysout("--> books:booklist/science:book");
		NodeList result2 = (NodeList) xPath.evaluate(
				"books:booklist/science:book", example, XPathConstants.NODESET);
		sysout("Result is of length " + result2.getLength());
		sysout("The expression does not work in both cases.");
	}

	private static void example1(Document example)
			throws XPathExpressionException, TransformerException {
		sysout("\n*** First example - namespacelookup hardcoded ***");

		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new HardcodedNamespaceResolver());

		sysout("Using any namespaces results in a NodeList:");
		sysout("--> books:booklist/technical:book");
		NodeList result1 = (NodeList) xPath.evaluate(
				"books:booklist/technical:book", example, XPathConstants.NODESET);
		sysout(result1);

		sysout("--> books:booklist/fiction:book");
		NodeList result2 = (NodeList) xPath.evaluate(
				"books:booklist/fiction:book", example, XPathConstants.NODESET);
		sysout(result2);

		sysout("The default namespace works also:");
		sysout("--> books:booklist/technical:book/:author");
		String result = xPath.evaluate("books:booklist/technical:book/:author",
				example);
		sysout(result);
	}

	private static void example2(Document example)
			throws XPathExpressionException, TransformerException {
		sysout("\n*** Second example - namespacelookup delegated to document ***");

		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new UniversalNamespaceResolver(example));

		try {
			sysout("Try to use the science prefix: no result");
			sysout("--> books:booklist/science:book");
			NodeList result1 = (NodeList) xPath.evaluate(
					"books:booklist/science:book", example,
					XPathConstants.NODESET);
			sysout(result1);
		} catch (XPathExpressionException e) {
			sysout("The resolver only knows namespaces of the first level!");
			sysout("To be precise: Only namespaces above of the node, putted in the constructor.");
		}

		sysout("The fiction namespace is such a namespace:");
		sysout("--> books:booklist/fiction:book");
		NodeList result2 = (NodeList) xPath.evaluate(
				"books:booklist/fiction:book", example, XPathConstants.NODESET);
		sysout(result2);

		sysout("The default namespace works also:");
		sysout("--> books:booklist/fiction:book[1]/:author");
		String result = xPath.evaluate(
				"books:booklist/fiction:book[1]/:author", example);
		sysout(result);
	}

	private static void example3(Document example)
			throws XPathExpressionException, TransformerException {
		sysout("\n*** Third example - namespaces of toplevel node cached ***");

		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new UniversalNamespaceCache(example, true));

		try {
			sysout("Try to use the science prefix:");
			sysout("--> books:booklist/science:book");
			NodeList result1 = (NodeList) xPath.evaluate(
					"books:booklist/science:book", example,
					XPathConstants.NODESET);
			sysout(result1);
		} catch (XPathExpressionException e) {
			sysout("The cache only knows namespaces of the first level!");
		}

		sysout("The fiction namespace is such a namespace:");
		sysout("--> books:booklist/fiction:book");
		NodeList result2 = (NodeList) xPath.evaluate(
				"books:booklist/fiction:book", example, XPathConstants.NODESET);
		sysout(result2);

		sysout("The default namespace works also:");
		sysout("--> books:booklist/fiction:book[1]/:author");
		String result = xPath.evaluate(
				"books:booklist/fiction:book[1]/:author", example);
		sysout(result);
	}

	private static void example4(Document example)
			throws XPathExpressionException, TransformerException {
		sysout("\n*** Fourth example - namespaces all levels cached ***");

		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new UniversalNamespaceCache(example, false));

		sysout("Now the use of the science prefix works as well:");
		sysout("--> books:booklist/science:book");
		NodeList result1 = (NodeList) xPath.evaluate(
				"books:booklist/science:book", example, XPathConstants.NODESET);
		sysout(result1);

		sysout("The fiction namespace is resolved:");
		sysout("--> books:booklist/fiction:book");
		NodeList result2 = (NodeList) xPath.evaluate(
				"books:booklist/fiction:book", example, XPathConstants.NODESET);
		sysout(result2);

		sysout("The default namespace works also:");
		sysout("--> books:booklist/fiction:book[1]/:author");
		String result = xPath.evaluate(
				"books:booklist/fiction:book[1]/:author", example);
		sysout(result);
	}

	private static void sysout(String string) {
		System.out.println(string);
	}

	private static void sysout(Node node) throws TransformerException {
		sysout(Tools.putOutAsString(node));
	}

	private static void sysout(NodeList nodelist) throws TransformerException {
		sysout("Number of Nodes: " + nodelist.getLength());
		for (int i = 0; i < nodelist.getLength(); i++) {
			sysout(nodelist.item(i));
		}
	}
}
