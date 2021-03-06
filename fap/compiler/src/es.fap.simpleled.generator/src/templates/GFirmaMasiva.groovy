package templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.emf.ecore.EObject;
import org.apache.commons.lang3.StringEscapeUtils;

import generator.utils.*;
import es.fap.simpleled.led.*;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

import generator.utils.CampoUtils;

public class GFirmaMasiva extends GElement{

	FirmaMasiva firmaMasiva;
	GElement gPaginaPopup;
	Controller controller;
	CampoUtils campo;
	String stringParamsAdded;
	Set<Entity> globalSetEntityParent;
	
	public GFirmaMasiva(FirmaMasiva firmaMasiva, GElement container){
		super(firmaMasiva, container);
		this.firmaMasiva = firmaMasiva;
		campo = CampoUtils.create(firmaMasiva.documentos.campo);
		gPaginaPopup = getPaginaOrPopupContainer();
		//stringParamsAdded = new ArrayList<String>();
	}
	
	public String viewWithParams (Set<Entity> setEntityParent){
		globalSetEntityParent = setEntityParent;
		view();
	}
	
	public String view(){
		String controllerName = gPaginaPopup.controllerFullName();
		TagParameters params = new TagParameters();
		params.putStr 'id', id();
		Entidad entidad = campo.entidad;
		if(campo.getCampo().getAtributos() != null && !entidad.isSingleton()) {
			// No le han llegado los parámetros
			String finalStrParams = "";
			if (globalSetEntityParent != null && globalSetEntityParent.size() > 0) {
				finalStrParams = StringUtils.params(globalSetEntityParent.collect{if (!entidad.id.equals(it.id)) if ((it.id) != null) it.id});
				// Debemos eliminar los que no empiezan por id y los que ya están en params
				if (finalStrParams.trim().length() > 0)
					params.put 'urlFirmaMasiva', "@${controllerName}.${controllerMethodName()}(${entidad.id}, "+finalStrParams+")";
				else
					params.put 'urlFirmaMasiva', "@${controllerName}.${controllerMethodName()}(${entidad.id})";
			} else {
				params.put 'urlFirmaMasiva', "@${controllerName}.${controllerMethodName()}(${entidad.id})";
			}
		}
		else
			params.put 'urlFirmaMasiva', "@${controllerName}.${controllerMethodName()}(${StringUtils.params(globalSetEntityParent.collect{it.id})})";
        if (firmaMasiva.titulo)
			params.putStr 'titulo', firmaMasiva.titulo;
		params.putStr 'campo', campo.str;
		if (firmaMasiva.alto)
			params.putStr 'alto', firmaMasiva.alto;

		botonesPopup(params);
		botonesPagina(params);
		if (firmaMasiva.recargarPagina)
			params.put("recargarPagina", true)

		if (gPaginaPopup instanceof GPopup)
			params.putStr 'tipoContainer', "popup";
		else
			params.putStr 'tipoContainer', "pagina";
		params.putStr("idEntidad", "${Entidad.create(campo.ultimaEntidad).id}");
	
		controller = Controller.create(getPaginaOrPopupContainer());
		
		if (firmaMasiva.documentos.campo.entidad.name.equals(controller.campo?.ultimaEntidad?.name) && (firmaMasiva.paginaCrear || firmaMasiva.popupCrear) && !controller.entidad.isSingleton()){
			params.put 'crearEntidad', "accion == 'crear'";
			params.putStr 'nameContainer', gPaginaPopup.name;
			params.putStr 'idContainer', controller.entidad.id;
			params.put 'urlContainerCrear', controller.getRouteIndex("crear", false, false);
			params.put 'urlContainerEditar', controller.getRouteIndex("editar", false, true);
			params.put 'urlCrearEntidad', controller.getRouteAccion("crearForfirmaMasivas");
		}
		
		//Nombre de los botones (opcional)
		if(firmaMasiva.nombreBotonVer != null){
			params.putStr 'nombreBotonVer', firmaMasiva.nombreBotonVer;
		}
		
		if (firmaMasiva.popupCrear){
			params.put 'urlRedirigir', controller.getRouteIndex("editar", false, true);
		}
		
		if (firmaMasiva.paginaCrear){
			params.put 'urlBeforeOpenPageTable', controller.getRouteBeforeOpenPageTable("editar");
		}
		if (firmaMasiva.paginaCrear != null) {
			params.put 'urlCrear', Controller.create(GElement.getInstance(firmaMasiva.paginaCrear, null)).getRouteIndex("crear", true, true);
			if (firmaMasiva.paginaCrear.permiso)
				params.putStr 'permisoCrear', firmaMasiva.paginaCrear.permiso.name;
		}
		
		if (firmaMasiva.popup || firmaMasiva.popupCrear){
			params.put 'urlRedirigir', controller.getRouteIndex("editar", false, true);
		}
		
		StringBuffer columnasView = new StringBuffer();

		if(firmaMasiva.columnas.isEmpty()){
			Columna c = LedFactory.eINSTANCE.createColumna();
			c.campo = CampoUtils.create("${LedCampoUtils.getUltimaEntidad(firmaMasiva.documentos.campo).name}.id").campo;
			firmaMasiva.columnas.add(c);
		}
		for(Columna c : firmaMasiva.columnas)
			columnasView.append (columnaView(c));

		if (controller.algoQueGuardar())
			params.put("saveEntity", true);
		else
			params.put("saveEntity", false);

		String view = """
#{fap.firmaMasiva ${params.lista(true)}
}
	${columnasView}
#{/fap.firmaMasiva}
"""
		return view;
	}
	
