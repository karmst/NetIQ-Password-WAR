/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novell.password.servlet;

import java.util.*;
import javax.naming.*;
import javax.naming.ldap.*;
import com.sun.jndi.ldap.*;

public class MyLdapCtx
{        
    public static LdapContext getLdapCtx(String ldapHost, String loginDN, String pwd, boolean ssl)
    {
        LdapContext ldapCtx = null;
        int ldapPort;
        
        try
        { 
            Hashtable env = new Hashtable(5, 0.75f);
      
            if(ssl)
            {
                ldapPort     = LdapCtx.DEFAULT_SSL_PORT;
                env.put(Context.SECURITY_PROTOCOL, "ssl");
            }
            else
            {
                ldapPort     = LdapCtx.DEFAULT_PORT;
            }
            
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, "ldap://" + ldapHost +":" + ldapPort);        
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, loginDN);
            env.put(Context.SECURITY_CREDENTIALS, pwd); 

           // Construct an LdapContext object.       
            ldapCtx = new InitialLdapContext(env, null);
        } 
        catch (NamingException e)
        {
            System.err.println("Error getting LdapCtx:  ");
            e.printStackTrace();
        }
            
        return ldapCtx;
    }
}