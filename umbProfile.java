/*
 * umbProfile.java
 *
 * Created on February 13, 2002, 11:14 AM
 */

/**
 *
 * @author  chris
 */

import java.util.*;
import java.net.*;
import java.util.logging.*;

public class umbProfile extends java.lang.Object {
    public String sPOPServer = new String();
    public String sUsername = new String();
    public String sPassword = new String();
    public String sSMTPServer = new String();
    public String sName = new String();
    public int iMsgs = -1;
    
    private Socket theSock;
    private java.io.InputStream theIS;
    private java.io.OutputStream theOS;
    private java.io.BufferedInputStream theBIS;
    private java.io.BufferedOutputStream theBOS;
    
    
    /** Creates a new instance of umbProfile */
    public umbProfile() {
    }
    
    public umbProfile(String newName) {
        sName = newName;
    }
    
    public String toString() {
        String ret = new String();
        
        return ret;
    }
    
    public void loadPrefs(java.util.prefs.Preferences thePrefs) {
        sPOPServer = thePrefs.get("POPServer","");
        sUsername = thePrefs.get("Username","");
        sPassword = thePrefs.get("Password","");
        sSMTPServer = thePrefs.get("SMTPServer","");
    }
    
    public void savePrefs(java.util.prefs.Preferences thePrefs) {
        thePrefs.put("POPServer",sPOPServer);
        thePrefs.put("Username",sUsername);
        thePrefs.put("Password",sPassword);
        thePrefs.put("SMTPServer",sSMTPServer);
    }
    
    public void close() {
        try {
            theSock.close();
        } catch (java.io.IOException eIgnore) {}
    }
    
    public void login(unwantedmailblocker.Prefs thePrefs, unwantedmailblocker.Main theMain) throws Exception {
        String s;
        String sError;
        
        try {
            theSock = new Socket(sPOPServer,110); // connect to server...
            theIS = theSock.getInputStream();
            theBIS = new java.io.BufferedInputStream(theIS);
            theOS = theSock.getOutputStream();
            theBOS = new java.io.BufferedOutputStream(theOS);
        } catch (Exception e) {
            thePrefs.theLog.log(Level.SEVERE,"Unable to create socket in login of POPServer.java");
            throw new Exception("Unable to create socket.",e);
        }
        
        try {
            s = this.readSingleLine(thePrefs.theLog);
            theMain.addInfoLine(s);
            if (!s.startsWith("+OK")) {
                // oh, too bad!
                sError = "Unable to talk to POP3 server in login of POPServer.java";
                thePrefs.theLog.log(Level.SEVERE,sError);
                throw new Exception(sError);
            }
            
            s = "USER ".concat(sUsername);
            theMain.addInfoLine(s);
            this.writeSingleLine(s,thePrefs.theLog);
            
            s = this.readSingleLine(thePrefs.theLog);
            theMain.addInfoLine(s);
            if (!s.startsWith("+OK")) {
                // oh, too bad!
                sError = "Username failed. Message: ".concat(s);
                thePrefs.theLog.log(Level.SEVERE,sError);
                throw new Exception(sError);
            }
            
            s = "PASS ".concat(sPassword);
            theMain.addInfoLine("PASS (shh!)");
            this.writeSingleLine(s,thePrefs.theLog);
            
            s = this.readSingleLine(thePrefs.theLog);
            theMain.addInfoLine(s);
            if (!s.startsWith("+OK")) {
                // oh, too bad!
                sError = "Password failed. Message: ".concat(s);
                thePrefs.theLog.log(Level.SEVERE,sError);
                throw new Exception(sError);
            }
            
            s = "LIST";
            theMain.addInfoLine(s);
            this.writeSingleLine(s,thePrefs.theLog);
            
            s = this.readSingleLine(thePrefs.theLog);
            theMain.addInfoLine(s);
            if (!s.startsWith("+OK")) {
                // oh, too bad!
                // we don't know HOW many... well... crap. We'll live without it.
                iMsgs = -2;
            } else {
                // how many messages?
                try {
                    s = s.substring(4);
                    s = s.substring(0,s.indexOf(" messages"));
                    thePrefs.theLog.log(Level.INFO,"POPServer.login.s @ MsgCount = ".concat(s));
                    iMsgs = Integer.parseInt(s);
                    s = new String().valueOf(iMsgs).concat(" messages on the server.");
                    theMain.addInfoLine(s);
                } catch (Exception e) {
                    iMsgs = -2; // sorry, still can't read....
                }
            }
            
            // Now, stop. We'll call retrieveMsg(1) et al soon enough...
            // Done, well done!
        } catch (java.io.IOException eio) {
            sError = "Unable to talk to POP3 server or interrupted in login of POPServer.java";
            thePrefs.theLog.log(Level.SEVERE,sError);
            throw new Exception(sError,eio);
        }
    }
    
