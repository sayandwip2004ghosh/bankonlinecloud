package com.sayandwip.utils;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

public class AccountUtils {

    public static final String ACCOUNT_EXISTS_CODE    = "001";
    public static final String ACCOUNT_EXISTS_MESSAGE = "This user already has an account!";

    public static final String ACCOUNT_CREATION_SUCCESS = "002";
    public static final String ACCOUNT_CREATION_MESSAGE = "Account created successfully!";

    public static final String ACCOUNT_NOT_EXIST_CODE    = "003";
    public static final String ACCOUNT_NOT_EXIST_MESSAGE = "Account not found";

    public static final String ACCOUNT_FOUND_CODE    = "004";
    public static final String ACCOUNT_FOUND_SUCCESS = "User Account Found";

    public static final String ACCOUNT_CREDITED_SUCCESS         = "005";
    public static final String ACCOUNT_CREDITED_SUCCESS_MESSAGE = "User Account Credited";

    public static final String INSUFFICIENT_BALANCE_CODE    = "006";
    public static final String INSUFFICIENT_BALANCE_MESSAGE = "Insufficient Balance";

    public static final String ACCOUNT_DEBITED_SUCCESS  = "007";
    public static final String ACCOUNT_DEBITED_MESSAGE  = "Account debited successfully";

    public static final String TRANSFER_SUCCESSFUL_CODE    = "008";
    public static final String TRANSFER_SUCCESSFUL_MESSAGE = "Transfer Successful";

    // FIX: accepts a duplicate-checker so callers can retry until unique
    // Usage: AccountUtils.generateAccountNumber(userRepository::existsByAccountNumber)
    public static String generateAccountNumber(java.util.function.Predicate<String> existsCheck) {
        String year = String.valueOf(Year.now().getValue());
        String number;
        int attempts = 0;
        do {
            int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
            number = year + random;
            attempts++;
            if (attempts > 100) {
                throw new IllegalStateException("Could not generate a unique account number after 100 attempts");
            }
        } while (existsCheck.test(number));
        return number;
    }
}
