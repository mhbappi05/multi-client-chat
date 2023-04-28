import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private Hashtable<Socket, String> clients = new Hashtable<>();
    private ServerSocket serverSocket;
    private boolean running = true;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;

    public static void main(String[] args) {
        Server server = new Server();
        server.runServer();
    }

    public void runServer() {
        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server started on port 8080...");

            // Initialize FileWriter and BufferedWriter for server event messages
            fileWriter = new FileWriter("server.txt", true);
            bufferedWriter = new BufferedWriter(fileWriter);

            while (running) {
                Socket socket = serverSocket.accept();
                String clientName = "Client-" + clients.size();
                clients.put(socket, clientName);
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();

                // Send all previous messages to the new client
                sendPreviousMessages(socket);
                System.out.println(clientName + " connected!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stopServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPreviousMessages(Socket socket) throws IOException {
        Enumeration<String> e = clients.elements();
        while (e.hasMoreElements()) {
            String clientName = e.nextElement();
            if (!clientName.equals(clients.get(socket))) {
                String message = "Server: " + clientName + " joined the chat";
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(message);
            }
        }
    }

    public void stopServer() throws IOException {
        running = false;
        serverSocket.close();
        fileWriter.close();
        bufferedWriter.close();
    }

    private class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String clientName = clients.get(socket);

                while (true) {
                    String message = reader.readLine();
                    if (message == null) {
                        throw new SocketException(); // Trigger catch block when socket is closed
                    }
                    message = clientName + ": " + message;
                    broadcast(message, socket);
                }
            } catch (SocketException e) {
                System.out.println("Client disconnected unexpectedly: " + e.getMessage());
                clients.remove(socket); // Remove disconnected client from clients Hashtable
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void broadcast(String message, Socket socket) throws IOException {
        for (Enumeration<Socket> e = clients.keys(); e.hasMoreElements();) {
            Socket clientSocket = e.nextElement();
            if (!clientSocket.equals(socket)) {
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                writer.println(message);
            }
        }
        bufferedWriter.write(message + "\n");
        bufferedWriter.flush();
        System.out.println(message);
    }
}