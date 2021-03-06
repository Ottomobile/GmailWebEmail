import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class HttrackWebpageRetriever {
	/**
	 * Retrieve webpage using Httrack
	 * 
	 * @param webpageURL 		URL of the webpage to retrieve
	 * @param fileDirParent 	Parent directory of the directory that will store the retrieved webpage
	 * @param fileDirWebpage	Directory that will store the retrieved webpage
	 * @param options 		Additional options (filters, recursion level, etc.)
	 * @throws IOException
	 */
	public static void retrieveWebpageHttrack(String webpageURL, String fileDirParent, String fileDirWebpage, String options)
		throws IOException
	{
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(String.format("/opt/local/bin/httrack %s -O %s%s %s", webpageURL, fileDirParent, fileDirWebpage, options));
		
		// Print output of process
		String s = null;
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		while((s = stdInput.readLine()) != null){
			System.out.println(s);
		}
	}
	
	/**
	 * Zip a folder
	 * 
	 * @param fileDirParent		Parent directory containing the directory you want to zip
	 * @param fileDirWebpage 	Directory to zip
	 * @param zipName		Name of the zipped directory
	 * @throws IOException
	 */
	public static void zipFolder(String fileDirParent, String fileDirWebpage, String zipName)
		throws IOException 
	{
		Runtime rt = Runtime.getRuntime();
		File workingDirectory = new File(fileDirParent).getAbsoluteFile();
		Process process = rt.exec(String.format("zip -r %s %s", zipName, fileDirWebpage), null, workingDirectory);
		
		// Print output of process
		String s = null;
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		while((s = stdInput.readLine()) != null){
			System.out.println(s);
		}
		
		System.out.printf("Finished zipping %s%s directory to %s\n", fileDirParent, fileDirWebpage, zipName);
	}
}

