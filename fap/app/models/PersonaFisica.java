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

@Auditable
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class PersonaFisica extends FapModel {
	// Código de los atributos

	@Transient
	public String nombreCompleto;

	@Transient
	public String numeroId;

	public String nombre;

	public String primerApellido;

	public String segundoApellido;

	@ValueFromTable("sexo")
	@FapEnum("enumerado.fap.gen.SexoEnum")
	public String sexo;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaNacimiento"), @Column(name = "fechaNacimientoTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaNacimiento;

	@CheckWith(NipCheck.class)
	@Embedded
	public Nip nip;

	public PersonaFisica() {
		init();
	}

	public void init() {

		if (nip == null)
			nip = new Nip();

		postInit();
	}

	// === MANUAL REGION START ===
	/**
	 * Nombre completo: Unión de nombre, primerApellido y segundoApellido
	 * @return
	 */
	public String getNombreCompleto() {
		return utils.StringUtils.join(" ", nombre, primerApellido, segundoApellido);
	}

	public String getNumeroId() {
		return nip.valor;
	}

	// === MANUAL REGION END ===

}
