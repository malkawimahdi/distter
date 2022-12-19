import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistterServer
{

    //Creates a default constructor for the DistterServer class.
    public DistterServer() {}

    public static void main(String[] args) throws IOException
    {
        //Instantiate an instance of the DistterServer class.
        DistterServer server = new DistterServer();
        server.run();
    }

    //run function for the DistterServer class. NOT the same as the ClientThread class.
    public void run() throws IOException
    {
        final int port = 20111;

        // Set up to accept incoming TCP connections
        // Creates a new socket object taking the port number as a parameter.
        System.out.println("Opening the server socket on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);

        //Whilst true, a new connection will be accepted.
        while (true)
        {
            // Waits until a client connects
            System.out.println("Server waiting for client...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            //New thread will be created which takes the clientSocket and calls the start method which extends the
            //thread class.
            new ClientThreads(clientSocket).start();
        }
    }

    //class ClientThreads allows multithreading by allowing an unset amount of connections to the server.
    static class ClientThreads extends Thread
    {
        Socket clientSocket;

        ClientThreads(Socket socket) {
            clientSocket = socket;
        }

        //Colour Red used within UNIX terminals to display ERROR. Does NOT work on WINDOWS!
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_RED = "\u001B[31m";
        //Overridden thread function which includes the functionality of the DISTTER protocol.
        @Override
        public void run()
        {
            try {
                // Set up readers and writers for convenience
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

                //First message written is the Server name which has been specified as Server.
                // As specified in protocol specification.
                writer.write("HELLO? DISTTER/1.0 Server\r\n");
                writer.flush();

                String firstMessage = reader.readLine();
                String helloMessage = firstMessage.substring(0, 18);

                // First message of protocol should be this. (Similar to HTTP)
                if (helloMessage.equals("HELLO? DISTTER/1.0"))
                {
                    String variableName = firstMessage.substring(19);

                    //Regex that accepts ANYTHING apart from whitespace and silent characters.
                    Pattern pattern = Pattern.compile("\\S$");
                    Matcher matcher = pattern.matcher(variableName);
                    if (!matcher.find())
                    {
                        clientSocket.close();
                    }

                    //Creates an ArrayList object that stores contains a collection of Post objects.
                    ArrayList<Post> posts = new ArrayList<>();

                    Post firstPost = new Post("martin.brain@city.ac.uk", """
                            Hello Everyone!
                            Welcome to Distter!
                            """);
                    firstPost.tags.add("#distter");
                    firstPost.tags.add("#IN2011");
                    firstPost.tags.add("#2022");

                    Post secondPost = new Post( "mahdi.malkawi@city.ac.uk", "Creation of a new post\n");
                    secondPost.tags.add("#IN2011");

                    //Adds the posts into an ArrayList of type Post
                    posts.add(firstPost);
                    posts.add(secondPost);

                    while (true)
                    {
                        //The string variable message has been initialised to store the value that is obtained by the reader.
                        String message = reader.readLine();

                        /*IF statement that checks if the whole readline contains these keywords, if so another string variable
                        called substring will be initialised and used to split the string such that it can be analysed
                        based on the requirements of the RFC. */
                        if (message.contains("POSTS?") || message.contains("FETCH?"))
                        {
                            //This string variable is a substring which stores the value of the reader up to the first space.
                            String subString = message.substring(0, message.indexOf(' '));

                            if (subString.equals("POSTS?"))
                            {
                                //Another string variable will continue after the whitespace to accept arguments.
                                String postSubString = message.substring(6);

                                //The arguments of the POSTS? is split using the regex to check for whitespace and
                                //stored in a fixed length array.
                                String[] postSubStringSplitResult = postSubString.split("\\s");

                                //Converts the int value into an unsigned integer. If unable to do so, then a NumberFormatException is thrown.
                                long postSinceUnixTimeStamp = Integer.parseInt(Integer.toUnsignedString(Integer.parseInt(postSubStringSplitResult[1])));
                                int postHeader = Integer.parseInt(Integer.toUnsignedString(Integer.parseInt(postSubStringSplitResult[2])));

                                /* A new array list called potentialPosts is created */
                                ArrayList<Post> potentialPosts = new ArrayList<>();

                                //Enhanced FOR loop iterates the ArrayList.
                                for (Post post : posts)
                                {
                                    /* IF statement that checks if the post timestamp is greater than the header timestamp, if the
                                    condition of the first header is met then the additional IF and FOR loops will be used to check
                                    if any posts meet the condition of the second header. Post is then added to potential posts*/
                                    long currentUnixTimestamp = Instant.now().getEpochSecond();

                                    if (post.getCreatedUnixTimeStamp() > postSinceUnixTimeStamp && postSinceUnixTimeStamp <= currentUnixTimestamp)
                                    {
                                        potentialPosts.add(post);
                                    } else
                                    {
                                        writer.write( ANSI_RED + "ERROR: FUTURE UNIX EPOCH TIMESTAMP HAS BEEN PROVIDED" + ANSI_RESET + "\n");
                                        writer.flush();
                                        clientSocket.close();
                                    }
                                }

                                /*For loop which takes the value from the postHeader and decrements downwards, which is used
                                as the condition to look ONLY to where the hashtags are supposed to be. */
                                for (int decrement = postHeader; decrement > 0; --decrement)
                                {
                                    String searchTerm = reader.readLine();

                                    //A new array list is created which stores the new Potential posts.
                                    ArrayList<Post> newPotentialPosts = new ArrayList<>();

                                    //For loop iterates on the potential posts arraylist.
                                    for (int counter2 = 0; counter2 < potentialPosts.size(); ++counter2)
                                    {
                                        //Stores the current post at the counter into the appropriately named variable.
                                        Post currentPost = posts.get(counter2);

                                        /*For loop that iterates upon the ArrayList called tags that is stored within the
                                        currentPost object. */
                                        for (int counter3 = 0; counter3 < currentPost.tags.size(); ++counter3)
                                        {
                                            //Stores the current tag in a String variable.
                                            String currentTag = currentPost.tags.get(counter3);

                                            /*If the currentTag is equal to the search term then, it is added to the
                                            newPotentialPosts ArrayList */
                                            if (currentTag.equals(searchTerm))
                                            {
                                                newPotentialPosts.add(currentPost);
                                            }
                                        }
                                    }

                                /*The original array, which was empty now only contains posts which match the
                                first search term. If there are more search terms, then this array will reduce or
                                be equal to the current size. */
                                    potentialPosts = newPotentialPosts;
                                }
                                //Sends to the peer the number of options.
                                writer.write("OPTIONS " + potentialPosts.size() + "\n");

                                //Sends to the peer the posts that have met the criteria of tags.
                                for (Post currentPost : potentialPosts)
                                {
                                    writer.write("SHA-256 " + currentPost.generatePostID() + "\n");
                                }
                                writer.flush();

                            } else if (subString.equals("FETCH?"))
                            {
                                //Stores the SHA-256 + hash which is a subset of the message stored in subStringHash
                                String subStringHash = message.substring(7);

                                //Creates an array which stores the result of the hash being split.
                                String[] subStringHashSplitResult = subStringHash.split("\\s");

                                //Search hash variables takes the value of the content of position[1] of the subStringHashSplitResult array.
                                String searchHash = subStringHashSplitResult[1];

                                if (!subStringHashSplitResult[0].equals("SHA-256"))
                                {
                                    writer.write(ANSI_RED + "ERROR: UNDEFINED CHARACTER(S) ENTERED. EXPECTED KEYWORD SHA-256" + ANSI_RESET + "\n");
                                    writer.flush();
                                    clientSocket.close();
                                }

                                if (subStringHashSplitResult[1].length() != 64)
                                {
                                    writer.write(ANSI_RED + "ERROR: SHA-256 UNSUPPORTED CHARACTER LENGTH" + ANSI_RESET + "\n");
                                    writer.flush();
                                    clientSocket.close();
                                }

                                System.out.println(subStringHashSplitResult[0]);
                                System.out.println(subStringHashSplitResult[1]);

                                int postCounter = 0;
                                //For loop which iterates the array list which stores all posts.
                                for (Post post : posts)
                                {
                                    ++postCounter;

                                    /*Checks if the userDefined hash is equal to the stored hash. If so, then the details of the
                                    post are then sent to the peer. */
                                    if (searchHash.equals(post.generatePostID()))
                                    {
                                        writer.write("FOUND\n");
                                        writer.write("Post-id: SHA-256 " + post.generatePostID() + "\n");
                                        writer.write("Created: " + post.getCreatedUnixTimeStamp() + "\n");
                                        writer.write("Author: " + post.getAuthor() + "\n");

                                        for (int i = 0; i < posts.get(i).tags.size(); i++)
                                        {
                                            posts.get(i).tags.forEach(currentTag ->
                                            {
                                                try
                                                {
                                                    writer.write(currentTag + "\n");
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            });

                                        }
                                        //Creates a new InputStream from the String stored within the Post contents variable.
                                        InputStream contentLineStream = new ByteArrayInputStream(post.getContents().getBytes(StandardCharsets.UTF_8));
                                        BufferedReader readerContentLines = new BufferedReader(new InputStreamReader(contentLineStream));

                                        int lines = 0;
                                        //While loop is strictly used to only count the number of lines within the content.
                                        while (readerContentLines.readLine() != null)
                                        {
                                            ++lines;
                                        }
                                        readerContentLines.close();

                                        //Output the contents lines to the peer.
                                        writer.write("Contents: " + lines + "\n");
                                        writer.write(post.getContents());
                                        writer.flush();
                                        break;
                                    }


                                    /*ELSE IF that checks that the searchHash is not equals to post ID. It will only
                                     perform an action on the last element within the array, in which the peer is
                                     notified that the post does not exist and a break clause is used to ensure
                                     that this header is not repeated.*/
                                    int postsArraySize = posts.size();
                                    if (postCounter == postsArraySize && !searchHash.equals(post.generatePostID()))
                                    {
                                            writer.write("SORRY\n");
                                            writer.flush();
                                            break;
                                    }
                                }
                            }
                        }

                        //The protocol will enter this state is the WHOLE message only includes the string specified below.
                        if (message.equals("WHEN?"))
                        {
                            //Creates a long unixTimeStamp which stores the epoch time specified by the Instant Class.
                            long unixTimestamp = Instant.now().getEpochSecond();

                            //Outputs unix time stamp to peer.
                            writer.write("NOW " + unixTimestamp + "\n");
                            writer.flush();

                            //Closes server socket if the WHOLE message equals GOODBYE!
                        } else if (message.equals("GOODBYE!"))
                        {
                            clientSocket.close();
                        }
                    }
                } else
                {
                    writer.write(ANSI_RED + "ERROR: CONNECTION TERMINATED DUE TO ILLEGITIMATE FIRST MESSAGE" + ANSI_RESET + "\n");
                    writer.close();
                    clientSocket.close();
                }
            } catch (IOException | NoSuchAlgorithmException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}