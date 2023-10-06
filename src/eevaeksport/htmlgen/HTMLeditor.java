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
    
	
	public void genChart() {
        // Define the paths for the HTML and text files
       

       
        
        try {
            // Step 1: Read the values from the text file and store them in a map
            Map<String, String[]> agentData = new HashMap<>();
            agentData.put("EEE2", new String[] { "['" + "Erik" + " (" + "EEE2"+ ")'", "350" + "]," });
            agentData.put("AIV1", new String[] { "['" + "Aivar" + " (" + "AIV1"+ ")'", "500" + "]," });
            agentData.put("KAT1", new String[] { "['" + "Katrin" + " (" + "KAT1"+ ")'", "430" + "]," });

         



            // Step 2: Read the Diagram file and locate the data section
            BufferedReader diagramReader = new BufferedReader(new InputStreamReader(HTMLeditor.class.getResourceAsStream( "DiagramChart.html")));
            BufferedReader donutReader = new BufferedReader(new InputStreamReader(HTMLeditor.class.getResourceAsStream( "DonutChart.html")));
            StringBuilder updatedDiagram = new StringBuilder();
            StringBuilder updatedDonut = new StringBuilder();
            boolean inDataSection = false;
            String line = null;
            // Diagram file
            

            while ((line = diagramReader.readLine()) != null) {
                if (line.contains("<!-- Start Data Section -->")) {
                    inDataSection = true;
                    updatedDiagram.append(line).append("\n");
                    // Add the new data here with indentation
                    for (Map.Entry<String, String[]> entry : agentData.entrySet()) {
                        updatedDiagram.append("        ").append(entry.getValue()[0]).append(", ").append(entry.getValue()[1]).append("\n");
                    }
                } else if (line.contains("<!-- End Data Section -->")) {
                    inDataSection = false;
                    updatedDiagram.append(line).append("\n");
                } else if (!inDataSection) {
                    updatedDiagram.append(line).append("\n");
                }
            }      
            diagramReader.close();
            // Donut file
            while ((line = donutReader.readLine()) != null) {
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

            


            //Html2
            ArrayList graph2 = new ArrayList();
            graph2.add(updatedDiagram.toString());
            graph2.add(updatedDonut.toString());
            //Html3
            ArrayList graph6 = new ArrayList();
            graph6.add(updatedDiagram.toString());
            graph6.add(updatedDonut.toString());
            graph6.add(updatedDiagram.toString());
            graph6.add(updatedDonut.toString());
            graph6.add(updatedDiagram.toString());
            graph6.add(updatedDonut.toString());



            //
   
            Html2.generateHTML(graph2);
            Html6withLabels.generateHTML(graph6);

            System.out.println("HTML file has been updated with text file data.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }
}