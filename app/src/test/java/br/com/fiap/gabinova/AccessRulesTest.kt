package br.com.fiap.gabinova

import br.com.fiap.gabinova.domain.canCreateIdea
import br.com.fiap.gabinova.domain.canEvaluateIdea
import br.com.fiap.gabinova.domain.canManageGuidelines
import br.com.fiap.gabinova.domain.canViewDashboard
import br.com.fiap.gabinova.model.UserRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessRulesTest {

    @Test
    fun operadorPodeCriarIdeia() {

        val role = UserRole.COLLABORATOR

        assertTrue(
            canCreateIdea(role)
        )
    }

    @Test
    fun operadorNaoPodeAvaliarIdeias() {

        val role = UserRole.COLLABORATOR

        assertFalse(
            canEvaluateIdea(role)
        )
    }

    @Test
    fun gestorPodeAvaliarIdeias() {

        val role = UserRole.MANAGER

        assertTrue(
            canEvaluateIdea(role)
        )
    }

    @Test
    fun gestorNaoPodeGerenciarDiretrizes() {

        val role = UserRole.MANAGER

        assertFalse(
            canManageGuidelines(role)
        )
    }

    @Test
    fun liderancaPodeGerenciarDiretrizes() {

        val role = UserRole.ADMIN

        assertTrue(
            canManageGuidelines(role)
        )
    }

    @Test
    fun operadorNaoPodeVerDashboardExecutivo() {

        val role = UserRole.COLLABORATOR

        assertFalse(
            canViewDashboard(role)
        )
    }
}