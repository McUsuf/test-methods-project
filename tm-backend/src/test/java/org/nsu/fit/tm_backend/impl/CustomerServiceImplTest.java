package org.nsu.fit.tm_backend.impl;

import java.util.*;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.shared.Authority;
import org.nsu.fit.tm_backend.shared.Globals;

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
    private static final String USER_ID = "1235";
    private static final String USER_LOGIN = "john_wick@example.com";
    private static final Set <String> AUTHORITIES_ADMIN = Collections.singleton(Authority.ADMIN_ROLE);
    private static final Set <String> AUTHORITIES_CUSTOMER = Collections.singleton(Authority.CUSTOMER_ROLE);


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

    private CustomerPojo getCustomerPojo(){
        final CustomerPojo customerPojo = new CustomerPojo();
        customerPojo.firstName = "John";
        customerPojo.lastName = "Wick";
        customerPojo.login = USER_LOGIN;
        customerPojo.pass = "Baba_Jaga";
        customerPojo.balance = 0;

        return customerPojo;
    }
    private CustomerPojo getCustomerPojoWithId(){
        final CustomerPojo customerPojo = getCustomerPojo();
        customerPojo.id = UUID.randomUUID();

        return customerPojo;
    }
    private CustomerPojo cloneCustomerPojo(CustomerPojo customerPojo){
        final CustomerPojo newCustomerPojo = new CustomerPojo();

        newCustomerPojo.id = customerPojo.getId();
        newCustomerPojo.firstName = customerPojo.getFirstName();
        newCustomerPojo.lastName = customerPojo.getLastName();
        newCustomerPojo.login = customerPojo.getLogin();
        newCustomerPojo.pass = customerPojo.getPass();
        newCustomerPojo.balance = customerPojo.getBalance();

        return newCustomerPojo;
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
    @DisplayName("Get login with admin authority")
    void testMeWithAdminAuthority() {
       final AuthenticatedUserDetails authenticatedUserDetails = new AuthenticatedUserDetails(
               USER_ID, USER_LOGIN, AUTHORITIES_ADMIN);

       assertEquals( Globals.ADMIN_LOGIN, customerService.me(authenticatedUserDetails).getLogin());
    }

    @Test
    @DisplayName("Get customer info without admin authority")
    void testMeWithoutAdminAuthority() {
        final AuthenticatedUserDetails authenticatedUserDetails = new AuthenticatedUserDetails(
                USER_ID, USER_LOGIN, AUTHORITIES_CUSTOMER);
        final CustomerPojo customerPojo = getCustomerPojoWithId();

        Mockito.when(repository.getCustomerByLogin(USER_LOGIN)).thenReturn(customerPojo);

        assertEquals(customerPojo, customerService.me(authenticatedUserDetails));
        Mockito.verify(repository).getCustomerByLogin(USER_LOGIN);
    }

    @Test
    @DisplayName("Delete customer from repo")
    void testDeleteCustomer(){
        final CustomerPojo customerPojo = getCustomerPojoWithId();
        doNothing().when(repository).deleteCustomer(customerPojo.id);
        customerService.deleteCustomer(customerPojo.id);
        Mockito.verify(repository, times(1)).deleteCustomer(customerPojo.id);
    }

    @Test
    @DisplayName("Delete customer from repo without id")
    void testDeleteCustomerWithoutId(){
        final CustomerPojo customerPojo = getCustomerPojo();

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                customerService.deleteCustomer(customerPojo.id));
        assertEquals("Argument 'customer id' is null.", exception.getMessage());

        Mockito.verify(repository, never()).deleteCustomer(customerPojo.id);
    }

    @Test
    @DisplayName("Raise customer balance")
    void testTopUpBalance(){
        final CustomerPojo customerPojo = getCustomerPojoWithId();
        //customerPojo не имеет интерфейса Cloneable :(
        final CustomerPojo customerPojoUpdated = cloneCustomerPojo(customerPojo);
        final Random random = new Random();
        //Сдвигаем диапазон рандома вправо на 1 и не допускаем переполнения баланса пользователя
        final int money = random.nextInt(Integer.MAX_VALUE - (customerPojo.balance + 1)) + 1;

        Mockito.when(repository.getCustomer(customerPojo.id)).thenReturn(customerPojo);
        doNothing().when(repository).editCustomer(customerPojo);

        customerPojoUpdated.balance = customerPojo.getBalance() + money;
        assertEquals(customerPojoUpdated, customerService.topUpBalance(customerPojo.id, money));

        Mockito.verify(repository, times(1)).editCustomer(customerPojo);
    }

    @Test
    @DisplayName("Raise customer balance but plus 0")
    void testTopUpBalanceWithZero(){
        final CustomerPojo customerPojo = getCustomerPojoWithId();
        final int money = 0;

        Mockito.when(repository.getCustomer(customerPojo.id)).thenReturn(customerPojo);
        doNothing().when(repository).editCustomer(customerPojo);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                customerService.topUpBalance(customerPojo.id, money));
        assertEquals("Argument 'money' must be more than 0", exception.getMessage());
        Mockito.verify(repository, never()).editCustomer(customerPojo);
    }

    @Test
    @DisplayName("Raise customer balance but plus negative num")
    void testTopUpBalanceWithNegative(){
        final CustomerPojo customerPojo = getCustomerPojoWithId();

        final Random random = new Random();
        final int money =  random.ints(Integer.MIN_VALUE, 0).findFirst().getAsInt();;

        Mockito.when(repository.getCustomer(customerPojo.id)).thenReturn(customerPojo);
        doNothing().when(repository).editCustomer(customerPojo);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                customerService.topUpBalance(customerPojo.id, money));
        assertEquals("Argument 'money' must be more than 0", exception.getMessage());
        Mockito.verify(repository, never()).editCustomer(customerPojo);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(repository);
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