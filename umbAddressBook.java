package umb;

/*
 * umbAddressBook.java
 *
 * Created on February 13, 2002, 11:13 AM
 */

/**
 *
 * @author  chris
 */

import java.util.prefs.*;

public class umbAddressBook extends java.util.LinkedList {
    private java.util.LinkedList llAddresses = new java.util.LinkedList();
    
    /** Creates a new instance of umbAddressBook */
    public umbAddressBook(Preferences theNode) {
        super();

        String addyList = theNode.get("Addresses",""); // get from prefs
        // convert "someone@nowhere.com,someoneelse@anywhere.com,spam@busters.org" to a LinkedList
        // loads comma-delimited list into LinkedList...
        java.util.StringTokenizer st = new java.util.StringTokenizer(addyList,",");
        
        clear();
        while (st.hasMoreTokens()) {
            add(st.nextToken());
        }
    }
    
    public void saveToPrefs(Preferences theNode) {
        // must save address book...
        theNode.put("Addresses",toString());
    }
    
    public String toString() {
        // return comma-delimited list of addresses...
        String retValue = new String();
        
        java.util.ListIterator li = llAddresses.listIterator();
        while (li.hasNext()) {
            retValue += (String) li.next();
            if (li.hasNext()) retValue += ",";
        }
        return retValue;
    }
    
    public boolean isOKAddress(umbMessage theEmail) {
        int i = 0;
        String s;
        java.util.ListIterator li = llAddresses.listIterator();
        
        while (li.hasNext()) {
            s = (String) li.next();
            if (s.equalsIgnoreCase(theEmail.sFrom) || s.equalsIgnoreCase(theEmail.sFrom2)) {
                return true;
            }
        }
        
        return false;
    }
    
}
