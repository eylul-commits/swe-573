package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.entity.Handshake;
import com.thehive.model.entity.Offer;
import com.thehive.model.entity.TimebankTransaction;
import com.thehive.model.entity.User;
import com.thehive.repository.HandshakeRepository;
import com.thehive.repository.TimebankTransactionRepository;
import com.thehive.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimebankTransactionServiceTest {

    @Mock
    private TimebankTransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HandshakeRepository handshakeRepository;

    @InjectMocks
    private TimebankTransactionService timebankTransactionService;

    private User sender;
    private User receiver;
    private Handshake handshake;
    private Offer offer;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1);
        sender.setName("Sender");
        sender.setBalanceHours(5);

        receiver = new User();
        receiver.setId(2);
        receiver.setName("Receiver");
        receiver.setBalanceHours(3);

        offer = new Offer();
        offer.setId(1);
        offer.setTitle("Test Service");

        handshake = new Handshake();
        handshake.setId(1);
        handshake.setSeeker(sender);
        handshake.setProvider(receiver);
        handshake.setOffer(offer);
        handshake.setDurationHours(2);
    }

    @Test
    void createTransaction_Success() {
        // Arrange
        TimebankTransaction expectedTransaction = new TimebankTransaction();
        expectedTransaction.setId(1);
        expectedTransaction.setAmount(2);

        when(userRepository.findById(1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.of(receiver));
        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));
        when(transactionRepository.save(any(TimebankTransaction.class))).thenReturn(expectedTransaction);

        // Act
        TimebankTransaction result = timebankTransactionService.createTransaction(1, 2, 1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(3, sender.getBalanceHours()); // 5 - 2
        assertEquals(5, receiver.getBalanceHours()); // 3 + 2
        verify(userRepository, times(2)).save(any(User.class));
        verify(transactionRepository).save(any(TimebankTransaction.class));
    }

    @Test
    void createTransaction_InsufficientBalance_ThrowsException() {
        // Arrange
        sender.setBalanceHours(1); // Not enough for 2 hours

        when(userRepository.findById(1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.of(receiver));
        when(handshakeRepository.findById(1)).thenReturn(Optional.of(handshake));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> timebankTransactionService.createTransaction(1, 2, 1, 2)
        );

        assertTrue(exception.getMessage().contains("Insufficient balance"));
        verify(userRepository, never()).save(any(User.class));
        verify(transactionRepository, never()).save(any(TimebankTransaction.class));
    }

    @Test
    void createTransaction_SenderNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> timebankTransactionService.createTransaction(1, 2, 1, 2)
        );

        verify(transactionRepository, never()).save(any(TimebankTransaction.class));
    }

    @Test
    void createTransaction_ReceiverNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> timebankTransactionService.createTransaction(1, 2, 1, 2)
        );

        verify(transactionRepository, never()).save(any(TimebankTransaction.class));
    }

    @Test
    void createTransaction_HandshakeNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.of(receiver));
        when(handshakeRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> timebankTransactionService.createTransaction(1, 2, 1, 2)
        );

        verify(transactionRepository, never()).save(any(TimebankTransaction.class));
    }
}

