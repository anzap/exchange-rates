CREATE TABLE `exhange_rates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `provider_name` varchar(255) NOT NULL,
  `from_currency` varchar(3) NOT NULL,
  `to_currency` varchar(3) NOT NULL,
  `exchange_rate` decimal(20,8) NOT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
); 
