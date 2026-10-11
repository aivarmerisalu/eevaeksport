package eevaeksport.htmlgen;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;



public class Html6 {
	public static String generateHTML(ArrayList<String> graph6, String url, String topLabel) {
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append(".container {");
        html.append("  display: grid;");
        html.append("  grid-template-columns: repeat(3, 1fr);"); // Divide into 3 columns
        html.append("  grid-template-rows: repeat(2, 1fr);");    // Divide into 2 rows
        html.append("  height: 92vh;"); // Set the container to full viewport height
        html.append("  gap: 5px;");     // Add a small gap between windows
        html.append("}");

        // Define styles for each window
        for (int i = 1; i <= 6; i++) {
            html.append(".window").append(i).append(" {");
            html.append("  border: 1px solid lightgray;");
            html.append("  padding: 5px;"); // Reduce the padding
            html.append("}");
        }

        html.append(".top-header {");
        html.append("  display: flex;");
        html.append("  justify-content: space-between;"); // Horizontally space items
        html.append("  align-items: center;");           // Vertically align items
        html.append("}");

        html.append(".logo img {");
        html.append("  width: 60px;");    // Adjust the width as needed
        html.append("  height: 60px;");   // Adjust the height as needed
        html.append("  margin-right: 10px;"); // Add some spacing between the logo and label
        html.append("}");

        html.append(".dateandtime {");
        html.append("  text-align: right;"); // Align to the right
        html.append("  font-size: 18px;");
        html.append("  margin-top: 10px;");  // Add some spacing between the labels
        html.append("  margin-left: auto;"); // Push to the rightmost side of the top-header
        html.append("}");

        html.append(".top-label {");
        html.append("  text-align: left;");
        html.append("  font-size: 18px;");
        html.append("  margin-top: 10px;"); // Add some spacing between the labels
        html.append("}");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='top-header'>");
        html.append("  <div class='logo'>");
        html.append("    <img src='"+url+"' alt='Logo'>");
        html.append("  </div>");
        // Get the current date and time
        Date currentDate = new Date();

        // Define the date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

        // Format the current date and time as a string
        String formattedDate = dateFormat.format(currentDate);

        // Print the formatted date and time
        System.out.println("Current Date and Time: " + formattedDate);
        html.append("  <div class='top-label'>TOPLABEL_PLACEHOLDER</div>");
        html.append("  <div class='dateandtime'>" + formattedDate + "&nbsp;&nbsp;&nbsp;</div>");
        html.append("</div>");
        html.append("<div class='container'>");
        int i = 1;

        // Create six windows
        for (String gr : graph6) {
            html.append("<div class='window").append(i).append("'>");
            //html.append("Window " +  i);
            String updatedGraph = gr.replace("ID_PLACEHOLDER", "ID" + i)
                    .replace("HEIGHT_PLACEHOLDER", "100%")
                    .replace("WIDTH_PLACEHOLDER", "100%")
                    .replace("HEIGHT_PLACEHOLD3R", "400")
                    .replace("WIDTH_PLACEHOLD3R", "600")
                    ;
            
            html.append(updatedGraph);
            html.append("</div>");
            i++;
        }

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        try {
        	
        	File fol = new File("dashboard");
        	if(!fol.exists()) {
        		fol.mkdir();
        	}
        	
            FileWriter writer = new FileWriter("dashboard/6Window.html");
          
            writer.write(html.toString().replace("TOPLABEL_PLACEHOLDER", topLabel));
            writer.close();
            System.out.println("HTML file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return html.toString();
    }


}
