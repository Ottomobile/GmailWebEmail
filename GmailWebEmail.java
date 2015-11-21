import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.model.ListMessagesResponse;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class GmailWebEmail {
    // Application name
    private static final String APPLICATION_NAME = "Gmail API Webpage";
    
    // Directory to store user credentials for this application
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/gmail-webpage");
    
    // Global instance of the {@link FileDataStoreFactory}
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    
    // Global instance of the JSON factory
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    
    // Global instance of the HTTP transport
    private static HttpTransport HTTP_TRANSPORT;
    
    // Global instance of the scopes for labels
    private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_LABELS,
    							     GmailScopes.GMAIL_READONLY,
    							     GmailScopes.GMAIL_MODIFY,
    							     GmailScopes.MAIL_GOOGLE_COM,
    							     GmailScopes.GMAIL_INSERT,
    							     GmailScopes.GMAIL_COMPOSE);
    
    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        }
        catch (Throwable t){
            t.printStackTrace();
            System.exit(1);
        }
    }
    
    // Interval to wait between queries to the inbox (milliseconds)
    static final int DELAY_INTERVAL = 1000;
    
    // Email constants
    static final String CLIENT_EMAIL = "firstName.lastName@hotmail.com";
    static final String SENDER_EMAIL = "firstName.lastName@gmail.com";
    static final String PARENT_DIR = "/Users/OL/Desktop/Webpages/";
    
    // Methods for retrieving webpage
    static final char HTML_RETRIEVER_METHOD = '1';
    static final char HTTRACK_METHOD = '2';
    static final char SELENIUM_GOOGLE = '3';
    static final char SELENIUM_PDF = '4';
    
    // Default option settings for Httrack
    static final String MAX_FILE_SIZE = " -M5000000"; // bytes
    static final String DEFAULT_RECURSION_LEVEL = " -r1";
    
    public static void main(String[] args) throws IOException, InterruptedException {
        // Build a new authorized API client service
        Gmail service = getGmailService();
        
        // Time to stop execution of this program
        LocalTime stopTime = LocalTime.of(16, 00, 00);
  	  	System.out.printf("Stop Time: %s\n", stopTime.toString());
  	  	
  	  	// Regex to find keywords
  	  	Pattern STOPpattern = Pattern.compile("(?i)STOPSTOP");
  	  	Pattern URLpattern = Pattern.compile("^(https?:\\/\\/)");
	  
  	  	while(LocalTime.now().compareTo(stopTime) < 0) {
        	System.out.printf("Current time: %s\n", LocalTime.now());
		  
	        // Get only the unread non-junk emails from a specific user and with a particular subject heading
	        String query = String.format("in:inbox is:unread -category:(promotions OR social) from:%s", CLIENT_EMAIL);
	        List<Message> unreadMessages = listMessagesMatchingQuery(service,"me",query);
	        
	        if( unreadMessages.size() > 0){
	        	System.out.println("There are " + unreadMessages.size() + " unread messages");
	        	
	        	// Print out the ids of the unread messages
	            for (Message unreadMessage : unreadMessages) {
	            	System.out.printf("Unread Message Id:\n\t%s\n", unreadMessage.getId());
	            	
	            	// Need to get additional info from unread message
	          	    Message currentMessage = service.users().messages().get("me", unreadMessage.getId()).execute();
	          	    String currentMessageSubject = getPayloadHeaderInfo(currentMessage, "Subject");
	          	    String currentMessageBody = currentMessage.getSnippet();
	          	    
	                // Prepare the message to be sent
	          	    String recipientEmail = getPayloadHeaderInfo(currentMessage, "From");
	                String emailSubject = "Reply to: " + currentMessageSubject;
	                String emailBody = "Query: " + currentMessageBody;
	      
	          	    // If the received email has the subject 'StopStop', terminate the program
		          	Matcher STOPmatcher = STOPpattern.matcher(currentMessageSubject);
	        	    while(STOPmatcher.find()) {
	        	    	try {
		        	    	// Create the email notifying that the program will be stopped and then sent it
		                	MimeMessage messageToBeSent = createMimeMessage(recipientEmail, SENDER_EMAIL, emailSubject, emailBody, null, null);
		                	sendMessage(service, "me", messageToBeSent);
	        	    	}
	        	    	catch (Exception e) {
	        	    		e.printStackTrace();
	        	    	}
	        	    	finally {
	        	    		// Mark retrieved unread email as read and move message under Label_4 ("Work")
			                List<String> labelsToAdd = Arrays.asList("Label_4");
			                List<String> labelsToRemove = Arrays.asList("UNREAD","INBOX","IMPORTANT");
			                modifyMessage(service, "me", unreadMessages.get(0).getId(), labelsToAdd, labelsToRemove );
			                System.out.println("Marked received message as read and moved it under the label 'Work'\n");
			                
			                System.out.printf("STOP indicator found.\nTerminating Program.\n");
		        	    	System.exit(1);
	        	    	}
	        	    }
	          	    
	        	    // Get the retrieval method from the subject of the email
	        	    char retrievalMethod = currentMessageSubject.charAt(0);
	        	    
	        	    // See if email body contains valid url from which to retreive the webpage
	          	    boolean URLpatternFound = false;
	        	    Matcher URLmatcher = URLpattern.matcher(currentMessageBody);
	        	    while(URLmatcher.find()) {
	        	  	  System.out.printf("Valid URL found.%n");
	        		  URLpatternFound = true;
	        	    }
	        	    if(!URLpatternFound) {
	        		 	System.out.printf("No URL found.%n");
	        	    }
	        	    
	        	    try {
	        	    	// Check if retrieval method is specified
	        	    	String[] subjectParts = currentMessageSubject.split("-",2);
	    	    		if (subjectParts.length != 2) {
	    	    			// Send email stating that retrieval method was not indicated
	    	    			emailBody += "\nRetrieval method not indicated.\n";
	    	    			MimeMessage messageToBeSent = createMimeMessage(recipientEmail, SENDER_EMAIL, emailSubject, emailBody, null, null);
		                	sendMessage(service, "me", messageToBeSent);
	    	    		}
	    	    		
	    	    		else if (retrievalMethod == SELENIUM_GOOGLE) {
	        	    		String searchQuery = currentMessageBody;
	        	    		String projectFilename = subjectParts[1]+".html";
	        	    		
	        	    		Runtime rt = Runtime.getRuntime();
	        	    		File workingDirectory = new File("/Users/OL/Documents/GmailWebSeleniumGoogle").getAbsoluteFile();
	        	    		String[] commandLineArgs = {"java",
	    							    "-cp",
	    					 		    ".:selenium-java-2.48.2-srcs.jar:selenium-java-2.48.2.jar:selenium-server-standalone-2.48.2.jar:junit-4.12.jar",
	    							    "GmailWebSelenium",
	    						   	    searchQuery, 
	    							    PARENT_DIR, 
	    							    projectFilename
	    							   };
	        	    		Process process = rt.exec(commandLineArgs, null, workingDirectory);
	        	    		
	        	    		// Print the output of this process
	        	    		String s = null;
	        	    		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        	    		while((s = stdInput.readLine()) != null){
	        	    			System.out.println(s);
	        	    		}
	        	    		
	        	    		// Send email containing an html file with Google search results
		                	MimeMessage messageToBeSent = createMimeMessage(recipientEmail, SENDER_EMAIL, emailSubject, emailBody, PARENT_DIR, projectFilename);
		                	sendMessage(service, "me", messageToBeSent);
	        	    	}
	        	    	
	    	    		else if (URLpatternFound) {
	        	    		MimeMessage messageToBeSent;
	        	    		
	        	    		switch (retrievalMethod) {
	        	    		
	        	    		case HTML_RETRIEVER_METHOD :
		        	    		String filename = subjectParts[1] + ".html";
		        	    		
			                	// Retrieve the webpage specified in the body of the client's email
			              	    HTMLRetriever.RetrieveWebpage(currentMessageBody, PARENT_DIR, filename);
			                	
			                	// Send the email with the attached html webpage
			                	messageToBeSent = createMimeMessage(recipientEmail, SENDER_EMAIL, emailSubject, emailBody, PARENT_DIR, filename);
			                	sendMessage(service, "me", messageToBeSent);
	        	    			break;
	        	    		
	        	    		case HTTRACK_METHOD :
		        	    		String projectDir = subjectParts[1];
		        	    		String webpageURL;
		        	    		String options;
		        	    		
	        	    			String[] bodyParts = currentMessageBody.split(" ",2);
	        	    			if (bodyParts.length != 2) {
	        	    				System.out.println("No options found. Using Httrack defaults");
	        	    				emailBody = emailBody + "\nOriginal message did not contain recursion level. Used default:" + DEFAULT_RECURSION_LEVEL;
		        	    			webpageURL = currentMessageBody;
		        	    			options = "";
	        	    			}
	        	    			else{
		        	    			webpageURL = bodyParts[0];
		        	    			options = bodyParts[1];
	        	    			}
		        	    		
		        	    		// Ensure the email specifies a practical recursion depth so that Httrack will eventually finish
		        	    		boolean recursionLevelValid = false;
		        	    		Pattern recurionLevelPattern = Pattern.compile("-r[1-3]");
		        	    		Matcher recurionLevelMathcer = recurionLevelPattern.matcher(options);
		    	        	    while(recurionLevelMathcer.find()) {
		    	        	    	recursionLevelValid = true;
		    	        	    }
		    	        	    
		    	        	    // Retrieve the webpage using Httrack
		        	    		if (!recursionLevelValid) {
		        	    			// Use the default recursion level of 1
		        	    			HttrackWebpageRetriever.retrieveWebpageHttrack(webpageURL, PARENT_DIR, projectDir, DEFAULT_RECURSION_LEVEL + MAX_FILE_SIZE);
		        	    		}
		        	    		else {        	    		
		        	    			HttrackWebpageRetriever.retrieveWebpageHttrack(webpageURL, PARENT_DIR, projectDir, options + MAX_FILE_SIZE);
		        	    		}
		        	    		
		        	    		// Zip the retrieved webpage files so that they could be easily attached and sent
		        	    		HttrackWebpageRetriever.zipFolder(PARENT_DIR, projectDir, projectDir+".zip");
		        	    		
		        	    		// Send email with the attached webpage zip
			                	messageToBeSent = createMimeMessage(recipientEmail, SENDER_EMAIL, emailSubject, emailBody, PARENT_DIR, projectDir+".zip");
			                	sendMessage(service, "me", messageToBeSent);
			                	break;
			                
	        	    		case SELENIUM_PDF : 
		        	    		String webpageUrl = currentMessageBody;
		        	    		File downloadedPdf = SeleniumPdf.downloadPdf(webpageUrl);
		        	    		String downloadDir = downloadedPdf.getParent()+"/";
		        	    		String downloadedFilename = downloadedPdf.getName();
		        	    		
		        	    		// Create the email with the attached html file containing the Google search results and then send it
			                	messageToBeSent = createMimeMessage(recipientEmail, SENDER_EMAIL, emailSubject, emailBody, downloadDir, downloadedFilename);
			                	sendMessage(service, "me", messageToBeSent);
			                	break;
			                	
			                default:
			                	break;
	        	    		}
	        	    	}
	        	    }
	                catch (Exception e) {
	                	System.out.println("An exception occurred: " + e.getMessage());
				try {
					// Create the email notifying that exception occurred and then send it
					MimeMessage messageToBeSent = createMimeMessage(recipientEmail, SENDER_EMAIL, "EXCEPTION: "+emailSubject, emailBody, null, null);
					sendMessage(service, "me", messageToBeSent);
				} 
				catch (MessagingException e1) {
					e1.printStackTrace();
				}
	                }
	    		finally {
		                // Mark retrieved unread email as read and move message under Label_4 ("Work")
		                List<String> labelsToAdd = Arrays.asList("Label_4");
		                List<String> labelsToRemove = Arrays.asList("UNREAD","INBOX","IMPORTANT");
		                modifyMessage(service, "me", unreadMessages.get(0).getId(), labelsToAdd, labelsToRemove );
		                System.out.println("Marked received message as read and moved it under the label 'Work'");
        	    	}
	            }
	        }
	        else {
        		System.out.printf("No unread messages from %s\n", CLIENT_EMAIL);
	        }
	        
	        // Interval of time to wait before querying inbox
	        Thread.sleep(DELAY_INTERVAL);
	    }
        System.out.println("Program finished executing");
    }
    
    
    /**
     * Prints the labels from the user's inbox.
     * 
     * @param service 	Authorized Gmail API instance.
     * @param userId 	User's email address.  Special value "me" indicates the authenticated user.
     */
    public static void printLabels(Gmail service, String userId) throws IOException {
    	ListLabelsResponse listResponse = service.users().labels().list(userId).execute();
        List<Label> labels = listResponse.getLabels();
        if(labels.size() == 0) {
            System.out.println("No labels found");
        }
        else {
            System.out.println("Labels:");
            for (Label label : labels) {
                System.out.printf("- %s: %s\n", label.getId(), label.getName());
            }
        }
        System.out.println("Finished getting labels\n");
    }
    
    
    /**
     * Creates an authorized Credential object
     * 
     * @return an authorized Credential object
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        //Load client secrets
        InputStream in = GmailWebEmail.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        
        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(DATA_STORE_FACTORY)
            .setAccessType("offline")
            .build();
        
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        
        System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath() +"\n");
        
        return credential;
    }
    
    
    /**
     * Build and return an authorized Gmail client service.
     * 
     * @return an authorized Gmail client service
     * @throws IOException
     */
    public static Gmail getGmailService() throws IOException {
        Credential credential = authorize();
        return new Gmail.Builder(HTTP_TRANSPORT,JSON_FACTORY,credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }
    
    
    /**
     * List all Message of the user's mailbox matching the query.
     * 
     * @param service 	Authorized Gmail API instance.
     * @param userId 	User's email address.  Special value "me" indicates the authenticated user.
     * @param query 	String used to filter the Messages listed.
     * @throws IOException
     */
    public static List<Message> listMessagesMatchingQuery (Gmail service, String userId, String query)
    	throws IOException {
    	
    	System.out.printf("Query: %s\n", query);
    	ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();
    	
    	List<Message> messages = new ArrayList<Message>();
    	while (response.getMessages() != null) {
    		messages.addAll(response.getMessages());
    		if (response.getNextPageToken() != null) {
    			String pageToken = response.getNextPageToken();
    			response = service.users().messages().list(userId).setQ(query).setPageToken(pageToken).execute();
    		}
    		else {
    			break;
    		}
    	}
    	
    	if (messages.size() == 0) {
    		System.out.println("No messages match query");
    	}
    	else {
    		System.out.println("Ids of messages matching query");
    		for (Message message : messages) {
        		System.out.println(message.toPrettyString());
        	}
    	}
    	
    	System.out.println("Finished querying inbox\n");
    	return messages;
    }
    

    /**
     * Get the Message with the given ID.
     * 
     * @param service 	Authorized Gmail API instance.
     * @param userId 	User's email address.  Special value "me" indicates the authenticated user
     * @param messageId ID of the Message to retrieve
     * @return Message 	Retrieved Message
     * @throws IOException
     */
    public static Message getMessage(Gmail service, String userId, String messageId)
    	throws IOException {
    	
    	Message message = service.users().messages().get(userId, messageId).execute();
    	
    	System.out.printf("Message Ids:\n%s\n", message.toPrettyString());
    	System.out.printf("Message snippet:\n%s\n", message.getSnippet());
    	printPayloadHeaders(message);
    	
    	System.out.printf("Finished getting message with id: %s\n\n", messageId);
    	return message;
    }
    
    
    /**
     * Print the message's payload headers
     * 
     * @param message 	Message containing the headers you want to print out
     */
    public static void printPayloadHeaders(Message message) {
    	List<MessagePartHeader> payloadHeaders = message.getPayload().getHeaders();
    	System.out.println("Header count: " + message.getPayload().getHeaders().size());
    	for(int i = 0; i < payloadHeaders.size(); i++) {
    		MessagePartHeader payloadHeaderInfo = payloadHeaders.get(i);
    		System.out.println("Payload Header " + i + " Info");
        	System.out.println("\t Name: " + payloadHeaderInfo.getName());
        	System.out.println("\t Value: " + payloadHeaderInfo.getValue());
    	}
    	System.out.println();
    }
    
    
    /**
     * Get value of a specified header part
     * 
     * @param message 		Message that contains the header to be examined
     * @param headerPart 	Name of the header part corresponding to the value of the header part to retrieve 
     * @return Value of the specified header part
     */
    public static String getPayloadHeaderInfo(Message message, String headerPart){
    	List<MessagePartHeader> payloadHeaders = message.getPayload().getHeaders();
    	for(MessagePartHeader currentHeaderPart : payloadHeaders){
    		if(currentHeaderPart.getName().equals(headerPart)){
    			return currentHeaderPart.getValue();
    		}
    	}
    	return null;
    }
    
    
    /**
     * Create a MimeMessage using the parameters provided.
     * 
     * @param to 		Email address of the receiver.
     * @param from 		Email address of the sender.
     * @param subject 	Subject of the email.
     * @param bodyText 	Body text of the email.
     * @param fileDir 	Path of the directory containing attachment.
     * 					Passing a null value indicates no attachment.
     * @param filename 	Name of file to be attached.
     * 					Passing a null value indicates no attachment.
     * @return MimeMessage that will be used to send the email.
     * @throws MessagingException
     */
    public static MimeMessage createMimeMessage (String to, String from, String subject, String bodyText, String fileDir, String filename)
    	throws MessagingException, IOException {
    	
    	Properties props = new Properties();
    	Session session = Session.getDefaultInstance(props, null);
    	
    	MimeMessage email = new MimeMessage(session);
    	
    	// Set header info
    	email.setFrom(new InternetAddress(from));
    	email.addRecipient(javax.mail.Message.RecipientType.TO,  new InternetAddress(to));
    	email.setSubject(subject);
    	
    	if(fileDir != null && filename != null){
    		Multipart multipart = new MimeMultipart();
    		
	    	// Set body info
	    	MimeBodyPart mimeBodyPart = new MimeBodyPart();
	    	mimeBodyPart.setContent(bodyText, "text/plain");
	    	mimeBodyPart.setHeader("Content-Type",  "text/plain; charset=\"UTF-8\"");
	    	multipart.addBodyPart(mimeBodyPart);
	    	
	    	// Set attachment info
    		mimeBodyPart = new MimeBodyPart();
    		DataSource source = new FileDataSource(fileDir + filename);
    		mimeBodyPart.setDataHandler(new DataHandler(source));
    		mimeBodyPart.setFileName(filename);
    		String contentType = Files.probeContentType(FileSystems.getDefault().getPath(fileDir, filename));
    		mimeBodyPart.setHeader("Content-Type",  contentType + "; name=\"" + filename + "\"");
    		mimeBodyPart.setHeader("Content-Transfer-Encoding", "base64");
    		multipart.addBodyPart(mimeBodyPart);
    		
    		email.setContent(multipart);
    	}
    	else {
    		// For simple emails without attachments
        	email.setText(bodyText);
    	}
    	
    	System.out.println("Finished creating MimeMessage");
    	return email;
    }
    
    
    /**
     * Create Message from prepared MimeMessage.
     * 
     * @param email 	The prepared MimeMessage that will set the raw field of the Message.
     * @return Message containing base64url encoded email.
     * @throws IOException
     * @throws MessagingException
     */
    public static Message createEmailMessage (MimeMessage email)
    	throws MessagingException, IOException {
    	
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    	email.writeTo(bytes);
    	String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
    	Message message = new Message();
    	message.setRaw(encodedEmail);
    	
    	return message;
    }
    
    
    /**
     * Send an email from the user's mailbox to its recipient.
     * 
     * @param service 	Authorized Gmail API instance.
     * @param userId 	User's email address. The special value "me" indicates the authenticated user.
     * @param email 	Email to be sent.
     * @throws MessagingException
     * @throws IOException
     */
    public static void sendMessage (Gmail service, String userId, MimeMessage email)
    	throws MessagingException, IOException {
    	
    	Message message = createEmailMessage(email);
    	message = service.users().messages().send(userId, message).execute();
    	
    	System.out.println("Message id: " + message.getId());
    	System.out.println(message.toPrettyString());
    	System.out.println("Message has been sent");
    }
    
    
    /**
     * Modify the labels associated with a message
     * 
     * @param service 			Authorized Gmail API instance.
     * @param userId 			User's email address.  Special value "me" indicates authenticated user.
     * @param messageId 		ID of the Message to modify.
     * @param labelsToAdd 		List of label ids to add.
     * @param labelsToRemove 	List of label ids to remove.
     * @throws IOException
     */
    public static void modifyMessage (Gmail service, String userId, String messageId, List<String> labelsToAdd, List<String> labelsToRemove)
    	throws IOException {
    	
    	// Print the label ids associated with the messages before modifying the message's labels
    	Message targetMessage = getMessage(service, userId, messageId);
    	List<String> beforeMessageLabels = targetMessage.getLabelIds();
    	for(String currentLabel : beforeMessageLabels) {
    		System.out.println(currentLabel);
    	}
    	
    	// Modify the message's associated labels
    	ModifyMessageRequest mods = new ModifyMessageRequest().setAddLabelIds(labelsToAdd).setRemoveLabelIds(labelsToRemove);
    	Message message = service.users().messages().modify(userId, messageId, mods).execute();
    	
    	System.out.println("Message id: " + message.getId());
    	System.out.println(message.toPrettyString());
    	
    	// Print the label ids associated with the message after modifying the message's labels
    	List<String> afterMessageLabels = message.getLabelIds();
    	for(String currentLabel : afterMessageLabels) {
    		System.out.println(currentLabel);
    	}
    }
}