	private String id(){
		return firmaMasiva.name ?: campo.str.replace(".", "_");
	}
	
	private void botonesPopup(TagParameters params){
		if (firmaMasiva.popup != null) {
			Controller popupUtil = Controller.create(GElement.getInstance(firmaMasiva.popup, null));
			params.put 'urlLeer', popupUtil.getRouteIndex("leer", true, true);
			params.putStr 'popupLeer', firmaMasiva.popup.name;
			params.put 'urlCrear', popupUtil.getRouteIndex("crear", true, true);
			params.putStr 'popupCrear', firmaMasiva.popup.name;
			params.put 'urlEditar', popupUtil.getRouteIndex("editar", true, true);
			params.putStr 'popupEditar', firmaMasiva.popup.name;
			params.put 'urlBorrar', popupUtil.getRouteIndex("borrar", true, true);
			params.putStr 'popupBorrar', firmaMasiva.popup.name;
			if (firmaMasiva.popup.permiso)
				params.putStr 'permisoCrear', firmaMasiva.popup.permiso.name;
		}
		if (firmaMasiva.popupLeer != null) {
			params.put 'urlLeer', Controller.create(GElement.getInstance(firmaMasiva.popupLeer, null)).getRouteIndex("leer", true, true);
			params.putStr 'popupLeer', firmaMasiva.popupLeer.name;
		}
		if (firmaMasiva.popupCrear != null) {
			params.put 'urlCrear', Controller.create(GElement.getInstance(firmaMasiva.popupCrear, null)).getRouteIndex("crear", true, true);
			params.putStr 'popupCrear', firmaMasiva.popupCrear.name;
			if (firmaMasiva.popupCrear.permiso)
				params.putStr 'permisoCrear', firmaMasiva.popupCrear.permiso.name;
		}
		if (firmaMasiva.popupEditar != null) {
			params.put 'urlEditar', Controller.create(GElement.getInstance(firmaMasiva.popupEditar, null)).getRouteIndex("editar", true, true);
			params.putStr 'popupEditar', firmaMasiva.popupEditar.name;
		}
		if (firmaMasiva.popupBorrar != null) {
			params.put 'urlBorrar', Controller.create(GElement.getInstance(firmaMasiva.popupBorrar, null)).getRouteIndex("borrar", true, true);
			params.putStr 'popupBorrar', firmaMasiva.popupBorrar.name;
		}
	}
	
	private void botonesPagina(TagParameters params){
		if (firmaMasiva.paginaLeer != null) {
			params.put 'urlLeer', Controller.create(GElement.getInstance(firmaMasiva.paginaLeer, null)).getRouteIndex("leer", true, true);
		}
		if (firmaMasiva.paginaCrear != null) {
			params.put 'urlCrear', Controller.create(GElement.getInstance(firmaMasiva.paginaCrear, null)).getRouteIndex("crear", true, true);
			if (firmaMasiva.paginaCrear.permiso)
				params.putStr 'permisoCrear', firmaMasiva.paginaCrear.permiso.name;
		}
	}
	
