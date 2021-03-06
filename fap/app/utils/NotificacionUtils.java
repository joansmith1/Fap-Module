package utils;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import config.InjectorConfig;

import play.db.jpa.Transactional;
import play.modules.guice.InjectSupport;

import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.NotificacionService;

import messages.Messages;
import models.Documento;
import models.DocumentoNotificacion;
import models.ExpedienteAed;
import models.Interesado;
import models.Notificacion;
import models.SolicitudGenerica;
import enumerado.fap.gen.EstadoNotificacionEnum;
import es.gobcan.aciisi.servicios.enotificacion.dominio.notificacion.DocumentoNotificacionEnumType;
import es.gobcan.aciisi.servicios.enotificacion.dominio.notificacion.EstadoNotificacionEnumType;
import es.gobcan.aciisi.servicios.enotificacion.dominio.notificacion.EstadoNotificacionType;
import es.gobcan.aciisi.servicios.enotificacion.dominio.notificacion.InteresadoType;
import es.gobcan.aciisi.servicios.enotificacion.dominio.notificacion.NotificacionType;

@InjectSupport
public class NotificacionUtils {
	
	@Inject
    protected static NotificacionService notificacionService;
	
	//Inyeccion manual del gestorDoc
	protected static GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);

	public static InteresadoType convertInteresadoToInteresadoType(Interesado interesado){
		InteresadoType interesadoType = new InteresadoType();
		interesadoType.setEmail(interesado.email);
		interesadoType.setMovil(interesado.movil);
		interesadoType.setNif(interesado.persona.getNumeroId());
		interesadoType.setUriTerceros(interesado.persona.getNumeroId());
		if (interesado.persona.tipo.equals("fisica")){
			interesadoType.setApellido1(interesado.persona.fisica.primerApellido);
			interesadoType.setApellido2(interesado.persona.fisica.segundoApellido);
			interesadoType.setNombre(interesado.persona.fisica.nombre);	
			return interesadoType;
		} else if (interesado.persona.tipo.equals("juridica")){
			interesadoType.setApellido1("");
			interesadoType.setApellido2("");
			interesadoType.setNombre(interesado.persona.juridica.entidad);
			return interesadoType;
		} else {// Algo raro
			return null;
		}
	}
	
	public static Notificacion convertNotificacionTypeToNotificacion (NotificacionType notificacionType){
		Notificacion notificacion = new Notificacion();
		notificacion.asunto = notificacionType.getAsunto();
		notificacion.descripcion = notificacionType.getCuerpo();
		notificacion.estado = convertEstadoNotificacionEnumTypeToEstadoNotificacion(notificacionType.getEstadoNotificacion().getEstado());
		notificacion.fechaPuestaADisposicion = new DateTime(notificacionType.getEstadoNotificacion().getFechaCreacion().toGregorianCalendar().getTime());
		if (notificacionType.getFechaHoraFinPlazoRespuesta() != null)
			notificacion.fechaFinPlazo = new DateTime(notificacionType.getFechaHoraFinPlazoRespuesta().toGregorianCalendar().getTime());
		if (notificacionType.getFechaHoraFinPlazoAcceso() != null)
			notificacion.fechaLimite = new DateTime(notificacionType.getFechaHoraFinPlazoAcceso().toGregorianCalendar().getTime());
		notificacion.idExpedienteAed = notificacionType.getNumeroExpediente();
		notificacion.plazoAcceso = notificacionType.getPlazoAcceso();
		notificacion.plazoRespuesta = notificacionType.getPlazoRespuesta();
		notificacion.frecuenciaRecordatorioAcceso = notificacionType.getRecordatorioAcceso();
		notificacion.frecuenciaRecordatorioRespuesta = notificacionType.getRecordatorioRespuesta();
		notificacion.uri = notificacionType.getUriNotificacion();
		notificacion.uriProcedimiento = notificacionType.getUriProcedimiento();
		for (String uri: notificacionType.getUrisDocumentosAdjuntos())
			notificacion.documentosAnexos.add(new DocumentoNotificacion(uri));
		for (String uri: notificacionType.getUrisDocumentosANotificar())
			notificacion.documentosANotificar.add(new DocumentoNotificacion(uri));
		return notificacion;
	}
	
	public static String convertEstadoNotificacionEnumTypeToEstadoNotificacion (EstadoNotificacionEnumType estadoNotificacionEnumType){
		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.ANULADA.name()))
			return EstadoNotificacionEnum.anulada.name();
		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.EXPIRADA.name()))
			return EstadoNotificacionEnum.expirada.name();
		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.LEIDA.name()))
			return EstadoNotificacionEnum.leida.name();
		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.LEIDA_PLAZO_RESPUESTA_VENCIDO.name()))
			return EstadoNotificacionEnum.leidaplazorespuestavencido.name();
		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.PLAZO_RESPUESTA_VENCIDO.name()))
			return EstadoNotificacionEnum.plazorespuestavencido.name();
		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.PUESTA_A_DISPOSICION.name()))
			return EstadoNotificacionEnum.puestaadisposicion.name();
		if (estadoNotificacionEnumType.name().equals(EstadoNotificacionEnumType.RESPONDIDA.name()))
			return EstadoNotificacionEnum.respondida.name();
		return "";
	}
	
	@Transactional
	public static void recargarNotificacionesFromWS (String uriProcedimiento){
		if (notificacionService != null){
			List<Notificacion> notificaciones = notificacionService.getNotificaciones(uriProcedimiento);
			if (notificaciones != null){
				for (Notificacion notificacion: notificaciones){
					Notificacion dbNotificacion = (Notificacion) Notificacion.find("select notificacion from Notificacion notificacion where notificacion.uri=?", notificacion.uri).first();
					if (dbNotificacion != null){
						dbNotificacion.actualizar(notificacion);
						dbNotificacion.save();
					}
				}
			} else {
				play.Logger.error("Hubo un problema al actualizar desde el servicio web, las notificaciones");
			}
		} else {
			play.Logger.error("No se pudo inyectar el servicio de Notificaciones");
		}
	}
	
	@Transactional
	public static void recargarDocumentosNotificacionesFromWS (String uriProcedimiento){
		if (notificacionService != null){
			List<Notificacion> notificaciones = notificacionService.getNotificaciones(uriProcedimiento);
			if (notificaciones != null){
				for (Notificacion notificacion: notificaciones){
					Notificacion dbNotificacion = (Notificacion) Notificacion.find("select notificacion from Notificacion notificacion where notificacion.uri=?", notificacion.uri).first();
					if (dbNotificacion != null){
						dbNotificacion.actualizarDocumentacion(notificacion);
						dbNotificacion.save();
					}
				}
			} else {
				play.Logger.error("Hubo un problema al actualizar desde el servicio web, las notificaciones");
			}
		} else {
			play.Logger.error("No se pudo inyectar el servicio de Notificaciones");
		}
	}
	
	//Subir nuevos documentos al Expediente en el AED
	public static void subirDocumentosNotificacionExpediente (List<DocumentoNotificacion> documentos, Notificacion notificacion){
		//Pasar de documentoNotificacion a Documento para subir al AED
		
		List<ExpedienteAed> listaExp = new ArrayList<ExpedienteAed>();
		List<String> listaDocs = new ArrayList<String>();
		
		for (DocumentoNotificacion documentoNotificacion : documentos) {
			if (documentoNotificacion.uri != null){
				listaDocs.add(documentoNotificacion.uri);
			}
		}
		
		// 1) Obtener el expediente del AED al que pertenece la notificacion
		listaExp = new ArrayList<ExpedienteAed>();
		ExpedienteAed expedienteAED = ExpedienteAed.find("select expediente from ExpedienteAed expediente where expediente.idAed = ?", notificacion.idExpedienteAed).first();
		listaExp.add(expedienteAED);  //ExpedienteAED de solicitud
		// 2) Copiar al AED 
		try {
			gestorDocumentalService.copiarListaDocumentoEnExpediente(listaDocs, listaExp);
		} catch (GestorDocumentalServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Messages.error("Error copiando el documento al AED");
		}
	}
	
	//Subir un único Documento al expediente en el AED
	public static void subirDocumentoNotificacionExpediente (String uri, Notificacion notificacion){
		//Guardar en temporal
		// 1) Obtener el expediente del AED al que pertenece la notificacion
		List<ExpedienteAed> listaExp = new ArrayList<ExpedienteAed>();
		ExpedienteAed expedienteAED = ExpedienteAed.find("select expediente from ExpedienteAed expediente where expediente.idAed = ?", notificacion.idExpedienteAed).first();
	
		listaExp.add(expedienteAED);  //ExpedienteAED de solicitud	
		
		// 2) Copiar al AED 
		try {
			gestorDocumentalService.copiarDocumentoEnExpediente(uri, listaExp);
		} catch (GestorDocumentalServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Messages.error("Error copiando el documento al AED");
		}
	}
	
	public static Documento obtenerDocumentos(Notificacion notificacion, DocumentoNotificacionEnumType tipo){
		Documento documento = null;
		try {
			documento = notificacionService.obtenerDocumentoNotificacion(notificacion.agente.username.toString(), notificacion.uri, tipo);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return documento;
	}
	
	public static String obtenerUriDocumentos(Notificacion notificacion, DocumentoNotificacionEnumType tipo){
		String uri = "";
		try {
			uri = notificacionService.obtenerUriDocumentoNotificacion(notificacion.agente.username.toString(), notificacion.uri, tipo);
			if(uri == null)
				uri="";
		} catch (Exception e) {
			// TODO: handle exception
		}
		return uri;
	}
	
	public static String obtenerDescripcionDocumento(String uriDocumento) {
		String descripcion = "";
		try {
			descripcion = gestorDocumentalService.getDescripcionDocumento(uriDocumento);
		} catch (Exception e) {
			play.Logger.error("Se ha producido un error obteniendo la descripción del documento");
		}	
		return descripcion;
	}
	
	public static String obtenerTipoDocumento(String uriDocumento) {
		String tipo = "";
		try {
			tipo = gestorDocumentalService.getTipoDocumento(uriDocumento);
		} catch (Exception e) {
			play.Logger.error("Se ha producido un error obteniendo el tipo del documento");
		}	
		return tipo;
	}
	
	public static boolean obtenerFirmadoDocumentoNotificacion(String idUsuario, String uriNotificacion, DocumentoNotificacionEnumType tipoDocumento) {
		boolean firmado = false;
		try {
			firmado = notificacionService.obtenerFirmadoDocumentoNotificacion(idUsuario, uriNotificacion, tipoDocumento);
		} catch (Exception e) {
			play.Logger.error("Se ha producido un error comprobando si el documento está firmado");
		}
		return firmado;
	}
	
}
