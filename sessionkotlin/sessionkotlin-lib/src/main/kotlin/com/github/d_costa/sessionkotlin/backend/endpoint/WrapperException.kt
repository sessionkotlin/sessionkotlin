package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.backend.SessionKotlinBackendException

public class WrapperException : SessionKotlinBackendException("Only socket connections can be wrapped.")
