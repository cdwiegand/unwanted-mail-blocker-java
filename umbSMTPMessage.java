/*
 * umbSMTPMessage.java
 *
 * Created on February 18, 2002, 4:00 PM
 */

package umb;

/**
 *
 * @author  chris
 */
public class umbSMTPMessage {
    public String sSubject = new String("");
    public java.util.LinkedList llRecip = new java.util.LinkedList(); // Strings
    public String sBody = new String("");
    
    /** Creates a new instance of umbSMTPMessage */
    public umbSMTPMessage() {
    }
    
    public void PrepSpam(umbMessage theMsg) {
        sSubject = "Spam report, Re: ".concat(theMsg.sSubject);
        sBody = "Please note: the following is spam sent to me.\r\nIf you are the sender, please take any appropriate action to remove me from lists.\r\n.If you are an ISP or mail server administrator, please take appropriate action against any spammers on your network.\r\n\r\n";
        sBody = sBody.concat(theMsg.QuoteMessage());
        if (theMsg.sFrom.length() > 2) addRecip(theMsg.sFrom);
    }
    
    public void PrepAuthorize(umbMessage theMsg) {
        sSubject = "Authorize request, Re: ".concat(theMsg.sSubject);
        sBody = "Hello. This user uses the Unwanted Mail Blocker, and has requested that you respond with a request for authorization. Please state the reason you wish to contact this user.\r\n\r\n";
        if (theMsg.sFrom.length() > 2) addRecip(theMsg.sFrom);
    }

    public void addRecip(String sRecip) {
        llRecip.add(sRecip);
    }
    
    public java.util.StringTokenizer recipTokenizer() {
        String s = this.recipString();
        java.util.StringTokenizer st = new java.util.StringTokenizer(s);
        return st;
    }
    
    public String recipString() {
        String s = new String("");
        java.util.ListIterator li = llRecip.listIterator();
        
        while (li.hasNext()) {
            s = s.concat((String) li.next());
            if (li.hasNext()) s = s.concat(",");
        }
        return s;
    }
    
}
