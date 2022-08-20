package com.kenzie.marketing.referral.service;

import com.kenzie.marketing.referral.model.CustomerReferrals;
import com.kenzie.marketing.referral.model.LeaderboardEntry;
import com.kenzie.marketing.referral.model.Referral;
import com.kenzie.marketing.referral.model.ReferralRequest;
import com.kenzie.marketing.referral.model.ReferralResponse;
import com.kenzie.marketing.referral.service.converter.ReferralConverter;
import com.kenzie.marketing.referral.service.dao.ReferralDao;
import com.kenzie.marketing.referral.service.exceptions.InvalidDataException;
import com.kenzie.marketing.referral.service.model.ReferralRecord;
import org.w3c.dom.Node;

import javax.inject.Inject;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ReferralService {

    private ReferralDao referralDao;
    private ExecutorService executor;

    @Inject
    public ReferralService(ReferralDao referralDao) {
        this.referralDao = referralDao;
        this.executor = Executors.newCachedThreadPool();
    }

    // Necessary for testing, do not delete
    public ReferralService(ReferralDao referralDao, ExecutorService executor) {
        this.referralDao = referralDao;
        this.executor = executor;
    }

    public List<LeaderboardEntry> getReferralLeaderboard() {
        // Task 3 Code Here
        List<ReferralRecord> roots = referralDao.findUsersWithoutReferrerId();
        List<LeaderboardEntry> permTopFive = new ArrayList<>();
        for (ReferralRecord root: roots) {
            List<LeaderboardEntry> treeTopFive = findTopFive(root);
            permTopFive.addAll(treeTopFive);
            permTopFive = permTopFive.stream()
                    .sorted(Comparator.comparingInt(LeaderboardEntry::getNumReferrals).reversed())
                    .limit(5)
                    .collect(Collectors.toList());
        }
        return permTopFive;
    }

    private List<LeaderboardEntry> findTopFive (ReferralRecord root) {
        Stack<Referral> rootsStack = new Stack<>();
        List<LeaderboardEntry> referrals1 = new ArrayList<>();
        Referral rootReferral = new Referral();
        rootReferral.setCustomerId(root.getCustomerId());
        Referral currentNode = rootReferral;
        rootsStack.push(rootReferral);

        while(!rootsStack.isEmpty()) {
            currentNode = rootsStack.pop();
            List<Referral> children = getDirectReferrals(currentNode.getCustomerId());

            //check for max
            LeaderboardEntry tempMax = new LeaderboardEntry();
            tempMax.setCustomerId(currentNode.getCustomerId());
            tempMax.setNumReferrals(children.size());
            referrals1.add(tempMax);

            if(children != null) {
                rootsStack.addAll(children);
            }
        }
        return referrals1.stream()
                .sorted(Comparator.comparingInt(LeaderboardEntry::getNumReferrals).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }



    public CustomerReferrals getCustomerReferralSummary(String customerId) {
        CustomerReferrals referrals = new CustomerReferrals();

        // Task 2 Code Here
        List<Referral> referrals1 = getDirectReferrals(customerId);
        List<Referral> referrals2 = new ArrayList<>();
        List<Referral> referrals3 = new ArrayList<>();
        for (Referral referral: referrals1) {
            referrals2.addAll(getDirectReferrals(referral.getCustomerId()));
        }

        for (Referral referral: referrals2) {
            referrals3.addAll(getDirectReferrals(referral.getCustomerId()));
        }

        referrals.setNumFirstLevelReferrals(referrals1.size());
        referrals.setNumSecondLevelReferrals(referrals2.size());
        referrals.setNumThirdLevelReferrals(referrals3.size());


        return referrals;
    }


    public List<Referral> getDirectReferrals(String customerId) {
        List<ReferralRecord> records = referralDao.findByReferrerId(customerId);

        // Task 1 Code Here
        List<Referral> referrals = records.stream()
                .map(record -> ReferralConverter.fromRecordToReferral(record))
                .collect(Collectors.toList());

        return referrals;
    }


    public ReferralResponse addReferral(ReferralRequest referral) {
        if (referral == null || referral.getCustomerId() == null || referral.getCustomerId().length() == 0) {
            throw new InvalidDataException("Request must contain a valid Customer ID");
        }
        ReferralRecord record = ReferralConverter.fromRequestToRecord(referral);
        referralDao.addReferral(record);
        return ReferralConverter.fromRecordToResponse(record);
    }
}
