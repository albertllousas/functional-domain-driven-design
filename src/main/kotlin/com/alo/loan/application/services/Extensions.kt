package com.alo.loan.application.services

import arrow.core.Either

fun <L,R> Either<L, R>.peek(consume: (R) -> Unit): Either<L,R> = this.map(consume).let { this }
