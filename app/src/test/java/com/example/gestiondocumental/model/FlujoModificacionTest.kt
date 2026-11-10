package com.example.gestiondocumental.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FlujoModificacionTest {

    @Test
    fun contratoYLicenciaRequierenRevision() {
        assertEquals(
            TipoCambio.CON_REVISION,
            FlujoModificacion.tipoCambioPara(CategoriaDocumento.CONTRATO)
        )
        assertEquals(
            TipoCambio.CON_REVISION,
            FlujoModificacion.tipoCambioPara(CategoriaDocumento.LICENCIA_MEDICA)
        )
        assertEquals(
            TipoCambio.CON_REVISION,
            FlujoModificacion.tipoCambioPara(CategoriaDocumento.ANTECEDENTE_SENSIBLE)
        )
    }

    @Test
    fun permisoYReporteSonDirectos() {
        assertEquals(
            TipoCambio.DIRECTO,
            FlujoModificacion.tipoCambioPara(CategoriaDocumento.PERMISO)
        )
        assertEquals(
            TipoCambio.DIRECTO,
            FlujoModificacion.tipoCambioPara(CategoriaDocumento.REPORTE)
        )
    }

    @Test
    fun siguienteVersionIncrementa() {
        assertEquals(3, FlujoModificacion.siguienteVersion(2))
        assertEquals(1, FlujoModificacion.siguienteVersion(0))
    }

    @Test
    fun aprobacionDejaDocumentoVigente() {
        assertEquals(EstadoDocumento.VIGENTE, FlujoModificacion.estadoTrasAprobacion())
    }

    @Test
    fun rechazoDejaSolicitudRechazada() {
        assertEquals(EstadoSolicitud.RECHAZADA, FlujoModificacion.estadoSolicitudRechazada())
    }

    @Test
    fun soloRrhhYJefaturaResuelven() {
        assertEquals(true, FlujoModificacion.puedeResolver(Rol.RRHH))
        assertEquals(true, FlujoModificacion.puedeResolver(Rol.JEFATURA))
        assertEquals(false, FlujoModificacion.puedeResolver(Rol.FUNCIONARIO))
    }
}
