package com.alo.loan.infrastructure.adapters.outgoing.client

import com.alo.loan.domain.model.GetLoanRecords
import com.alo.loan.domain.model.evaluation.CustomerId
import com.alo.loan.domain.model.evaluation.LoanRecord

// fake http client, just for the sake of the demo
class InMemoryLoanRecordsFakeHttpClient(
    private val staticGetLoanRecordsAnswer: List<LoanRecord>
) : GetLoanRecords {
    override fun invoke(customerId: CustomerId): List<LoanRecord> = staticGetLoanRecordsAnswer
}
