package eevaeksport.htmlgen;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Html6 {
    

    public static String generateHTML(ArrayList<String> graph) {
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append(".container {");
        html.append("  display: grid;");
        html.append("  grid-template-columns: repeat(3, 1fr);"); // Divide into 3 columns
        html.append("  grid-template-rows: repeat(2, 1fr);");    // Divide into 2 rows
        html.append("  height: 98vh;"); // Set the container to full viewport height
        html.append("  gap: 5px;");     // Add a small gap between windows
        html.append("}");

        // Define styles for each window
        for (int i = 1; i <= 6; i++) {
            html.append(".window").append(i).append(" {");
            html.append("  border: 1px solid #000;");
            html.append("  padding: 5px;"); // Reduce the padding
            html.append("}");
        }

        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");
        int i = 1;

        // Create six windows
        for (String gr : graph) {
            html.append("<div class='window").append(i).append("'>");
            html.append("Window " +  i);
            String updatedGraph = gr.replace("ID_PLACEHOLDER", "ID" + i).replace("DIVID_PLACEHOLDER", "ID" + i);
            html.append(updatedGraph);
            html.append("</div>");
            i++;
        }

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        try {
            FileWriter writer = new FileWriter("C:\\HTMLproj2\\6Window.html");
            writer.write(html.toString());
            writer.close();
            System.out.println("HTML file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return html.toString();
    }
}
       
