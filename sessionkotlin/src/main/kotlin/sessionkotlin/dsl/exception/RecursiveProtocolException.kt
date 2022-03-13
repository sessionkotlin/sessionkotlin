package sessionkotlin.dsl.exception

import sessionkotlin.dsl.Role

class RecursiveProtocolException():
    SessionKotlinException("Cannot define instructions after a recursive call.")
