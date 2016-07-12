package ibmXML.namespaceresolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Tools {

	public static Document readExampleDocument()
			throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder builder = builderFactory.newDocumentBuilder();

		String input = readFile(new File("src/test/java/ibmXML/example/BookList.xml"));
		InputSource inputSource = new InputSource(new StringReader(input));
		return builder.parse(inputSource);
	}

	public static String readFile(File file) throws IOException {
		StringBuffer content = new StringBuffer();

		FileInputStream fileInputStream = new FileInputStream(file);
		InputStreamReader streamReader = new InputStreamReader(fileInputStream,
				"UTF-8");
		int readChars = 0;
		do {
			char[] contentBuffer = new char[1024];
			readChars = streamReader.read(contentBuffer);
			content.append(contentBuffer, 0, readChars);
		} while (readChars == 1024);
		streamReader.close();
		return content.toString();
	}

	public static String putOutAsString(Node node) throws TransformerException {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();

		StringWriter writer = new StringWriter();
		Result result = new StreamResult(writer);
		transformer.transform(new DOMSource(node), result);
		return writer.toString();
	}

}
