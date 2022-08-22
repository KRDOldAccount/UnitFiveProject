package com.kenzie.marketing.referral.service.caching;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kenzie.marketing.referral.service.dao.NonCachingReferralDao;
import com.kenzie.marketing.referral.service.dao.ReferralDao;
import com.kenzie.marketing.referral.service.model.ReferralRecord;

import javax.inject.Inject;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class CachingReferralDao implements ReferralDao {

    private static final int REFERRAL_READ_TTL = 60 * 60;
    private static final String REFERRAL_KEY = "ReferralKey::%s";
    private final CacheClient cacheClient;
    private final NonCachingReferralDao referralDao;

    @Inject
    public CachingReferralDao(CacheClient cacheClient, NonCachingReferralDao referralDao) {
        this.cacheClient = cacheClient;
        this.referralDao = referralDao;
    }

    // Create the Gson object with instructions for ZonedDateTime
    GsonBuilder builder = new GsonBuilder().registerTypeAdapter(
            ZonedDateTime.class,
            new TypeAdapter<ZonedDateTime>() {
                @Override
                public void write(JsonWriter out, ZonedDateTime value) throws IOException {
                    out.value(value.toString());
                }
                @Override
                public ZonedDateTime read(JsonReader in) throws IOException {
                    return ZonedDateTime.parse(in.nextString());
                }
            }
    ).enableComplexMapKeySerialization();
    // Store this in your class
    Gson gson = builder.create();

    // Converting out of the cache
    private List<ReferralRecord> fromJson(String json) {
        return gson.fromJson(json, new TypeToken<ArrayList<ReferralRecord>>() { }.getType());
    }

    // Setting value
    private void addToCache(List<ReferralRecord> records) {
        for (ReferralRecord record: records) {
            cacheClient.setValue(
                    /* your implementation for cache key */
                    String.format(REFERRAL_KEY, record.getCustomerId()),
                    REFERRAL_READ_TTL,
                    gson.toJson(records)
            );
        }
    }

    @Override
    public ReferralRecord addReferral(ReferralRecord referral) {
        // Invalidate
        // Add referral to database
//        public ReadingLog updateReadingProgress(String userId, String isbn, ZonedDateTime timestamp, int pageNumber, boolean isFinished) {
//            String key = "books-read::" + userId + "::" + ZonedDateTime.now().getYear();
//            ReadingLog readingLog = nonCachingReadingLogDao.updateReadingProgress(userId, isbn, timestamp, pageNumber, isFinished);
//
//            if (isFinished) {
//                cacheClient.invalidate(key);
//            }
//            return readingLog;
//        }
//            String key = "ReferralKey::" + referral.getCustomerId();
            cacheClient.invalidate(referral.getReferrerId());
            referralDao.addReferral(referral);

            return referral;
    }

    @Override
    public List<ReferralRecord> findByReferrerId(String referrerId) {
        // Look up data in cache
        // Convert between JSON
        // If the data doesn't exist in the cache,
        // Get the data from the data source
        // Add data to the cache, convert between JSON
//        public int getBooksReadInYear(String userId, int year) {
//            String key = "books-read::" + userId + "::" + year;
//
//            if (cacheClient.getValue(key) == null) {
//                int booksRead = nonCachingReadingLogDao.getBooksReadInYear(userId, year);
//                cacheClient.setValue(key, 60 * 60, String.valueOf(year));
//                return booksRead;
//            }
//
//            return Integer.parseInt(cacheClient.getValue(key));
//        }
        List<ReferralRecord> referralRecordList = new ArrayList<>();

        if (cacheClient.getValue(referrerId) == null) {
            addToCache(referralDao.findByReferrerId(referrerId));
            return referralDao.findByReferrerId(referrerId);
        }
        return fromJson(String.valueOf(cacheClient.getValue(referrerId)));
    }

    @Override
    public List<ReferralRecord> findUsersWithoutReferrerId() {
        // Look up customer from the data source
        return referralDao.findUsersWithoutReferrerId();
    }
}
