/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vheineck.handlers;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import sun.misc.BASE64Encoder;

/**
 *
 * @author vheineck
 */
public class SimpleWSAuthHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        
        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
  
        if (outboundProperty.booleanValue()) {
  
            try {
                
                String usernameStr = "myusername";
                String passwordStr = "myPassword";
                
                if(context.containsKey("USERNAME")){
                   usernameStr = context.get("USERNAME").toString();
                }
                
                if(context.containsKey("PASSWORD")){
                   passwordStr = context.get("PASSWORD").toString();
                }
                
                System.out.println("Username[" + usernameStr + "], Password[" + passwordStr + "]");

                //From the spec: Password_Digest = Base64 ( SHA-1 ( nonce + created + password ) )
                //Make the nonce
                SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
                rand.setSeed(System.currentTimeMillis());
                byte[] nonceBytes = new byte[16];
                rand.nextBytes(nonceBytes);

                //Make the created date
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                String createdDate = df.format(Calendar.getInstance().getTime());
                byte[] createdDateBytes = createdDate.getBytes("UTF-8");

                //Make the password
                byte[] passwordBytes = passwordStr.getBytes("UTF-8");

                //SHA-1 hash the bunch of it.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(nonceBytes);
                baos.write(createdDateBytes);
                baos.write(passwordBytes);
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] digestedPassword = md.digest(baos.toByteArray());

                //Encode the password and nonce for sending                   
                String passwordB64 = (new BASE64Encoder()).encode(digestedPassword);
                String nonceB64 = (new BASE64Encoder()).encode(nonceBytes);

                //Now create the header with all the appropriate elements
                SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
                
                if (envelope.getHeader() != null) {
                    envelope.getHeader().detachNode();
                }
                 
                SOAPHeader header = envelope.addHeader();
                SOAPElement security = header.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");

                SOAPElement username = usernameToken.addChildElement("Username", "wsse");
                username.addTextNode(usernameStr);

                SOAPElement password = usernameToken.addChildElement("Password", "wsse");
                password.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
                password.addTextNode(passwordB64);

                SOAPElement nonce = usernameToken.addChildElement("Nonce", "wsse");
                nonce.addTextNode(nonceB64);

                SOAPElement created = usernameToken.addChildElement("Created", "wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
                created.addTextNode(createdDate);

                context.getMessage().writeTo(System.out);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    
        return outboundProperty;
        
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public void close(MessageContext context) {
    }
    
}
