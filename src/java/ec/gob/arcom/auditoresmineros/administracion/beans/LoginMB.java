/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.arcom.auditoresmineros.administracion.beans;

import ec.gob.arcom.auditoresmineros.persistencia.daos.FuncionarioSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.Funcionario;
import ec.gob.arcom.auditoresmineros.util.Crypt;
import ec.gob.arcom.auditoresmineros.util.FacesUtilComun;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Will
 */
@Named(value = "loginMB")
@SessionScoped
public class LoginMB implements Serializable {
@EJB
    private FuncionarioSBLocal funcionarioSB;
    
    private String userName;
    private String userPassword;

    private boolean logged;
    private boolean administrador;
    
    private String nombreFuncionario;

    /**
     * Get the value of administrador
     *
     * @return the value of administrador
     */
    public boolean isAdministrador() {
        return administrador;
    }


    /**
     * Get the value of logged
     *
     * @return the value of logged
     */
    public boolean isLogged() {
        return logged;
    }

    /**
     * Get the value of userPassword
     *
     * @return the value of userPassword
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * Set the value of userPassword
     *
     * @param userPassword new value of userPassword
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }


    /**
     * Get the value of userName
     *
     * @return the value of userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the value of userName
     *
     * @param userName new value of userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    
    /**
     * Creates a new instance of LoginMB
     */
    public LoginMB() {
    }
    
    /**
     * Cierra la sesión del usuario actual
     * @return El caso de navegación a donde será dirigido
     */
    public String logoutAction() {
        HttpSession session = FacesUtilComun.getSession();
        session.invalidate();
        return "login";
    }
    
    public String loginAction() {
        return validarLogin(userName, userPassword);
    }
    
    private String validarLogin(String usr, String pwd) {
        boolean result= this.obtenerUsuario(usr, pwd);
        if(result) {
            FacesUtilComun.showInfoMessage("Bienvenid@", nombreFuncionario);
            return "cuentaadmin";
        } else {
            FacesUtilComun.showErrorMessage("Login invalido!", "Usuario o clave incorrectos!");
        }
        return "login";
    }
    
    private boolean obtenerUsuario(String userName, String userPassword) {
        Funcionario f= this.funcionarioSB.findByUserName(userName);
        
        if (f!=null) {
            if(validarCredenciales(userPassword, f.getUserPassword())) {
                this.logged= true;
                this.administrador= f.isAdministrador();
                this.nombreFuncionario= f.getNombre().toUpperCase() + " " + f.getApellido().toUpperCase();
                HttpSession session = FacesUtilComun.getSession();
                session.setAttribute("logged", logged);
                session.setAttribute("admin", f.isAdministrador());
                session.setAttribute("nombre", f.getNombre().toUpperCase());
                session.setAttribute("apellido", f.getApellido().toUpperCase());
                session.setAttribute("username", userName);
                session.setAttribute("editarcurso", false);
                session.setAttribute("tipofuncionario", f.getTipoFuncionario());
                return true;
            }
        }
        return false;
    }
    
    private boolean validarCredenciales(String pwdUser, String pwdUserDB) {
        if(pwdUserDB.equals(Crypt.cryptMD5(pwdUser)))
            return true;
        return false;
    }
}