    public EmailMsg retrieveMsg(int iMsgNum, unwantedmailblocker.Prefs thePrefs, unwantedmailblocker.Main theMain) throws Exception {
        EmailMsg eMail = new EmailMsg();
        String s;
        String s2;
        String sMsgNum = new String().valueOf(iMsgNum);
        boolean bBreakOut = false;
        
        try {
            flushInput(thePrefs.theLog);
            // FIXME: support head, if the server supports it (add a boolean variable, and do the check in login()
            writeSingleLine("RETR ".concat(sMsgNum),thePrefs.theLog);
            s = this.readSingleLine(thePrefs.theLog);
            if (s.startsWith("-ERR")) {
                // oh, too bad!
                thePrefs.theLog.log(Level.SEVERE,"Message retrieve (".concat(sMsgNum).concat(") failed. Message: ").concat(s));
                throw new Exception("Message retrieve (".concat(sMsgNum).concat(") failed. Message: ").concat(s));
            } else {
                // get it...
                // scan each line until we get a line with ONLY . on it.
                // file it into the email message
                bBreakOut = false; // init the breakout
                while (!bBreakOut) {
                    java.util.StringTokenizer st = new java.util.StringTokenizer(s,"\r\n");
                    while (st.hasMoreTokens()) {
                        s2 = st.nextToken();
                        thePrefs.theLog.log(Level.INFO,"POP3: ".concat(s2));
                        eMail.InterpretLine(s2);
                        if (s2.equalsIgnoreCase(".")) {
                            // end of message
                            bBreakOut = true;
                        }
                    }
                    try {
                        if (!bBreakOut) s = this.readSingleLine(thePrefs.theLog);
                    } catch (java.io.IOException e) {
                        // ahh... oh well, try to continue as best as we can...
                    }
                } // while !bBreakOut
                
            } // if -ERR
            
        } catch (Exception e) {
            thePrefs.theLog.log(Level.SEVERE,"Unable to talk to POP3 server or interrupted in login of POPServer.java");
            throw new Exception("Unable to talk to POP3 server or interrupted.",e);
        }
        return eMail;
    }
    
    public void closeServer(unwantedmailblocker.Prefs thePrefs) {
        try {
            this.writeSingleLine("QUIT",thePrefs.theLog);
            theSock.close();
        } catch (Exception eIgnore) {}
    }
    
    private void flushInput(java.util.logging.Logger theLog) {
        // we exist only to flush out any remaining junk in the input buffer
        try {
            if (theBIS.available() > 0) {
                theBIS.read(); // flush it out!
            }
        } catch (java.io.IOException e) {
            // ignore
        }
    }
    
    private String readSingleLine(java.util.logging.Logger theLog) throws java.io.IOException {
        return readSingleLine(theLog,30);
    }
    
    private String readSingleLine(java.util.logging.Logger theLog, int iWaitSeconds) throws java.io.IOException {
        byte b[] = new byte[65535];
        String sRet;
        int l = 0;
        
        while (theBIS.available() < 3 & theSock.isConnected()) {
            // do nothing....
            try {
                this.wait(1000); // wait one second
            } catch (Exception eIgnore) {}
        }
        
        if (theBIS.available() < 3) {
            // not yet ready...
            throw new java.io.IOException("Nothing in socket.");
        } else {
            l = theBIS.read(b); // read from socket...
            sRet = new String(b,0,l);
            theLog.log(Level.FINEST,"Received: ".concat(sRet));
        }
        return sRet;
    }
    
    private void writeSingleLine(String s,java.util.logging.Logger theLog) throws java.io.IOException {
        theLog.log(Level.FINEST,"Sent: ".concat(s));
        theBOS.write(s.concat("\r\n").getBytes());
        theBOS.flush();
    }
    
}
