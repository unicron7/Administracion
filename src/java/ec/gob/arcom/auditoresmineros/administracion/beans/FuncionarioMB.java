/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.arcom.auditoresmineros.administracion.beans;

import ec.gob.arcom.auditoresmineros.catalogos.Catalogo;
import ec.gob.arcom.auditoresmineros.controllers.CatalogoController;
import ec.gob.arcom.auditoresmineros.persistencia.daos.CatalogoSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.FuncionarioSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.TipoCatalogoSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.Funcionario;
import ec.gob.arcom.auditoresmineros.util.Crypt;
import ec.gob.arcom.auditoresmineros.util.FacesUtilComun;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

/**
 *
 * @author Will
 */
@Named(value = "funcionarioMB")
@RequestScoped
public class FuncionarioMB implements Serializable {
    @EJB
    private FuncionarioSBLocal funcionarioSB;
    @EJB
    private TipoCatalogoSBLocal tipoCatalogoSB;
    @EJB
    private CatalogoSBLocal catalogoSB;
    
    /**
     * Creates a new instance of FuncionarioMB
     */
    public FuncionarioMB() {
        this.funcionario= new Funcionario();
    }
    
    @PostConstruct
    private void cargarFuncionarioEditar() {
        this.passwd= "";
        this.edit= (Boolean) FacesUtilComun.getSession().getAttribute("editarfuncionario");
        if(edit) {
            this.funcionario= (Funcionario) FacesUtilComun.getSession().getAttribute("funcionario");
        }
    }
    
    private Funcionario funcionario;
    private String passwd;
    private boolean edit;
    
    @Inject
    private AuditorAdminMB auditorAdminMB;
    
    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }
    
    public String saveAction() {
        if(this.edit) {
            funcionario.setFechaActualizacion(Calendar.getInstance().getTime());
            funcionario.setHoraActualizacion(Calendar.getInstance().getTime());
            funcionarioSB.update(funcionario);
            FacesUtilComun.showInfoMessage("Aviso", "¡Actualización exitosa!");
            this.resetAll();
            auditorAdminMB.obtenerFuncionarios();
            return "cuentaadmin";
        }
        
        funcionario.setUserPassword(Crypt.cryptMD5(funcionario.getUserPassword()));
        funcionario.setFechaRegistro(Calendar.getInstance().getTime());
        funcionario.setHoraRegistro(Calendar.getInstance().getTime());
        funcionarioSB.save(funcionario);
        auditorAdminMB.obtenerFuncionarios();
        FacesUtilComun.showInfoMessage("Aviso", "¡Usuario guardado con éxito!");
        return "cuentaadmin";
    }
    
    public String cancelAction() {
        this.resetAll();
        return "cuentaadmin";
    }
    
    private void resetAll() {
        this.funcionario= new Funcionario();
        this.passwd= "";
       FacesUtilComun.getSession().setAttribute("editarfuncionario", false);
    }
    
    public String savePassword() {
        funcionario.setUserPassword(Crypt.cryptMD5(passwd));
        funcionario.setFechaActualizacion(Calendar.getInstance().getTime());
        funcionario.setHoraActualizacion(Calendar.getInstance().getTime());
        funcionarioSB.update(funcionario);
        this.resetAll();
        FacesUtilComun.showInfoMessage("Aviso", "¡Actualización de clave exitosa!");
        return "cuentaadmin";
    }
}
