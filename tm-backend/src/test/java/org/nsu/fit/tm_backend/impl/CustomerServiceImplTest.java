package org.nsu.fit.tm_backend.impl;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.service.impl.CustomerServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Лабораторная 2: покрыть unit тестами класс CustomerServiceImpl на 100%.
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @Mock
    private Repository repository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    CustomerPojo createCustomerInput;
    CustomerPojo createCustomerOutput;

    @BeforeEach
    public void initEach(){
        createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "1234567";
        createCustomerInput.balance = 0;

        createCustomerOutput = new CustomerPojo();
        createCustomerOutput.id = UUID.randomUUID();
        createCustomerOutput.firstName = "John";
        createCustomerOutput.lastName = "Wick";
        createCustomerOutput.login = "john_wick@example.com";
        createCustomerOutput.pass = "Baba_Jaga";
        createCustomerOutput.balance = 0;
    }

    @Test
    void testCreateCustomer() {
        // arrange: готовим входные аргументы и настраиваем mock'и.


        when(repository.createCustomer(createCustomerInput)).thenReturn(createCustomerOutput);

        // act: вызываем метод, который хотим протестировать.
        CustomerPojo customer = customerService.createCustomer(createCustomerInput);

        // assert: проверяем результат выполнения метода.
        assertEquals(customer.id, createCustomerOutput.id);

        // Проверяем, что метод по созданию Customer был вызван ровно 1 раз с определенными аргументами
        verify(repository, times(1)).createCustomer(createCustomerInput);

        // Проверяем, что другие методы не вызывались...
        verify(repository, times(1)).getCustomers();
    }

    @Test
    void testCreateCustomerWithNullArgument() {
        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                customerService.createCustomer(null));
        assertEquals("Argument 'customer' is null.", exception.getMessage());
    }

    @Test
    void testCreateCustomerWithEasyPassword() {
        // arrange
        createCustomerInput.pass = "123qwe";

        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput));
        assertEquals("Password is very easy.", exception.getMessage());
    }

    @Test
    void testCreateCustomerWithNullPassword() {
        // arrange
        createCustomerInput.pass = null;

        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput));
        assertEquals("Field 'customer.pass' is null.", exception.getMessage());
    }

    @Test
    void testCreateCustomerWithWrongPassword() {
        // arrange
        createCustomerInput.pass = "";

        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput));
        assertEquals("Password's length should be more or equal 6 symbols and less or equal 12 symbols.", exception.getMessage());
    }

    @Test
    void testCreateCustomerWithUsedLogin() {
        Set<CustomerPojo> list= new HashSet<>();
        list.add(createCustomerInput);
        when(customerService.getCustomers()).thenReturn(list);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput));
        assertEquals("Login already used.", exception.getMessage());
    }

    @Test
    void testGetCustomers() {
        Set<CustomerPojo> list= new HashSet<>();
        list.add(createCustomerInput);
        when(customerService.getCustomers()).thenReturn(list);
        assertEquals(customerService.getCustomers().size(),list.size());
    }
    @Test
    void testGetCustomersIds(){
        Set <UUID> list= new HashSet<UUID>();
        UUID temp = new UUID(1,1);
        list.add(temp);
        when(customerService.getCustomerIds()).thenReturn(list);
        assertEquals(customerService.getCustomerIds().size(),list.size());
    }
    @Test
    void testGetCustomer(){
        UUID temp = new UUID(1,1);
        createCustomerInput.id=temp;
        when(customerService.getCustomer(createCustomerInput.id)).thenReturn(createCustomerInput);
        assertEquals(customerService.getCustomer(createCustomerInput.id),createCustomerInput);
    }
    @Test
    void testLookupCustomerByLogin(){
        Set<CustomerPojo> list= new HashSet<>();
        list.add(createCustomerInput);
        when(customerService.getCustomers()).thenReturn(list);
        assertEquals(customerService.lookupCustomer(createCustomerInput.login),createCustomerInput);
    }
    @Test
    void testLookupCustomerById(){
        UUID temp = new UUID(1,1);
        createCustomerInput.id=temp;
        Set<CustomerPojo> set= new HashSet<>();
        set.add(createCustomerInput);
        when(customerService.getCustomers()).thenReturn(set);
        assertEquals(customerService.lookupCustomer(createCustomerInput.id),createCustomerInput);
    }

}