package org.david.sessionkotlin_lib.dsl.exception

class BranchingException :
    SessionKotlinException("Branching must be the final operation in a protocol.")
