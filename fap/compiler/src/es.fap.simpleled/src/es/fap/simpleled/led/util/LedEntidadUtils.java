package es.fap.simpleled.led.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.google.inject.Inject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.CampoAtributos;
import es.fap.simpleled.led.CompoundType;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.impl.LedFactoryImpl;

public class LedEntidadUtils {
	
	@Inject
	public static LedPackage ledPackage;
	
	public static List<Attribute> getAllDirectAttributes(Entity entidad) {
		List<Attribute> attrs = new ArrayList<Attribute>();
		while (entidad != null) {
			LedEntidadUtils.addId(entidad);
			for (Attribute attr : entidad.getAttributes())
				attrs.add(attr);
			entidad = entidad.getExtends();
		}
		return attrs;
	}
	
	public static Entity getEntidad(Attribute attr) {
		if (attr == null || attr.getType().getCompound() == null)
			return null;
		return attr.getType().getCompound().getEntidad();
	}

	public static boolean esSingleton(Entity entidad){
		if (entidad.getExtends() == null)
			return false;
		if (entidad.getExtends().getName().equals("Singleton"))
			return true;
		return esSingleton(entidad.getExtends());
	}
	
	public static boolean isReferencia(Attribute attr){
		return attr != null && attr.getType().getCompound() != null && attr.getType().getCompound().getEntidad() != null;
	}
	
	/**
	 * En el caso de que el atributo sea una referencia, devuelve el tipo
	 * de la referencia(OneToOne, OneToMany, ManyToOne, ManyToMany)
	 * El tipo por defecto es OneToOne
	 * @param attr
	 * @return
	 */
	public static String getTipoReferencia(Attribute attr){
		if (!isReferencia(attr)) return null;
		CompoundType c = attr.getType().getCompound();
		if(c.getTipoReferencia() == null){
			return "OneToOne";
		}		
		return c.getTipoReferencia().getType();
	}
	
	public static boolean equals(Entity entidad1, Entity entidad2){
		if (entidad1 == null || entidad2 == null)
			return entidad1 == entidad2;
		return entidad1.getName().equals(entidad2.getName());
	}
	
	/**
	 * Comprueba si el attributo attr es una referencia del tipo refType
	 * @param attr
	 * @param refType
	 * @return
	 */
	public static boolean isRefType(Attribute attr, String refType){
		if(attr == null || refType == null) return false;
		String attrRefType = getTipoReferencia(attr);
		return refType.equals(attrRefType);
	}

	public static boolean isOneToOne(Attribute attr){
		return isRefType(attr, "OneToOne");
	}
	
	public static boolean isOneToMany(Attribute attr){
		return isRefType(attr, "OneToMany");
	}
	
	public static boolean isManyToOne(Attribute attr){
		return isRefType(attr, "ManyToOne");
	}
	
	public static boolean isManyToMany(Attribute attr){
		return isRefType(attr, "ManyToMany");
	}

	/**
	 * Comprueba si un atribute es una referencia OneToOne o ManyToOne
	 * @param attr
	 * @return
	 */
	public static boolean xToOne(Attribute attr) {
		return isOneToOne(attr) || isManyToOne(attr);
	}

	/**
	 * Comprueba si un atribute es una referencia OneToMany o ManyToMany
	 * @param attr
	 * @return
	 */
	public static boolean xToMany(Attribute attr){
		return isOneToMany(attr) || isManyToMany(attr);
	}
	
	/**
	 * Comprueba si un atribute es una referencia OneToOne o OneToMany
	 * @param attr
	 * @return
	 */
	public static boolean OneToX(Attribute attr) {
		return isOneToOne(attr) || isOneToOne(attr);
	}

	/**
	 * Comprueba si un atribute es una referencia ManyToOne o ManyToMany
	 * @param attr
	 * @return
	 */
	public static boolean ManyToX(Attribute attr){
		return isManyToOne(attr) || isManyToMany(attr);
	}
	
	public static boolean esLista(Attribute attr){
		if (attr.getType().getCompound() == null){
			return false;
		}
		if (attr.getType().getCompound().getLista() != null){
			return true;
		}
		return false;
	}
	
