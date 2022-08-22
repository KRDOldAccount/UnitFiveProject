package com.kenzie.marketing.referral.service.dao;

import com.kenzie.marketing.referral.service.model.ReferralRecord;

import java.util.List;

public interface ReferralDao {
    ReferralRecord addReferral(ReferralRecord referral);
    List<ReferralRecord> findByReferrerId(String referrerId);
    List<ReferralRecord> findUsersWithoutReferrerId();
}