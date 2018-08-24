/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.arcom.auditoresmineros.administracion.beans;

import ec.gob.arcom.auditoresmineros.catalogos.Catalogo;
import ec.gob.arcom.auditoresmineros.config.SystemConfiguration;
import ec.gob.arcom.auditoresmineros.controllers.AuditoriaController;
import ec.gob.arcom.auditoresmineros.controllers.FileDownloadController;
import ec.gob.arcom.auditoresmineros.controllers.FileUploadController;
import ec.gob.arcom.auditoresmineros.controllers.LocalidadController;
import ec.gob.arcom.auditoresmineros.keygen.Keygen;
import ec.gob.arcom.auditoresmineros.mail.FromTo;
import ec.gob.arcom.auditoresmineros.mail.MailSender;
import ec.gob.arcom.auditoresmineros.persistencia.daos.AuditorSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.AuditoriaSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.CursoSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.EmpresaConsultoraSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.FuncionarioSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.LocalidadSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.ProfesionalAuditorSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.daos.SystemConfigurationSBLocal;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.Auditor;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.Auditoria;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.Curso;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.EmpresaConsultoraAuditoria;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.Funcionario;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.Localidad;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.ProfesionalAuditorExterno;
import ec.gob.arcom.auditoresmineros.persistencia.entidades.TipoAuditor;
import ec.gob.arcom.auditoresmineros.util.Crypt;
import ec.gob.arcom.auditoresmineros.util.FacesUtilComun;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Will
 */
@Named(value = "auditorAdminMB")
@SessionScoped
public class AuditorAdminMB implements Serializable {
    
    private static final String NATURAL= "NATURAL";
    private static final String JURIDICA= "JURIDICA";
    private static final String RESOLUCION= "RESOLUCION";
    private static final String ASUNTOMSG= "Info Auditores Técnicos Mineros";
    
    @EJB
    private AuditorSBLocal auditorSB;
    @EJB
    private ProfesionalAuditorSBLocal personaNaturalSB;
    @EJB
    private EmpresaConsultoraSBLocal personaJuridicaSB;
    @EJB
    private LocalidadSBLocal localidadSB;
    @EJB
    private CursoSBLocal cursoSB;
    @EJB
    private FuncionarioSBLocal funcionarioSB;
    @EJB
    private SystemConfigurationSBLocal sysConfigSB;
    @EJB
    private AuditoriaSBLocal auditoriaDao;

    /**
     * Creates a new instance of AuditorAdminMB
     */
    public AuditorAdminMB() {
        personaNatural= new ProfesionalAuditorExterno();
        personasNaturales= new ArrayList<>();
        personaJuridica= new EmpresaConsultoraAuditoria();
        personasJuridicas= new ArrayList<>();
        systemConfigs= new ArrayList<>();
        gestionarTabs();
    }
    
    @PostConstruct
    private void cargarListas() {
        obtenerPersonasNaturales();
        obtenerPersonasJuridicas();
        obtenerCursos();
        obtenerFuncionarios();
        obtenerConfiguraciones();
        obtenerAllAuditorias();
    }
    
    private void gestionarTabs() {
        Catalogo tipoFuncionario= (Catalogo) FacesUtilComun.getSession().getAttribute("tipofuncionario");
        if(tipoFuncionario.getNemonico().compareTo(Funcionario.ADMINISTRADOR)==0 && FacesUtilComun.isAdmin()) {
            showtabpn= true;
            showtabpj= true;
            showtabcursos= true;
            showtabusuarios= true;
            showtabreportes= true;
            showtabauditorias= true;
            disabledInformeLnk= false;
            disabledResolicionLnk= false;
            renderedPanelBusqueda= true;
            showResetPassword= true;
        } else if(tipoFuncionario.getNemonico().compareTo(Funcionario.CALIFICADOR)==0){
            showtabpn= true;
            showtabpj= true;
            showtabcursos= false;
            showtabusuarios= false;
            showtabreportes= true;
            showtabauditorias= true;
            disabledInformeLnk= false;
            disabledResolicionLnk= true;
            renderedPanelBusqueda= true;
            showResetPassword= false;
        } else if(tipoFuncionario.getNemonico().compareTo(Funcionario.TALENTO_HUMANO)==0) {
            showtabpn= false;
            showtabpj= false;
            showtabcursos= true;
            showtabusuarios= false;
            showtabreportes= false;
            showtabauditorias= false;
            disabledInformeLnk= true;
            disabledResolicionLnk= true;
            renderedPanelBusqueda= false;
            showResetPassword= false;
        } else if(tipoFuncionario.getNemonico().compareTo(Funcionario.JURIDICO)==0) {
            showtabpn= true;
            showtabpj= true;
            showtabcursos= false;
            showtabusuarios= false;
            showtabreportes= false;
            showtabauditorias= false;
            disabledInformeLnk= true;
            disabledResolicionLnk= false;
            renderedPanelBusqueda= true;
            showResetPassword= false;
        } else if(tipoFuncionario.getNemonico().compareTo(Funcionario.DIRECTIVO)==0) {
            showtabpn= false;
            showtabpj= false;
            showtabcursos= false;
            showtabusuarios= false;
            showtabreportes= true;
            showtabauditorias= true;
            disabledInformeLnk= false;
            disabledResolicionLnk= false;
            renderedPanelBusqueda= false;
            showResetPassword= false;
        }
    }
    
