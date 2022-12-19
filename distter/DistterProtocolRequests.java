import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class DistterProtocolRequests
{
    /**
     * Initialise a new client. To run the client, call run().
     */
    public DistterProtocolRequests() {}

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        DistterProtocolRequests protocolRequests = new DistterProtocolRequests();
        protocolRequests.run();
    }

    /*** Output the result ***/

    public void run() throws IOException
    {
        final String host = "distter.city.ac.uk";

        final int port = 20111;

        System.out.println("TCPClient connecting to " + host + ":" + port);
        Socket clientSocket = new Socket(host, port);

        // Set up readers and writers for convenience
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

        String initialMessage = reader.readLine();
        System.out.println("Server said: " + initialMessage);

        // Actual communication goes here...
        System.out.println("Requesting connection start to " + host);

        System.out.println("What would you like the host name to be? ");

        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        String userHostName = scanner.nextLine();
        //System.out.println(userHostName);

        writer.write("HELLO? DISTTER/1.0 " + userHostName + "\n");
        writer.flush();

        System.out.println("Retrieving Current Server Time...");
        writer.write("WHEN?\n");
        writer.flush();
        System.out.println(reader.readLine());

        System.out.println("Retrieving ALL Posts EVER...");
        writer.write("POSTS? 0 0\n");
        writer.flush();

        String options = reader.readLine();
        int optionsCount = Integer.parseInt(options.substring(8));

        System.out.println("OPTIONS " + optionsCount);
        for (int i = 0; i < optionsCount; i++)
        {
            System.out.println(reader.readLine());
        }

        System.out.println("Please enter the SHA-256 Hash of the post you would like to view");

        String userHash = scanner.nextLine();
        writer.write("FETCH? " + userHash + "\n");
        writer.flush();

        //Terminates connection after 10s.
        long time = System.currentTimeMillis();

        while (System.currentTimeMillis() - time < 2500)
        {
            System.out.println(reader.readLine());
        }

        clientSocket.shutdownInput();
        clientSocket.shutdownOutput();
        clientSocket.close();
    }
}

