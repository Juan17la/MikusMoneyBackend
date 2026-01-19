package com.example.mikusmoneybackend.transfer;

import com.example.mikusmoneybackend.miku.Miku;
import com.example.mikusmoneybackend.transactions.Transaction;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "sends")
@DiscriminatorValue("SEND")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Transfer extends Transaction {

    @ManyToOne
    @JoinColumn(name = "sender_miku_id", nullable = false)
    private Miku sender;

    @ManyToOne
    @JoinColumn(name = "receiver_miku_id", nullable = false)
    private Miku receiver;
}