    /*
    * Propiedades
    */
    private ProfesionalAuditorExterno personaNatural;
    private List<ProfesionalAuditorExterno> personasNaturales;
    private List<ProfesionalAuditorExterno> filteredPersonasNaturales;
    private EmpresaConsultoraAuditoria personaJuridica;
    private List<EmpresaConsultoraAuditoria> personasJuridicas;
    private List<EmpresaConsultoraAuditoria> filteredPersonasJuridicas;
    private List<Curso> cursos;
    private List<Funcionario> funcionarios;
    private int activeTab= 0;
    private boolean showtabreportes= false;
    private boolean showtabauditorias= false;
    private boolean showtabpn= false;
    private boolean showtabpj= false;
    private boolean showtabcursos= false;
    private boolean showtabusuarios= false;
    private boolean disabledInformeLnk= false;
    private boolean disabledResolicionLnk= false;
    private boolean renderedPanelBusqueda= false;
    private String enviarResolucionMsgPersona;
    private List<SystemConfiguration> systemConfigs;
    private boolean showfuadjuntosfase02= false;
    private boolean filtroAplicadoPN= false;
    private boolean filtroAplicadoPJ= false;
    private boolean showResetPassword= false;
    private int auditorModificar=0;
    
    private Catalogo selectedCatalogValue;
    private Catalogo selectedEstadoValue;
    private List<String> tiposAuditor;
    
    //Cargar archivos
    private String rucPersona;
    private Catalogo estadoActual;
    private boolean renderedUploadPanel;
    private boolean renderedMsgPanel;
    private String reporteAuditores= "../../birt/frameset?__report=report/auditortecnicominero/reporteAuditores.rptdesign&__format=xlsx";
    
    private List<Auditoria> allAuditorias;
    
    //Cargar las provincias
    private List<Localidad> provincias;
    
    public List<Auditoria> getAllAuditorias() {
        return allAuditorias;
    }

    /*
     * Getters and Setters
     */
    public void setAllAuditorias(List<Auditoria> allAuditorias) {
        this.allAuditorias = allAuditorias;
    }

    public List<String> getTiposAuditor() {
        if(this.personaNatural.getId()!=null) {
            this.tiposAuditor= this.generarListaTipos(personaNatural.getTiposAuditor());
        } else if(this.personaJuridica.getId()!=null) {
            this.tiposAuditor= this.generarListaTipos(personaJuridica.getTiposAuditor());
        }
        
        return tiposAuditor;
    }

    public String getReporteAuditores() {
        return reporteAuditores;
    }

    public void setReporteAuditores(String reporteAuditores) {
        this.reporteAuditores = reporteAuditores;
    }

    public Catalogo getSelectedEstadoValue() {
        return selectedEstadoValue;
    }

    public void setSelectedEstadoValue(Catalogo selectedEstadoValue) {
        this.selectedEstadoValue = selectedEstadoValue;
    }
    
    public void setTiposAuditor(List<String> tiposAuditor) {
        this.tiposAuditor = tiposAuditor;
    }
    
    public Catalogo getSelectedCatalogValue() {
        return selectedCatalogValue;
    }

    public void setSelectedCatalogValue(Catalogo selectedCatalogValue) {
        this.selectedCatalogValue = selectedCatalogValue;
    }
    
    public boolean isFiltroAplicadoPN() {
        return filtroAplicadoPN;
    }

    public void setFiltroAplicadoPN(boolean filtroAplicadoPN) {
        this.filtroAplicadoPN = filtroAplicadoPN;
    }

    public boolean isFiltroAplicadoPJ() {
        return filtroAplicadoPJ;
    }

    public void setFiltroAplicadoPJ(boolean filtroAplicadoPJ) {
        this.filtroAplicadoPJ = filtroAplicadoPJ;
    }
    
    public boolean isShowfuadjuntosfase02() {
        return showfuadjuntosfase02;
    }
    
    public void setShowfuadjuntosfase02(boolean showfuadjuntosfase02) {    
        this.showfuadjuntosfase02 = showfuadjuntosfase02;
    }

