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
public class CesionPDF extends FapModel {
	// Código de los atributos

	@ValueFromTable("listaCesiones")
	@FapEnum("enumerado.fap.gen.ListaCesionesEnum")
	public String tipo;

	public String cabeceraPrimera;

	public String cabeceraSegunda;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public RegistroCesion registro;

	public CesionPDF() {
		init();
	}

	public void init() {

		if (registro == null)
			registro = new RegistroCesion();
		else
			registro.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
