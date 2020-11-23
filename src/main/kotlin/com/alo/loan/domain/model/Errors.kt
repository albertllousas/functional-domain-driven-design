package com.alo.loan.domain.model

import com.alo.loan.domain.model.evaluation.CustomerId

sealed class Error

data class CustomerNotFound(val customerId: CustomerId) : Error()
