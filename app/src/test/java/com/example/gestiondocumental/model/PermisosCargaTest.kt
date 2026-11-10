package com.example.gestiondocumental.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermisosCargaTest {

    @Test
    fun funcionarioPuedeCargarSuPropioDocumentoNoSensible() {
        assertTrue(
            Permisos.puedeCargarDocumento(
                Rol.FUNCIONARIO, CategoriaDocumento.CONTRATO,
                propio = true, mismaDependencia = true
            )
        )
    }

    @Test
    fun funcionarioNoPuedeCargarCategoriasSensibles() {
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.FUNCIONARIO, CategoriaDocumento.LICENCIA_MEDICA,
                propio = true, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.FUNCIONARIO, CategoriaDocumento.ANTECEDENTE_SENSIBLE,
                propio = true, mismaDependencia = true
            )
        )
    }

    @Test
    fun funcionarioNoPuedeCargarDocumentosAjenos() {
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.FUNCIONARIO, CategoriaDocumento.CONTRATO,
                propio = false, mismaDependencia = true
            )
        )
    }

    @Test
    fun jefaturaPuedeCargarDentroDeSuDependencia() {
        assertTrue(
            Permisos.puedeCargarDocumento(
                Rol.JEFATURA, CategoriaDocumento.CERTIFICADO,
                propio = false, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.JEFATURA, CategoriaDocumento.CERTIFICADO,
                propio = false, mismaDependencia = false
            )
        )
    }

    @Test
    fun tiCiberseguridadYSupervisorNoCargan() {
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.TI_CIBERSEGURIDAD, CategoriaDocumento.CONTRATO,
                propio = true, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.SUPERVISOR, CategoriaDocumento.CONTRATO,
                propio = true, mismaDependencia = true
            )
        )
    }

    @Test
    fun rrhhPuedeCargarCategoriasSensibles() {
        assertTrue(
            Permisos.puedeCargarDocumento(
                Rol.RRHH, CategoriaDocumento.LICENCIA_MEDICA,
                propio = false, mismaDependencia = false
            )
        )
    }

    @Test
    fun categoriasSensiblesSoloLasCargaRrhh() {
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.JEFATURA, CategoriaDocumento.LICENCIA_MEDICA,
                propio = false, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.ADMINISTRATIVO, CategoriaDocumento.ANTECEDENTE_SENSIBLE,
                propio = false, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.ADMIN_SISTEMA, CategoriaDocumento.LICENCIA_MEDICA,
                propio = false, mismaDependencia = true
            )
        )
    }

    @Test
    fun adminSistemaNoCargaContenido() {
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.ADMIN_SISTEMA, CategoriaDocumento.CONTRATO,
                propio = false, mismaDependencia = false
            )
        )
        assertFalse(
            Permisos.puedeCargarDocumento(
                Rol.ADMIN_SISTEMA, CategoriaDocumento.ANTECEDENTE_SENSIBLE,
                propio = false, mismaDependencia = false
            )
        )
    }

    @Test
    fun modificacionSigueLaVisibilidadYExcluyePerfilesDeConsulta() {
        assertTrue(
            Permisos.puedeModificarDocumento(
                Rol.FUNCIONARIO, CategoriaDocumento.CONTRATO,
                propio = true, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeModificarDocumento(
                Rol.TI_CIBERSEGURIDAD, CategoriaDocumento.CONTRATO,
                propio = false, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeModificarDocumento(
                Rol.SUPERVISOR, CategoriaDocumento.CONTRATO,
                propio = false, mismaDependencia = true
            )
        )
    }
}
