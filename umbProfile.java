package umb;
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
    public String sEmailAddress = new String();
    public String sName = new String();
    public int iMsgs = -1;
    
    private Socket thePOPSock;
    private java.io.BufferedInputStream thePOPBIS;
    private java.io.BufferedOutputStream thePOPBOS;
    
    private Socket theSMTPSock;
    private java.io.BufferedInputStream theSMTPBIS;
    private java.io.BufferedOutputStream theSMTPBOS;
    
    /** Creates a new instance of umbProfile */
    public umbProfile() {
    }
    
    public umbProfile(String newName) {
        sName = newName;
    }
    
    public void loadPrefs(java.util.prefs.Preferences thePrefs) {
        sPOPServer = thePrefs.get("POPServer","");
        sUsername = thePrefs.get("Username","");
        sPassword = thePrefs.get("Password","");
        sSMTPServer = thePrefs.get("SMTPServer","");
        // defaults to username@smtpserver
        sEmailAddress = thePrefs.get("SMTPEmailAddress",sUsername.concat("@").concat(sSMTPServer));
    }
    
    protected void finalize(java.util.prefs.Preferences thePrefs) {
        savePrefs(thePrefs);
    }
    
    public void savePrefs(java.util.prefs.Preferences thePrefs) {
        thePrefs.put("POPServer",sPOPServer);
        thePrefs.put("Username",sUsername);
        thePrefs.put("Password",sPassword);
        thePrefs.put("SMTPServer",sSMTPServer);
        thePrefs.put("SMTPEmailAddress",sEmailAddress);
    }
    
    public void loginPOP3(umbPrefs thePrefs, umbMain theMain) throws Exception {
        String s;
        String sError;
        
        try {
            thePOPSock = new Socket(sPOPServer,110); // connect to server...
            thePOPBIS = new java.io.BufferedInputStream(thePOPSock.getInputStream());
            thePOPBOS = new java.io.BufferedOutputStream(thePOPSock.getOutputStream());
        } catch (Exception e) {
            thePrefs.theLog.log(Level.SEVERE,"Unable to create socket in login of POPServer.java");
            throw new Exception("Unable to create socket.",e);
        }
        
        try {
            s = this.readSingleLine(thePOPBIS,thePrefs.theLog);
            theMain.addInfoLine(s);
            if (!s.startsWith("+OK")) {
                // oh, too bad!
                sError = "Unable to talk to POP3 server in login of POPServer.java";
                thePrefs.theLog.log(Level.SEVERE,sError);
                throw new Exception(sError);
            }
            
            s = "USER ".concat(sUsername);
            theMain.addInfoLine(s);
            this.writeSingleLine(thePOPBOS,s,thePrefs.theLog);
            
            s = this.readSingleLine(thePOPBIS,thePrefs.theLog);
            theMain.addInfoLine(s);
            if (!s.startsWith("+OK")) {
                // oh, too bad!
                sError = "Username failed. Message: ".concat(s);
                thePrefs.theLog.log(Level.SEVERE,sError);
                throw new Exception(sError);
            }
            
            s = "PASS ".concat(sPassword);
            theMain.addInfoLine("PASS (shh!)");
            this.writeSingleLine(thePOPBOS,s,thePrefs.theLog);
            
            s = this.readSingleLine(thePOPBIS,thePrefs.theLog);
            theMain.addInfoLine(s);
            if (!s.startsWith("+OK")) {
                // oh, too bad!
                sError = "Password failed. Message: ".concat(s);
                thePrefs.theLog.log(Level.SEVERE,sError);
                throw new Exception(sError);
            }
            
            s = "LIST";
            theMain.addInfoLine(s);
            this.writeSingleLine(thePOPBOS,s,thePrefs.theLog);
            
            s = this.readSingleLine(thePOPBIS,thePrefs.theLog);
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
    
    public void deletePOPMessage(int iMsgNum, umbPrefs thePrefs, umbMain theMain) throws Exception {
        String s;
        String s2;
        String sMsgNum = new String().valueOf(iMsgNum);
        
        try {
            flushInput(thePOPBIS,thePrefs.theLog);
            writeSingleLine(thePOPBOS,"DELE ".concat(sMsgNum),thePrefs.theLog);
            s = this.readSingleLine(thePOPBIS,thePrefs.theLog);
            if (!s.startsWith("+OK")) {
                // oh, too bad!
                thePrefs.theLog.log(Level.SEVERE,"Message delete (".concat(sMsgNum).concat(") failed. Message: ").concat(s));
                throw new Exception("Message delete (".concat(sMsgNum).concat(") failed. Message: ").concat(s));
            }
        } catch (Exception e) {
            thePrefs.theLog.log(Level.SEVERE,"Unable to talk to POP3 server or interrupted in login of POPServer.java");
            throw new Exception("Unable to talk to POP3 server or interrupted.",e);
        }
    }
    
    public umbMessage retrievePOPMsg(int iMsgNum, umbPrefs thePrefs, umbMain theMain) throws Exception {
        umbMessage eMail = new umbMessage();
        String s;
        String s2;
        String sMsgNum = new String().valueOf(iMsgNum);
        boolean bBreakOut = false;
        
        try {
            flushInput(thePOPBIS,thePrefs.theLog);
            writeSingleLine(thePOPBOS,"RETR ".concat(sMsgNum),thePrefs.theLog);
            s = this.readSingleLine(thePOPBIS,thePrefs.theLog);
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
                        eMail.InterpretPOP3Line(s2);
                        if (s2.equalsIgnoreCase(".")) {
                            // end of message
                            bBreakOut = true;
                        }
                    }
                    try {
                        if (!bBreakOut) s = this.readSingleLine(thePOPBIS,thePrefs.theLog);
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
    
    public void sendMailSMTP(umbPrefs thePrefs, umbMain theMain, umbSMTPMessage theMsg) throws Exception {
        String s;
        String sError;
        
        try {
            theSMTPSock = new Socket(sSMTPServer,25); // connect to server...
            theSMTPBIS = new java.io.BufferedInputStream(theSMTPSock.getInputStream());
            theSMTPBOS = new java.io.BufferedOutputStream(theSMTPSock.getOutputStream());
        } catch (Exception e) {
            thePrefs.theLog.log(Level.SEVERE,"Unable to create socket in sendMsg of POPServer.java");
            throw new Exception("Unable to create socket.",e);
        }
        
        try {
            s = this.readSingleLine(theSMTPBIS,thePrefs.theLog);
            theMain.addInfoLine(s);
            if (!s.startsWith("220")) {
                // oh, too bad!
                sError = "Unable to talk to SMTP server in sendMail";
                thePrefs.theLog.log(Level.SEVERE,sError);
                throw new Exception(sError);
            }
            
            s = "HELO ".concat(theSMTPSock.getLocalAddress().getHostName());
            theMain.addInfoLine(s);
            this.writeSingleLine(theSMTPBOS,s,thePrefs.theLog);
            
            s = this.readSingleLine(theSMTPBIS,thePrefs.theLog);
            theMain.addInfoLine(s);
            if (!s.startsWith("250")) {
                // oh, too bad!
                sError = "SMTP-HELO negotiation failed. Message: ".concat(s);
                thePrefs.theLog.log(Level.SEVERE,sError);
                throw new Exception(sError);
            }
            
            s = "MAIL FROM: ".concat(sEmailAddress);
            theMain.addInfoLine(s);
            this.writeSingleLine(theSMTPBOS,s,thePrefs.theLog);
            
            s = this.readSingleLine(theSMTPBIS,thePrefs.theLog);
            theMain.addInfoLine(s);
            if (!s.startsWith("250")) {
                // oh, too bad!
                sError = "From failed. Message: ".concat(s);
                thePrefs.theLog.log(Level.SEVERE,sError);
                throw new Exception(sError);
            }
            
            java.util.StringTokenizer st = theMsg.recipTokenizer();
            while (st.hasMoreTokens()) {
                String sRcpt = st.nextToken();
                s = "RCPT TO: ".concat(sRcpt);
                theMain.addInfoLine(s);
                this.writeSingleLine(theSMTPBOS,s,thePrefs.theLog);
                
                s = this.readSingleLine(theSMTPBIS,thePrefs.theLog);
                theMain.addInfoLine(s);
                if (!s.startsWith("250")) {
                    // oh, too bad!
                    sError = "Recipient failed. Message: ".concat(s);
                    thePrefs.theLog.log(Level.SEVERE,sError);
                    // throw new Exception(sError); // not for now FIXME eval whether to enable or not? --cdw
                }
            }
            
            s = "DATA";
            theMain.addInfoLine(s);
            this.writeSingleLine(theSMTPBOS,s,thePrefs.theLog);
            
            s = this.readSingleLine(theSMTPBIS,thePrefs.theLog);
            theMain.addInfoLine(s);
            if (s.startsWith("354")) {
                this.writeSingleLine(theSMTPBOS,"Date: ".concat(new Date().toString()),thePrefs.theLog);
                this.writeSingleLine(theSMTPBOS,"From: ".concat(sEmailAddress),thePrefs.theLog);
                this.writeSingleLine(theSMTPBOS,"To: ".concat(theMsg.recipString()),thePrefs.theLog);
                this.writeSingleLine(theSMTPBOS,"Subject: ".concat(theMsg.sSubject),thePrefs.theLog);
                this.writeSingleLine(theSMTPBOS,"",thePrefs.theLog);
                this.writeSingleLine(theSMTPBOS,theMsg.sBody,thePrefs.theLog);
                this.writeSingleLine(theSMTPBOS,"",thePrefs.theLog);
                this.writeSingleLine(theSMTPBOS,".",thePrefs.theLog);
                
                // read back that it got through ok
                s = this.readSingleLine(theSMTPBIS,thePrefs.theLog);
                theMain.addInfoLine(s);
                if (!s.startsWith("250")) {
                    // oh, too bad!
                    sError = "Message send MAY have failed. Message: ".concat(s);
                    thePrefs.theLog.log(Level.SEVERE,sError);
                    throw new Exception(sError);
                }

            } else {
                // failed...
                // oh, too bad!
                sError = "Mail send failed. Message: ".concat(s);
                thePrefs.theLog.log(Level.SEVERE,sError);
                throw new Exception(sError);
            }
            
            // Done, well done!
        } catch (java.io.IOException eio) {
            sError = "Unable to talk to SMTP server.";
            thePrefs.theLog.log(Level.SEVERE,sError);
            throw new Exception(sError,eio);
        }
    }
    
    public void logoutPOP3(umbPrefs thePrefs) {
        try {
            this.writeSingleLine(thePOPBOS,"QUIT",thePrefs.theLog);
        } catch (Exception eIgnore) {}
        
        try {
            thePOPSock.close();
        } catch (Exception eIgnore) {}
    }
    
    private void flushInput(java.io.BufferedInputStream theBIS, java.util.logging.Logger theLog) {
        // we exist only to flush out any remaining junk in the input buffer
        try {
            if (theBIS.available() > 0) {
                theBIS.read(); // flush it out!
            }
        } catch (java.io.IOException e) {
            // ignore
        }
    }
    
    private String readSingleLine(java.io.BufferedInputStream theBIS, java.util.logging.Logger theLog) throws java.io.IOException {
        return readSingleLine(theBIS,theLog,30);
    }
    
    private String readSingleLine(java.io.BufferedInputStream theBIS, java.util.logging.Logger theLog, int iWaitSeconds) throws java.io.IOException {
        byte b[] = new byte[65535];
        String sRet;
        int l = 0;
        
        try {
            while (theBIS.available() < 3) {
                // do nothing....
                try {
                    this.wait(1000); // wait one second
                } catch (Exception eIgnore) {}
            }
        } catch (java.io.IOException eInterrupted) {}
        
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
    
    private void writeSingleLine(java.io.BufferedOutputStream theBOS, String s,java.util.logging.Logger theLog) throws java.io.IOException {
        theLog.log(Level.FINEST,"Sent: ".concat(s));
        theBOS.write(s.concat("\r\n").getBytes());
        theBOS.flush();
    }
    
}