    public List<SystemConfiguration> getSystemConfigs() {
        return systemConfigs;
    }
    
    public void setSystemConfigs(List<SystemConfiguration> systemConfigs) {
        this.systemConfigs = systemConfigs;
    }

    public List<ProfesionalAuditorExterno> getPersonasNaturales() {
        return personasNaturales;
    }

    public void setPersonasNaturales(List<ProfesionalAuditorExterno> personasNaturales) {
        this.personasNaturales = personasNaturales;
    }

    public List<ProfesionalAuditorExterno> getFilteredPersonasNaturales() {
        return filteredPersonasNaturales;
    }

    public void setFilteredPersonasNaturales(List<ProfesionalAuditorExterno> filteredPersonasNaturales) {
        this.filteredPersonasNaturales = filteredPersonasNaturales;
    }

    public List<EmpresaConsultoraAuditoria> getPersonasJuridicas() {
        return personasJuridicas;
    }

    public void setPersonasJuridicas(List<EmpresaConsultoraAuditoria> personasJuridicas) {
        this.personasJuridicas = personasJuridicas;
    }

    public List<EmpresaConsultoraAuditoria> getFilteredPersonasJuridicas() {
        return filteredPersonasJuridicas;
    }

    public void setFilteredPersonasJuridicas(List<EmpresaConsultoraAuditoria> filteredPersonasJuridicas) {
        this.filteredPersonasJuridicas = filteredPersonasJuridicas;
    }

    public ProfesionalAuditorExterno getPersonaNatural() {
        return personaNatural;
    }

    public void setPersonaNatural(ProfesionalAuditorExterno personaNatural) {
        this.personaNatural = personaNatural;
    }

    public EmpresaConsultoraAuditoria getPersonaJuridica() {
        return personaJuridica;
    }

    public void setPersonaJuridica(EmpresaConsultoraAuditoria personaJuridica) {
        this.personaJuridica = personaJuridica;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }
    
    //Cargar provincias
    public List<Localidad> getProvincias() {
        this.provincias= LocalidadController.getProvincias(localidadSB);
        return provincias;
    }

    public void setProvincias(List<Localidad> provincias) {
        this.provincias = provincias;
    }

    public boolean isShowtabreportes() {
        return showtabreportes;
    }

    public void setShowtabreportes(boolean showtabreportes) {
        this.showtabreportes = showtabreportes;
    }

    public boolean isShowtabauditorias() {
        return showtabauditorias;
    }

    public void setShowtabauditorias(boolean showtabauditorias) {
        this.showtabauditorias = showtabauditorias;
    }

    public int getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(int activeTab) {
        this.activeTab = activeTab;
    }

    public boolean isShowtabpn() {
        return showtabpn;
    }

    public void setShowtabpn(boolean showtabpn) {
        this.showtabpn = showtabpn;
    }

    public boolean isShowtabpj() {
        return showtabpj;
    }

    public void setShowtabpj(boolean showtabpj) {
        this.showtabpj = showtabpj;
    }

    public boolean isShowtabcursos() {
        return showtabcursos;
    }

    public void setShowtabcursos(boolean showtabcursos) {
        this.showtabcursos = showtabcursos;
    }

    public boolean isShowtabusuarios() {
        return showtabusuarios;
    }

    public void setShowtabusuarios(boolean showtabusuarios) {
        this.showtabusuarios = showtabusuarios;
    }

    public boolean isDisabledInformeLnk() {
        return disabledInformeLnk;
    }

    public void setDisabledInformeLnk(boolean disabledInformeLnk) {
        this.disabledInformeLnk = disabledInformeLnk;
    }

    public boolean isDisabledResolicionLnk() {
        return disabledResolicionLnk;
    }

    public void setDisabledResolicionLnk(boolean disabledResolicionLnk) {
        this.disabledResolicionLnk = disabledResolicionLnk;
    }

    public boolean isRenderedPanelBusqueda() {
        return renderedPanelBusqueda;
    }

    public void setRenderedPanelBusqueda(boolean renderedPanelBusqueda) {
        this.renderedPanelBusqueda = renderedPanelBusqueda;
    }

    public boolean isShowResetPassword() {
        return showResetPassword;
    }

    public void setShowResetPassword(boolean showResetPassword) {
        this.showResetPassword = showResetPassword;
    }

    public int getAuditorModificar() {
        return auditorModificar;
    }

    public void setAuditorModificar(int auditorModificar) {
        this.auditorModificar = auditorModificar;
    }
    
