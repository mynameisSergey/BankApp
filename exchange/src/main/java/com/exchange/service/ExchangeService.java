package com.exchange.service;

import com.exchange.metrics.CustomMetrics;
import com.exchange.model.dto.CurrencyEnum;
import com.exchange.model.dto.ExchangeDto;
import com.exchange.model.entities.Exchange;
import com.exchange.repository.ExchangeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;
    private final CustomMetrics customMetrics;

    public void setExchange(ExchangeDto exchangeDto) {
        log.info("сохранение курса валюты {}: {}", exchangeDto.getCurrency().name(), exchangeDto.getValue());
        Exchange exchange = exchangeRepository.findByCurrency(exchangeDto.getCurrency());
        if(Objects.isNull(exchange)) {
            exchange = new Exchange();
            exchange.setCurrency(exchangeDto.getCurrency());
        }
        exchange.setValue(exchangeDto.getValue());
        exchangeRepository.save(exchange);
        customMetrics.incrementCurrencyUpdate();
    }

    public Double getExchange(CurrencyEnum currency) {
        Exchange exchange = exchangeRepository.findByCurrency(currency);
        return exchange.getValue();
    }
}
