package org.nsu.fit.tm_backend.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    CustomerPojo customerPojo;
    StatisticPerCustomerBO statisticPerCustomerBO;
    Set<UUID> setCustomerUuids;
    Set<UUID> subscriptionUuids;
    UUID subscriptionUuid;
    List<UUID> listCustomerUuids;
    SubscriptionPojo subscriptionPojo;
    List<SubscriptionPojo>subscriptionPojos;
    UUID customerUuid;
    StatisticBO statisticBO;
    @BeforeEach
    public void initEach(){
        listCustomerUuids=new ArrayList<>();
        setCustomerUuids=new HashSet<UUID>();
        subscriptionUuids=new HashSet<UUID>();
        customerPojo = new CustomerPojo();
        customerUuid = UUID.randomUUID();
        statisticPerCustomerBO = new StatisticPerCustomerBO();
        subscriptionPojos= new ArrayList<>();
        customerPojo.id=customerUuid;
        customerPojo.firstName = "John";
        customerPojo.lastName = "Wick";
        customerPojo.login = "john_wick@example.com";
        customerPojo.pass = "1234567";
        customerPojo.balance = 20;
        setCustomerUuids.add(customerUuid);

        listCustomerUuids.add(customerUuid);

        statisticPerCustomerBO.setOverallBalance(20);
        statisticPerCustomerBO.setOverallFee(30);
        statisticPerCustomerBO.setSubscriptionIds(subscriptionUuids);
        for(int i=0;i<3;i++){
            subscriptionPojo = new SubscriptionPojo();
            subscriptionPojo.planName="test";
            subscriptionPojo.planDetails="test";
            subscriptionUuid = UUID.randomUUID();
            subscriptionPojo.id=subscriptionUuid;
            subscriptionPojo.planFee=10;
            subscriptionUuids.add(subscriptionUuid);
            subscriptionPojo.customerId=listCustomerUuids.get(0);
            subscriptionPojos.add(subscriptionPojo);
        }
        Set <StatisticPerCustomerBO> statisticPerCustomerBOS= new HashSet<StatisticPerCustomerBO>();
        statisticPerCustomerBOS.add(statisticPerCustomerBO);
        statisticBO = new StatisticBO();
        statisticBO.setCustomers(statisticPerCustomerBOS);
        statisticBO.setOverallBalance(20);
        statisticBO.setOverallFee(30);
    }
    @Test
    void testCalculateWithId(){
        Mockito.when(customerService.lookupCustomer(customerUuid)).thenAnswer(
                (Answer) invocation -> {
                    Object[] args = invocation.getArguments();
                    Object mock = invocation.getMock();
                    return customerPojo;
                }
        );
        Mockito.when(subscriptionService.getSubscriptions(customerUuid)).thenReturn(subscriptionPojos);
        Assertions.assertEquals(statisticService.calculate(customerPojo.id),statisticPerCustomerBO);
    }

    @Test
    void testCalculateWithIdWhereCustomerIsNull(){
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
        statisticService=Mockito.spy(statisticService);
        Mockito.when(customerService.getCustomerIds()).thenReturn(setCustomerUuids);
        Mockito.when(customerService.lookupCustomer(customerUuid)).thenAnswer(
                (Answer) invocation -> {
                    Object[] args = invocation.getArguments();
                    Object mock = invocation.getMock();
                    return customerPojo;
                }
        );
        Mockito.when(subscriptionService.getSubscriptions(customerUuid)).thenReturn(subscriptionPojos);
        Assertions.assertEquals(statisticService.calculate(),statisticBO);
    }
    @Test
    void testCalculateWithoutIdWhereCustomerIsNull(){
        Set<StatisticPerCustomerBO> statisticPerCustomerBOS = new HashSet<StatisticPerCustomerBO>();
        StatisticBO statisticBO = new StatisticBO();
        statisticBO.setCustomers(statisticPerCustomerBOS);
        statisticBO.setOverallFee(0);
        statisticBO.setOverallBalance(0);
        Set <UUID> customersUuids= new HashSet<UUID>();
        customersUuids.add(UUID.randomUUID());
        customerPojo.id=customerUuid;

        statisticService=Mockito.spy(statisticService);
        Mockito.when(customerService.getCustomerIds()).thenReturn(customersUuids);

        Assertions.assertEquals(statisticService.calculate(),statisticBO);
    }
}