    /*
    * Acciones
    */
    //Enviar la nueva clave al correo del usuario
    private void sendNewPassword(String razonSocial, String ruc, String key, String email) {
        FromTo ft= new FromTo();
        MailSender ms= new MailSender();
        ft.setTo(email);
        ft.setSubject(AuditorAdminMB.ASUNTOMSG);
        ft.setMsg(ms.getPasswordResetMsg(razonSocial, ruc, key));
        ms.sendMailHTML(ft);
    }
    
    //Resetear la clave de un usuario y enviar la misma al correo electrónico registrado
    public void resetPasswordNatural() {
        this.personaNatural= this.personasNaturales.get(auditorModificar);
        String key= Keygen.getPassword();
        this.personaNatural.setPasswd(Crypt.cryptMD5(key));
        this.personaNaturalSB.updatePAE(personaNatural);
        sendNewPassword(personaNatural.getRazonSocial(), personaNatural.getRuc(), key, personaNatural.getEmail());
        FacesUtilComun.showInfoMessage("", "Clave restablecida correctamente");
    }
    
    //Resetear la clave de un usuario y enviar la misma al correo electrónico registrado
    public void resetPasswordJuridica() {
        this.personaJuridica= this.personasJuridicas.get(auditorModificar);
        String key= Keygen.getPassword();
        this.personaJuridica.setPasswd(Crypt.cryptMD5(key));
        this.personaJuridicaSB.updateECA(personaJuridica);
        sendNewPassword(personaJuridica.getRazonSocial(), personaJuridica.getRuc(), key, personaJuridica.getEmail());
        FacesUtilComun.showInfoMessage("", "Clave restablecida correctamente");
    }
    
    public void deleteActionNatural() {
        auditorSB.delete(personasNaturales.get(auditorModificar));
        obtenerPersonasNaturales();
        FacesUtilComun.showInfoMessage("", "Auditor eliminado correctamente");
    }
    
    public void deleteActionJuridica() {
        auditorSB.delete(personasJuridicas.get(auditorModificar));
        obtenerPersonasJuridicas();
        FacesUtilComun.showInfoMessage("", "Auditor eliminado correctamente");
    }
    
    public void establecerAuditorModificar(Integer row) {
        this.auditorModificar= row;
    }
    
    //Cargar todos los auditores del tipo persona natural
    public void obtenerPersonasNaturales() {
        this.personasNaturales= personaNaturalSB.listarPAEActivo();
    }
    
    //Cargar los datos a ser modificados y mostrar el formulario
    public String editNaturalAction(Integer row) {
        if(filtroAplicadoPN) {
            this.personaNatural= this.filteredPersonasNaturales.get(row);
        } else {
            this.personaNatural= this.personasNaturales.get(row);
        }
        this.rucPersona= this.personaNatural.getRuc();
        this.estadoActual= this.personaNatural.getEstado();
        this.personaNatural= this.personaNaturalSB.getPAEJoinFetchPago(personaNatural.getId());
        this.enviarResolucionMsgPersona= AuditorAdminMB.NATURAL;
        FacesUtilComun.getSession().setAttribute("idAuditor", this.personaNatural.getId());
        
        if(FacesUtilComun.isAdmin())
            return "editpnat";
        return "editpnatestados";
    }
    
     //Cargar todos los auditores del tipo persona juridica
    public void obtenerPersonasJuridicas() {
        this.personasJuridicas= personaJuridicaSB.listarECAActivo();
    }
    
    //Cargar los datos a ser modificados y mostrar el formulario
    public String editJuridicaAction(Integer row) {
        if(filtroAplicadoPJ) {
            this.personaJuridica= this.filteredPersonasJuridicas.get(row);
        } else {
            this.personaJuridica= this.personasJuridicas.get(row);
        }
        
        this.rucPersona= this.personaJuridica.getRuc();
        this.estadoActual= this.personaJuridica.getEstado();
        this.personaJuridica= this.personaJuridicaSB.getECAJoinFetchPago(personaJuridica.getId());
        this.enviarResolucionMsgPersona= AuditorAdminMB.JURIDICA;
        FacesUtilComun.getSession().setAttribute("idAuditor", this.personaJuridica.getId());
        
        if(FacesUtilComun.isAdmin())
            return "editpjur";
        return "editpjurestados";
    }
    
    public String newCursoAction() {
        return "cursoform";
    }
    
    public void deleteCursoAction(Integer row) {
        cursoSB.delete(cursos.get(row));
        obtenerCursos();
    }
    
    //Cargar los datos del curso a ser modificado y mostrar el formulario
    public String editCursoAction(Integer row) {
        FacesUtilComun.getSession().setAttribute("editarcurso", true);
        FacesUtilComun.getSession().setAttribute("curso", this.cursos.get(row));
        return "cursoform";
    }
    
    //Cargar todos los cursos
    public void obtenerCursos() {
        this.cursos= cursoSB.listByEstado();
    }
    
