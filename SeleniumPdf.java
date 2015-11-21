import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;


public class SeleniumPdf {
	static final String FILETYPE = ".pdf";
	static final String DOWNLOAD_DIR = "/Users/OL/Downloads";
	
	/**
	 * Download a webpage as a pdf
	 * 
	 * @param webpageUrl Url of the webpage to download as a pdf
	 * @return File object of the downloaded pdf
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static File downloadPdf(String webpageUrl) throws IOException, InterruptedException {
		// Get the initial number of files in the download directory with the specific filetype
		int numOfFilesInitial = getSpecificFileType(DOWNLOAD_DIR, FILETYPE).size();
		System.out.printf("Number of %s files in %s: %d\n", FILETYPE, DOWNLOAD_DIR, numOfFilesInitial);
		
		// Run the Java program to download webpage to pdf
		Runtime rt = Runtime.getRuntime();
		File workingDirectory = new File("/Users/OL/Documents/GmailWebSeleniumPDF").getAbsoluteFile();
		String[] commandLineArgs = {"java",
					    "-cp",
			         	    ".:selenium-java-2.48.2-srcs.jar:selenium-java-2.48.2.jar:selenium-server-standalone-2.48.2.jar:junit-4.12.jar",
					    "SeleniumDownloadPdf",
					    webpageUrl
					   };
		Process process = rt.exec(commandLineArgs, null, workingDirectory);
		System.out.println("Waiting for webpage to pdf conversion");
		process.waitFor();
		System.out.println("Downloaded pdf");
		
		// Ensure that while loop below will eventually terminate if download fails
		LocalTime startTime = LocalTime.now();
		LocalTime stopTime = startTime.plusSeconds(30);
		
		// Wait for the pdf to download
		while(numOfFilesInitial == getSpecificFileType(DOWNLOAD_DIR, FILETYPE).size()){
			if(LocalTime.now().compareTo(stopTime) > 0){
				System.out.println("Could not download file");
				return null;
			}
		}
		
		// Get the downloaded file
		ArrayList<File> pdfFiles = getSpecificFileType(DOWNLOAD_DIR, FILETYPE);
		File lastModifiedFile = lastFileModified(pdfFiles);
		System.out.println("Last modified file: " + lastModifiedFile.getName());
		
		return lastModifiedFile;
	}
	
	
	/**
	 * Get a list of all the files in the directory with a specific filetype
	 * 
	 * @param directory 	Directory where the files are contained
	 * @param filetype 	Extension of the files to retrieve ({.pdf}, {.doc}, etc.)
	 * @return List of files of the specified filetype
	 */
	public static ArrayList<File> getSpecificFileType(String directory, String filetype){
		File[] folder = new File(directory).listFiles();
		ArrayList<File> targetFiles = new ArrayList<File>();
		
		if( folder.length != 0) {
			for(File file : folder){
				String name = file.getName();
		    	int pos = name.lastIndexOf(".");
		    	if (pos > 0) {
		    		// Extract the extension of the file
		    	    name = name.substring(pos, name.length());
		    	    
		    	    // Determine if the extracted extension matches the specified filetype
		    	    if(name.equals(filetype)){
		    	    	targetFiles.add(file);
		    	    }
		    	}
			}
		}
		return targetFiles;
	}
	
	
	/**
	 * Get the last modified file from the list of files
	 * 
	 * @param files List of files to examine
	 * @return Last modified file of the list
	 */
	public static File lastFileModified(ArrayList<File> files) {
	    if(files != null) {
		    long lastMod = Long.MIN_VALUE;
		    File choice = null;
		    
		    for (File file : files) {
		    	String name = file.getName();
		    	int pos = name.lastIndexOf(".");
		    	if (pos > 0) {
		    	    name = name.substring(0, pos);
		    	}
		    	
		        if (file.lastModified() > lastMod) {
		            choice = file;
		            lastMod = file.lastModified();
		        }
		    }
		    return choice;
	    }
	    return null;
	}
}

