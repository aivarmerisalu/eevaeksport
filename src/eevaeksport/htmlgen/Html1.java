package eevaeksport.htmlgen;
import java.io.FileWriter;
import java.io.IOException;
public class Html1 {
    public static String generateHTML(String graph) {
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append(".container {");
        html.append("  display: grid;");
        html.append("  grid-template-columns: 1fr;"); // Single column
        html.append("  grid-template-rows: 1fr;");    // Single row
        html.append("  height: 98vh;"); // Set the container to full viewport height
        html.append("  gap: 5px;");     // Add a small gap around the window
        html.append("}");

        // Define style for the big window
        html.append(".window {");
        html.append("  border: 1px solid #000;");
        html.append("  padding: 10px;"); // Larger padding for the big window
        html.append("}");

        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Create the big window
        html.append("<div class='window'>");
        html.append("Big Window");
        html.append(graph);
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        try {
            FileWriter writer = new FileWriter("C:\\HTMLproj2\\1Window.html");
            writer.write(html.toString());
            writer.close();
            System.out.println("HTML file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return html.toString();
    }
}
	

