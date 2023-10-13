package eevaeksport.htmlgen;
import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import eevaeksport.locale.Resource_ee;

public class HTMLeditor {
	

	 public static final int DEFAULT_BUFFER_SIZE = 8192;
	 
    private  void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {
        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }
    
	public  void  loadFiles() {
		InputStream in = HTMLeditor.class.getResourceAsStream( "DiagramChart.html");
		InputStream in1 = HTMLeditor.class.getResourceAsStream( "DonutChart.html");
		try {
			System.out.println(in == null);
			if(in != null) {
			copyInputStreamToFile(in, new File("DiagramChart.html"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  
    }
    
	
	public void genChart(String url, ArrayList<Map> data, String topLabel) {
        // Define the paths for the HTML and text files
		int elemCount = data.size();
		// Table, Cauge
       

       
        
        try {
            // Step 1: Read the values from the text file and store them in a map
           



            // Step 2: Read the Diagram file and locate the data section
            
            
            
            
            boolean inDataSection = false;
            String line = null;
            
            // Diagram file
            ArrayList<String> result = new ArrayList();
            for (Map<String, String[]> agentData: data) {
            	BufferedReader reader = null;
            	StringBuilder updatedData = new StringBuilder();
            	System.out.println(agentData.toString());
            	String label = agentData.get("Label")[0];
            	
            	String type = agentData.get("Type")[0];
            	
            	if (type.equals("Bar")) {
            		reader = new BufferedReader(new InputStreamReader(HTMLeditor.class.getResourceAsStream( "BarChart.html")));;
            	}
            	if (type.equals("Pie")) {
            		reader = new BufferedReader(new InputStreamReader(HTMLeditor.class.getResourceAsStream( "PieChart.html")));;
            		
            	}
            	if (type.equals("Donut")) {
            		reader = new BufferedReader(new InputStreamReader(HTMLeditor.class.getResourceAsStream( "DonutChart.html")));;
            		
            	}
            	if (type.equals("Table")) {
            		reader = new BufferedReader(new InputStreamReader(HTMLeditor.class.getResourceAsStream( "TableChart1.html")));;
            		
            	}
            	if (type.equals("Gauge")) {
            		reader = new BufferedReader(new InputStreamReader(HTMLeditor.class.getResourceAsStream( "GaugeChart.html")));;
            		
            	}
            while ((line = reader.readLine()) != null) {
                if (line.contains("<!-- Start Data Section -->")) {
                    inDataSection = true;
                    updatedData.append(line).append("\n");
                    // Add the new data here with indentation
                    for (String key : agentData.keySet()) {
                    	if (!key.equals("Label") && !key.equals("Type")) {
                    		if (type.equals("Table") || type.equals("Gauge")) {
                    			updatedData.append("        ").append((agentData.get(key))[0]).append(", ");
                    		} else {
                    			updatedData.append("        ").append((agentData.get(key))[0]).append(", ").append((agentData.get(key))[1]).append("\n"); 
                    		}
                    	}
                    }
                } else if (line.contains("<!-- End Data Section -->")) {
                    inDataSection = false;
                    updatedData.append(line).append("\n");
                } else if (!inDataSection) {
                    updatedData.append(line).append("\n");
                }
            }  
            result.add(updatedData.toString().replace("TITLE_PLACEHOLDER", label));
            reader.close();
            	
            }
            
            
            
            
            // Donut file
          /*  while ((line = donutReader.readLine()) != null) {
                if (line.contains("<!-- Start Data Section -->")) {
                    inDataSection = true;
                    updatedDonut.append(line).append("\n");
                    // Add the new data here with indentation
                    for (Map.Entry<String, String[]> entry : agentData.entrySet()) {
                    	updatedDonut.append("        ").append(entry.getValue()[0]).append(", ").append(entry.getValue()[1]).append("\n");
                    }
                } else if (line.contains("<!-- End Data Section -->")) {
                    inDataSection = false;
                    updatedDonut.append(line).append("\n");
                } else if (!inDataSection) {
                	updatedDonut.append(line).append("\n");
                }
            } 
            donutReader.close();
            */

            


            //Html2
            if (elemCount == 2){
		        ArrayList graph2 = new ArrayList();
		        for(String element:result) {
		        graph2.add(element);
		        }
		        Html2.generateHTML(graph2, url, topLabel);
		        
            }
            
            //Html3
            if (elemCount == 3){
	            ArrayList graph3 = new ArrayList();
	            for(String element:result) {
			        graph3.add(element);
			        }
			        Html3.generateHTML(graph3, url, topLabel);
            }
           
            //Html4
            if (elemCount == 4){
	            ArrayList graph4 = new ArrayList();
	            for(String element:result) {
			        graph4.add(element);
			        } 
	            Html4.generateHTML(graph4, url, topLabel);
            }
            
            //Html5
            if (elemCount == 5){
            	ArrayList graph5 = new ArrayList();
            	for(String element:result) {
    		        graph5.add(element);
    		        }
	            Html5.generateHTML(graph5, url, topLabel);
            }
            
            //Html6
            if (elemCount == 6){
		        ArrayList graph6 = new ArrayList();
		        for(String element:result) {
    		        graph6.add(element);
    		        }
		        Html6.generateHTML(graph6, url, topLabel);
            }



            //
   
            
            //Html3.generateHTML(graph3, url);
            //Html4.generateHTML(graph4, url);
            //Html5.generateHTML(graph5, url);
            //Html6withLabels.generateHTML(graph6, url);

            System.out.println("HTML file has been updated with text file data.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }
}