package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall.payment.mapper")
public class GmallPaymentWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallPaymentWebApplication.class, args);
	}

}

