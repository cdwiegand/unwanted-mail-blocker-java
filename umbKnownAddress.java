/*
 * umbKnownAddress.java
 *
 * Created on February 19, 2002, 6:09 PM
 */

package umb;

/**
 *
 * @author  chris
 */
public class umbKnownAddress {
    public String sAddress = new String();
    public String sName = new String();
    public boolean bMaySendAttachments = true;
    public boolean bMaySendMail = true;
    
    /** Creates a new instance of umbKnownAddress */
    public umbKnownAddress(String sToParse) {
        // sToParse ?= my.email@some.isp;Chris Wiegand;Y;Y;
        // sToParse ?= my.email@some.isp
        try {
            java.util.StringTokenizer st = new java.util.StringTokenizer(sToParse,";");
            sAddress = st.nextToken();
            if (st.hasMoreTokens()) sName = st.nextToken();
            if (st.hasMoreTokens()) bMaySendMail = (st.nextToken().equalsIgnoreCase("Y"));
            if (st.hasMoreTokens()) bMaySendAttachments = (st.nextToken().equalsIgnoreCase("Y"));
        } catch (Exception eIgnore) {
            // user might have screwed up addressbook, if so, continue as well as possible
        }
    }
    
    public String toString() { // override...
        String sRet = new String();

        // sAddress;
        sRet = sAddress.concat(";");
        
        // sAddress;sName
        if (sName.length() > 1) {
            sRet = sRet.concat(sName).concat(";");
        } else {
            java.util.StringTokenizer stTemp = new java.util.StringTokenizer(sAddress,"@");
            sRet = sRet.concat(stTemp.nextToken()).concat(";");
            // returns the part before the @ in the email
        }

        if (bMaySendMail) {
            sRet = sRet.concat("Y;");
        } else {
            sRet = sRet.concat("Y;");
        }
        // sAddress;sName;Y;
        
        if (bMaySendAttachments) {
            sRet = sRet.concat("Y;");
        } else {
            sRet = sRet.concat("Y;");
        }
        // sAddress;sName;Y;Y;
        
        return sRet;
    }
    
}
