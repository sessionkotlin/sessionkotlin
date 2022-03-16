package sessionkotlin.dsl.exception

class RecursiveProtocolException :
    SessionKotlinException("Cannot define instructions after a recursive call.")
