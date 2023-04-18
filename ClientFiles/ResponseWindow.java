package ClientFiles;

import javax.swing.*;

public class ResponseWindow extends JFrame {
    private JTextArea messageArea;

    public ResponseWindow() {
        // Set up the window
        setTitle("Message Window");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create the text area for displaying responses
        messageArea = new JTextArea();
        add(new JScrollPane(messageArea));

        // Show the window
        setVisible(true);
    }

    public void updateResponse(String response) {
        // Update the text area with the response
        messageArea.append(response + "\n");
    }
}