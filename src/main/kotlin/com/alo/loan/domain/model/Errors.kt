package com.alo.loan.domain.model

sealed class Error

data class CustomerNotFound(val customerId: CustomerId) : Error()
