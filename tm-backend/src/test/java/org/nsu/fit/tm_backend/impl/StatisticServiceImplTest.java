package org.nsu.fit.tm_backend.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.service.SubscriptionService;
import org.nsu.fit.tm_backend.service.data.StatisticBO;
import org.nsu.fit.tm_backend.service.data.StatisticPerCustomerBO;
import org.nsu.fit.tm_backend.service.impl.CustomerServiceImpl;
import org.nsu.fit.tm_backend.service.impl.StatisticServiceImpl;
import org.nsu.fit.tm_backend.service.impl.SubscriptionServiceImpl;

import java.util.*;

// Лабораторная 2: покрыть unit тестами класс StatisticServiceImpl на 100%.
// Чтобы протестировать метод calculate() используйте Mockito.spy(statisticService) и переопределите метод
// calculate(UUID customerId) чтобы использовать стратегию "разделяй и властвуй".
@ExtendWith(MockitoExtension.class)
public class StatisticServiceImplTest {
    @Mock
    private Repository repository;

    @InjectMocks
    private StatisticServiceImpl statisticService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private CustomerServiceImpl customerService;
    @Test
    void testCalculateWithId(){
        UUID customerUuid = UUID.randomUUID();
        Set<UUID> subscriptionUuids=new HashSet<UUID>();
        UUID subscriptionUuid = new UUID(0,0);
        List<SubscriptionPojo>subscriptionPojos= new ArrayList<>();
            CustomerPojo customerPojo=new CustomerPojo();
            customerPojo.id=customerUuid;
            customerPojo.firstName = "John";
            customerPojo.lastName = "Wick";
            customerPojo.login = "john_wick@example.com";
            customerPojo.pass = "1234567";
            customerPojo.balance = 20;
        for(int i=0;i<3;i++){
            SubscriptionPojo subscriptionPojo = new SubscriptionPojo();
            subscriptionPojo.planName="test";
            subscriptionPojo.planDetails="test";
            subscriptionUuid = UUID.randomUUID();
            subscriptionPojo.id=subscriptionUuid;
            subscriptionPojo.planFee=10;
            subscriptionUuids.add(subscriptionUuid);
            subscriptionPojo.customerId=customerUuid;
            subscriptionPojos.add(subscriptionPojo);
        }
        Mockito.when(customerService.lookupCustomer(customerUuid)).thenAnswer(
                (Answer) invocation -> {
                    Object[] args = invocation.getArguments();
                    Object mock = invocation.getMock();
                    return customerPojo;
                }
        );
        Mockito.when(subscriptionService.getSubscriptions(customerUuid)).thenReturn(subscriptionPojos);
        StatisticPerCustomerBO statisticPerCustomerBO = new StatisticPerCustomerBO();
        statisticPerCustomerBO.setOverallBalance(20);
        statisticPerCustomerBO.setOverallFee(30);
        statisticPerCustomerBO.setSubscriptionIds(subscriptionUuids);
        Assertions.assertEquals(statisticService.calculate(customerPojo.id),statisticPerCustomerBO);
    }

    @Test
    void testCalculateWithIdWhereCustomerIsNull(){
        UUID customerUuid = UUID.randomUUID();
        CustomerPojo customerPojo=new CustomerPojo();
        customerPojo.id=customerUuid;
        Mockito.when(customerService.lookupCustomer(customerUuid)).thenAnswer(
                (Answer) invocation -> {
                    Object[] args = invocation.getArguments();
                    Object mock = invocation.getMock();
                    return null;
                }
        );
        StatisticPerCustomerBO statisticPerCustomerBO=null;
        Assertions.assertEquals(statisticService.calculate(customerPojo.id),statisticPerCustomerBO);
    }
    @Test
    void TestCalculateWithoutId(){
        List<UUID> listCustomerUuids=new ArrayList<UUID>();
        Set<UUID> setCustomerUuids=new HashSet<UUID>();
        Set<UUID> subscriptionUuids=new HashSet<UUID>();
        List<SubscriptionPojo>subscriptionPojos= new ArrayList<>();
        UUID subscriptionUuid=null;
            UUID customerUuid = UUID.randomUUID();
            listCustomerUuids.add(customerUuid);
            setCustomerUuids.add(customerUuid);
            CustomerPojo customerPojo = new CustomerPojo();
            customerPojo.id = customerUuid;
            customerPojo.firstName = "John";
            customerPojo.lastName = "Wick";
            customerPojo.login = "john_wick@example.com";
            customerPojo.pass = "1234567";
            customerPojo.balance = 20;
        for(int i=0;i<3;i++){
            SubscriptionPojo subscriptionPojo = new SubscriptionPojo();
            subscriptionPojo.planName="test";
            subscriptionPojo.planDetails="test";
            subscriptionUuid = UUID.randomUUID();
            subscriptionPojo.id=subscriptionUuid;
            subscriptionPojo.planFee=10;
            subscriptionUuids.add(subscriptionUuid);
            subscriptionPojo.customerId=listCustomerUuids.get(0);
            subscriptionPojos.add(subscriptionPojo);
        }
        StatisticPerCustomerBO statisticPerCustomerBO = new StatisticPerCustomerBO();
        statisticPerCustomerBO.setOverallBalance(20);
        statisticPerCustomerBO.setOverallFee(30);
        statisticPerCustomerBO.setSubscriptionIds(subscriptionUuids);
        Set <StatisticPerCustomerBO> statisticPerCustomerBOS= new HashSet<StatisticPerCustomerBO>();
        statisticPerCustomerBOS.add(statisticPerCustomerBO);
        Mockito.when(customerService.getCustomerIds()).thenReturn(setCustomerUuids);
        Mockito.when(customerService.lookupCustomer(customerUuid)).thenAnswer(
                (Answer) invocation -> {
                    Object[] args = invocation.getArguments();
                    Object mock = invocation.getMock();
                    return customerPojo;
                }
        );
        Mockito.when(subscriptionService.getSubscriptions(customerUuid)).thenReturn(subscriptionPojos);
        StatisticBO statisticBO = new StatisticBO();
        statisticBO.setCustomers(statisticPerCustomerBOS);
        statisticBO.setOverallBalance(20);
        statisticBO.setOverallFee(30);
        Assertions.assertEquals(statisticService.calculate(),statisticBO);
    }
    @Test
    void testCalculateWithoutIdWhereCustomerIsNull(){
        Set <UUID> customersUuids= new HashSet<UUID>();
        customersUuids.add(UUID.randomUUID());
        UUID customerUuid = UUID.randomUUID();
        CustomerPojo customerPojo=new CustomerPojo();
        customerPojo.id=customerUuid;
        Mockito.when(customerService.getCustomerIds()).thenReturn(customersUuids);
        Set<StatisticPerCustomerBO> statisticPerCustomerBOS = new HashSet<StatisticPerCustomerBO>();
        StatisticBO statisticBO = new StatisticBO();
        statisticBO.setCustomers(statisticPerCustomerBOS);
        statisticBO.setOverallFee(0);
        statisticBO.setOverallBalance(0);
        Assertions.assertEquals(statisticService.calculate(),statisticBO);
    }
}