	private void addPopupBoton(Map popups, Popup popup, List<String> botones){
		if (popups.get(popup) != null){
			List last = popups.get(popup);
			last.addAll(botones);
			popups.put(popup, last);
		}
		else{
			popups.put(popup, botones);
		}
	}
	
	private String columnaView(Columna c){
		List<CampoUtils> campos = GFirmaMasiva.camposDeColumna(c);
		c.titulo = c.titulo ?: campos.collect{it.str}.join(', ');
		c.ancho  = c.ancho ?: "200";
				
		TagParameters params = new TagParameters();
		params.putStr "cabecera", c.titulo;
		params.put "ancho", c.ancho
		String positionDefault = "left";
		if(c.campo != null){
			params.putStr("campo", CampoUtils.create(c.campo).sinEntidad());
			if (CampoUtils.create(c.campo).getUltimoAtributo()?.type?.special?.type?.equals("Moneda")) {
				// Si es un campo Moneda lo alineamos a la derecha
				positionDefault = "right";
			}
		}
		else if(c.funcion != null){
			String str = StringEscapeUtils.escapeJava(c.funcion).replace("'", "\\'");
			params.putStr("funcion", funcionSinEntidades(str));	
		}
		else if(c.funcionRaw != null){
			String str = StringEscapeUtils.escapeJava(c.funcionRaw).replace("'", "\\'");
			params.putStr("funcionRaw", funcionSinEntidades(str));
		}
		
		if (c.position != null) {
			params.putStr("alignPosition", c.position);
		} else {
			params.putStr("alignPosition", positionDefault);
		}
		
		if (c.permiso != null){
			params.putStr('permiso', c.permiso.name)
		}	

		if(c.isExpandir())
			params.put("expandir", "true")	
		
		return """
	#{fap.columna ${params.lista()} /}
		"""
	}
	
