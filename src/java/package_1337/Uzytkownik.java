/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package package_1337;

/**
 *
 * @author Michał
 */
public class Uzytkownik implements java.io.Serializable  {
    String login = null;
    String haslo = null;
    String IP = null;
    String aktywnyProjekt = null;

    public Uzytkownik() {
    }
    
    public Uzytkownik(String login, String IP) {
        this.login = login;
        this.IP = IP;
    }
    
    

    
    @Override
    public String toString() {
        return "Uzytkownik [login=" + login + ", IP=" + IP + ", aktywnyProjekt=" + aktywnyProjekt + "]";
    }

    @Override
    public boolean equals(Object o) {
        Uzytkownik u = (Uzytkownik) o;
        
        /*if (u.getHaslo().length() > 0)
            return login.equals(u.getLogin()) &&  haslo.equals(u.getHaslo());*/
//        
//        if (u.getHaslo().length() == 0 && u.getIP().length() == 0 )
//            return login.equals(u.getLogin());
        if (getLogin().equals(""))
            return IP.equals(u.getIP()) ;
        return login.equals(u.getLogin());
        
        
    }

    public boolean validate(Uzytkownik u) {
        return login.equals(u.getLogin()) && haslo.equals(u.getHaslo()) ;
    }
    
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHaslo() {
        return haslo;
    }

    public void setHaslo(String haslo) {
        this.haslo = haslo;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getAktywnyProjekt() {
        return aktywnyProjekt;
    }

    public void setAktywnyProjekt(String aktywnyProjekt) {
        this.aktywnyProjekt = aktywnyProjekt;
    }
    
    
}
