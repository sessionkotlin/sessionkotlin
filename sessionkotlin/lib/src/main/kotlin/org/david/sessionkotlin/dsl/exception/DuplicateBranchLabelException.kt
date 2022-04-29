package org.david.sessionkotlin.dsl.exception

public class DuplicateBranchLabelException(label: String) :
    SessionKotlinDSLException("Branch label $label is not unique.")
