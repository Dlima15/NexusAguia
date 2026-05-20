package br.com.fiap.gabinova.domain

import br.com.fiap.gabinova.model.UserRole

fun canCreateIdea(role: UserRole): Boolean {
    return role == UserRole.COLLABORATOR
}

fun canEvaluateIdea(role: UserRole): Boolean {
    return role == UserRole.MANAGER
}

fun canManageGuidelines(role: UserRole): Boolean {
    return role == UserRole.ADMIN
}

fun canViewDashboard(role: UserRole): Boolean {
    return role == UserRole.ADMIN || role == UserRole.ANALYST
}

