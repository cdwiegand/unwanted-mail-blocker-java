package umb;
/*
 * umbMessage.java
 *
 * Created on February 13, 2002, 11:12 AM
 */

/**
 *
 * @author  chris
 */
public class umbMessage extends java.lang.Object {
    public String sFrom = new String(); // From: someone@nowhere.com in headers
    public String sFrom2 = new String(); // From someone@nowhere.com at top of message
    public String sSubject = new String(); // Subject: Something or other
    public String sDateSent = new String(); // Date: (sent)
    private boolean bNotInHeadersAnymore = false; // set to true when we're in the body
    public java.util.LinkedList llHeaders = new java.util.LinkedList(); // headers
    public java.util.LinkedList llBody = new java.util.LinkedList(); // body
    
    /** Creates a new instance of umbMessage */
    public umbMessage() {
    }
    
    public String QuoteMessage() {
        String s = new String();
        java.util.ListIterator li;
        
        li = llHeaders.listIterator();
        while (li.hasNext()) {
            s = s.concat("> ".concat((String) li.next()).concat("\r\n"));
        }

        s = s.concat("> ".concat("\r\n"));
        
        li = llBody.listIterator();
        while (li.hasNext()) {
            s = s.concat("> ".concat((String) li.next()).concat("\r\n"));
        }
        
        return s;
    }
    
    public void InterpretPOP3Line(String s) {
        if (bNotInHeadersAnymore) {
            // in body...
            llBody.add(s);
            // FIXME: add detection of "attachments" for future filtering...
        } else {
            // in headers...
            llHeaders.add(s);
            if (s.startsWith("From ")) {
                sFrom2 = s.substring(5);
                return;
            }
            if (s.startsWith("From: ")) {
                sFrom = s.substring(6);
                return;
            }
            if (s.startsWith("Date: ")) {
                sDateSent = s.substring(6);
                return;
            }
            if (s.startsWith("Subject: ")) {
                sSubject = s.substring(9);
                return;
            }
            if (s.trim().length() == 0) {
                bNotInHeadersAnymore = true;
                return;
            }
        }
    }
    
}
