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
    private boolean bNotInHeadersAnymore = false; // set to true when we're in the body
    
    /** Creates a new instance of umbMessage */
    public umbMessage() {
    }
    
    public String toString() {
        String retValue = new String();
        
        return retValue;
    }
    
    public void InterpretLine(String s) {
        if (bNotInHeadersAnymore) return; // done before we began
        if (s.startsWith("From ")) {
            sFrom2 = s.substring(5);
            return;
        }
        if (s.startsWith("From: ")) {
            sFrom = s.substring(6);
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