	public static boolean esColeccion(Attribute attr){
		if (attr.getType().getCompound() == null){
			return false;
		}
		if (attr.getType().getCompound().getCollectionType() != null){
			return true;
		}
		return false;
	}
	
	public static boolean esSimple(Attribute attr){
		return attr.getType().getSimple() != null || attr.getType().getSpecial() != null;
	}
	
	public static String getSimpleTipo(Attribute attr){
		if (attr.getType().getSimple() != null){
			return attr.getType().getSimple().getType();
		}	
		if (attr.getType().getSpecial() != null){
			return attr.getType().getSpecial().getType();
		}
		return null;
	}
		
	public static Attribute getAttribute(Entity entidad, String atributo){
		for (Attribute attr: getAllDirectAttributes(entidad)){
			if (attr.getName().equals(atributo)){
				return attr;
			}
		}
		return null;
	}
	
	public static void addId(Entity entidad){
		if (entidad.getExtends() != null){
			return;
		}
		for (Attribute attr: entidad.getAttributes()){
			if (attr.getName().equals("id")){
				return;
			}
		}
		LedFactory factory = new LedFactoryImpl();
		Attribute id = factory.createAttribute();
		id.setName("id");
		id.setType(factory.createType());
		id.getType().setSimple(new LedFactoryImpl().createSimpleType());
		id.getType().getSimple().setType("Long");
		entidad.getAttributes().add(id);
	}
	
	public static List<Attribute> getAllDirectAttributesExceptId(Entity entidad){
		List<Attribute> attrs = getAllDirectAttributes(entidad);
		for (int i = 0; i < attrs.size(); i++){
			if (attrs.get(i).getName().equals("id")){
				attrs.remove(i);
				break;
			}
		}
		return attrs;
	}
	
	/*
	 * Devuelve la entidad asociada a una pagina, que sera la siguiente:
	 * 		Ultimo atributo del campo definido en la pagina, si no es null
	 * 		�: ultimo atributo del campo definido en el formulario, si no es null
	 * 		�: null
	 */
	public static Entity getEntidadPaginaPopup(EObject paginaPopup){
		return LedCampoUtils.getUltimaEntidad(LedCampoUtils.getCampoPaginaPopup(paginaPopup));
	}
	
	/*
	 * Solicitud.documentos --> {Solicitud, Documento}
	 */
	public static List<Entity> getEntidadesPaginaPopup(EObject paginaPopup){
		Campo campo = LedCampoUtils.getCampoPaginaPopup(paginaPopup);
		List<Entity> entidades = new ArrayList<Entity>();
		if (campo == null)
			return entidades;
		entidades.add(campo.getEntidad());
		CampoAtributos atributos = campo.getAtributos();
		while (atributos != null){
			Attribute attr = atributos.getAtributo();
			if (xToMany(attr) || atributos.getAtributos() == null){
				Entity entidad = getEntidad(attr);
				if (entidad != null)
					entidades.add(entidad);
			}
			atributos = atributos.getAtributos();
		}
		return entidades;
	}
	
	public static Set<Entity> getSingletons(Resource res) {
		Set<Entity> singletons = new HashSet<Entity>();
		for (Entity entidad : ModelUtils.<Entity>getVisibleNodes(ledPackage.getEntity(), res)){
			if (LedEntidadUtils.esSingleton(entidad))
				singletons.add(entidad);
		}
		return singletons;
	}
	
	public static Map<String, Entity> eliminaSolicitudGenerica(Map<String, Entity> entidades){
		if (entidades.containsKey("Solicitud"))
			entidades.remove("SolicitudGenerica");
		return entidades;
	}
	
	public static boolean extend(Entity entidad, String extend) {
		while (entidad != null){
			if (entidad.getName().equals(extend))
				return true;
			entidad = entidad.getExtends();
		}
		return false;
	}

	public static boolean isMoneda (Attribute attr) {
		if (attr.getType().getSpecial() != null) {
			if (attr.getType().getSpecial().getType().equals("Moneda"))
				return true;
		}
		return false;
	}
}
