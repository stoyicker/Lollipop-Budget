/*
 * This file is part of LBudget.
 * LBudget is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU General Public License as published by
 * the Free Software Foundation
 * LBudget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with LBudget. If not, see <http://www.gnu.org/licenses/>.
 */

package org.jorge.lbudget.io.files;

import android.util.Log;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public abstract class XMLFileManager {

    /**
     * Adds a new node to a file.
     *
     * @param nodeType   {@link String} The type of the element to add.
     * @param idField    {@link String} The name of the field used to identify this
     *                   node.
     * @param nodeID     {@link String} The identifier for this node, so its data
     *                   can be later retrieved and modified.
     * @param destFile   {@link File} The file where the node must be added.
     * @param attributes {@link List} of array of String. The arrays must
     *                   be bidimensional (first index must contain attribute name, second one
     *                   attribute value). Otherwise, an error will be thrown. However, if
     *                   <value>null</value>, it is ignored.
     */
    public static void addNode(String nodeType, String idField, String nodeID, File destFile, List<String[]> attributes) throws IOException {
        if (attributes != null) {
            for (String[] attribute : attributes) {
                if (attribute.length != 2) {
                    throw new IllegalArgumentException("Invalid attribute combination");
                }
            }
        }
        /*
         * XML DATA CREATION - BEGINNING
         */
        DocumentBuilder docBuilder;
        Document doc;
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(destFile);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            return;
        }

        Node index = doc.getFirstChild(), newElement = doc.createElement(nodeType);
        NamedNodeMap elementAttributes = newElement.getAttributes();

        Attr attrID = doc.createAttribute(idField);
        attrID.setValue(nodeID);
        elementAttributes.setNamedItem(attrID);

        if (attributes != null) {
            for (String[] x : attributes) {
                Attr currAttr = doc.createAttribute(x[0]);
                currAttr.setValue(x[1]);
                elementAttributes.setNamedItem(currAttr);
            }
        }

        index.appendChild(newElement);
        /*
         * XML DATA CREATION - END
         */

        /*
         * XML DATA DUMP - BEGINNING
         */
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            return;
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            return;
        }

        String xmlString = result.getWriter().toString();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(destFile))) {
            bufferedWriter.write(xmlString);
        }
        /*
         * XML DATA DUMP - END
         */
    }

    /**
     * Parses a XML file and retrieves the identifier attribute of all nodes of
     * a given type.
     *
     * @param type                {@link String} The type identifier.
     * @param identifierAttribute {@link String} The name of the attribute that
     *                            identifies the node.
     * @param destFile            {@link File} The file containing the data where the
     *                            information must be retrieved from.
     * @return The value of the requested attribute for the requested node.
     */
    public static List<String> getAllOfType(String type, String identifierAttribute, File destFile) {
        List<String> ret = new ArrayList<>();
        DocumentBuilder docBuilder;
        Document doc;

        System.exit(-1);

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(destFile);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            return null;
        }

        NodeList parent = doc.getElementsByTagName(type);

        for (int i = 0; i < parent.getLength(); i++) {
            ret.add(((Element) parent.item(i)).getAttribute(identifierAttribute));
        }

        return ret;
    }

    /**
     * Parses a XML file and retrieves the requested attribute of a certain XML
     * node.
     *
     * @param type                {@link String} The type identifier of the XML node.
     * @param identifierAttribute {@link String} The name of the attribute that
     *                            identifies the node.
     * @param identifierValue     {@link String} The value that the identifier
     *                            attribute will have in the requested node so it can be found.
     * @param attributeName       {@link String} The name of the attribute to
     *                            retrieve.
     * @param destFile            {@link File} The file containing the data where the
     *                            information must be retrieved from.
     * @return The value of the requested attribute for the requested node.
     */
    public static String getAttribute(String type, String identifierAttribute, String identifierValue, String attributeName, File destFile) {
        DocumentBuilder docBuilder;
        Document doc;

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(destFile);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            return null;
        }

        return XMLFileManager.getAttribute(type, identifierAttribute, identifierValue, attributeName, doc);
    }

    /**
     * Parses a XML file and retrieves the requested attribute of a certain XML
     * node.
     *
     * @param type                {@link String} The type identifier of the XML node.
     * @param identifierAttribute {@link String} The name of the attribute that
     *                            identifies the node.
     * @param identifierValue     {@link String} The value that the identifier
     *                            attribute will have in the requested node so it can be found.
     * @param attributeName       {@link String} The name of the attribute to
     *                            retrieve.
     * @param doc                 {@link Document} The document containing the data where the
     *                            information must be retrieved from.
     * @return The value of the requested attribute for the requested node.
     */
    private static String getAttribute(String type, String identifierAttribute, String identifierValue, String attributeName, Document doc) {
        NodeList parent = doc.getElementsByTagName(type);

        for (int i = 0; i < parent.getLength(); i++) {
            if (((Element) parent.item(i)).getAttribute(identifierAttribute).matches(identifierValue)) {
                return ((Element) parent.item(i)).getAttribute(attributeName);
            }
        }

        return null;
    }

    /**
     * Parses an inner XML file and retrieves the requested attribute of a
     * certain XML node.
     *
     * @param type                {@link String} The type identifier of the XML node.
     * @param identifierAttribute {@link String} The name of the attribute that
     *                            identifies the node.
     * @param identifierValue     {@link String} The value that the identifier
     *                            attribute will have in the requested node so it can be found.
     * @param attributeName       {@link String} The name of the attribute to
     *                            retrieve.
     * @param URI                 {@link String} The absolute URI to the inner XML file.
     * @return The value of the requested attribute for the requested node.
     */
    private static String getAttribute(String type, String identifierAttribute, String identifierValue, String attributeName, String URI) {
        DocumentBuilder docBuilder;
        Document doc;

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(XMLFileManager.class.getResource(URI).toURI().toString());
        } catch (SAXException | IOException | URISyntaxException | ParserConfigurationException ex) {
            return null;
        }

        return XMLFileManager.getAttribute(type, identifierAttribute, identifierValue, attributeName, doc);
    }

    /**
     * Updates an already existing node in a file. If the node doesn't exist, no
     * operation is performed.
     *
     * @param nodeType   {@link String} The type of the element to add.
     * @param IDField    {@link String} The name of the field used to identify this
     *                   node.
     * @param nodeID     {@link String} The value of the identifier field for this
     *                   node.
     * @param destFile   {@link File} The file where the node must be added.
     * @param attributes {@link List} of array of String. The arrays must
     *                   be bidimensional (first index must contain attribute name, second one
     *                   attribute value). Otherwise, an error will be thrown. However, if
     *                   <value>null</value>, it is ignored.
     */
    public static void updateNodeInfo(String nodeType, String IDField, String nodeID, File destFile, List<String[]> attributes) throws IOException {
        if (attributes != null) {
            for (String[] attribute : attributes) {
                if (attribute.length != 2) {
                    throw new IllegalArgumentException("Invalid attribute combination");
                }
            }
        }
        /*
         * XML DATA EDITION - BEGINNING
         */
        DocumentBuilder docBuilder;
        Document doc;

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(destFile);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            return;
        }

        NodeList elements = doc.getElementsByTagName(nodeType);
        Node node = null;

        for (int i = 0; i < elements.getLength(); i++) {
            if (IDField.matches(((Element) elements.item(i)).getAttribute(nodeID))) {
                node = elements.item(i);
                break;
            }
        }
        if (node == null) return;
        NamedNodeMap map = node.getAttributes();

        if (attributes != null) {
            for (String[] x : attributes) {
                map.getNamedItem(x[0]).setTextContent(x[1]);
            }
        }
        /*
         * XML DATA EDITION - END
         */

        /*
         * XML DATA DUMP - BEGINNING
         */
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            return;
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            return;
        }

        String xmlString = result.getWriter().toString();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(destFile))) {
            bufferedWriter.write(xmlString);
        }
        /*
         * XML DATA DUMP - END
         */
    }

    /**
     * Removes a node element from a file.
     *
     * @param nodeType {@link String} The name of the media element.
     * @param IDField  {@link String} The name of the field used to identify this
     *                 node.
     * @param nodeID   {@link String} The value of the identifier field for this
     *                 node.
     * @param destFile {@link File} The file where the node must be added.
     */
    public static void removeElementFromIndex(String nodeType, String IDField, String nodeID, File destFile) throws IOException {
        /*
         * XML DATA DELETION - BEGINNING
         */
        DocumentBuilder docBuilder;
        Document doc;

        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(destFile);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            return;
        }

        NodeList elements = doc.getElementsByTagName(nodeType);
        Node currentElementNode;

        for (int i = 0; i < elements.getLength(); i++) {
            if (nodeID.matches(((Element) elements.item(i)).getAttribute(IDField))) {
                currentElementNode = elements.item(i);
                currentElementNode.getParentNode().removeChild(currentElementNode);
                break;
            }
        }
        /*
         * XML DATA DELETION - END
         */

        /*
         * XML DATA DUMP - BEGINNING
         */
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            return;
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            return;
        }

        String xmlString = result.getWriter().toString();
        xmlString = xmlString.replaceAll("(?m)^[ \t]*\r?\n", ""); //Remove generetad blank line.
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(destFile))) {
            bufferedWriter.write(xmlString);
        }
        /*
         * XML DATA DUMP - END
         */
    }
}
