package com.novell.password.servlet;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;

import com.novell.security.nmas.mgmt.NMASPwdException;
import com.novell.security.nmas.mgmt.NMASPwdMgr;
import com.novell.security.nmas.mgmt.PwdJLdapTransport;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileInputStream;

import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import java.util.Properties;

//import javax.xml.transform.Transformer;
//import javax.xml.transform.sax.SAXResult;
//import javax.xml.transform.Result;
//import java.io.IOException;

public class PasswordServlet extends HttpServlet {

    private static final long serialVersionUID = -908918093488215264L;

    /** Name of User (in LDAP format) */
    protected static final String USER_REQUEST_PARAM = "user";
    /** Value of the Password */
    protected static final String PWD_REQUEST_PARAM = "pwd";

    /** The TransformerFactory used to create Transformer instances */
    protected TransformerFactory transFactory = null;

    /** URIResolver for use by this servlet */
    protected URIResolver uriResolver;

    public void init() throws ServletException {
        this.transFactory = TransformerFactory.newInstance();
        this.transFactory.setURIResolver(this.uriResolver);
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException {
        try {
            //Get parameters

            // Convert the User DN from UTF-8 in case it breaks!
            // KEITH ADDED THIS CODE, 26th November 2013
            System.out.println( "[Password reset] User DN we received: [" + request.getParameter(USER_REQUEST_PARAM) + "]");

            String USER_DN = convertFromUTF8(request.getParameter(USER_REQUEST_PARAM));
            System.out.println( "[Password reset] User DN after conversion: [" + USER_DN + "]");
            
            String NEW_PASSWORD = request.getParameter(PWD_REQUEST_PARAM);
            
            //Analyze parameters and decide with method to use
            if ((USER_DN != null) && (NEW_PASSWORD != null))
            {
                Properties props = new Properties();
                javax.servlet.ServletContext context = getServletContext();
                props.load(context.getResourceAsStream("/WEB-INF/directory.conf"));
                PrintWriter out = response.getWriter();

            String IDV_HOST = props.getProperty("IDV_HOST");
          String IDV_PORT = props.getProperty("IDV_PORT");
          String IDV_ADMIN = props.getProperty("IDV_ADMIN");
          String IDV_PASSWORD = props.getProperty("IDV_PASSWORD");
          Boolean USE_SSL = Boolean.parseBoolean(props.getProperty("USE_SSL"));

//          String IDV_HOST = "192.168.174.146"
//          String IDV_PORT = "636";
//          String IDV_ADMIN = "cn=admin,ou=users,ou=admin,o=belkast";
//          String IDV_PASSWORD = "novell";
//          String USER_DN = "uid=KARMST,ou=Employees,ou=Identities,ou=Resources,o=belkast";
//          String NEW_PASSWORD = "Test123";
//          Boolean USE_SSL = true;
 
            System.out.println( "[Password reset] Setting password for User: [" + USER_DN + "]");
 
            String MyResponse = setPassword(IDV_HOST,IDV_PORT,IDV_ADMIN,IDV_PASSWORD,USER_DN,NEW_PASSWORD,NEW_PASSWORD,USE_SSL);
            System.out.println( "[Password reset] Response from password reset: " + MyResponse);
 
            response.setContentType("text/html");
                out.print(MyResponse);
            }
//            else {
//                response.setContentType("text/html");
//                PrintWriter out = response.getWriter();
//                out.println("<html><head><title>Error</title></head>\n"
//                          + "<body><h1>Password Servlet Error</h1><h3>Invalid request param given.</body></html>");
//            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Converts a String parameter to a JAXP Source object.
     * @param param a String parameter
     * @return Source the generated Source object
     */
    protected Source convertString2Source(String param) {
        Source src;
        try {
            src = uriResolver.resolve(param, null);
        } catch (TransformerException e) {
            src = null;
        }
        if (src == null) {
            src = new StreamSource(new File(param));
        }
        return src;
    }

    public static String setPassword(String ldapHost, String port, String loginDN, String password, String objectDN, String pwd1, String pwd2, Boolean USE_SSL) throws Exception
  {

        try {
            NMASPwdMgr pwdMgrAdmin = doBind (ldapHost, port, loginDN, password, USE_SSL);            
            // Get the password
            String returnData = pwdMgrAdmin.getPwd("",objectDN);
//            return returnData;
            System.out.println("[Password reset] Current password for User [" + objectDN + "] is ["  + returnData + "]");

            NMASPwdMgr pwdMgrUser = doBind (ldapHost, port, objectDN, returnData, USE_SSL);
            pwdMgrUser.setPwd("",objectDN, pwd1);
        }
        catch( NMASPwdException pwde )
        {
            System.out.println( "[Password reset] ERROR [" + pwde.toString() + "] and NMAS Return Code [" + pwde.getNmasRetCode() + "]");
            return Integer.toString(pwde.getNmasRetCode());
        }
        
    return Integer.toString(0);

  }

private static NMASPwdMgr doBind (String ldapHost, String port, String loginDN, String password, Boolean USE_SSL) throws Exception
  {
        boolean jldap = false;        // Set to "true" for JLDAP and "false" for JNDI
        int methodID[]	= {0x00};
        NMASPwdMgr pwdMgr = null;

        int    ldapVersion = LDAPConnection.LDAP_V3;
        
        int    ldapPort = Integer.parseInt(port);

        if(jldap)
        {
            LDAPConnection ld  = new LDAPConnection();

            try
            {
                ld.connect( ldapHost, ldapPort);            // connect to the server
                ld.bind( ldapVersion, loginDN, password );  // bind to the server
                System.out.println( "bind() succeeded");
            }
            catch( LDAPException e )
            {
                System.out.println( "Error: " + e.toString() );
            }

            pwdMgr = new NMASPwdMgr(new PwdJLdapTransport(ld));
        }
        else
        {
            pwdMgr = new NMASPwdMgr(MyLdapCtx.getLdapCtx(ldapHost, loginDN, password, USE_SSL));
        }

return pwdMgr;
    }

        public static String readProperty(File MyFile, String varProperty)
            {
                Properties props = new Properties();
                String propertyValue = "";

                try
                    {
                        props.load( new FileInputStream(MyFile));
                        propertyValue = props.getProperty(varProperty);
                        System.out.println("<debug>Reading properties file for " + varProperty + ", got "      + propertyValue + "</debug>");
                    }
                catch (Exception e)
                    {
                        System.out.println("Exception: " + e.toString());
                    }
                return propertyValue;
            }
        
        public static String convertToUTF8(String s)
            {
                String out = null;
                try
                {
                    out = new String(s.getBytes("UTF-8"),"ISO-8859-1");
                }
                catch (java.io.UnsupportedEncodingException e)
                {
                    return null;
                }
                return out;
            }

        public static String convertFromUTF8(String s)
            {
                String out = null;
                try
                {
                    out = new String(s.getBytes("ISO-8859-1"),"UTF-8");
                }
                catch (java.io.UnsupportedEncodingException e)
                {
                    return null;
                }
                return out;
            }
}