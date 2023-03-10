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
    public boolean bStopRunning = false; // used instead of Thread().stop(), 'cause that's deprecated now.. ): --cdw
    
    /** Creates a new instance of umbFilter */
    public umbFilter(umbMain pTheMainForm, umbPrefs pThePrefs) {
        super();
        theMainForm = pTheMainForm;
        thePrefs = pThePrefs;
    }
    
    public void run() {
        // check all POP3 servers...
        java.util.ListIterator li;
        umbAddressBook theBook = thePrefs.theBook;
        umbKnownAddress ka;
        umbProfile thePOP;
        umbMessage theMail;
        
        try {
            li = thePrefs.llProfiles.listIterator(); // get list iterator
            while (li.hasNext()) { // while more items
                thePOP = (umbProfile) li.next(); // convert to clsPOPServer
                try {
                    int iMsg = 0;
                    int iMsgCount = 0;
                    theMainForm.updateStatus(0,1,"Connecting to server ".concat(thePOP.sPOPServer));
                    if (bStopRunning) throw new Exception("Thread stopped.");
                    
                    thePOP.loginPOP3(thePrefs,theMainForm); // try to login...
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
                                theMainForm.updateStatus(iMsg,iMsgCount,"Retrieving message ".concat(new String().valueOf(iMsg)).concat("..."));
                                if (bStopRunning) throw new Exception("Thread stopped.");
                                
                                theMail = thePOP.retrievePOPMsg(iMsg,thePrefs,theMainForm);
                                theMainForm.addInfoLine("Message: ".concat(theMail.sSubject));
                                theMainForm.addInfoLine("From: ".concat(theMail.sFrom));
                                // Is it a recognized address?
                                if (bStopRunning) return;
                                
                                ka = theBook.getAddress(theMail.sFromAddress);
                                if (ka != null) {
                                    // found a recipient, verify no other problems...
                                    if (!ka.bMaySendMail) {
                                        umbSMTPMessage newSMTP = new umbSMTPMessage();
                                        newSMTP.PrepResponse(theMail,"Spam");
                                        thePOP.sendMailSMTP(thePrefs,theMainForm,newSMTP);
                                    }
                                    if (!ka.bMaySendAttachments && theMail.iAttachments > 1) {
                                        umbSMTPMessage newSMTP = new umbSMTPMessage();
                                        newSMTP.PrepResponse(theMail,"Blocked Attachment");
                                        thePOP.sendMailSMTP(thePrefs,theMainForm,newSMTP);
                                    }
                                    
                                } else {
                                    // unknown...
                                    theMainForm.addInfoLine("Unknown recipient - prompting for action.");
                                    umbPrompt thePrompt = new umbPrompt(theMainForm,true,theMail);
                                    thePrompt.show();
                                    if (thePrompt.getReturnStatus() == thePrompt.RET_CANCEL) {
                                        // cancelled - ignore this spam
                                    } else {
                                        String sResponse;
                                        if (thePrompt.getAddUser()) {
                                            theBook.AddAddress(theMail.sFrom);
                                        }
                                        if (thePrompt.getDeleteMsg()) {
                                            thePOP.deletePOPMessage(iMsg,thePrefs,theMainForm);
                                        }
                                        sResponse = thePrompt.getReplyTemplate();
                                        if (sResponse.length() > 0) {
                                            umbSMTPMessage newSMTP = new umbSMTPMessage();
                                            newSMTP.PrepResponse(theMail,sResponse);
                                            thePOP.sendMailSMTP(thePrefs,theMainForm,newSMTP);
                                        }
                                    } // return status ?= cancel (ignore) or ok (filter)
                                } // known vs. unknwon recip
                                
                            } catch (Exception e) {
                                // Hmmm.... couldn't get it. Stop processing THIS pop3 account
                                iMsg = iMsgCount + 1;
                            }
                        } // for iMsg
                    } // if iMsgCount > 0
                    
                } catch (Exception e2) {
                    theMainForm.addInfoLine("Failed to login to POP3 server ".concat(thePOP.sPOPServer));
                    theMainForm.addInfoLine(e2.getMessage());
                } finally {
                    // do REGARDLESS of interruption!!
                    thePOP.logoutPOP3(thePrefs);
                }
                
            }
            
        } catch (Exception e) {
            theMainForm.addInfoLine("Error: ".concat(e.getMessage()));
        } finally {
            // Do REGARDLESS of interruption at end of function!!!
            theMainForm.addInfoLine("Done!");
            theMainForm.updateStatus(1,1,"Done!");
            theMainForm.threadDone();
        }
    } // end run
}
