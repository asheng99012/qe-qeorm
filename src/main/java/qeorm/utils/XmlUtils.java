package qeorm.utils;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ashen on 2017-2-3.
 */
public class XmlUtils {
    private static DocumentBuilder getBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder();
    }

    public static Element getRootElement(String fileName)
            throws ParserConfigurationException, IOException, SAXException {
        return getBuilder().parse(fileName).getDocumentElement();
    }

    public static Element getRootElement(InputStream inputstream)
            throws ParserConfigurationException, IOException, SAXException {
        return getBuilder().parse(inputstream).getDocumentElement();
    }

    public static Element parseXmlString(String xmlString)
            throws ParserConfigurationException, IOException, SAXException {
        return getBuilder().parse(new InputSource(new StringReader(xmlString))).getDocumentElement();
    }

    public static List<Node> getChildNodes(Element element, String name) {
        if (element == null) return new ArrayList<Node>();
        return toList(element.getElementsByTagName(name));
    }

    public static Node getChildNode(Element element, String name) {
        List<Node> list = getChildNodes(element, name);
        if (list.size() > 0) return list.get(0);
        return null;
    }

    public static List<Node> getChildNodes(Node element) {
        if (element == null) return new ArrayList<Node>();
        return toList(element.getChildNodes());
    }

    public static Node getChildNode(Element element) {
        List<Node> list = getChildNodes(element);
        if (list.size() > 0) return list.get(0);
        return null;
    }

    private static List<Node> toList(NodeList nodes) {
        List<Node> nodeList = new ArrayList<Node>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
                nodeList.add(node);
        }
        return nodeList;
    }

    public static Map<String, String> getAttributes(Node element) {
        Map<String, String> map = new HashMap<String, String>();
        if (element != null && element.hasAttributes()) {
            NamedNodeMap attrs = element.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attrNode = attrs.item(i);
                map.put(attrNode.getNodeName().trim(), attrNode.getNodeValue().trim());
            }
        }
        return map;
    }

    public static String getAttributeValue(Node element, String attributeName) {
        Map<String, String> map = getAttributes(element);
        if (map.containsKey(attributeName))
            return map.get(attributeName);
        return null;
    }

    public static String getValue(Node node) {
        if (node == null) return null;
        return node.getTextContent().trim();
    }
}