    //Cargar todos los funcionarios
    public void obtenerFuncionarios() {
        this.funcionarios= funcionarioSB.list();
    }
    
    public String cancelAction() {
        this.resetAll();
        return "cuentaadmin";
    }
    
    private void resetAll() {
        personaNatural= new ProfesionalAuditorExterno();
        personaJuridica= new EmpresaConsultoraAuditoria();
    }
    
    public String updateActionPNat() {
        Long idTemp= personaNatural.getId();
        
        if(this.personaNatural.getEstado().getId().compareTo(this.estadoActual.getId())!=0) {
            if(this.personaNatural.getEstado().getNemonico().compareTo(Auditor.REGISTRADO)==0) {
                if(!existeInforme()) {
                    FacesUtilComun.showErrorMessage("Error", "Debe cargar el informe de inscripción");
                    return null;
                }
            } else if(this.personaNatural.getEstado().getNemonico().compareTo(Auditor.INSCRITO_CALIFICADO)==0) {
                if(!existeResolucion()) {
                    FacesUtilComun.showErrorMessage("Error", "No ha sido cargada la resolución de calificación");
                    return null;
                }
            }
        }
        
        this.personaNaturalSB.updatePAE(personaNatural);
        this.personasNaturales.removeAll(this.personasNaturales);
        //
        this.personaNatural= this.personaNaturalSB.findById(idTemp);
        this.personasNaturales.add(this.personaNatural);
        FacesUtilComun.showInfoMessage("Exito","Actualización Correcta");
        this.activeTab= 0;
        
        //Enviar notificación al correo
        if(this.personaNatural.getEstado().getId().compareTo(this.estadoActual.getId())!=0) {
            try {
                sendMailAction(this.personaNatural, AuditorAdminMB.NATURAL, this.personaNatural.getEstado());
            } catch (Exception ex) {
                System.out.println("No se pudo enviar el correo");
            }
        }
        this.resetAll();
        if(filtroAplicadoPN) {
            obtenerPersonasNaturales();
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('pnatsTable').filter();");
        }
        return "cuentaadmin";
    }
    
    public String updateActionPJur() {
        Long idTemp= personaJuridica.getId();
        
        if(this.personaJuridica.getEstado().getId().compareTo(this.estadoActual.getId())!=0) {
            if(this.personaJuridica.getEstado().getNemonico().compareTo(Auditor.REGISTRADO)==0) {
                if(!existeInforme()) {
                    FacesUtilComun.showErrorMessage("Error", "Debe cargar el informe de inscripción");
                    return null;
                }
            } else if(this.personaJuridica.getEstado().getNemonico().compareTo(Auditor.INSCRITO_CALIFICADO)==0) {
                if(!existeResolucion()) {
                    FacesUtilComun.showErrorMessage("Error", "No ha sido cargada la resolución de calificación");
                    return null;
                }
            }
        }
        
        this.personaJuridicaSB.updateECA(personaJuridica);
        this.personasJuridicas.removeAll(this.personasJuridicas);
        //
        this.personaJuridica= this.personaJuridicaSB.findById(idTemp);
        this.personasJuridicas.add(this.personaJuridica);
        FacesUtilComun.showInfoMessage("Exito","Actualización Correcta");
        this.activeTab= 1;
        
        //Enviar notificación al correo
        if(this.personaJuridica.getEstado().getId().compareTo(this.estadoActual.getId())!=0) {
            try {
                sendMailAction(this.personaJuridica, AuditorAdminMB.JURIDICA, this.personaJuridica.getEstado());
            } catch(Exception ex) {
                System.out.println("No se pudo enviar el correo");
            }
        }
        this.resetAll();
        if(filtroAplicadoPJ) {
            obtenerPersonasJuridicas();
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('pjursTable').filter();");
        }
        return "cuentaadmin";
    }
    
