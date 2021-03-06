
package security;

import messages.Messages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import properties.FapProperties;
import resolucion.ResolucionBase;
import verificacion.VerificacionUtils;
import models.Agente;
import models.Busqueda;
import models.Convocatoria;
import models.Documento;
import models.LineaResolucionFAP;
import models.Participacion;
import models.PeticionCesiones;
import models.Registro;
import models.RegistroModificacion;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import models.Verificacion;

import org.joda.time.DateTime;

import properties.FapProperties;
import resolucion.ResolucionBase;
import verificacion.VerificacionUtils;
import controllers.SolicitudesController;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import enumerado.fap.gen.AccesoAgenteEnum;
import enumerado.fap.gen.EstadosModificacionEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;

public class SecureFap extends Secure {

	public SecureFap(Secure next) {
		super(next);
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso hayNuevaDocumentacionVerificacionAccion(Map<String, Long> ids, Map<String, Object> vars) {
		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);
		if (solicitud == null)
			return new ResultadoPermiso(Accion.Denegar);

		List<Documento> documentosNuevos = VerificacionUtils.existDocumentosNuevosVerificacionTipos(solicitud.verificacion, solicitud.verificaciones, solicitud.documentacion.documentos, solicitud.id);
		if ((documentosNuevos == null) || (documentosNuevos.isEmpty()) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enVerificacionNuevosDoc.name())) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.iniciada.name())))
			return new ResultadoPermiso(Accion.Denegar);
		
		return new ResultadoPermiso(Accion.All);
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso hayNuevaDocumentacionVerificacion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);
		if (solicitud == null)
			return new ResultadoPermiso(Accion.Denegar);

		List<Documento> documentosNuevos = VerificacionUtils.existDocumentosNuevosVerificacionTipos(solicitud.verificacion, solicitud.verificaciones, solicitud.documentacion.documentos, solicitud.id);
		if ((documentosNuevos.isEmpty()) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enVerificacionNuevosDoc.name())) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.iniciada.name())) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enRequerimiento)) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enRequerido)) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enRequerimientoFirmaSolicitada)) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.verificacionNegativa)) || (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.verificacionPositiva))|| (solicitud.verificacion.estado.equals(EstadosVerificacionEnum.plazoVencido)))
			return new ResultadoPermiso(Accion.Denegar);

		return new ResultadoPermiso(Accion.All);
	}

	public SolicitudGenerica getSolicitudGenerica(Map<String, Long> ids, Map<String, Object> vars) {
		if (vars != null && vars.containsKey("solicitud"))
			return (SolicitudGenerica) vars.get("solicitud");
		else if (ids != null && ids.containsKey("idSolicitud"))
			return SolicitudGenerica.findById(ids.get("idSolicitud"));

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso loginTipoUser(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		Agente agente = AgenteController.getAgente();
		if ((FapProperties.getBoolean("fap.login.type.user")) && ((agente.acceso == null) || (!agente.acceso.equals(AccesoAgenteEnum.certificado.name()))))
			return new ResultadoPermiso(Accion.All);
		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso loginTipoUserAccion(Map<String, Long> ids, Map<String, Object> vars) {
		if (FapProperties.getBoolean("fap.login.type.user"))
			return new ResultadoPermiso(Accion.All);
		
		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso listaSolicitudesConBusqueda(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		Agente agente = AgenteController.getAgente();
		if (!agente.rolActivo.toString().equals("usuario".toString())
				&& FapProperties.getBoolean("fap.index.search")) {
			return new ResultadoPermiso(Accion.All);
		}
		
		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso listaSolicitudesConBusquedaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		Agente agente = AgenteController.getAgente();
		if (!agente.rolActivo.toString().equals("usuario".toString())
				&& FapProperties.getBoolean("fap.index.search")) {
			return new ResultadoPermiso(Accion.All);
		}
		
		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso listaSolicitudesSinBusqueda(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		Agente agente = AgenteController.getAgente();
		if (agente.rolActivo.toString().equals("usuario".toString())
				|| !FapProperties.getBoolean("fap.index.search")) {
			return new ResultadoPermiso(Accion.All);
		}
		
		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso listaSolicitudesSinBusquedaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		Agente agente = AgenteController.getAgente();
		if (agente.rolActivo.toString().equals("usuario".toString())
				|| !FapProperties.getBoolean("fap.index.search")) {
			return new ResultadoPermiso(Accion.All);
		}

		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso mostrarResultadoBusqueda(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		Busqueda busqueda = SolicitudesController.getBusqueda();
		if ( (busqueda.mostrarTabla != null) && (busqueda.mostrarTabla) )
			return new ResultadoPermiso(Accion.All);

		return new ResultadoPermiso(Accion.Denegar);
	}

	public ResultadoPermiso mostrarResultadoBusquedaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		Busqueda busqueda = SolicitudesController. getBusqueda();
		if ( (busqueda.mostrarTabla != null) && (busqueda.mostrarTabla) )
			return new ResultadoPermiso(Accion.All);
		
		return new ResultadoPermiso(Accion.Denegar);
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso esFuncionarioHabilitadoYActivadaProperty(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		if ((agente.funcionario.toString().equals("true".toString())) && (properties.FapProperties.getBoolean("fap.firmaYRegistro.funcionarioHabilitado"))) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso esFuncionarioHabilitadoYActivadaPropertyAccion(Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		if ((agente.funcionario.toString().equals("true".toString())) && (properties.FapProperties.getBoolean("fap.firmaYRegistro.funcionarioHabilitado")))
			return new ResultadoPermiso(Accion.Editar);

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso prepararSolicitudModificacion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("editar".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("false".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Grafico.Editable);
		}

		if ((accion.toString().equals("editar".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("true".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Grafico.Visible);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso prepararSolicitudModificacionAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("editar".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("false".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("editar".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("true".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso enBorradorSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || ((registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("true".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso enBorradorSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || ((registro != null && registro.fasesRegistro != null && registro.fasesRegistro.borrador.toString().equals("true".toString())) && (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso mensajeIntermedioSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("true".toString()) || registro != null && registro.fasesRegistro != null && registro.fasesRegistro.registro.toString().equals("true".toString()) || registro != null && registro.fasesRegistro != null && registro.fasesRegistro.expedienteAed.toString().equals("true".toString())) && registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso mensajeIntermedioSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		if ((registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("true".toString()) || registro != null && registro.fasesRegistro != null && registro.fasesRegistro.registro.toString().equals("true".toString()) || registro != null && registro.fasesRegistro != null && registro.fasesRegistro.expedienteAed.toString().equals("true".toString())) && registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))
			return new ResultadoPermiso(Accion.Editar);

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso mensajeIntermedioAlegacionFirmar(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if (solicitud.alegaciones.actual.registro.fasesRegistro.firmada.toString().equals("false".toString()) && Messages.hasErrors()) {
			return new ResultadoPermiso(Grafico.Editable);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso mensajeIntermedioAlegacionRegistrar(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if (Messages.hasErrors()) {
			return new ResultadoPermiso(Grafico.Editable);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso mensajeIntermedioAlegacionJuridica(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if (Messages.hasErrors()) {
			return new ResultadoPermiso(Grafico.Editable);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso mensajeIntermedioAceptarRenunciarFirmar(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if (solicitud.aceptarRenunciar.registro.fasesRegistro.firmada.toString().equals("false".toString()) && Messages.hasErrors()) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso clasificadaSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("true".toString()))) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso clasificadaSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("true".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso habilitarFHPresentacionModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((registro != null && registro.habilitaFuncionario.toString().equals("true".toString()))) {
			return new ResultadoPermiso(Grafico.Visible);
		}

		if ((registro != null && registro.habilitaFuncionario == null) || (registro != null && registro.habilitaFuncionario.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso habilitarFHPresentacionModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		if ((registro != null && registro.habilitaFuncionario.toString().equals("true".toString())))
			return new ResultadoPermiso(Accion.Editar);

		if ((registro != null && registro.habilitaFuncionario == null) || (registro != null && registro.habilitaFuncionario.toString().equals("false".toString())))
			return new ResultadoPermiso(Accion.Editar);

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso firmarRegistrarSolicitudModificadaFH(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if (((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) && (agente.funcionario.toString().equals("true".toString()))) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso firmarRegistrarSolicitudModificadaFHAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if (((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) && (agente.funcionario.toString().equals("true".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso firmarRegistrarSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso firmarRegistrarSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso firmarSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso firmarSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso registrarSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("true".toString()) && registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso registrarSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.firmada.toString().equals("true".toString()) && registro != null && registro.fasesRegistro != null && registro.fasesRegistro.clasificarAed.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso modificarSolicitudModificada(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.registro.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Accion.All);

		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso modificarSolicitudModificadaAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		acciones.clear();

		acciones.add("editar");
		acciones.add("leer");
		acciones.add("crear");
		acciones.add("borrar");

		for (String accion : acciones) {
			if ((accion.toString().equals("leer".toString())) || (registro != null && registro.fasesRegistro != null && registro.fasesRegistro.registro.toString().equals("false".toString())))
				return new ResultadoPermiso(Accion.parse(accion));
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso modificacionTrasPresentacionDeSolicitud(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas", "revisor")) {
			return new ResultadoPermiso(Accion.All);
		}

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && !solicitud.estado.toString().equals("borrador".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("false".toString())) {
			return new ResultadoPermiso(Grafico.Visible);
		}

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("true".toString())) {
			return new ResultadoPermiso(Grafico.Visible);
		}

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("false".toString()) && solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).getEstado().equals("Expirada".toString())) {
			if (!accion.equals("crear"))
				return new ResultadoPermiso(Grafico.Visible);
		}

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("false".toString()) && !solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).getEstado().equals("Expirada".toString())) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso modificacionTrasPresentacionDeSolicitudAccion(Map<String, Long> ids, Map<String, Object> vars) {
		String grafico = "visible";
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Registro registro=null;
		RegistroModificacion registroModificacion= null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty())){
			registro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).registro;
			registroModificacion = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1);
		}else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		List<String> acciones = new ArrayList<String>();

		if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas"))
			return new ResultadoPermiso(Accion.Editar);

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && !solicitud.estado.toString().equals("borrador".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("false".toString()))
			return new ResultadoPermiso(Accion.Leer);

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("true".toString()))
			return new ResultadoPermiso(Accion.Leer);

		if (agente.rolActivo.toString().equals("usuario".toString()) && solicitud != null && solicitud.estado.toString().equals("modificacion".toString()) && solicitud != null && solicitud.activoModificacion.toString().equals("true".toString()) && registro != null && registro.fasesRegistro.borrador.toString().equals("false".toString())
				&& (registroModificacion.estado.equals(EstadosModificacionEnum.enCurso.name())))
			return new ResultadoPermiso(Accion.Editar);

		return null;
	}

		/**
	 * Si no tiene verificaciones anteriores, no se cumple el permiso.
	 * @param grafico
	 * @param accion
	 * @param ids
	 * @param vars
	 * @return
	 */
	@SuppressWarnings("unused")
	private ResultadoPermiso verificarObtenerNoProcede(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);
		if (solicitud.verificaciones.size() == 0)
			return null;
		Verificacion verificacion = getVerificacion(ids, vars);

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas", "revisor")) && ((utils.StringUtils.in(accion.toString(), "leer", "editar")) && verificacion != null && utils.StringUtils.in(verificacion.estado.toString(), "obtenerNoProcede"))) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	public Verificacion getVerificacion(Map<String, Long> ids, Map<String, Object> vars) {
		if (vars != null && vars.containsKey("verificacion"))
			return (Verificacion) vars.get("verificacion");
		else if (ids != null && ids.containsKey("idVerificacion"))
			return Verificacion.findById(ids.get("idVerificacion"));

		return null;
	}

	public PeticionCesiones getPeticionCesiones(Map<String, Long> ids, Map<String, Object> vars) {
		if (vars != null && vars.containsKey("peticionCesiones"))
			return (PeticionCesiones) vars.get("peticionCesiones");
		else if (ids != null && ids.containsKey("idPeticionCesiones"))
			return PeticionCesiones.findById(ids.get("idPeticionCesiones"));
		
		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso menuConModificacion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		RegistroModificacion registroModificacion = null;
		if ((solicitud != null) && (!solicitud.registroModificacion.isEmpty()))
			registroModificacion = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1);
		else
			return null;

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "revisor", "gestor", "gestorTenerife", "gestorLasPalmas") && (solicitud.estado.toString().equals("modificacion".toString()))
			&& (registroModificacion != null) && (registroModificacion.estado.equals(EstadosModificacionEnum.enCurso.name()))) {
			return new ResultadoPermiso(Accion.All);
		}

		if (agente.rolActivo.toString().equals("usuario".toString()) && (solicitud.estado.toString().equals("modificacion".toString()))
				&& (registroModificacion != null) && (registroModificacion.estado.equals(EstadosModificacionEnum.enCurso.name()))) {
			return new ResultadoPermiso(Accion.All);
		}
		
		return new ResultadoPermiso(Accion.Denegar);
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoGenerarBaremacionResolucion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();
		ResolucionBase resolucion = null;
		Long idResolucion = null;
		if (ids != null && ids.containsKey("idResolucionFAP"))
			idResolucion = (ids.get("idResolucionFAP"));
		if (idResolucion != null){
			try {
				resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			}
			catch (Throwable e) {
				// TODO: handle exception
			}

			Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
			//Desde que se indique que se quiera generar algún documento de baremación, se muestra el grupo
			if (resolucion.resolucion.conBaremacion){
				if (resolucion.resolucion.estadoPublicacion != null && resolucion.resolucion.estadoPublicacion.toString().equals("publicada".toString()) && utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas", "jefeServicio") && resolucion.resolucion.conBaremacion.toString().equals("true".toString())) {
					if ("editar".equals(accion))
						return new ResultadoPermiso(Accion.Editar);
					else
						return null;
				}
				
				if (resolucion.resolucion.estadoPublicacion != null && resolucion.resolucion.conBaremacion.toString().equals("true".toString())) {
					return new ResultadoPermiso(Grafico.Visible);
				}
			}
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoGenerarInformeConComentarios(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();
		Long idResolucion = null;
		ResolucionBase resolucion = null;

		if (ids != null && ids.containsKey("idResolucionFAP"))
			idResolucion = (ids.get("idResolucionFAP"));
		if (idResolucion != null){
			try {
				resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			}
			catch (Throwable e) {
				// TODO: handle exception
			}
		}

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		if(resolucion.isGenerarDocumentoBaremacionCompletoConComentarios()){
			if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas", "jefeServicio", "revisor") && resolucion.resolucion.estadoInformeBaremacionConComentarios == null && (resolucion.resolucion.estadoDocBaremacionResolucion != null && "clasificado".toString().equals(resolucion.resolucion.estadoDocBaremacionResolucion.toString()))
					&& resolucion.resolucion.estadoInformeBaremacionConComentarios == null) {
				return new ResultadoPermiso(Grafico.Editable);

			}
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoGenerarInformeSinComentarios(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		Long idResolucion = null;
		ResolucionBase resolucion = null;

		if (ids != null && ids.containsKey("idResolucionFAP"))
			idResolucion = (ids.get("idResolucionFAP"));
		if (idResolucion != null){
			try {
				resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			}
			catch (Throwable e) {
				// TODO: handle exception
			}
		}

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		if(resolucion.isGenerarDocumentoBaremacionCompletoSinComentarios()){
			if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas", "jefeServicio", "revisor") && (resolucion.resolucion.estadoDocBaremacionResolucion != null && "clasificado".toString().equals(resolucion.resolucion.estadoDocBaremacionResolucion.toString()))
					&& resolucion.resolucion.estadoInformeBaremacionSinComentarios == null) {
				return new ResultadoPermiso(Grafico.Editable);
			}
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoClasificarInformeConComentarios(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		Long idResolucion = null;
		ResolucionBase resolucion = null;

		if (ids != null && ids.containsKey("idResolucionFAP"))
			idResolucion = (ids.get("idResolucionFAP"));
		if (idResolucion != null){
			try {
				resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			}
			catch (Throwable e) {
				// TODO: handle exception
			}
		}

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		if(resolucion.isGenerarDocumentoBaremacionCompletoConComentarios()){
			if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas", "jefeServicio", "revisor") && resolucion.resolucion.estadoInformeBaremacionConComentarios != null && resolucion.resolucion.estadoInformeBaremacionConComentarios.toString().equals("generado".toString())) {
				return new ResultadoPermiso(Grafico.Editable);
			}
		}
		
		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoClasificarInformeSinComentarios(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		Long idResolucion = null;
		ResolucionBase resolucion = null;

		if (ids != null && ids.containsKey("idResolucionFAP"))
			idResolucion = (ids.get("idResolucionFAP"));
		if (idResolucion != null){
			try {
				resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			}
			catch (Throwable e) {
				// TODO: handle exception
			}
		}

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
		if(resolucion.isGenerarDocumentoBaremacionCompletoSinComentarios()){
			if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas", "jefeServicio", "revisor") && resolucion.resolucion.estadoInformeBaremacionSinComentarios != null && resolucion.resolucion.estadoInformeBaremacionSinComentarios.toString().equals("generado".toString())) {
				return new ResultadoPermiso(Grafico.Editable);
			}
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoFirmarDocBaremacionResolucion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		ResolucionBase resolucion = null;
		Long idResolucion = null;
		if (ids != null && ids.containsKey("idResolucionFAP"))
			idResolucion = (ids.get("idResolucionFAP"));
		if (idResolucion != null){
			try {
				resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
			}
			catch (Throwable e) {
				// TODO: handle exception
			}

			if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor","gestorTenerife", "gestorLasPalmas", "jefeServicio", "revisor") && resolucion.resolucion.conBaremacion.toString().equals("true".toString()) && resolucion.resolucion.estadoPublicacion != null && !resolucion.resolucion.estado.equals("publicada")){
				//Tengo que generar todos los docs
				if (resolucion.isGenerarDocumentoBaremacionCompletoConComentarios() && resolucion.resolucion.estadoInformeBaremacionConComentarios!= null && resolucion.resolucion.estadoInformeBaremacionConComentarios.toString().equals("clasificado".toString())
						&& resolucion.isGenerarDocumentoBaremacionCompletoSinComentarios()
						&& resolucion.resolucion.estadoInformeBaremacionSinComentarios!= null && resolucion.resolucion.estadoInformeBaremacionSinComentarios.toString().equals("clasificado".toString())){
					return new ResultadoPermiso(Grafico.Editable);
				} else if (!resolucion.isGenerarDocumentoBaremacionCompletoSinComentarios() && resolucion.isGenerarDocumentoBaremacionCompletoConComentarios() && resolucion.resolucion.estadoInformeBaremacionConComentarios!= null && resolucion.resolucion.estadoInformeBaremacionConComentarios.toString().equals("clasificado".toString())){
					return new ResultadoPermiso(Grafico.Editable);
				}else if (!resolucion.isGenerarDocumentoBaremacionCompletoConComentarios() && resolucion.isGenerarDocumentoBaremacionCompletoSinComentarios() && resolucion.resolucion.estadoInformeBaremacionSinComentarios!= null && resolucion.resolucion.estadoInformeBaremacionSinComentarios.toString().equals("clasificado".toString())){
					return new ResultadoPermiso(Grafico.Editable);
				}
			}
		}
		
		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoOficioRemision(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		ResolucionFAP resolucion = getResolucionFAP(ids, vars);
		if (utils.StringUtils.in(agente.rolActivo.toString(), "gestor", "gestorTenerife", "gestorLasPalmas", "administrador", "revisor")) {
			for (LineaResolucionFAP linea: resolucion.lineasResolucion) {
				// Se da permiso mientras haya alguna línea con el oficio de remisión sin generar, firmar, registrar o clasificar
				if ((linea.registro.oficial.uri == null) || (((linea.registro.fasesRegistro.firmada == null) || (linea.registro.fasesRegistro.firmada == false)) || ((linea.registro.fasesRegistro.registro == null) || (linea.registro.fasesRegistro.registro == false)) || ((linea.registro.fasesRegistro.clasificarAed == null) || (linea.registro.fasesRegistro.clasificarAed == false)))) {
					return new ResultadoPermiso(Accion.All);
				}
			 }
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoGenerarOficioRemision(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		ResolucionFAP resolucion = getResolucionFAP(ids, vars);

		if (utils.StringUtils.in(agente.rolActivo.toString(), "gestor", "gestorTenerife", "gestorLasPalmas", "administrador", "revisor")) {
			for (LineaResolucionFAP linea: resolucion.lineasResolucion) {
				// Se da permiso mientras haya alguna línea con el oficio de remisión sin generar
				if (linea.registro.oficial.uri == null) {
					return new ResultadoPermiso(Accion.All);
				}
			 }
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoFirmarOficioRemision(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		ResolucionFAP resolucion = getResolucionFAP(ids, vars);

		if (utils.StringUtils.in(agente.rolActivo.toString(), "gestor", "gestorTenerife", "gestorLasPalmas", "administrador", "revisor")) {
			for (LineaResolucionFAP linea: resolucion.lineasResolucion) {
				// Se da permiso cuando todas las líneas tengan el oficio de remisión generado y quede alguno sin firmar, registrar y/o clasificar
				if ((linea.registro.oficial.uri != null) && (((linea.registro.fasesRegistro.firmada == null) || (linea.registro.fasesRegistro.firmada == false)) || ((linea.registro.fasesRegistro.registro == null) || (linea.registro.fasesRegistro.registro == false)) || ((linea.registro.fasesRegistro.clasificarAed == null) || (linea.registro.fasesRegistro.clasificarAed == false)))) {
					return new ResultadoPermiso(Accion.All);
				}
			 }
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoNotificar(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		ResolucionFAP resolucion = getResolucionFAP(ids, vars);

		if (utils.StringUtils.in(agente.rolActivo.toString(), "gestor", "gestorTenerife", "gestorLasPalmas", "administrador", "revisor")) {
			for (LineaResolucionFAP linea: resolucion.lineasResolucion) {
				if ((linea.registro.oficial.uri == null) || (((linea.registro.fasesRegistro.firmada == null) || (linea.registro.fasesRegistro.firmada == false)) || ((linea.registro.fasesRegistro.registro == null) || (linea.registro.fasesRegistro.registro == false)) || ((linea.registro.fasesRegistro.clasificarAed == null) || (linea.registro.fasesRegistro.clasificarAed == false)))) {
					return null;
				}
			 }
		}
		// Se da permiso cuando todas las líneas tengan el oficio de remisión generado, firmado, registrado y clasificado
		return new ResultadoPermiso(Accion.All);
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso notificarResolucion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		ResolucionFAP resolucion = getResolucionFAP(ids, vars);

		if (utils.StringUtils.in(agente.rolActivo.toString(), "gestor", "gestorTenerife", "gestorLasPalmas", "administrador", "revisor")) {
			if (utils.StringUtils.in(resolucion.estado.toString(), "registrada", "publicada")) {
				if ((resolucion.estadoNotificacion == null) || (utils.StringUtils.in(resolucion.estadoNotificacion.toString(), "noNotificada", "oficiosRemisionPendientesPortafirma", "oficiosRemisionFirmados"))) {
					return new ResultadoPermiso(Accion.All);
				}
				for (LineaResolucionFAP linea: resolucion.lineasResolucion) {
					if (!linea.notificada) {
						return new ResultadoPermiso(Accion.All);
					}
				}
			}
		}

		return null;
	}

	public ResolucionFAP getResolucionFAP(Map<String, Long> ids, Map<String, Object> vars) {
		if (vars != null && vars.containsKey("resolucionFAP"))
			return (ResolucionFAP) vars.get("resolucionFAP");
		else if (ids != null && ids.containsKey("idResolucionFAP"))
			return ResolucionFAP.findById(ids.get("idResolucionFAP"));
		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso finalizarResolucion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		ResolucionFAP resolucion = getResolucionFAP(ids, vars);

		boolean publicar = properties.FapProperties.getBoolean("fap.resoluciones.publicarTablonAnuncios");
		boolean notificar = properties.FapProperties.getBoolean("fap.resoluciones.notificar");

		if (publicar && notificar) {
			if (utils.StringUtils.in(resolucion.estado.toString(), "publicadaYNotificada") && utils.StringUtils.in(agente.rolActivo.toString(), "gestor", "gestorTenerife", "gestorLasPalmas", "administrador", "jefeServicio")) {
				return new ResultadoPermiso(Accion.All);
			}
		}

		if (!publicar && notificar) {
			if (utils.StringUtils.in(resolucion.estado.toString(), "notificada") && utils.StringUtils.in(agente.rolActivo.toString(), "gestor", "gestorTenerife", "gestorLasPalmas", "administrador", "jefeServicio")) {
				return new ResultadoPermiso(Accion.All);
			}
		}

		if (publicar && !notificar) {
			if (utils.StringUtils.in(resolucion.estado.toString(), "publicada") && utils.StringUtils.in(agente.rolActivo.toString(), "gestor", "gestorTenerife", "gestorLasPalmas", "administrador", "jefeServicio")) {
				return new ResultadoPermiso(Accion.All);
			}
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso noHayverificacion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		Verificacion verificacion = Verificacion.find("select verificacion from SolicitudGenerica s where s.id=?", solicitud.id).first();

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas", "revisor")) && ((verificacion == null) || ((verificacion.estado == null) || (utils.StringUtils.in(verificacion.estado.toString(), "enRequerido", "plazoVencido", "verificacionPositiva", "verificacionNegativa"))))) {
			return new ResultadoPermiso(Accion.All);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso permisoCopiaExpedientes(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		ResolucionFAP resolucion = getResolucionFAP(ids, vars);

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		boolean publicar = properties.FapProperties.getBoolean("fap.resoluciones.publicarTablonAnuncios");

		if ((resolucion.copiadoExpedientes == null || resolucion.copiadoExpedientes.toString().equals("false".toString()))
				&& (resolucion.estado != null)
				&& (resolucion.estado.toString().equals("publicada".toString()) || (resolucion.estado.toString().equals("notificada".toString()) && (!publicar)) || resolucion.estado.toString().equals("publicadaYNotificada".toString()))
				&& (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas", "jefeServicio", "revisor"))) {
			if ("editar".equals(accion))
				return new ResultadoPermiso(Accion.Editar);
			else
				return null;
		}

		if (utils.StringUtils.in(agente.rolActivo.toString(), "administrador", "gestor", "gestorTenerife", "gestorLasPalmas", "jefeServicio", "revisor")) {
			return new ResultadoPermiso(Grafico.Visible);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso firmarRegistrarAceptarRenunciar(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		DateTime currentDate = new DateTime();

		boolean sobrepasada = false;

		if (solicitud.fechaFinDeAceptacion == null) return null;

		//for (LineaResolucion lr : solicitud.lineasResolucion) {
		if (solicitud.fechaFinDeAceptacion != null &&
			currentDate.toString("yyyyMMdd").compareTo(solicitud.fechaFinDeAceptacion.toString("yyyyMMdd"))> 0) {
			sobrepasada = true;
			//break;
		}

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString()))
		|| (solicitud.aceptarRenunciar.registro.fasesRegistro.firmada.toString().equals("false".toString()) && !sobrepasada)
		|| (solicitud.aceptarRenunciar.registro.fasesRegistro.firmada.toString().equals("true".toString()) && solicitud.aceptarRenunciar.registro.fasesRegistro.registro.toString().equals("false".toString()) && !sobrepasada)
		|| (solicitud.aceptarRenunciar.registro.fasesRegistro.firmada.toString().equals("true".toString()) && solicitud.aceptarRenunciar.registro.fasesRegistro.registro.toString().equals("true".toString()) && solicitud.aceptarRenunciar.registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))
		) {
			return new ResultadoPermiso(Grafico.Editable);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso firmarAceptarRenunciar(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		DateTime currentDate = new DateTime();

		boolean sobrepasada = false;

		if (solicitud.fechaFinDeAceptacion == null) return null;

		//for (LineaResolucion lr : solicitud.lineasResolucion) {
		if (solicitud.fechaFinDeAceptacion != null &&
			currentDate.toString("yyyyMMdd").compareTo(solicitud.fechaFinDeAceptacion.toString("yyyyMMdd"))> 0) {
			sobrepasada = true;
			//break;
		}

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString()))
		    || (solicitud.aceptarRenunciar.registro.fasesRegistro.firmada.toString().equals("false".toString()) && !sobrepasada)
			) {
			return new ResultadoPermiso(Grafico.Editable);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso registrarAceptarRenunciar(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		DateTime currentDate = new DateTime();

		boolean sobrepasada = false;

		if (solicitud.fechaFinDeAceptacion == null) return null;

		//for (LineaResolucion lr : solicitud.lineasResolucion) {
		if (solicitud.fechaFinDeAceptacion != null &&
			currentDate.toString("yyyyMMdd").compareTo(solicitud.fechaFinDeAceptacion.toString("yyyyMMdd"))> 0) {
			sobrepasada = true;
			//break;
		}

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString()))
		|| (solicitud.aceptarRenunciar.registro.fasesRegistro.firmada.toString().equals("true".toString()) && solicitud.aceptarRenunciar.registro.fasesRegistro.registro.toString().equals("false".toString()) && solicitud.aceptarRenunciar.registro.fasesRegistro.clasificarAed.toString().equals("false".toString()) && !sobrepasada)
		|| (solicitud.aceptarRenunciar.registro.fasesRegistro.firmada.toString().equals("true".toString()) && solicitud.aceptarRenunciar.registro.fasesRegistro.registro.toString().equals("true".toString()) && solicitud.aceptarRenunciar.registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) {
			if ("editar".equals(accion))
				return new ResultadoPermiso(Accion.Editar);
			else
				return null;
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso firmarRegistrarAlegacion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		DateTime currentDate = new DateTime();

		boolean sobrepasada = false;

		if (solicitud.fechaFinDeAlegacion == null) return null;

		//for (LineaResolucion lr : solicitud.lineasResolucion) {
		if (solicitud.fechaFinDeAlegacion != null &&
			currentDate.toString("yyyyMMdd").compareTo(solicitud.fechaFinDeAlegacion.toString("yyyyMMdd"))> 0) {
			sobrepasada = true;
			//break;
		}

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString()))
		|| (solicitud.alegaciones.actual.registro.fasesRegistro.firmada.toString().equals("false".toString()) && !sobrepasada)
		|| (solicitud.alegaciones.actual.registro.fasesRegistro.firmada.toString().equals("true".toString()) && solicitud.alegaciones.actual.registro.fasesRegistro.registro.toString().equals("false".toString()) && !sobrepasada)
		|| (solicitud.alegaciones.actual.registro.fasesRegistro.firmada.toString().equals("true".toString()) && solicitud.alegaciones.actual.registro.fasesRegistro.registro.toString().equals("true".toString()) && solicitud.alegaciones.actual.registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))
		) {
			return new ResultadoPermiso(Grafico.Editable);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso firmarAlegacion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		DateTime currentDate = new DateTime();

		boolean sobrepasada = false;

		if (solicitud.fechaFinDeAlegacion == null) return null;

		//for (LineaResolucion lr : solicitud.lineasResolucion) {
		if (solicitud.fechaFinDeAlegacion != null &&
			currentDate.toString("yyyyMMdd").compareTo(solicitud.fechaFinDeAlegacion.toString("yyyyMMdd"))> 0) {
			sobrepasada = true;
			//break;
		}

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString()))
		    || (solicitud.alegaciones.actual.registro.fasesRegistro.firmada.toString().equals("false".toString()) && !sobrepasada)
		) {
			return new ResultadoPermiso(Grafico.Editable);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private ResultadoPermiso registrarAlegacion(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		DateTime currentDate = new DateTime();

		boolean sobrepasada = false;

		if (solicitud.fechaFinDeAlegacion == null) return null;

		//for (LineaResolucion lr : solicitud.lineasResolucion) {
		if (solicitud.fechaFinDeAlegacion != null &&
			currentDate.toString("yyyyMMdd").compareTo(solicitud.fechaFinDeAlegacion.toString("yyyyMMdd"))> 0) {
			sobrepasada = true;
			//break;
		}

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		if ((accion.toString().equals("leer".toString()))
		|| (solicitud.alegaciones.actual.registro.fasesRegistro.firmada.toString().equals("true".toString()) && solicitud.alegaciones.actual.registro.fasesRegistro.registro.toString().equals("false".toString()) && solicitud.alegaciones.actual.registro.fasesRegistro.clasificarAed.toString().equals("false".toString()) && !sobrepasada)
		|| (solicitud.alegaciones.actual.registro.fasesRegistro.firmada.toString().equals("true".toString()) && solicitud.alegaciones.actual.registro.fasesRegistro.registro.toString().equals("true".toString()) && solicitud.alegaciones.actual.registro.fasesRegistro.clasificarAed.toString().equals("false".toString()))) {
			return new ResultadoPermiso(Grafico.Editable);
		}

		return null;
	}

	//Permite firmar documentos si el agente tiene participación de tipo "Solicitante"
	// o "Representante" para la solicitud indicada

	@SuppressWarnings("unused")
	private ResultadoPermiso editarFirmaDocumento(String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars) {
		//Variables
		Agente agente = AgenteController.getAgente();

		SolicitudGenerica solicitud = getSolicitudGenerica(ids, vars);

		List<Participacion> participaciones = Participacion.find("select p from Participacion p where p.agente=? AND p.solicitud=?", agente, solicitud).fetch();

		Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);

		for (Participacion participacion: participaciones) {

			if (agente.rolActivo.toString().equals("usuario".toString())) {

				if (participacion.tipo.toString().equals("solicitante".toString()) || participacion.tipo.toString().equals("representante".toString()))
					return new ResultadoPermiso(Accion.Editar);

				else if (participacion.tipo.toString().equals("autorizado".toString()))
					return new ResultadoPermiso(Accion.Leer);
			}
		}

		if (!secure.checkGrafico("usuario", "visible", accion, ids, vars)) {
			if ("editar".equals(accion))
				return new ResultadoPermiso(Accion.Editar);
			else if ("leer".equals(accion))
				return new ResultadoPermiso(Accion.Leer);
			else
				return null;
		}

		return null;
	}
}