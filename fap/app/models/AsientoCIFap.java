package models;

import java.util.*;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.data.validation.*;
import org.joda.time.DateTime;
import models.*;
import messages.Messages;
import validation.*;
import audit.Auditable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Entity
public class AsientoCIFap extends FapModel {
	// Código de los atributos

	public String observaciones;

	public String resumen;

	public Integer numeroDocumentos;

	public String interesado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ReturnUnidadOrganicaFap unidadOrganicaDestino;

	public Boolean asientoAmpliado;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ReturnUnidadOrganicaFap unidadOrganicaOrigen;

	public String asuntoCodificado;

	public String userId;

	@Transient
	public String password;

	public String tipoTransporte;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "asientocifap_uris")
	public List<ListaUris> uris;

	public AsientoCIFap() {
		init();
	}

	public void init() {

		if (asientoAmpliado == null)
			asientoAmpliado = false;

		if (uris == null)
			uris = new ArrayList<ListaUris>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
