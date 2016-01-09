package ems.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;

public class XMLDataFetcher {
	
	int[] n_days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	double[] v_list = new double[24];
	String filePath = "";
	
	public XMLDataFetcher(String filePath){
		this.filePath = filePath;
	}
	
	public double[] getSolarByDate(int month, int day) {
		
		int day_id = dayID_transform(month, day);
		try {
			File fXmlFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
		
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize(); //optional, but recommended
		
		
			NodeList nList = doc.getElementsByTagName("day");
		
			Node nNode = nList.item(day_id-1);
			nList = nNode.getChildNodes();
			int j = 0;
			for (int i = 0; i < nList.getLength(); i++) {
				Element element = null;
				if(nList.item(i).getNodeType() == Node.ELEMENT_NODE) {
					element = (Element)(nList.item(i));
				}
				//System.out.println(i + " ;type: " + nList.item(i).getNodeName() + ";value: "+nList.item(i).getTextContent());
				
				if(element != null && element.getNodeName().equals("solar")) {
					
					v_list[j++] = Double.valueOf(element.getTextContent());
					//System.out.println((double)Math.round((v_list[j-1] / 1000) * 1000) / 1000);
				}

			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return v_list;
	}
	
	int dayID_transform(int month, int day) { 
		int d = 0;
		for(int i = 0; i < month - 1; i++) {
			d += n_days[i];
		}
		d += day;
		return d;
	}
	
	public static void main(String[] args) {
		new XMLDataFetcher("./_input_data/re.xml").getSolarByDate(3,7);
	}
	
}
