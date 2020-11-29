package com.alo.loan.infrastructure.adapters.outgoing.database

import com.alo.loan.domain.model.GetLoanRecords
import com.alo.loan.domain.model.evaluation.CustomerId
import com.alo.loan.domain.model.evaluation.LoanRecord

// fake database replicated data of the customer records, just for the sake of the demo
class InMemoryFakeReplicatedLoanRecordRepository(
    private val staticGetLoanRecordsAnswer: List<LoanRecord>
) : GetLoanRecords {
    override fun invoke(customerId: CustomerId): List<LoanRecord> = staticGetLoanRecordsAnswer
}
