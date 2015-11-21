import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;


public class HTMLRetriever {
	/**
	 * Retrieve a webpage as a HTML file
	 * 
	 * @param webpageURL 	Webpage's url
	 * @param directory 	Directory to store the retrieved webpage
	 * @param filename 	File name of the webpage's HTML contents
	 */
	public static void RetrieveWebpage(String webpageURL, String directory, String filename) {
		  try{
		        URL url = new URL(webpageURL);
		        URLConnection urlConnection = url.openConnection();
		        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		        PrintWriter out = new PrintWriter(String.format("%s%s", directory, filename));
		        
		        String inputLine;
		        while ((inputLine = in.readLine()) != null) {
		        	// System.out.println(inputLine);
		        	out.println(inputLine);
		        }
		            
		        in.close();
		        out.close();
		        System.out.println("Successfully retrieved HTML file\n");
		  }
		  catch(Exception e){
			  System.out.println("Error retrieving webpage from url\n");
		  }
	  }
}

