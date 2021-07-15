package com.example1.accounts

import com.example1.accounts.AccountCommand.CloseAccount
import com.example1.accounts.AccountCommand.DepositMoney
import com.example1.accounts.AccountCommand.OpenAccount
import com.example1.accounts.AccountCommand.WithdrawMoney
import com.example1.accounts.AccountEvent.AccountClosed
import com.example1.accounts.AccountEvent.AccountOpened
import com.example1.accounts.AccountEvent.MoneyDeposited
import com.example1.accounts.AccountEvent.MoneyWithdrawn
import io.github.crabzilla.core.Command
import io.github.crabzilla.core.CommandHandler
import io.github.crabzilla.core.CommandHandlerApi.ConstructorResult
import io.github.crabzilla.core.DomainEvent
import io.github.crabzilla.core.DomainState
import io.github.crabzilla.core.EventHandler
import io.github.crabzilla.core.Snapshot
import io.github.crabzilla.core.StatefulSession
import io.github.crabzilla.core.javaModule
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.util.UUID

/**
 * Account events
 */
@Serializable
sealed class AccountEvent : DomainEvent() {
  @Serializable
  @SerialName("AccountOpened")
  data class AccountOpened(@Contextual val id: UUID, val bonusCredit: Double? = 0.00) : AccountEvent()

  @Serializable
  @SerialName("MoneyDeposited")
  data class MoneyDeposited(val amount: Double) : AccountEvent()

  @Serializable
  @SerialName("MoneyWithdrawn")
  data class MoneyWithdrawn(val amount: Double) : AccountEvent()

  @Serializable
  @SerialName("AccountClosed")
  data class AccountClosed(val reason: String) : AccountEvent()
}

/**
 * Account commands
 */
@Serializable
sealed class AccountCommand : Command() {
  @Serializable
  @SerialName("OpenAccount")
  data class OpenAccount(@Contextual val id: UUID)
    : AccountCommand()

  @Serializable
  @SerialName("DepositMoney")
  data class DepositMoney(val amount: Double) : AccountCommand()

  @Serializable
  @SerialName("WithdrawMoney")
  data class WithdrawMoney(val amount: Double) : AccountCommand()

  @Serializable
  @SerialName("CloseAccount")
  data class CloseAccount(val closingReason: String) : AccountCommand()
}

/**
 * Account aggregate root
 */
@Serializable
@SerialName("Account")
data class Account(@Contextual val id: UUID,
                   val balance: Double = 0.00,
                   val closingReason: String? = null) : DomainState() {

  companion object {
    /**
     * Open an account giving a bonus credit
     */
    fun open(id: UUID, bonusCredit: Double): ConstructorResult<Account, AccountEvent> {
      return if (bonusCredit == 0.00) {
        ConstructorResult(
          state = Account(id = id),
          events = arrayOf(
            AccountOpened(id = id))
        )
      } else {
        ConstructorResult(
          state = Account(id = id),
          events = arrayOf(
            AccountOpened(id = id, bonusCredit = bonusCredit),
            MoneyDeposited(amount = bonusCredit)))
      }
    }
  }

  fun deposit(amount: Double): List<AccountEvent> {
    if (closingReason == null) {
      return listOf(MoneyDeposited(amount))
    } else {
      throw AccountIsClosed(id)
    }
  }

  fun withdraw(amount: Double): List<AccountEvent> {
    if (closingReason == null) {
      return if (balance >= amount) {
        listOf(MoneyWithdrawn(amount))
      } else {
        throw AccountBalanceNotEnough(id)
      }
    } else {
      throw AccountIsClosed(id)
    }
  }

  fun close(reason: String): List<AccountEvent> {
    if (closingReason == null) {
      if (balance == 0.00) {
        return listOf(AccountClosed(reason))
      } else {
        throw AccountStillHaveBalance(id)
      }
    } else {
      throw AccountIsClosed(id)
    }
  }

}

/**
 * This function will apply an event to user state
 */
val acctEventHandler = EventHandler<Account, AccountEvent> { state, event ->
  when (event) {
    is AccountOpened -> Account.open(id = event.id, bonusCredit = event.bonusCredit?: 0.00).state
    is MoneyDeposited -> state!!.copy(balance = state.balance + event.amount)
    is MoneyWithdrawn -> state!!.copy(balance = state.balance - event.amount)
    is AccountClosed -> state!!.copy(closingReason = event.reason)

  }
}

/**
 * Account errors
 */
class AccountAlreadyExists(id: UUID) : IllegalStateException("Account $id already exists")
class AccountBalanceNotEnough(id: UUID) : IllegalStateException("Account $id doesn't have enough balance")
class AccountIsClosed(id: UUID) : IllegalStateException("Account $id is closed")
class AccountStillHaveBalance(id: UUID) : IllegalStateException("Account $id still have some balance")

/**
 * Account command handler
 */
class AccountCommandHandler : CommandHandler<Account, AccountCommand, AccountEvent> {

  override fun handleCommand(
    command: AccountCommand,
    eventHandler: EventHandler<Account, AccountEvent>,
    snapshot: Snapshot<Account>?
    )
  : StatefulSession<Account, AccountEvent> {

    return when (command) {
      is OpenAccount -> {
        if (snapshot == null)
          withNew(Account.open(command.id, bonusCredit = 0.00), acctEventHandler)
        else throw AccountAlreadyExists(command.id)
      }
      is DepositMoney -> with(snapshot!!, acctEventHandler).execute { it.deposit(command.amount) }
      is WithdrawMoney -> with(snapshot!!, acctEventHandler).execute { it.withdraw(command.amount) }
      is CloseAccount -> with(snapshot!!, acctEventHandler).execute { it.close(command.closingReason) }
    }
  }
}

/**
 * kotlinx.serialization
 */
@kotlinx.serialization.ExperimentalSerializationApi
val acctModule = SerializersModule {
  include(javaModule)
  polymorphic(DomainState::class) {
    subclass(Account::class, Account.serializer())
  }
  polymorphic(Command::class) {
    subclass(OpenAccount::class, OpenAccount.serializer())
    subclass(DepositMoney::class, DepositMoney.serializer())
    subclass(WithdrawMoney::class, WithdrawMoney.serializer())
    subclass(CloseAccount::class, CloseAccount.serializer())
  }
  polymorphic(DomainEvent::class) {
    subclass(AccountOpened::class, AccountOpened.serializer())
    subclass(MoneyDeposited::class, MoneyDeposited.serializer())
    subclass(MoneyWithdrawn::class, MoneyWithdrawn.serializer())
    subclass(AccountClosed::class, AccountClosed.serializer())
  }
}

val acctJson = Json { serializersModule = acctModule }