package com.example1.accounts.controllers

import com.example1.accounts.WithdrawMoneyRequest
import com.example1.accounts.toCommand
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.reactivex.Single
import javax.validation.Valid

@Controller("/api/v1/accounts/withdraw")
open class WithdrawAccountController {

    @Post
    open fun open(@Valid @Body request: WithdrawMoneyRequest): Single<HttpResponse<String>> {
        val command = request.toCommand()
//        val metadata = CommandMetadata(AggregateRootId(command.id))
        return Single.create {
            it.onSuccess(HttpResponse.noContent())
        }
    }


}