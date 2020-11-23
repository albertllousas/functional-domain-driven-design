package com.alo.loan.fixtures

import com.alo.loan.domain.model.evaluation.Customer
import com.alo.loan.domain.model.evaluation.CustomerId
import com.github.javafaker.Faker
import java.math.BigDecimal
import java.util.UUID

private val faker = Faker()

fun buildCustomer(
    customerId: CustomerId = CustomerId(UUID.randomUUID()),
    fullName: String = faker.name().fullName(),
    address: String = faker.address().fullAddress(),
    age: Int = faker.number().numberBetween(18, 80),
    annualIncomes: BigDecimal = faker.number().randomNumber().toBigDecimal()
) = Customer(customerId, fullName, address, age, annualIncomes)
