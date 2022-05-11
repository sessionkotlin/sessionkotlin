package com.github.d_costa.sessionkotlin.dsl.types

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole

internal class ProjectionState(
    private val projectedRole: SKRole,
    /**
     * Collection of message labels that the projected role knows about
     */
    val names: MutableSet<String> = mutableSetOf(),

    /**
     * Label for the current branch
     */
    var branchLabel: String? = null,

    /**
     * The roles that received a message in the branch
     */
    var activeRoles: MutableSet<SKRole> = mutableSetOf(),

    /**
     * List of recursion tags that correspond to unguarded recursions.
     *
     * Tag presence in this list after projection means that
     * the correspondent recursion is unguarded.
     *
     */
    var unguardedRecursions: MutableSet<RecursionTag> = mutableSetOf()
) {

    /**
     * Whether the projected role sent a message, made choice or had recursion while not knowing
     * the outcome of a choice
     */
    var sentWhileDisabled: Boolean = false

    /**
     * The role that enabled the projected role (i.e. the sender of the first message received
     * in a branch)
     */
    var enabledBy: SKRole? = null

    /**
     * Returns whether the [projectedRole] is enabled.
     */
    fun enabled() = enabledBy != null && enabledBy != projectedRole

    fun copy(
        projectedRole: SKRole = this.projectedRole,
        activeRoles: MutableSet<SKRole> = this.activeRoles.toMutableSet(),
        branchLabel: String? = this.branchLabel,
        names: MutableSet<String> = this.names.toMutableSet(),
        unguardedRecursions: MutableSet<RecursionTag> = this.unguardedRecursions,
    ): ProjectionState = ProjectionState(projectedRole, names, branchLabel, activeRoles, unguardedRecursions)
}
