/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package package_1337;

import java.io.File;
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
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 *
 * @author Michał
 */
@WebService(serviceName = "GetDataService")
public class GetDataService {
    String rootPath = "d:\\dane\\chmura\\dropbox\\programowanie\\projekty\\NetBeans\\TimWebAppAndroid";
    static int i = 0;
    
    @Resource
    WebServiceContext wsContext; 
    
    @WebMethod(operationName = "hello")
    public String sayHello(@WebParam(name = "wiadomosc") String txt) {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST); 
        String clientIp =  req.getRemoteAddr(); 
        System.out.println("txt: " + txt);
        return "Hello " + clientIp + "!";
    }
    
    @WebMethod(operationName = "login")
    public String login(@WebParam(name = "wiadomosc") String txt) {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST); 
        String clientIp =  req.getRemoteAddr(); 
        String clientLogin = null;
        String clientPassword = null;
        
        
        
        Uzytkownik u = new Uzytkownik("", clientIp);
        if (Bean.isUzytkownikZalogowany(u))
            return "zalogowany1";
        
        if (txt.contains(";")) {
            String[] loginhaslo = txt.split(";");
            
            
            clientLogin = loginhaslo[0];
            clientPassword = loginhaslo[1];
            
            u = new Uzytkownik();
            u.setLogin(clientLogin);
            u.setHaslo(clientPassword);
            
            if (Bean.isUzytkownikZalogowany(u))
                return "zalogowany2";
            
            if (Bean.validateUzytkownik(u)) {
                u = Bean.getUzytkownikKonto(u);
                u.setIP(clientIp);
                Bean.zalogujUzytkownika(u); // tu nastepuje logowanie
                return "zalogowano";
            }
        }
        
        System.out.println("txt: " + txt);
        return "niepoprawne dane";
    }
    
    @WebMethod(operationName = "wyloguj")
    public String wyloguj(@WebParam(name = "wiadomosc") String txt) {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST); 
        String clientIp =  req.getRemoteAddr(); 
        String clientLogin = null;
        String clientPassword = null;
        
        Bean.load(); 
        
        Uzytkownik u = new Uzytkownik("", clientIp);
        if (Bean.isUzytkownikZalogowany(u)) {
            Bean.wylogujUzytkownika(u);
            return "wylogowano"; 
        }
             
        //System.out.println("txt: " + txt);
        return "wylogowany";
    }
    
    @WebMethod(operationName = "rejestracja")
    public String rejestracja(@WebParam(name = "wiadomosc") String txt) {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST); 
        String clientIp =  req.getRemoteAddr(); 
        String clientLogin = null;
        String clientPassword = null;
        
        Bean.load();
        
        Uzytkownik u = new Uzytkownik("", clientIp);
        if (Bean.isUzytkownikZalogowany(u))
            return "zalogowany";
        
        if (txt.contains(";")) {
            String[] loginhaslo = txt.split(";");
            
            clientLogin = loginhaslo[0];
            clientPassword = loginhaslo[1];
            
            u = new Uzytkownik();
            u.setLogin(clientLogin);
            
            
            /*if (Bean.isUzytkownikZalogowany(u))
                return "zalogowany";*/
            
            if (Bean.isUzytkownikZarejestrowany(u)) {
                return "zarejestrowany";
            } else {
                u.setHaslo(clientPassword);
                Bean.zarejestrujUzytkownika(u); // tu nastepuje rejestracja
                Bean.save();
                return "zarejestrowano";
            }
        }
        
        System.out.println("txt: " + txt);
        return "niepoprawne dane";
    }
    
    
    @WebMethod(operationName = "setActiveProject")
    public String setActiveProject(@WebParam(name = "wiadomosc") String txt) {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST); 
        String clientIp =  req.getRemoteAddr(); 
        
        Bean.load();
        
        Uzytkownik u = new Uzytkownik("", clientIp);
        
        if (Bean.isUzytkownikZalogowany(u)) {
            Bean.setUzytkownikAktywnyProjekt(u, txt);
            Bean.save();
            System.out.println("txt: " + txt);
            return "nazwa projektu zmieniona";
        } 
        return "uzytkownik nie jest zalogowany: " + clientIp;
    }
    
    
    @WebMethod(operationName = "getPhoto")
    public String getPhoto(@WebParam(name = "wiadomosc") String txt) {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST); 
        String clientIp =  req.getRemoteAddr(); 
        String clientLogin = null;
        String clientProjectName = null;
        
        Bean.load();
        
        Uzytkownik u = new Uzytkownik("", clientIp);
        
        if (Bean.isUzytkownikZalogowany(u)) {
            clientLogin = Bean.getUzytkownikLogin(u);
            clientProjectName = Bean.getUzytkownikAktywnyProjekt(u);

            if (clientProjectName == null) {
                Bean.setUzytkownikAktywnyProjekt(u, "project_" + System.currentTimeMillis());
                Bean.save();
            }
            clientProjectName = Bean.getUzytkownikAktywnyProjekt(u);

            try {
                Base64.Decoder decoder = Base64.getMimeDecoder();
                byte[] bytes = decoder.decode(txt);

                FileOutputStream fos ;
                String filename = rootPath + "\\users\\" + clientLogin + "\\" + clientProjectName + "\\obrazki";
                new File(filename).mkdirs();
                fos = new FileOutputStream(filename + "\\plik_" + currentTimeMillis() + ".jpg");
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


            return "Odebrano wiadomosc o dlugosci " + txt.length() + " from " + clientLogin + ".";
        }
    
        return "uzytkownik nie jest zalogowany: " + clientIp;
    }
    
    @WebMethod(operationName = "listaKont")
    public String listaKont(@WebParam(name = "wiadomosc") String txt) {
        
        return Bean.getListaKont();
    }
    
    @WebMethod(operationName = "listaProjektow")
    public String listaProjektow(@WebParam(name = "wiadomosc") String txt) {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST); 
        String clientIp =  req.getRemoteAddr(); 
        String clientLogin = null;
        
        Uzytkownik u = new Uzytkownik("", clientIp);
        
        if (Bean.isUzytkownikZalogowany(u)) {
            clientLogin = Bean.getUzytkownikLogin(u);
            
            return "oczekujące: \n" + 
                    ZarzadzaniePlikami.getZadaniaNaLiscie(ZarzadzaniePlikami.listaZadanOczekujacych, clientLogin) +
                    "\nprzetworzone: \n" +
                    ZarzadzaniePlikami.getZadaniaNaLiscie(ZarzadzaniePlikami.listaZadanUkonczonych, clientLogin);
        }
        
        return "uzytkownik nie jest zalogowany: " + clientIp;
    }
    
        @WebMethod(operationName = "listaZalogowanych")
    public String listaZalogowanych(@WebParam(name = "wiadomosc") String txt) {
        Bean.load();
        return Bean.getListaZalogowanych();
    }
    
    @WebMethod(operationName = "startPrzetwarzania")
    public String startPrzetwarzania(@WebParam(name = "wiadomosc") String txt) {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST); 
        String clientIp =  req.getRemoteAddr(); 
        String clientLogin = null;
        String clientProjectName = null;
        
        Bean.load();
        
        Uzytkownik u = new Uzytkownik("", clientIp);
        
        if (Bean.isUzytkownikZalogowany(u)) {
            clientLogin = Bean.getUzytkownikLogin(u);
            clientProjectName = Bean.getUzytkownikAktywnyProjekt(u);
            
            
            try {
                ZarzadzaniePlikami.dodajZadanie(clientLogin, clientProjectName);
                ZarzadzaniePlikami.wyswietlZadaniaNaLiscie(ZarzadzaniePlikami.listaZadanOczekujacych, clientLogin);
            } catch (IOException ex) {
                return ex.getMessage();
            }
        
            System.out.println("Rozpoczynanie przetwarzania. Sposob przetwarzania: " + txt);
            return "Rozpoczynanie przetwarzania. Sposob przetwarzania: " + txt;
        }
        return "uzytkownik nie jest zalogowany: " + clientIp;
    }
}
