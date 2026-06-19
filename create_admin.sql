-- Banking System - Admin User Setup
-- Author: Sayandwip

-- Create Admin User Account
INSERT INTO users (first_name, last_name, other_name, gender, address, state_of_origin, account_number, account_balance, email, password, phone_number, alternative_phone_number, status, role, created_at, modified_at)
VALUES (
  'Admin',
  'User',
  'Banking',
  'Male',
  '123 Bank Street',
  'Admin State',
  'ADMIN0001',
  99999999.99,
  'admin@bank.com',
  '$2a$10$S2d/.q5q6v4xvxNkZqKV5eWfPr.LHz6K9p8fj.sF0TzrUQaD9kT3e',
  '1234567890',
  '9876543210',
  'ACTIVE',
  'ROLE_ADMIN',
  NOW(),
  NOW()
);