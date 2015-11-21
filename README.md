# GmailWebEmail
Webpage Retriever Using Gmail API, Selenium, HTTrack and Java

This application processes a user's webpage request that was sent from their email client and then sends a response back to the user containing their desired webpage.

Application enables searching Google through Selenium to get the addresses of webpages
- Subject line should contain "3-[topic_of_search]"
- Message body contains the search keywords

Application supports the following methods of webpage retrieval

1. Retrieve plain HTML source
  - Subject line should contain "1-[filename_of_HTML]"
  - Message body contains the webpage address
  - Response email will contain the webpage attached as a plain HTML file

2. Use Httrack to mirror a website
  - Subject line should contain "2-[filename_of_zip]"
  - Message body contains the webpage address, recursion level or other HTTrack options
  - Response email will contain the mirrored webpage attached as a zip file.  Zip file organizes HTML files according to the webpage structure.
  - Setting recursion level enables the retrieval of webpages directed by the links in the main webpage

3. Download webpage as PDF using Selenium
  - Subject line should contain "4-[filename_of_pdf]"
  - Message body contains the webpage address
  - Benefit of this method is the ability to retain the webpage layout and pictures
