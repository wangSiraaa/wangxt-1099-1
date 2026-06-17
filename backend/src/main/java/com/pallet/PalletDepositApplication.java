package com.pallet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pallet.mapper")
public class PalletDepositApplication {
    public static void main(String[] args) {
        SpringApplication.run(PalletDepositApplication.class, args);
    }
}
