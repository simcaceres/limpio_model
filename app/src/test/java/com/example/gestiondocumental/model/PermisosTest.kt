package com.example.gestiondocumental.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermisosTest {

    @Test
    fun funcionarioSoloVeSusPropiosDocumentos() {
        assertTrue(
            Permisos.puedeVerDocumento(
                Rol.FUNCIONARIO, CategoriaDocumento.CONTRATO,
                propio = true, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeVerDocumento(
                Rol.FUNCIONARIO, CategoriaDocumento.CONTRATO,
                propio = false, mismaDependencia = true
            )
        )
    }

    @Test
    fun documentosSensiblesSoloRrhhYPropioFuncionario() {
        assertTrue(
            Permisos.puedeVerDocumento(
                Rol.RRHH, CategoriaDocumento.LICENCIA_MEDICA,
                propio = false, mismaDependencia = false
            )
        )
        assertFalse(
            Permisos.puedeVerDocumento(
                Rol.ADMIN_SISTEMA, CategoriaDocumento.ANTECEDENTE_SENSIBLE,
                propio = false, mismaDependencia = false
            )
        )
        assertFalse(
            Permisos.puedeVerDocumento(
                Rol.JEFATURA, CategoriaDocumento.LICENCIA_MEDICA,
                propio = false, mismaDependencia = true
            )
        )
        assertTrue(
            Permisos.puedeVerDocumento(
                Rol.FUNCIONARIO, CategoriaDocumento.LICENCIA_MEDICA,
                propio = true, mismaDependencia = true
            )
        )
    }

    @Test
    fun perfilesDePlataformaYConsultaNoVenExpedientesIndividuales() {
        assertFalse(
            Permisos.puedeVerDocumento(
                Rol.ADMIN_SISTEMA, CategoriaDocumento.CONTRATO,
                propio = false, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeVerDocumento(
                Rol.TI_CIBERSEGURIDAD, CategoriaDocumento.CONTRATO,
                propio = false, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeVerDocumento(
                Rol.SUPERVISOR, CategoriaDocumento.CONTRATO,
                propio = false, mismaDependencia = true
            )
        )
    }

    @Test
    fun soloRrhhYAdminGestionanUsuarios() {
        assertTrue(Permisos.puedeGestionarUsuarios(Rol.RRHH))
        assertTrue(Permisos.puedeGestionarUsuarios(Rol.ADMIN_SISTEMA))
        assertFalse(Permisos.puedeGestionarUsuarios(Rol.JEFATURA))
        assertFalse(Permisos.puedeGestionarUsuarios(Rol.FUNCIONARIO))
    }

    @Test
    fun aprobacionSoloRrhhYJefatura() {
        assertTrue(Permisos.puedeAprobar(Rol.RRHH))
        assertTrue(Permisos.puedeAprobar(Rol.JEFATURA))
        assertFalse(Permisos.puedeAprobar(Rol.ADMINISTRATIVO))
        assertFalse(Permisos.puedeAprobar(Rol.FUNCIONARIO))
    }

    @Test
    fun resolucionSoloRrhhYJefaturaNoSensibleEnSuDependencia() {
        assertTrue(
            Permisos.puedeResolverSolicitud(
                Rol.RRHH, CategoriaDocumento.LICENCIA_MEDICA, mismaDependencia = false
            )
        )
        assertTrue(
            Permisos.puedeResolverSolicitud(
                Rol.JEFATURA, CategoriaDocumento.CONTRATO, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeResolverSolicitud(
                Rol.JEFATURA, CategoriaDocumento.CONTRATO, mismaDependencia = false
            )
        )
        assertFalse(
            Permisos.puedeResolverSolicitud(
                Rol.JEFATURA, CategoriaDocumento.LICENCIA_MEDICA, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeResolverSolicitud(
                Rol.ADMIN_SISTEMA, CategoriaDocumento.CONTRATO, mismaDependencia = true
            )
        )
        assertFalse(
            Permisos.puedeResolverSolicitud(
                Rol.FUNCIONARIO, CategoriaDocumento.CONTRATO, mismaDependencia = true
            )
        )
    }

    @Test
    fun auditoriaSoloPerfilesAutorizados() {
        assertTrue(Permisos.puedeAuditar(Rol.RRHH))
        assertTrue(Permisos.puedeAuditar(Rol.ADMIN_SISTEMA))
        assertTrue(Permisos.puedeAuditar(Rol.TI_CIBERSEGURIDAD))
        assertFalse(Permisos.puedeAuditar(Rol.FUNCIONARIO))
    }
}
