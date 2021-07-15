package com.example1.accounts

import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
data class OpenAccountRequest(
    @field:NotNull
    val id: UUID,
    val bonusCredit: Double? = 0.00
)

@Introspected
data class DepositMoneyRequest(
    @field:[NotNull Positive]
    val amount: Double
)

@Introspected
data class WithdrawMoneyRequest(
    @field:[NotNull Positive]
    val amount: Double
)

@Introspected
data class CloseAccountRequest(
    @field:NotBlank
    val closingReason: String
)
