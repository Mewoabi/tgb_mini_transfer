package com.tgbsolutions.minitransfer;

import org.springframework.boot.SpringApplication;

public class TestMiniTransferApplication {

	public static void main(String[] args) {
		SpringApplication.from(MiniTransferApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
