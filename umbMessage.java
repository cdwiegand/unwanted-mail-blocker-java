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
    public String sFromAddress = new String(); // someone@nowhere.com in From: headers
    public String sSubject = new String(); // Subject: Something or other
    public String sDateSent = new String(); // Date: (sent)
    public int iAttachments = 0;
    private boolean bNotInHeadersAnymore = false; // set to true when we're in the body
    public java.util.LinkedList llHeaders = new java.util.LinkedList(); // headers
    public java.util.LinkedList llBody = new java.util.LinkedList(); // body
    
    /** Creates a new instance of umbMessage */
    public umbMessage() {
    }
    
    public java.util.LinkedList possibleSpamReportAddys() {
        String s = sFrom.substring(sFrom.indexOf("@")); // now contains only @someisp.com
        java.util.LinkedList ll = new java.util.LinkedList();

        ll.add("spam".concat(s));
        ll.add("abuse".concat(s));
        ll.add("administrator".concat(s));
        ll.add("postmaster".concat(s));
        ll.add("admin".concat(s));
        return ll;
    }
    
    public String QuoteMessage() {
        String s = new String("\r\n\r\n");
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
            if (s.startsWith("Content-Type:")) iAttachments += 1;
            // FIXME: add detection of "attachments" for future filtering...
        } else {
            // in headers...
            llHeaders.add(s);
            if (s.startsWith("From: ")) {
                sFrom = s.substring(6);

                // Also extract the ACTUAL email address...
                String sCompareTo = new String();
                java.util.StringTokenizer st = new java.util.StringTokenizer(sFrom," ");
                // first, extract the email address (vs. the name) from the From line
                // Some NAme Here <something@somewhere.us>
                while (st.hasMoreTokens()) sCompareTo = st.nextToken();
                // <something@somewhere.us>
                if (sCompareTo.startsWith("<")) sCompareTo = sCompareTo.substring(1);
                // something@somewhere.us>
                if (sCompareTo.endsWith(">")) sCompareTo = sCompareTo.substring(0,sCompareTo.length()-1);
                // something@somewhere.us
                sFromAddress = sCompareTo;
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
