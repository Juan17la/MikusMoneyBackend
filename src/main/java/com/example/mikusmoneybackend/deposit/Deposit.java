package com.example.mikusmoneybackend.deposit;

import com.example.mikusmoneybackend.miku.Miku;
import com.example.mikusmoneybackend.transactions.Transaction;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "deposits")
@DiscriminatorValue("DEPOSIT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Deposit extends Transaction {

    @ManyToOne
    @JoinColumn(name = "miku_id", nullable = false)
    private Miku miku;

}
