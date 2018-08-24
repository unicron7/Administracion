/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.arcom.auditoresmineros.administracion.beans;

import ec.gob.arcom.auditoresmineros.catalogos.Catalogo;
import ec.gob.arcom.auditoresmineros.persistencia.daos.CatalogoSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.CursoSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.TipoCatalogoSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.Curso;
import ec.gob.arcom.auditoresmineros.util.FacesUtilComun;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

/**
 *
 * @author Will
 */
@Named(value = "cursoMB")
@RequestScoped
public class CursoMB {
    @EJB
    private CursoSBLocal cursoSB;
    @EJB
    private TipoCatalogoSBLocal tipoCatalogoSB;
    @EJB
    private CatalogoSBLocal catalogoSB;

    /**
     * Creates a new instance of CourseMB
     */
    public CursoMB() {
        this.curso= new Curso();
    }
    
    @PostConstruct
    private void cargarCursoEditar() {
        this.edit= (Boolean) FacesUtilComun.getSession().getAttribute("editarcurso");
        if(edit) {
            this.curso= (Curso) FacesUtilComun.getSession().getAttribute("curso");
            this.selectItemValue= this.curso.getInstitucion();
        }
    }
    
    private Curso curso;
    private List<Curso> cursos;
    private Date fechaMinima;
    //private List<Instit> instituciones;
    private Catalogo selectItemValue;
    private boolean edit;
    
    @Inject
    private AuditorAdminMB auditorAdminMB;

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    public Date getFechaMinima() {
        this.fechaMinima= Calendar.getInstance().getTime();
        return fechaMinima;
    }

    /*public SelectItem[] getInstituciones() {
        instituciones= CatalogoController.getInstituciones(tipoCatalogoSB, catalogoSB);
        return instituciones;
    }

    public void setInstituciones(SelectItem[] instituciones) {
        this.instituciones = instituciones;
    }*/

    public Catalogo getSelectItemValue() {
        return selectItemValue;
    }

    public void setSelectItemValue(Catalogo selectItemValue) {
        this.selectItemValue = selectItemValue;
    }
    
    public String saveAction() {
        if(this.edit) {
            curso.setInstitucion(selectItemValue);
            cursoSB.update(curso);
            FacesUtilComun.showInfoMessage("Aviso", "¡Actualización exitosa!");
            this.resetAll();
            auditorAdminMB.obtenerCursos();
            return "cuentaadmin";
        }
        curso.setInstitucion(selectItemValue);
        curso.setFechaDeRegistro(Calendar.getInstance().getTime());
        curso.setHoraDeRegistro(Calendar.getInstance().getTime());
        cursoSB.save(curso);
        auditorAdminMB.obtenerCursos();
        FacesUtilComun.showInfoMessage("Aviso", "¡Curso guardado con éxito!");
        return "cuentaadmin";
    }
    
    public String cancelAction() {
        this.resetAll();
        return "cuentaadmin";
    }
    
    private void resetAll() {
        this.curso= new Curso();
        FacesUtilComun.getSession().setAttribute("editarcurso", false);
    }
    
    public void obtenerCursos() {
        this.cursos= this.cursoSB.listByEstado();
    }
    
}
