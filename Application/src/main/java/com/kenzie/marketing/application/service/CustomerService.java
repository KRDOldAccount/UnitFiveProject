package com.kenzie.marketing.application.service;

import com.kenzie.marketing.application.controller.model.CreateCustomerRequest;
import com.kenzie.marketing.application.controller.model.CustomerResponse;
import com.kenzie.marketing.application.controller.model.LeaderboardUiEntry;
import com.kenzie.marketing.application.repositories.CustomerRepository;
import com.kenzie.marketing.application.repositories.model.CustomerRecord;
import com.kenzie.marketing.referral.model.CustomerReferrals;
import com.kenzie.marketing.referral.model.LeaderboardEntry;
import com.kenzie.marketing.referral.model.Referral;
import com.kenzie.marketing.referral.model.ReferralRequest;
import com.kenzie.marketing.referral.model.client.ReferralServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.UUID.randomUUID;

@Service
public class CustomerService {
    private static final Double REFERRAL_BONUS_FIRST_LEVEL = 10.0;
    private static final Double REFERRAL_BONUS_SECOND_LEVEL = 3.0;
    private static final Double REFERRAL_BONUS_THIRD_LEVEL = 1.0;

    private CustomerRepository customerRepository;
    private ReferralServiceClient referralServiceClient;

    public CustomerService(CustomerRepository customerRepository, ReferralServiceClient referralServiceClient) {
        this.customerRepository = customerRepository;
        this.referralServiceClient = referralServiceClient;
    }

    /**
     * findAllCustomers
     * @return A list of Customers
     */
    public List<CustomerResponse> findAllCustomers() {
        List<CustomerRecord> records = StreamSupport
                .stream(customerRepository.findAll().spliterator(), true)
                .collect(Collectors.toList());

        // Task 1 - Add your code here
        return records.stream()
                .map(record -> toCustomerResponse(record))
                .collect(Collectors.toList());

    }

    /**
     * findByCustomerId
     * @param customerId
     * @return The Customer with the given customerId
     */
    public CustomerResponse getCustomer(String customerId) {
        Optional<CustomerRecord> record = customerRepository.findById(customerId);
        CustomerResponse response = null;
        // Task 1 - Add your code here
        if(record.isPresent()) {
            response = toCustomerResponse(record.get());
        }

        return response;
    }

    /**
     * addNewCustomer
     *
     * This creates a new customer.  If the referrerId is included, the referrerId must be valid and have a
     * corresponding customer in the DB.  This posts the referrals to the referral service
     * @param createCustomerRequest
     * @return A CustomerResponse describing the customer
     */
    public CustomerResponse addNewCustomer(CreateCustomerRequest createCustomerRequest) {

        if(createCustomerRequest.getReferrerId().isPresent() && createCustomerRequest.getReferrerId().get().length() == 0) {
            createCustomerRequest.setReferrerId(Optional.empty());
        }

        if(createCustomerRequest.getReferrerId().isPresent()) {
            if(!customerRepository.existsById(createCustomerRequest.getReferrerId().get())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID does not exist");
            }
        }

        // Task 1 - Add your code here
        CustomerRecord customerRecord = new CustomerRecord();
        customerRecord.setId(randomUUID().toString());
        customerRecord.setName(createCustomerRequest.getName());
        customerRecord.setDateCreated(LocalDateTime.now().toString());
        customerRecord.setReferrerId(createCustomerRequest.getReferrerId().orElse(null));

        ReferralRequest referralRequest = new ReferralRequest();
        referralRequest.setCustomerId(customerRecord.getId());
        referralRequest.setReferrerId(customerRecord.getReferrerId());

        customerRepository.save(customerRecord);

        if(createCustomerRequest.getReferrerId().isPresent() && createCustomerRequest.getReferrerId().get().length() == 0) {
            referralServiceClient.addReferral(new ReferralRequest(customerRecord.getId(), Optional.empty().toString()));
        }

        referralServiceClient.addReferral(referralRequest);

        return toCustomerResponse(customerRecord);
    }

    /**
     * updateCustomer - This updates the customer name for the given customer id
     * @param customerId - The Id of the customer to update
     * @param customerName - The new name for the customer
     */
    public CustomerResponse updateCustomer(String customerId, String customerName) {
        Optional<CustomerRecord> customerExists = customerRepository.findById(customerId);
        if (customerExists.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer Not Found");
        }
        CustomerRecord customerRecord = customerExists.get();
        customerRecord.setName(customerName);
        customerRepository.save(customerRecord);

        // Task 1 - Add your code here

        return toCustomerResponse(customerRecord);
    }

    /**
     * deleteCustomer - This deletes the customer record for the given customer id
     * @param customerId
     */
    public void deleteCustomer(String customerId) {
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Customer Name");
        }
        customerRepository.deleteById(customerId);
    }

    /**
     * calculateBonus - This calculates the referral bonus for the given customer according to the referral bonus
     * constants.
     * @param customerId
     * @return
     */
    public Double calculateBonus(String customerId) {
        CustomerReferrals referrals = referralServiceClient.getReferralSummary(customerId);

        Double calculationResult = REFERRAL_BONUS_FIRST_LEVEL * referrals.getNumFirstLevelReferrals() +
                REFERRAL_BONUS_SECOND_LEVEL * referrals.getNumSecondLevelReferrals() +
                REFERRAL_BONUS_THIRD_LEVEL * referrals.getNumThirdLevelReferrals();

        return calculationResult;
    }

    /**
     * getReferrals - This returns a list of referral entries for every customer directly referred by the given
     * customerId.
     * @param customerId
     * @return
     */
    public List<CustomerResponse> getReferrals(String customerId) {

        // Task 1 - Add your code here

        return null;
    }

    /**
     * getLeaderboard - This calls the referral service to retrieve the current top 5 leaderboard of the most referrals
     * @return
     */
    public List<LeaderboardUiEntry> getLeaderboard() {

        // Task 2 - Add your code here

        return null;
    }

    /* -----------------------------------------------------------------------------------------------------------
        Private Methods
       ----------------------------------------------------------------------------------------------------------- */

    // Add any private methods here

    private CustomerResponse toCustomerResponse(CustomerRecord record) {
        if (record == null) {
            return null;
        }

        CustomerResponse customerResponse = new CustomerResponse();

        if(record.getReferrerId() != null && !record.getReferrerId().isEmpty()) {
            if(customerRepository.findById(record.getReferrerId()).isPresent()) {
                customerResponse.setReferrerName(customerRepository.findById(record.getReferrerId()).get().getName());
            }
        }
        customerResponse.setId(record.getId());
        customerResponse.setName(record.getName());
        customerResponse.setDateJoined(record.getDateCreated());
        customerResponse.setReferrerId(record.getReferrerId());

        return customerResponse;
    }

}
