package umb;
/*
 * umbFilter.java
 *
 * Created on February 14, 2002, 12:28 PM
 */

/**
 *
 * @author  chris
 */
public class umbFilter extends java.lang.Object implements Runnable {
    umbMain theMainForm;
    umbPrefs thePrefs;
    
    /** Creates a new instance of umbFilter */
    public umbFilter(umbMain pTheMainForm, umbPrefs pThePrefs) {
       theMainForm = pTheMainForm;
       thePrefs = pThePrefs;
    }
    
    public void run() {
        // check all POP3 servers...
        java.util.ListIterator li;
        umbAddressBook theBook = thePrefs.theBook;
        umbProfile thePOP;
        
        // FIXME: eventually, I want a tab where we show current progress vs. errors.
        // FIXME: shouldn't this be a separate thread?
        li = thePrefs.llProfiles.listIterator(); // get list iterator
        while (li.hasNext()) { // while more items
            thePOP = (umbProfile) li.next(); // convert to clsPOPServer
            try {
                int iMsg = 0;
                int iMsgCount = 0;
                umbMessage theMail;
                thePOP.login(thePrefs,theMainForm); // try to login...
                theMainForm.addInfoLine("Logged into ".concat(thePOP.sPOPServer));
                theMainForm.addInfoLine("Messages: ".concat(new String().valueOf(thePOP.iMsgs)));
                
                // If, and only if, thePOP.iMsgs = -2, we weren't able to figure out how many
                // Try 999 and when we get an exception we know we're done...
                iMsgCount = thePOP.iMsgs;
                if (iMsgCount == -2) {
                    iMsgCount = 999;
                }
                
                if (iMsgCount > 0) {
                    // Now, retrieve each message... processing as we go.
                    for (iMsg = 1; iMsg <= iMsgCount; iMsg = iMsg + 1) {
                        try {
                            theMainForm.updateStatus(iMsg,iMsgCount);
                            theMail = thePOP.retrieveMsg(iMsg,thePrefs,theMainForm);
                            theMainForm.addInfoLine("Message: ".concat(theMail.sSubject));
                            theMainForm.addInfoLine("From: ".concat(theMail.sFrom).concat(" ").concat(theMail.sFrom2));
                            // Is it a recognized address?
                            if (theBook.isOKAddress(theMail)) {
                                theMainForm.addInfoLine("Known recipient - it's Ok");
                            } else {
                                // unknown...
                                theMainForm.addInfoLine("ARGH! MORE JUNK MAIL!!");
                            }
                            
                        } catch (Exception e) {
                            // Hmmm.... couldn't get it. Stop processing...
                            iMsg = iMsgCount + 1;
                        }
                    } // for iMsg
                } // if iMsgCount > 0
                thePOP.closeServer(thePrefs);
                
            } catch (Exception e2) {
                theMainForm.addInfoLine("Failed to login to POP3 server ".concat(thePOP.sPOPServer));
                theMainForm.addInfoLine(e2.getMessage());
            }
        }
        theMainForm.addInfoLine("Done!");
    }
}