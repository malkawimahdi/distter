import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;


public class Post
{
    long UnixTimeStamp;
    String author;
    public ArrayList<String> tags ; // Create an ArrayList object
    String contents;

    public Post(String author, String contents)
    {
        this.UnixTimeStamp = System.currentTimeMillis() / 1000L;
        this.author = author;
        this.tags = new ArrayList<>();
        this.contents = contents;
    }

    public String generatePostID() throws NoSuchAlgorithmException
    {
       String text = getCreatedUnixTimeStamp() + getAuthor() + getContents();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public long getCreatedUnixTimeStamp() {
        return UnixTimeStamp;
    }

    public String getAuthor() {
        return author;
    }

    public String getContents() {
        return contents;
    }

}
