/*
 * umbAddressBook.java
 *
 * Created on February 13, 2002, 11:13 AM
 */

/**
 *
 * @author  chris
 */
public class umbAddressBook extends java.lang.Object {
    
    /** Creates a new instance of umbAddressBook */
    public umbAddressBook() {
    }
    
    public String toString() {
        String retValue = new String();
        
        return retValue;
    }
    
    private boolean isOKAddress(unwantedmailblocker.EmailMsg theMail, unwantedmailblocker.Prefs thePrefs) {
        int i = 0;
        String s;
        java.util.ListIterator li;
        
        li = thePrefs.llAllowedAddresses.listIterator();
        while (li.hasNext()) {
            s = (String) li.next();
            if (s.equalsIgnoreCase(theMail.sFrom) || s.equalsIgnoreCase(theMail.sFrom2)) {
                return true;
            }
        }
        
        return false;
    }
    
}
