package eevaeksport.htmlgen;
import java.io.FileWriter;
import java.io.IOException;

public class Html4 {
	public static void main(String[] args) {
        String htmlContent = generateHTML();
        try {
            FileWriter writer = new FileWriter("C:\\HTMLproj2\\4Window.html");
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
        html.append("  height: 98vh;"); // Set the container to full viewport height
        html.append("  gap: 2px;");     // Add a small gap between windows
        html.append("}");

        // Define styles for each window
        for (int i = 1; i <= 4; i++) {
            html.append(".window").append(i).append(" {");
            html.append("  border: 1px solid #000;");
            html.append("  padding: 5px;"); // Reduce the padding
            html.append("}");
        }

        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Create four windows
        for (int i = 1; i <= 4; i++) {
            html.append("<div class='window").append(i).append("'>");
            html.append("Window ").append(i);
            html.append("</div>");
        }

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}