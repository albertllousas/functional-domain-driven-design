package com.alo.loan.fixtures

import com.alo.loan.domain.model.AmountToLend
import com.github.javafaker.Faker

private val faker = Faker()

fun buildAmountToLend() = AmountToLend(amount = faker.number().randomNumber().toBigDecimal())
