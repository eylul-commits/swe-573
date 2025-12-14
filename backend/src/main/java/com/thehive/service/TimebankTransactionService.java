package com.thehive.service;

import com.thehive.exception.ResourceNotFoundException;
import com.thehive.model.entity.Handshake;
import com.thehive.model.entity.TimebankTransaction;
import com.thehive.model.entity.User;
import com.thehive.repository.HandshakeRepository;
import com.thehive.repository.TimebankTransactionRepository;
import com.thehive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimebankTransactionService {

    private final TimebankTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final HandshakeRepository handshakeRepository;

    @Transactional
    public TimebankTransaction createTransaction(Integer senderId, Integer receiverId, 
                                                  Integer handshakeId, Integer hours) {
        // Validate users exist
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with id: " + senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id: " + receiverId));
        
        // Validate handshake exists
        Handshake handshake = handshakeRepository.findById(handshakeId)
                .orElseThrow(() -> new ResourceNotFoundException("Handshake not found with id: " + handshakeId));
        
        // Check if sender has enough balance
        if (sender.getBalanceHours() < hours) {
            throw new IllegalStateException(
                "Insufficient balance. Required: " + hours + 
                " hours, Available: " + sender.getBalanceHours() + " hours"
            );
        }
        
        // Deduct from sender
        sender.setBalanceHours(sender.getBalanceHours() - hours);
        
        // Add to receiver
        receiver.setBalanceHours(receiver.getBalanceHours() + hours);
        
        // Save users
        userRepository.save(sender);
        userRepository.save(receiver);
        
        // Create transaction record
        TimebankTransaction transaction = new TimebankTransaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setHandshake(handshake);
        transaction.setAmount(hours);
        
        // Build description
        String serviceTitle = handshake.getOffer() != null 
            ? handshake.getOffer().getTitle() 
            : (handshake.getRequest() != null ? handshake.getRequest().getTitle() : "service");
        transaction.setDescription("Time exchange for: " + serviceTitle);
        
        return transactionRepository.save(transaction);
    }
}

