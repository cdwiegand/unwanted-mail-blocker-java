package umb;
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
    public LinkedList llTemplates = new LinkedList(); // umbTemplates
    public umbAddressBook theBook = new umbAddressBook();
    public Logger theLog = Logger.getAnonymousLogger();
    private boolean bLogPlease = false;
    
    /** Creates a new instance of umbPrefs */
    public umbPrefs() {
        StringTokenizer st;
        String sTmp;
        Preferences prefs = Preferences.userRoot().node("/Wiegand/UnwantedMailBlocker"); // get prefs for THIS package
        Preferences childNode;
        
        // To assist end-users in finding their prefs (for debugging) we write this out:
        prefs.put("TestString","UMBTest");
        prefs.putBoolean("TestBoolean",true);
        
        bLogPlease = prefs.getBoolean("DebugLog",false); // debug log
        
        // load profiles
        String profiles = prefs.get("Profiles","");
        // convert "server1,server2,server3" to a LinkedList
        st = new StringTokenizer(profiles,","); // get tokenizer for this string
        while (st.hasMoreTokens()) {// while more tokens
            sTmp = st.nextToken(); // stmp = profile's name
            umbProfile thePOP = new umbProfile(sTmp); // make profile
            childNode = prefs.node(sTmp); // get child node for prefs
            thePOP.loadPrefs(childNode); // have it load it's preferences...
            llProfiles.add(thePOP);// put into linked list
        } // wash rinse repeat
        
        theLog.setLevel(Level.OFF);
        
        if (bLogPlease) {
            theLog.setLevel(Level.ALL); // Request that every detail gets logged.
            try {
                FileHandler fh = new FileHandler("umbdebug.txt");
                // Send logger output to our FileHandler.
                theLog.addHandler(fh); // try to use log file, if can't, oh well
            } catch (java.io.IOException eIgnoreForNow) {}
        }
    }
    
    protected void finalize() {
        String sTmp;
        Preferences prefs = Preferences.userRoot().node("/Wiegand/UnwantedMailBlocker"); // get prefs for THIS package
        Preferences childNode;
        java.util.ListIterator li;
        umbProfile thePOP;
        
        prefs.putBoolean("DebugLog",bLogPlease);
        
        // Save profiles...
        li = llProfiles.listIterator(); // get list iterator
        while (li.hasNext()) { // while more items
            thePOP = (umbProfile) li.next(); // convert to clsPOPServer
            childNode = prefs.node(thePOP.sName); // get prefs node
            thePOP.savePrefs(childNode); // save to prefs
        }
    }
    
}
