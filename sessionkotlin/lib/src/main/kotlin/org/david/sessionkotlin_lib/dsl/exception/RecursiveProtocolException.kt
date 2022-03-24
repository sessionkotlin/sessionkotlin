package org.david.sessionkotlin_lib.dsl.exception

class RecursiveProtocolException :
    SessionKotlinException("Recursive call must be a final operation in a protocol.")
