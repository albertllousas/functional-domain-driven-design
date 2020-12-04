package com.alo.loan.domain.model.loan

import com.alo.loan.domain.model.LoanApplication
import com.alo.loan.domain.model.LoanApplicationApproved
import com.alo.loan.domain.model.LoanApplicationHeldForFurtherVerification
import com.alo.loan.domain.model.LoanApplicationRejected
import com.alo.loan.domain.model.createEvents
import com.alo.loan.fixtures.buildApprovedLoan
import com.alo.loan.fixtures.buildFurtherVerificationNeededLoan
import com.alo.loan.fixtures.buildRejectedLoan
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateEventsShould {

    private val createEvents = LoanApplication.Companion::createEvents

    @Test
    fun `create an approved loan event when loan evaluation is approved`() {
        val approvedLoan = buildApprovedLoan()
        assertThat(createEvents(approvedLoan)).isEqualTo(listOf(LoanApplicationApproved(approvedLoan.id.value)))
    }

    @Test
    fun `create a rejected loan event when loan evaluation is approved`() {
        val rejectedLoan = buildRejectedLoan()
        assertThat(createEvents(rejectedLoan))
            .isEqualTo(listOf(LoanApplicationRejected(rejectedLoan.id.value)))
    }

    @Test
    fun `create held for further verification loan event when loan evaluation needs further verification`() {
        val furtherVerificationNeededLoan = buildFurtherVerificationNeededLoan()
        assertThat(createEvents(furtherVerificationNeededLoan))
            .isEqualTo(listOf(LoanApplicationHeldForFurtherVerification(furtherVerificationNeededLoan.id.value)))
    }
}