	public static String funcionSinEntidades(String funcion){
		Pattern funcionSinEntidadPattern = Pattern.compile('\\$\\{(.*?)\\}');
		Matcher matcher = funcionSinEntidadPattern.matcher(funcion);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String replacement = '${' + CampoUtils.create(matcher.group(1).trim()).sinEntidad() + '}';
			if (replacement != null) {
				matcher.appendReplacement(buffer, "");
				buffer.append(replacement);
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}
	
	/**
	 * Calcula los campos que se muestran en la columna
	 * En el caso de que se haya especificado el atributo campo : se muestra un único campo
	 * Si se especifico una función. Se analiza la función buscando los campos que aparecen en ella
	 * @return
	 */
	public static List<CampoUtils> camposDeColumna(Columna c){
		List<CampoUtils> campos = new ArrayList<CampoUtils>();
		if(c.campo != null){
			CampoUtils _campo = CampoUtils.create(c.campo)
			campos.add(_campo);
			if (LedEntidadUtils.isMoneda(_campo.getUltimoAtributo())){
				campos.add(CampoUtils.create(CampoUtils.getCampoStr(c.campo)+"_formatFapTabla"));
			}
		}
		else if(c.funcion != null || c.funcionRaw != null){
			String strFuncion = c.funcion != null ? c.funcion : c.funcionRaw;
			Pattern funcionSinEntidadPattern = Pattern.compile('\\$\\{(.*?)\\}');
			Matcher matcher = funcionSinEntidadPattern.matcher(strFuncion);
			StringBuffer buffer = new StringBuffer();
			while (matcher.find()) {
				campos.add(CampoUtils.create(matcher.group(1).trim()));
			}
		}
		return campos;
	}
	
	/**
	 * Convierte la funcion de la columna a una función de javascript
	 * @param c
	 * @return
	 */
	public static String renderer(Columna c){
		String strFuncion = c.funcion != null ? c.funcion : c.funcionRaw;
		if(strFuncion == null)
			return null;
		if(c.funcionRaw != null)
			return "return " + (strFuncion.replaceAll(/\$\{(.*?)\}/, 'record[\'$1\']')) + ";"
		else
			return  "return '" + (strFuncion.replaceAll(/\$\{(.*?)\}/, '\' + record[\'$1\'] + \'')) + "';"
	}
	
	public List<CampoUtils> uniqueCamposFirmaMasiva(FirmaMasiva firmaMasiva){
		List<CampoUtils> campos = new ArrayList<CampoUtils>();
		for(Columna c : firmaMasiva.columnas){
			campos.addAll(camposDeColumna(c));
		}
		//Añade el ID de la entidad
		campos.add(CampoUtils.create(campo.getUltimaEntidad().name + ".id"));
		return campos.unique();
	}
	
	public String controllerWithParams (Set<Entity> setEntityParent){
		//println setEntityParent;
		globalSetEntityParent = setEntityParent;
		stringParamsAdded = StringUtils.params(setEntityParent.collect{it.typeId});
		controller();
	}
	
	public String controller(){
		List<CampoUtils> campos = uniqueCamposFirmaMasiva(firmaMasiva);
				
		// Si tiene permiso definido la tabla
		String renderCode = "";
		String codeConPermiso = "";

		//Clase de la entidad que contiene la lista
		
		Entidad entidad = Entidad.create(campo.getUltimaEntidad());
		Entidad entidadRaiz = campo.entidad;
		entidadRaiz.singletonsId = true;
		
		//Consulta para obtener la solicitud de los documentos de firmaMasiva
		String querySolicitud = """${entidadRaiz.clase}.find("select ${entidadRaiz.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable} join ${campo.firstLower()} ${entidad.variable} where ${entidad.variable}.id=?", ${entidad.id}).first()""";
		
		//La consulta depende de si se listan todas las entidades de una clase, o se accede a un campo
		String query = null;
		String param = "";
		String idSingleton = "";
		
		if (firmaMasiva.metodoFilas)
			query = """${firmaMasiva.metodoFilas}""";
		else if (!campo.getCampo().getAtributos()) //Lista todas las entidades de ese tipo
			query = """${entidad.clase}.find("select ${entidadRaiz.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable}").fetch()""";
		else{ //Acceso a los campos de una entidad
			query = """${entidad.clase}.find("select ${entidad.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable} join ${campo.firstLower()} ${entidad.variable} where ${entidadRaiz.variable}.id=?", ${entidadRaiz.id}).fetch()""";
			if (entidadRaiz.isSingleton())
				idSingleton = "${entidadRaiz.typeId} = ${entidadRaiz.clase}.get(${entidadRaiz.clase}.class).id;";
			else
				param = entidadRaiz.typeId;
		}
		
		String rowsStr = campos.collect { '"' + it.sinEntidad() + '"'  }.join(", ");

		String finalStrParams = "";
		if (stringParamsAdded != null && stringParamsAdded.length() > 0) {
			finalStrParams = StringUtils.params(globalSetEntityParent.collect{if (!param.contains(it.typeId)) it.typeId});
			// Debemos eliminar los que no empiezan por id y los que ya están en params
			if ((param.trim().size() > 0) && (finalStrParams.trim().size() > 0))
				finalStrParams = param + ", "+finalStrParams;
			else if (param.trim().size() > 0)
				finalStrParams = param;
		} else {
			finalStrParams = param;
		}
		// Deberemos añadir además, los id necesarios en el formulario, página o popup que contiene a esta tabla.

		return """
			public static void ${controllerMethodName()}(${finalStrParams}){
				${idSingleton}
				java.util.List<${entidad.clase}> rows = ${query};
				${getCodePermiso(entidad)}
			    ${getCodeFilasPermiso(entidad)}
				renderJSON(response.toJSON($rowsStr));
			}

	public static String obtenerUrlDocumento${id()}(Long idDocumento){
		return FirmaUtils.obtenerUrlDocumento(idDocumento);
	}

	@javax.inject.Inject
    static GestorDocumentalService gestorDocumentalService;

	public static String obtenerFirmadoDocumento${id()}(Long idDocumento) {
      	if (!permiso("leer")) {
			HashMap error = new HashMap();
			error.put("error", "No tiene permisos suficientes");
			return new Gson().toJson(error);
		}
		Documento documento = Documento.find("select documento from Documento documento where documento.id=?", idDocumento).first();
		if (documento != null) {
			play.Logger.info("El documento " + documento.id + " tiene la uri " + documento.uri + " y  firmado a " + documento.firmado);
			HashMap json = new HashMap();
			if (FirmaUtils.hanFirmadoTodos(documento.firmantes.todos)) {
				json.put("firmado", true);
				json.put("descripcion", documento.descripcion);
				json.put("refAed", documento.refAed);
				return new Gson().toJson(json);
			} else {
				List<String> firmantes = new ArrayList<String>();
				for (Firmante firmante : documento.firmantes.todos) {
					firmantes.add(firmante.idvalor);
				}
				Firma firma = null;
				try {
					firma = gestorDocumentalService.getFirma(documento);
				} catch (GestorDocumentalServiceException e) {
					e.printStackTrace();
				}
				json.put("id", documento.id);
				json.put("firmado", false);
				if (firma != null) {
					json.put("firma", firma.getContenido());
				}
				json.put("refAed", documento.refAed);
				json.put("descripcion", documento.descripcion);
				json.put("firmantes", firmantes);
				json.put("url", FirmaUtils.obtenerUrlDocumento(documento.id));
				return new Gson().toJson(json);
			}
		}
		play.Logger.info("Error al obtener el documento " + idDocumento);
		return null;
	}

	@Util
	public static String firmar${id()}(Long idDocumento, String firma) {

        Map<String, Object> json = new HashMap<String, Object>();
		ArrayList<String> errores = new ArrayList<String>();
		ArrayList<String> aciertos = new ArrayList<String>();
		Documento documento = null;
		SolicitudGenerica solicitud = null;
		
		EntityTransaction tx = JPA.em().getTransaction();
		try {
			if (tx.isActive())
		    	tx.commit();
		    tx.begin();
		    
			documento = Documento.find("select documento from Documento documento where documento.id=?", idDocumento).first();
			solicitud = ${querySolicitud};
		
			if (documento != null && solicitud != null) {
				Messages.clear();

				play.Logger.info("Firmando documento " + documento.uri + " de la Solicitud " + solicitud.id);
	
				json.put("idDocumento", idDocumento);
				json.put("firmado", false);
				
			    if (documento.firmantes == null) {
					documento.firmantes = new Firmantes();
					documento.save();
				}

				//Calcula los firmantes del documento
				play.Logger.info("Calculando firmantes del documento " + documento.uri + " de la Solicitud " + solicitud.id);
				documento.firmantes.todos = calcularFirmantes${id()}(solicitud.id);
				documento.firmantes.save();

				FirmaUtils.firmarDocumento(documento, documento.firmantes.todos, firma, null);
	
				if (!Messages.hasErrors()) {
					play.Logger.info("Firma de documento " + documento.uri + " con éxito");
					if (documento.firmantes.todos.size() > 0 && FirmaUtils.hanFirmadoTodos(documento.firmantes.todos))
						json.put("firmado", true);
					else
						json.put("firmado", false);
				}

			} else {
				String errorDocumento = "";
				String errorSolicitud = "";
				if (documento == null) {
					errorDocumento = "Error al obtener el documento " + idDocumento;
					errores.add(errorDocumento);
				}
				if (solicitud == null) {
					errorSolicitud = "Error al obtener la solicitud";
					errores.add(errorSolicitud);
				}
			}
		    
			tx.commit();
	 	}catch (RuntimeException e) {
		    if ( tx != null && tx.isActive() ) tx.rollback();
		    play.Logger.error("Se ha producido un error en el proceso de firma: " + documento.descripcion + " de la solicitud: " + solicitud.id);
		    Messages.error("Error en el proceso de firma");
		    e.printStackTrace();
		}
		tx.begin();

		for (String mensaje : Messages.messages(MessageType.OK)) {
			aciertos.add(mensaje);
		}

		for (String mensaje : Messages.messages(MessageType.ERROR)) {
			errores.add(mensaje);
		}

		json.put("errores", errores);
		json.put("aciertos", aciertos);
		return new Gson().toJson(json);
	}
	
	public static List<Firmante> calcularFirmantes${id()}(Long idSolicitud) {

		SolicitudGenerica solicitud = null;
		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		} else {
			solicitud = SolicitudGenerica.findById(idSolicitud);
			if (solicitud == null) {
				Messages.fatal("Error al recuperar SolicitudGenerica");
			}
		}
		
		Firmantes firmantes = new Firmantes();
		//Solicitante de la solicitud
		Firmante firmanteSolicitante = new Firmante(solicitud.solicitante, "unico");
		firmantes.todos.add(firmanteSolicitante);

		//Comprueba los representantes
		if (solicitud.solicitante.isPersonaFisica() && solicitud.solicitante.representado) {
			// Representante de persona física
			Firmante representante = new Firmante(solicitud.solicitante.representante, "representante", "unico");
			firmantes.todos.add(representante);
		} else if (solicitud.solicitante.isPersonaJuridica()) {
			//Representantes de la persona jurídica
			for (RepresentantePersonaJuridica r : solicitud.solicitante.representantes) {
				String cardinalidad = null;
				if (r.tipoRepresentacion.equals("mancomunado")) {
					cardinalidad = "multiple";
				} else if ((r.tipoRepresentacion.equals("solidario")) || (r.tipoRepresentacion.equals("administradorUnico"))) {
					cardinalidad = "unico";
				}
				Firmante firmante = new Firmante(r, "representante", cardinalidad);
				firmantes.todos.add(firmante);
			}
		}
		return firmantes.todos;
	}

		"""
	}
	
	/**
	 * Devuelve el codigo para la consulta de la tabla con permisos, dada la entidad
	 * sobre la que se busca en la tabla
	 * @param entidad Campo de la tabla
	 * @param permiso Nombre del permiso que se va a consultar en esa tabla
	 * @param camposStr String con los campos que debemos devolver
	 * @return
	 */
	private String getCodePermiso(Entidad entidad) {
		if(firmaMasiva.permiso == null){
			return """
				Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
				List<${entidad.clase}> rowsFiltered = rows; //Tabla sin permisos, no filtra
			""";
		}
		return """
			List<${entidad.clase}> rowsFiltered = new ArrayList<${entidad.clase}>();
			Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
			for(${entidad.clase} ${entidad.variable}: rows){
				Map<String, Object> vars = new HashMap<String, Object>();
				vars.put("${entidad.variable}", ${entidad.variable});
				if (secure.checkAcceso("${firmaMasiva.permiso.name}", "leer", ids, vars)) {
					rowsFiltered.add(${entidad.variable});
				}
			}
		"""
	}
	
	private String getCodeFilasPermiso(Entidad entidad) {
		String ret="";
		String paramsPermiso="";
		String paramsNombrePermiso="";
	
		if(firmaMasiva.popup != null){
			Popup popUp = (Popup)firmaMasiva.popup;
			if (popUp.permiso != null){
				paramsPermiso+="true, true, true, \"${popUp.permiso.name}\", \"${popUp.permiso.name}\", \"${popUp.permiso.name}\", getAccion(), ids";
			} else {
				paramsPermiso+="false, false, false, \"\", \"\", \"\", getAccion(), ids";
			} 
		} else {
				if(firmaMasiva.popupEditar != null){
					Popup popUp = (Popup)firmaMasiva.popupEditar;
					if (popUp.permiso != null){
						paramsPermiso+="true, ";
						paramsNombrePermiso+="\"${popUp.permiso.name}\", ";
					} else{
						paramsPermiso+="false, ";
						paramsNombrePermiso+="\"\", ";
					}
				} else {
					paramsPermiso+="false, ";
					paramsNombrePermiso+="\"\", ";
				}
				
				if(firmaMasiva.popupBorrar != null){
					Popup popUp = (Popup)firmaMasiva.popupBorrar;
					if (popUp.permiso != null){
						paramsPermiso+="true, ";
						paramsNombrePermiso+="\"${popUp.permiso.name}\", ";
					} else{
						paramsPermiso+="false, ";
						paramsNombrePermiso+="\"\", ";
					}
				} else {
					paramsPermiso+="false, ";
					paramsNombrePermiso+="\"\", ";
				}
				
				if(firmaMasiva.popupLeer != null){
					Popup popUp = (Popup)firmaMasiva.popupLeer;
					if (popUp.permiso != null){
						paramsPermiso+="true, ";
						paramsNombrePermiso+="\"${popUp.permiso.name}\"";
					} else{
						paramsPermiso+="false, ";
						paramsNombrePermiso+="\"\"";
					}
				} else {
					paramsPermiso+="false, ";
					paramsNombrePermiso+="\"\"";
				}
			}

		paramsPermiso+=paramsNombrePermiso+", getAccion(), ids"

		ret+="""tables.TableRenderResponse<${entidad.clase}> response = new tables.TableRenderResponse<${entidad.clase}>(rowsFiltered, ${paramsPermiso});
			"""
		return ret;
	}
	
	private controllerMethodName(){
		return "firmaMasiva" + id();
	}
	
	public String routes(){
		String url = gPaginaPopup.url() + "/" + id();
		String action = gPaginaPopup.controllerFullName() + "." + controllerMethodName();
		
		return RouteUtils.to("GET", url, action).toString() + "\n";
	}
	
	public void addParams2Controller (String stringParams) {
		stringParamsAdded = stringParams;
	}
}