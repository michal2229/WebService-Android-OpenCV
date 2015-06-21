/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package package_1337;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Michał
 */

@ManagedBean()
@ApplicationScoped
public class Bean {
    private String wiadomosc = "pierwotna stesc wiadomosci";
    private String historiaWiadomosci = "77";

    public String getWiadomosc() {
        return wiadomosc;
    }

    public void setWiadomosc(String wiadomosc) {
        this.wiadomosc = wiadomosc;
        this.setHistoriaWiadomosci(wiadomosc);
    }

    public String getHistoriaWiadomosci() {
        return historiaWiadomosci;
    }

    public void setHistoriaWiadomosci(String historiaWiadomosci) {
        this.historiaWiadomosci += historiaWiadomosci;
    }
    
    
    
}
