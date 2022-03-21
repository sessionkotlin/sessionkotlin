package org.david.sessionkotlin_lib.dsl.exception

class RecursiveProtocolException :
    SessionKotlinException("Cannot define instructions after a recursive call.")
