package com.example1.accounts

fun OpenAccountRequest.toCommand() = AccountCommand.OpenAccount(
    id,
    bonusCredit
)

fun DepositMoneyRequest.toCommand() = AccountCommand.DepositMoney(
    amount
)

fun WithdrawMoneyRequest.toCommand() = AccountCommand.WithdrawMoney(
    amount
)

fun CloseAccountRequest.toCommand() = AccountCommand.CloseAccount(
    closingReason
)