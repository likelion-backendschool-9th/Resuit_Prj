package com.newlecture.resuit.dto;

// balance: 보유 크레딧, withdrawable: 그중 현금으로 환전할 수 있는 크레딧(피드백으로 적립한 몫)
public record CreditBalanceResponse(int balance, int withdrawable) {
}
