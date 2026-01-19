package com.example.mikusmoneybackend.withdraw;

import com.example.mikusmoneybackend.miku.Miku;
import com.example.mikusmoneybackend.transactions.Transaction;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "withdrawals")
@DiscriminatorValue("WITHDRAW")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Withdraw extends Transaction {

    @ManyToOne
    @JoinColumn(name = "miku_id", nullable = false)
    private Miku miku;

}
