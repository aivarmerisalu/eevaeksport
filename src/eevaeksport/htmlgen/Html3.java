package eevaeksport.htmlgen;
import java.io.FileWriter;
import java.io.IOException;


public class Html3 {
	public static void main(String[] args) {
        String htmlContent = generateHTML();
        try {
            FileWriter writer = new FileWriter("C:\\HTMLproj2\\3Window.html");
            writer.write(htmlContent);
            writer.close();
            System.out.println("HTML file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append(".container {");
        html.append("  display: grid;");
        html.append("  grid-template-columns: repeat(2, 1fr);"); // Divide into 2 columns
        html.append("  grid-template-rows: repeat(2, 1fr);");    // Divide into 2 rows
        html.append("  height: 100vh;"); // Set the container to full viewport height
        html.append("  gap: 5px;");     // Add a small gap between windows
        html.append("}");

        // Define styles for the top 2 windows
        for (int i = 1; i <= 2; i++) {
            html.append(".window").append(i).append(" {");
            html.append("  border: 1px solid #000;");
            html.append("  padding: 5px;"); // Reduce the padding
            html.append("}");
        }

        // Define style for the bottom larger window
        html.append(".window3 {");
        html.append("  border: 1px solid #000;");
        html.append("  padding: 10px;"); // Larger padding for the bigger window
        html.append("}");

        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Create the top 2 windows
        for (int i = 1; i <= 2; i++) {
            html.append("<div class='window").append(i).append("'>");
            html.append("Window ").append(i);
            html.append("</div>");
        }

        // Create the bottom larger window
        html.append("<div class='window3'>");
        html.append("Bigger Window");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
