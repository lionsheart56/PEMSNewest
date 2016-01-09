package ems.util;

import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class XmlBuilder {
	
	public Document document;
	private String filePath;
		
	public XmlBuilder(){
		this.document = DocumentHelper.createDocument();
	}
		
	// Create root element only has tag
	public Element createRootElement(String tag){
		Element element = document.addElement(tag);
		return element;
	}
		
	// Create root element has tag, attribute, text
	public Element createRootElement(String tag, String attrName, String attrText, String text){
		Element element = document.addElement(tag)
				.addAttribute(attrName, attrText)
				.addText(text);
		return element;
	}
	
	// Create child element has tag, attribute, text
	public Element createChildElement(String tag, String attrName, String attrText, String text, Element parent){
		Element child = parent.addElement(tag)
				.addAttribute(attrName, attrText)
				.addText(text);
		return child;
	}
	
	// Create child element has tag, attribute
	public Element createChildElement(String tag, String attrName, String attrText, Element parent){
		Element child = parent.addElement(tag)
				.addAttribute(attrName, attrText);
		return child;
	}
	
	// Create child element has tag, text
	public Element createChildElement(String tag, String text, Element parent){
		Element child = parent.addElement(tag)
				.addText(text);
		return child;
	}
	
	// Create child element has tag
	public Element createChildElement(String tag, Element parent){
		Element child = parent.addElement(tag);
		return child;
	}
	
	public void setFilePath(String path){
		this.filePath = path;
	}
		
	public void write() throws IOException{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(filePath), format);
		writer.write(document);
		writer.close();
	}
}