    public void sendMailAction(Object persona, String tipo, Catalogo estado) throws Exception {
        FromTo ft= new FromTo();
        MailSender ms= new MailSender();
        ft.setSubject(AuditorAdminMB.ASUNTOMSG);
        
        if(tipo.equals(AuditorAdminMB.NATURAL)) {
            ProfesionalAuditorExterno p= (ProfesionalAuditorExterno) persona;
            ft.setTo(p.getEmail());
            if(estado.getNemonico().equals(Auditor.REGISTRADO)) {
                ft.setMsg(ms.getInscripcionMsg(p.getRazonSocial()));
            } else if(estado.getNemonico().equals(Auditor.INSCRITO_CALIFICADO)) {
                ft.setMsg(ms.getCalificacionMsg(p.getRazonSocial()));
            } else if(estado.getNemonico().equals(AuditorAdminMB.RESOLUCION)) {
                ft.setMsg(ms.getResolucionMsg(p.getRazonSocial()));
            }
        } else if(tipo.equals(AuditorAdminMB.JURIDICA)) {
            EmpresaConsultoraAuditoria p=(EmpresaConsultoraAuditoria) persona;
            ft.setTo(p.getEmail());
            if(estado.getNemonico().equals(Auditor.REGISTRADO)) {
                ft.setMsg(ms.getInscripcionMsg(p.getRazonSocial()));
            } else if(estado.getNemonico().equals(Auditor.INSCRITO_CALIFICADO)) {
                ft.setMsg(ms.getCalificacionMsg(p.getRazonSocial()));
            } else if(estado.getNemonico().equals(AuditorAdminMB.RESOLUCION)) {
                ft.setMsg(ms.getResolucionMsg(p.getRazonSocial()));
            }
        }
        ms.sendMailHTML(ft);
    }
    
    public String obtenerNombreFuncionario() {
        return (String)FacesUtilComun.getSession().getAttribute("nombre") + " " + (String)FacesUtilComun.getSession().getAttribute("apellido");
    }
    
    /*
    * Implementación de busquedas
    */
    
    //Buscar en personas naturales y juridicas
    private String valorBuscarPersona;

    public String getValorBuscarPersona() {
        return valorBuscarPersona;
    }

    public void setValorBuscarPersona(String valorBuscarPersona) {
        this.valorBuscarPersona = valorBuscarPersona;
    }
    
    public void buscarPersona() {
        if(valorBuscarPersona.length()>0) {
            personasNaturales= personaNaturalSB.findByRuc(valorBuscarPersona, "RUC");
            if(personasNaturales.size()>0) {
                activeTab= 0;
            } else {
                personasJuridicas= personaJuridicaSB.findByRuc(valorBuscarPersona, "RUC");
                if(personasJuridicas.size()>0) {
                    activeTab= 1;
                } else {
                    FacesUtilComun.showInfoMessage("", "No se encontraron resultados");
                }
            }
        }
    }
    
    //Busqueda de cursos
    private String valorBuscarCurso;

    public String getValorBuscarCurso() {
        return valorBuscarCurso;
    }

    public void setValorBuscarCurso(String valorBuscarCurso) {
        this.valorBuscarCurso = valorBuscarCurso;
    }
    
    public void buscarCursos() {
        if(valorBuscarCurso.length()>0)
            this.cursos= this.cursoSB.findByName(valorBuscarCurso);
        this.valorBuscarCurso= "";
    }

    public boolean isRenderedUploadPanel() {
        return renderedUploadPanel;
    }

    public void setRenderedUploadPanel(boolean renderedUploadPanel) {
        this.renderedUploadPanel = renderedUploadPanel;
    }

    public boolean isRenderedMsgPanel() {
        return renderedMsgPanel;
    }

    public void setRenderedMsgPanel(boolean renderedMsgPanel) {
        this.renderedMsgPanel = renderedMsgPanel;
    }
    
    
    public void handleFileUploadInforme(FileUploadEvent event) {
        try {
            FileUploadController.copyFile(FileUploadController.obtenerDestinoInforme(this.rucPersona),
                    event.getFile().getFileName(), event.getFile().getInputstream());
            renderedUploadPanel= false;
            renderedMsgPanel= true;
            FacesUtilComun.showInfoMessage("", "Los archivos han sido cargados correctamente");
        } catch (IOException ex) {
            renderedUploadPanel= false;
            renderedMsgPanel= true;
            System.out.println(ex.toString());
            FacesUtilComun.showErrorMessage("", "Ocurrio un error al cargar los archivos");
        }
    }
    
    public void handleFileUploadResolucion(FileUploadEvent event) {
        Catalogo c= new Catalogo();
        c.setNemonico(AuditorAdminMB.RESOLUCION);
        
        try {
            FileUploadController.copyFile(FileUploadController.obtenerDestinoResolucion(this.rucPersona),
                    event.getFile().getFileName(), event.getFile().getInputstream());
            renderedUploadPanel= false;
            renderedMsgPanel= true;
            FacesUtilComun.showInfoMessage("", "Los archivos han sido cargados correctamente");
            
            if(this.enviarResolucionMsgPersona.equals(AuditorAdminMB.NATURAL)) {
                try {
                    
                    sendMailAction(this.personaNatural, AuditorAdminMB.NATURAL, c);
                } catch (Exception ex) {
                    System.out.println("No se pudo enviar el correo");
                }
            } else if(this.enviarResolucionMsgPersona.equals(AuditorAdminMB.JURIDICA)) {
                try {
                    sendMailAction(this.personaJuridica, AuditorAdminMB.JURIDICA, c);
                } catch (Exception ex) {
                    System.out.println("No se pudo enviar el correo");
                }
            }
        } catch (IOException ex) {
            renderedUploadPanel= false;
            renderedMsgPanel= true;
            System.out.println(ex.toString());
            FacesUtilComun.showErrorMessage("", "Ocurrio un error al cargar los archivos");
        }
    }
    
