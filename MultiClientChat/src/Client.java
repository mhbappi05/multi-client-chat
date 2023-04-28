import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    private static String clientName;

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to Console Group Messaging!");

        // Initialize client socket
        Socket clientSocket = new Socket(SERVER_ADDRESS, SERVER_PORT);

        // Get input and output streams
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);

        // Get client name from user input
        System.out.print("Enter your name: ");
        clientName = inputReader.readLine();

        // Send client name to server
        outputWriter.println(clientName);

        // Start a new thread to read messages from server
        Thread serverThread = new Thread(new ServerHandler(clientSocket));
        serverThread.start();

        // Read input from user and send to server
        String userInput;
        while ((userInput = inputReader.readLine()) != null) {
            // Send message to server
            outputWriter.println(userInput);

            // Write message to file
            Path filePath = Paths.get("client.txt");
            String message = clientName + ": " + userInput;
            Files.write(filePath, message.getBytes(), StandardOpenOption.APPEND);

            // Quit on "exit" command
            if (userInput.equals("exit")) {
                System.out.println("Goodbye!");
                break;
            }
        }

        // Close streams and socket
        outputWriter.close();
        inputReader.close();
        clientSocket.close();
    }


    static class ServerHandler implements Runnable {
        private Socket socket;

        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String serverMessage;
                while ((serverMessage = inputReader.readLine()) != null) {
                    System.out.println(serverMessage);

                    // Write message to file
                    Path filePath = Paths.get("client.txt");
                    Files.write(filePath, serverMessage.getBytes(), StandardOpenOption.APPEND);
                }

                // Close input stream
                inputReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
