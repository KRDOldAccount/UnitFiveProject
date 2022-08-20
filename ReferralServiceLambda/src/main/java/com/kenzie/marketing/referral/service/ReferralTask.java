package com.kenzie.marketing.referral.service;

import com.kenzie.marketing.referral.model.LeaderboardEntry;
import com.kenzie.marketing.referral.model.Referral;
import com.kenzie.marketing.referral.service.dao.ReferralDao;
import com.kenzie.marketing.referral.service.model.ReferralRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ReferralTask implements Callable<List<LeaderboardEntry>> {

    private ReferralDao referralDao;
    private ReferralService referralService;
    private ReferralRecord root;

    public ReferralTask(ReferralDao refDao, ReferralService refServ, ReferralRecord root) {
        this.referralDao = refDao;
        this.referralService = refServ;
        this.root = root;
    }

    @Override
    public List<LeaderboardEntry> call() throws Exception {
            Stack<Referral> rootsStack = new Stack<>();
            List<LeaderboardEntry> referrals1 = new ArrayList<>();
            Referral rootReferral = new Referral();
            rootReferral.setCustomerId(root.getCustomerId());
            Referral currentNode = rootReferral;
            rootsStack.push(rootReferral);

            while(!rootsStack.isEmpty()) {
                currentNode = rootsStack.pop();
                List<Referral> children = referralService.getDirectReferrals(currentNode.getCustomerId());

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
}
