/*
 * umbPrefs.java
 *
 * Created on February 13, 2002, 11:19 AM
 */

/**
 *
 * @author  chris
 */

import java.util.prefs.*;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.*;

public class umbPrefs extends java.lang.Object {
    public LinkedList llProfiles = new LinkedList(); // clsPOPServers
    public LinkedList llAllowedAddresses = new LinkedList(); // strings
    public Logger theLog = Logger.getAnonymousLogger();
    private boolean bLogPlease = false;
    
    /** Creates a new instance of umbPrefs */
    public umbPrefs() {
        StringTokenizer st;
        String sTmp;
        Preferences prefs = Preferences.userRoot(); // get prefs for THIS package
        Preferences childNode;
        
        prefs = prefs.node("Wiegand"); // /Wiegand
        prefs = prefs.node("UnwantedMailBlocker"); // /Wiegand/UnwantedMailBlocker
        
        // To assist end-users in finding their prefs (for debugging) we write this out:
        prefs.put("TestString","UMBTest");
        prefs.putBoolean("TestBoolean",true);
        
        bLogPlease = prefs.getBoolean("DebugLog",false); // debug log
        
        // load addresses
        String addyList = prefs.get("Addresses",""); // get from prefs
        // convert "someone@nowhere.com,someoneelse@anywhere.com,spam@busters.org" to a LinkedList
        st = new StringTokenizer(addyList,","); // get tokenizer for this string
        while (st.hasMoreTokens()) { // while more tokens
            llAllowedAddresses.add(st.nextToken()); // get token, put into linked list
        } // wash rinse repeat
        
        // load profiles
        String profiles = prefs.get("Profiles","");
        // convert "server1,server2,server3" to a LinkedList
        st = new StringTokenizer(profiles,","); // get tokenizer for this string
        while (st.hasMoreTokens()) {// while more tokens
            sTmp = st.nextToken(); // stmp = profile's name
            POPServer thePOP = new POPServer(sTmp); // make profile
            childNode = prefs.node(sTmp); // get child node for prefs
            thePOP.loadPrefs(childNode); // have it load it's preferences...
            llProfiles.add(thePOP);// put into linked list
        } // wash rinse repeat
        
        //    theLog.setLevel(Level.OFF);
        
        //    if (bLogPlease) {
        theLog.setLevel(Level.ALL); // Request that every detail gets logged.
        try {
            FileHandler fh = new FileHandler("umbdebug.txt");
            // Send logger output to our FileHandler.
            theLog.addHandler(fh); // try to use log file, if can't, oh well
        } catch (java.io.IOException eIgnoreForNow) {}
        //    }
    }
    
    public void SavePrefs() {
        String sTmp;
        Preferences prefs = Preferences.userRoot(); // get prefs for THIS package
        Preferences childNode;
        java.util.ListIterator li;
        POPServer thePOP;
        
        prefs = prefs.node("/Wiegand/UnwantedMailBlocker");
        
        // save addresses
        sTmp = new String();
        li = llAllowedAddresses.listIterator(); // get list iterator
        while (li.hasNext()) { // while more items
            sTmp = sTmp.concat((String) li.next()); // get next item and concat to sTmp
            if (li.hasNext()) sTmp = sTmp.concat(","); // append only if not LAST item
        }
        prefs.put("Addresses",sTmp); // save to prefs
        
        // Now save profiles...
        li = llProfiles.listIterator(); // get list iterator
        while (li.hasNext()) { // while more items
            thePOP = (POPServer) li.next(); // convert to clsPOPServer
            childNode = prefs.node(thePOP.sName); // get prefs node
            thePOP.savePrefs(childNode); // save to prefs
        }
    }
    
}
