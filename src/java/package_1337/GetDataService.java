/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package package_1337;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.System.currentTimeMillis;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michał
 */
@WebService(serviceName = "GetDataService")
public class GetDataService {

    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "hello")
    public String sayHello(@WebParam(name = "name") String txt) {
        System.out.println("txt: " + txt);
        return "Hello " + txt + "!";
    }
    
    @WebMethod(operationName = "getPhoto")
    public String getPhoto(@WebParam(name = "photo") String txt) {
        //System.out.println(txt);
        
        try {
            Base64.Decoder decoder = Base64.getMimeDecoder();
            byte[] bytes = decoder.decode(txt);
        
            FileOutputStream fos ;
            fos = new FileOutputStream("d:\\dane\\chmura\\dropbox\\programowanie\\projekty\\NetBeans\\TimWebAppAndroid\\work\\plik_" + currentTimeMillis() + ".jpg");
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException ex) {
            System.out.println("blad: " + ex.getMessage());
            return "blad: " + ex.getMessage();
        } catch (IOException ex) {
            System.out.println("blad: " + ex.getMessage());
            return "blad: " + ex.getMessage();
        } catch (Exception ex) {
            System.out.println("blad: " + ex.getMessage());
            return "blad: " + ex.getMessage();
        }
 
        return "Odebrano wiadomosc o dlugosci " + txt.length() + ".";
    }
}