    public void cerrarUploadInformeDialog() {
        RequestContext.getCurrentInstance().closeDialog("uploadinforme");
    }
    
    public void cerrarUploadResolucionDialog() {
        RequestContext.getCurrentInstance().closeDialog("uploadresolucion");
    }
    
    public void mostrarUploadInforme() {
        if(existeInforme()) {
            FacesUtilComun.showErrorMessage("Error", "Ya ha sido cargado el informe");
        } else {
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('uploadinf').show();");
        }
    }
    
    public void mostrarUploadResolucion() {
        if(existeResolucion()) {
            FacesUtilComun.showErrorMessage("Error", "Ya ha sido cargada la resolución");
        } else {
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('uploadres').show();");
        }
    }
    
    /*
    * Mostrar los adjuntos del auditor en la vista
    */
    private List<StreamedContent> adjuntosFiles;

    public List<StreamedContent> getDownloadedFiles() {
        cargarArchivosAdjuntos();
        return adjuntosFiles;
    }

    public void setDownloadedFiles(List<StreamedContent> downloadedFiles) {
        adjuntosFiles = downloadedFiles;
    }

    public void cargarArchivosAdjuntos() {
        adjuntosFiles= FileDownloadController.obtenerStreamedFiles(rucPersona);
    }
    
    /*
    * Mostrar informe de inscripcion del auditor en la vista
    */
    private List<StreamedContent> informeFiles;

    public List<StreamedContent> getInformeFiles() {
        cargarArchivosInformes();
        return informeFiles;
    }

    public void setInformeFiles(List<StreamedContent> informeFiles) {
        this.informeFiles = informeFiles;
    }
    
    public void cargarArchivosInformes() {
        informeFiles= FileDownloadController.obtenerStreamedInformeFiles(rucPersona);
    }
    
    /*
    * Mostrar certificado de aprobación del curso del auditor en la vista
    */
    private List<StreamedContent> certificadoFiles;

    public List<StreamedContent> getCertificadoFiles() {
        cargarArchivosCertificados();
        return certificadoFiles;
    }

    public void setCertificadoFiles(List<StreamedContent> certificadoFiles) {
        this.certificadoFiles = certificadoFiles;
    }
    
    public void cargarArchivosCertificados() {
        certificadoFiles= FileDownloadController.obtenerStreamedCertificadoFiles(rucPersona);
    }
    
    /*
    * Mostrar resolucion de calificacion del auditor en la vista
    */
    private List<StreamedContent> resolucionFiles;

    public List<StreamedContent> getResolucionFiles() {
        cargarArchivosResoluciones();
        return resolucionFiles;
    }

    public void setResolucionFiles(List<StreamedContent> resolucionFiles) {
        this.resolucionFiles = resolucionFiles;
    }
    
    public void cargarArchivosResoluciones() {
        resolucionFiles= FileDownloadController.obtenerStreamedResolucionFiles(rucPersona);
    }
    /*
    public String tipoUsuarioToString(Long tipo) {
        if(tipo.equals(Funcionario.ADMINISTRADOR)) {
            return "ADMINISTRADOR";
        } else if(tipo.equals(Funcionario.CALIFICADOR)) {
            return "CALIFICADOR";
        } else if(tipo.equals(Funcionario.JURIDICO)) {
            return "JURIDICO";
        } else if(tipo.equals(Funcionario.TALENTO_HUMANO)) {
            return "TALENTO HUMANO";
        } else if(tipo.equals(Funcionario.DIRECTIVO)) {
            return "DIRECTIVO";
        }
        return "OTROS";
    }*/
    
    public String esAdminToString(boolean admin) {
        if(admin) {
            return "SI";
        }
        return "NO";
    }
    
    //Mostrar el formulario para creación de nuevo funcionario
    public String newFuncionarioAction() {
        FacesUtilComun.getSession().setAttribute("editarfuncionario", false);
        return "usuarioform";
    }
    
    //Cargar los datos del usuario-funcionario a ser modificado y mostrar el formulario
    public String editFuncionarioAction(Integer row) {
        FacesUtilComun.getSession().setAttribute("editarfuncionario", true);
        FacesUtilComun.getSession().setAttribute("funcionario", this.funcionarios.get(row));
        return "usuarioform";
    }
    
    private String valorBuscarFuncionario;

    public String getValorBuscarFuncionario() {
        return valorBuscarFuncionario;
    }

