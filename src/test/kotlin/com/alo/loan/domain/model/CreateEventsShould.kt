package com.alo.loan.domain.model

import com.alo.loan.fixtures.buildApprovedLoan
import com.alo.loan.fixtures.buildFurtherVerificationNeededLoan
import com.alo.loan.fixtures.buildRejectedLoan
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateEventsShould {

    private val createEvents = LoanEvaluation.Behaviour.createEvents

    @Test
    fun `create an approved loan event when loan evaluation is approved`() {
        val approvedLoan = buildApprovedLoan()
        assertThat(createEvents(approvedLoan)).isEqualTo(listOf(LoanApproved(approvedLoan.id.value)))
    }

    @Test
    fun `create a rejected loan event when loan evaluation is approved`() {
        val rejectedLoan = buildRejectedLoan()
        assertThat(createEvents(rejectedLoan))
            .isEqualTo(listOf(LoanRejected(rejectedLoan.id.value, rejectedLoan.reasons)))
    }

    @Test
    fun `create held for further verification loan event when loan evaluation needs further verification`() {
        val furtherVerificationNeededLoan = buildFurtherVerificationNeededLoan()
        assertThat(createEvents(furtherVerificationNeededLoan))
            .isEqualTo(listOf(LoanHeldForFurtherVerification(furtherVerificationNeededLoan.id.value)))
    }
}
