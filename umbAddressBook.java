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

public class umbAddressBook {
    private java.util.LinkedList llAddresses = new java.util.LinkedList(); // umbKnownAddress
    
    /** Creates a new instance of umbAddressBook */
    public umbAddressBook() {
        java.util.prefs.Preferences prefs; // get prefs for THIS package
        prefs = java.util.prefs.Preferences.userRoot().node("/Wiegand/UnwantedMailBlocker");

        String addyList = prefs.get("Addresses",""); // get from prefs
        // convert "someone@nowhere.com Y N,someoneelse@anywhere.com N N,spam@busters.org Y Y" to a LinkedList
        // loads comma-delimited list into LinkedList...
        java.util.StringTokenizer st = new java.util.StringTokenizer(addyList,",");
        
        llAddresses.clear();
        umbKnownAddress ka;
        while (st.hasMoreTokens()) {
            ka = new umbKnownAddress(st.nextToken());
            llAddresses.add(ka);
        }
    }
    
    protected void finalize() {
        java.util.prefs.Preferences prefs; // get prefs for THIS package
        prefs = java.util.prefs.Preferences.userRoot().node("/Wiegand/UnwantedMailBlocker");

        // must save address book...
        prefs.put("Addresses",toString());
    }
    
    public void AddAddress(String sNewAddy) {
        System.out.println("Before:".concat(toString()));
        umbKnownAddress ka = new umbKnownAddress(sNewAddy);
        llAddresses.add(ka);
        System.out.println("After:".concat(toString()));
    }
    
    public String toString() {
        // return comma-delimited list of addresses...
        String retValue = new String();
        umbKnownAddress ka;
        
        java.util.ListIterator li = llAddresses.listIterator();
        while (li.hasNext()) {
            ka = (umbKnownAddress) li.next();
            retValue += ka.toString();
            if (li.hasNext()) retValue += ",";
        }
        return retValue;
    }
    
    public boolean isOKAddress(umbMessage theEmail) {
        int i = 0;
        String s;
        umbKnownAddress ka;
        java.util.ListIterator li = llAddresses.listIterator();
        
        System.out.println("theEmail.sFromAddress = ".concat(theEmail.sFromAddress));
        while (li.hasNext()) {
            ka = (umbKnownAddress) li.next();
            System.out.println("ka.sAddress = ".concat(ka.sAddress));
            if (ka.sAddress.equalsIgnoreCase(theEmail.sFromAddress)) {
                System.out.println("     matched");
                return true;
            }
        }
        
        System.out.println("     did not match");
        return false;
    }
    
}