    public void setValorBuscarFuncionario(String valorBuscarFuncionario) {
        this.valorBuscarFuncionario = valorBuscarFuncionario;
    }
    
    public void buscarFuncionarios() {
        if(valorBuscarFuncionario.length()>0)
            this.funcionarios= this.funcionarioSB.findByApellido(valorBuscarFuncionario);
        this.valorBuscarFuncionario= "";
    }
    
    public String editPasswordAction(Integer row) {
        FacesUtilComun.getSession().setAttribute("editarfuncionario", true);
        FacesUtilComun.getSession().setAttribute("funcionario", this.funcionarios.get(row));
        return "passwdchange";
    }
    
    //Cargar las configuraciones del sistema
    public void obtenerConfiguraciones() {
        this.systemConfigs= sysConfigSB.list();
    }
    
    //Guardar la configuracion del sistema
    public void saveSystemConfigAction(Integer row) {
        SystemConfiguration sc= this.systemConfigs.get(row);
        sc.setConfigValue(sc.getConfigValue().toUpperCase());
        sysConfigSB.update(sc);
    }
    
    //Cargar archivos adjuntos en la fase de revision
    public void handleFileUploadAdjuntosFase02(FileUploadEvent event) {
        try {
            FileUploadController.copyFile(FileUploadController.obtenerDestinoFase02(this.rucPersona),
                    event.getFile().getFileName(), event.getFile().getInputstream());
            FacesUtilComun.showInfoMessage("", "Los archivos han sido cargados correctamente");
        } catch (IOException ex) {
            System.out.println(ex.toString());
            FacesUtilComun.showErrorMessage("", "Ocurrio un error al cargar los archivos");
        }
    }
    
    //Show hide carga de archivos
    public void changeFileUploadStateFase02() {
        showfuadjuntosfase02= !showfuadjuntosfase02;
    }
    
    public void changeFiltroAplicadoPN() {
        filtroAplicadoPN= true;
    }
    
    public void changeFiltroAplicadoPJ() {
        filtroAplicadoPJ= true;
    }
    
    public void showAtributosSesion() {
        Enumeration<String> names= FacesUtilComun.getSession().getAttributeNames();
        while(names.hasMoreElements()) {
            System.out.println(names.nextElement());
        }
    }
    
    public void addTipoAuditor() {
        System.out.println(selectedCatalogValue.getNombre());
        //System.out.println(FacesUtilComun.getCatalogMB().obtenerCatalogoPorId(selectedCatalogValue));
        
        if(this.personaNatural.getId()!=null) {
             this.personaNatural.getTiposAuditor().add(generarTipoAuditor(selectedCatalogValue));
        } else if(this.personaJuridica.getId()!=null) {
             this.personaJuridica.getTiposAuditor().add(generarTipoAuditor(selectedCatalogValue));
        }
    }
    
    public TipoAuditor generarTipoAuditor(Catalogo c) {
        TipoAuditor tipo= new TipoAuditor();
        tipo.setCatalogo(c);
        return tipo;
    }
    
    public List<String> generarListaTipos(List<TipoAuditor> tiposAuditor) {
        List<String> tiposTxt= new ArrayList<>();
        tiposAuditor.forEach((tipo) -> {
            tiposTxt.add(tipo.getCatalogo().getNombre());
        });
        return tiposTxt;
    }
    
    public boolean existeInforme() {
        return this.informeFiles.size()>0;
    }
    
    public boolean existeResolucion() {
        return this.resolucionFiles.size()>0;
    }
    
    public boolean estaCalificadoPN() {
        return this.personaNatural.getEstado().getNemonico().compareTo(Auditor.INSCRITO_CALIFICADO)==0;
    }
    
    public boolean estaCalificadoPJ() {
        return this.personaJuridica.getEstado().getNemonico().compareTo(Auditor.INSCRITO_CALIFICADO)==0;
    }
    
    public void editarEstadoAuditor() {
        if(this.personaNatural.getId()!=null) {
             this.selectedEstadoValue= this.personaNatural.getEstado();
        } else if(this.personaJuridica.getId()!=null) {
            this.selectedEstadoValue= this.personaJuridica.getEstado();
        }
    }
    
    public void cambiarEstadoAuditor() {
        if(this.personaNatural.getId()!=null) {
            this.personaNatural.setEstado(selectedEstadoValue);
        } else if(this.personaJuridica.getId()!=null) {
            this.personaJuridica.setEstado(selectedEstadoValue);
        }
    }
    
    public String descargarReporteAuditores() {
        return reporteAuditores;
    }
    
    private void obtenerAllAuditorias() {
        allAuditorias= AuditoriaController.listarAuditorias(auditoriaDao);
    }
    
}
