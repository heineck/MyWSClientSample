/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mywsclientsample;

import javax.xml.ws.BindingProvider;

/**
 *
 * @author vheineck
 */
public class MyWSClientSample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String res = MyWSClientSample.hello("TESTVinicius");
        
        System.out.println("res: " + res);
        
    }

    private static String hello(java.lang.String name) {
        com.vheineck.ws.Plus_Service service = new com.vheineck.ws.Plus_Service();
        com.vheineck.ws.Plus port = service.getPlusPort();
        
        ((BindingProvider)port).getRequestContext().put("USERNAME", "vinicius");
        ((BindingProvider)port).getRequestContext().put("PASSWORD", "123456");
        
        return port.hello(name);
    }
    
}
